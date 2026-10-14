using System;

public enum TrainerTrainBrakeKind { Unknown, Service, FullService, Emergency }
public enum TrainerTrainBrakeCycleState { Idle, Applying, Holding, PreparingRelease, ReleaseBoost, Recharging, StoppedHold, AwaitingSand, SensorHold }

// All fields are observations or explicit scenario/operator requests. No native
// pointer, DLL call, force, pressure or output assignment belongs to this class.
public sealed class TrainerTrainBrakeCycleInput {
    public double Seconds, UrBar, ChargeBar, ActualTmBar;
    public double HeadSpeedKmh, MaximumConsistSpeedKmh;
    public double IndependentKvtPressureBar, MinBogiePressureBar, MaxBogiePressureBar;
    public bool SensorsKnown, Ready, AllAutoReleased, AllBrakesReleased;
    public bool OwnsCommands, BrakeDemand, StopRequired, Ended, ExplicitRestart, IsDownhill;
    public bool AdditionalStepDemand, HoldRecoveryOnDescent, SandConfirmed;
    public double RecoveryStepBar=.7;
    public double AdditionalStepBar=.3;
    public int Axles;
    public bool LoadedTrain=true;
    // Effective positions actually used by the preceding native pneumatic step,
    // not the requested decision and not a restored physical handle snapshot.
    public bool ObservedCommandsKnown;
    public int ObservedKm=2, ObservedKvt=2;
    public TrainerTrainBrakeKind ApplicationKind;
    public bool HasAmbientTemperature, ColdScenario;
    public double AmbientTemperatureC;
}
public sealed class TrainerTrainBrakeCycleDecision {
    public TrainerTrainBrakeCycleState State;
    public int Km, Kvt;
    public bool OwnsCommands, RequestCoast, CanApplyTraction, RequestSand;
    public string Reason;
    public double TargetUrBar, FirstReductionBar, LastStepReductionBar;
    public double ReleaseStartedSeconds=-1, TractionAllowedAtSeconds=-1;
    public double KvtBandStartedSeconds=-1, ColdDelayFactor=1;
    public TrainerTrainBrakeKind LastApplicationKind;
}

// Rules83 (user PDF SHA39DB7A...), section10.3.1: the release anchor is
// OBSERVED effective KM I. KVT is confirmed before release and retained at least
// 35s AND until the consist releases. It is not a35s delay before issuing KM I.
// The caller gives manual/emergency/SAUT commands priority and keeps this object
// alive across Advisor/Cancelled/Automatic transitions. No Reset-on-rearm API.
public sealed class TrainerTrainBrakeCycle {
    public const double FirstReduction=.7, MinimumLapSeconds=6, LocomotiveHoldSeconds=35;
    public const double RechargeSeconds=60, MovingReleaseDelaySeconds=60, ContinuousHoldSeconds=150;
    const double StandstillKmh=.3, PressureSlack=.000001;
    TrainerTrainBrakeCycleState state=TrainerTrainBrakeCycleState.Idle;
    TrainerTrainBrakeKind kind=TrainerTrainBrakeKind.Unknown;
    double lastSeconds=-1, lastUr, targetUr, applicationUr, firstReduction, lastStep;
    double holdSince=-1, releaseSince=-1, releaseRequestedSince=-1, releaseBaselineUr, applicationStarted=-1;
    double kvtBandSince=-1, lastReleaseDelay, lastColdFactor=1, releaseColdFactor=1;
    bool hasApplication, previousAutoReleased=true, stoppedLatch, releaseWasStopped;
    bool restartApproved, previousRestart, previousAdditional, previousDemand;
    bool firstStage, previousKvtBand, faultHeld, lastStopped, kvtReleased, passiveFirstPending, pendingAdditional, firstReductionKnown, lapPending;
    int lastCommandedKvt=2;
    public double ReleaseStartedSeconds { get { return releaseSince; } }
    public double PreviousFirstReductionBar { get { return firstReduction; } }
    public TrainerTrainBrakeKind LastApplicationKind { get { return kind; } }

    public TrainerTrainBrakeCycleDecision Observe(TrainerTrainBrakeCycleInput input) {
        return Evaluate(input,false);
    }
    public TrainerTrainBrakeCycleDecision Step(TrainerTrainBrakeCycleInput input) {
        return Evaluate(input,input!=null&&input.OwnsCommands);
    }
    static bool Finite(double x) { return !Double.IsNaN(x)&&!Double.IsInfinity(x); }
    static bool Pressure(double x) { return Finite(x)&&x>=-.001&&x<=12; }
    static bool Position(int x) { return x>=1&&x<=6; }
    string Validate(TrainerTrainBrakeCycleInput i) {
        if(i==null)return "Нет наблюдения тормозного цикла";
        if(!Finite(i.Seconds)||i.Seconds<0||lastSeconds>=0&&i.Seconds<lastSeconds)return "Неверное или обратное время тормозного цикла";
        if(!i.SensorsKnown)return "Показания тормозных датчиков не подтверждены";
        if(!Pressure(i.UrBar)||!Pressure(i.ChargeBar)||i.ChargeBar<=FirstReduction||!Pressure(i.ActualTmBar)||
           !Pressure(i.IndependentKvtPressureBar)||!Pressure(i.MinBogiePressureBar)||!Pressure(i.MaxBogiePressureBar)||i.MinBogiePressureBar>i.MaxBogiePressureBar)
            return "Неверное давление тормозного цикла";
        if(!Finite(i.HeadSpeedKmh)||!Finite(i.MaximumConsistSpeedKmh)||i.MaximumConsistSpeedKmh<0||Math.Abs(i.HeadSpeedKmh)>i.MaximumConsistSpeedKmh+.01||i.Axles<1)
            return "Движение всего состава не подтверждено";
        if(i.HasAmbientTemperature&&!Finite(i.AmbientTemperatureC))return "Неверная температура сценария";
        if(i.ObservedCommandsKnown&&(!Position(i.ObservedKm)||!Position(i.ObservedKvt)))return "Неизвестная фактическая позиция тормозных кранов";
        if(!Finite(i.AdditionalStepBar)||i.AdditionalStepBar<.3||i.AdditionalStepBar>.8)return "Добавочная ступень должна быть0.3–0.8";
        if(!Finite(i.RecoveryStepBar)||i.RecoveryStepBar<.7||i.RecoveryStepBar>.8)return "Восстановление удержания на спуске требует0.7–0.8";
        if((int)i.ApplicationKind<0||(int)i.ApplicationKind>3)return "Неизвестный класс торможения";
        return "";
    }
    static bool Stopped(TrainerTrainBrakeCycleInput i) {
        return Math.Abs(i.HeadSpeedKmh)<StandstillKmh&&i.MaximumConsistSpeedKmh<StandstillKmh;
    }
    static bool KvtBand(TrainerTrainBrakeCycleInput i) {
        return i.IndependentKvtPressureBar>=1&&i.IndependentKvtPressureBar<=2.2&&i.MinBogiePressureBar>=1&&i.MaxBogiePressureBar<=(i.AllAutoReleased?2.2:4.6);
    }
    static double ColdFactor(TrainerTrainBrakeCycleInput i) {
        // This is a declared conservative scenario policy, not inferred native TN.
        return !i.HasAmbientTemperature||i.ColdScenario||!Finite(i.AmbientTemperatureC)||i.AmbientTemperatureC<0?1.5:1;
    }
    void RaiseKind(TrainerTrainBrakeKind observed) {
        if((int)observed>(int)kind)kind=observed;
    }
    void RecordRelease(TrainerTrainBrakeCycleInput i) {
        if(releaseSince<0)releaseSince=i.Seconds;
        releaseColdFactor=Math.Max(releaseColdFactor,ColdFactor(i));
        releaseWasStopped|=Stopped(i);
    }
    void ObserveHistory(TrainerTrainBrakeCycleInput i,bool owns) {
        bool stopped=Stopped(i),hadApplication=hasApplication;
        if(i.ObservedCommandsKnown&&i.ObservedKm==6){hasApplication=true;RaiseKind(TrainerTrainBrakeKind.Emergency);}
        if(!i.AllAutoReleased) {
            hasApplication=true;
            RaiseKind(i.ApplicationKind);
            if(i.ObservedCommandsKnown&&i.ObservedKm==6)RaiseKind(TrainerTrainBrakeKind.Emergency);
        }
        if(!owns&&i.ObservedCommandsKnown&&(i.ObservedKm==5||i.ObservedKm==6)&&(!hadApplication||releaseSince>=0)) {
            // A new physical brake application invalidates the previous release.
            releaseSince=-1;releaseRequestedSince=-1;releaseWasStopped=false;restartApproved=false;
            applicationUr=lastUr>0?lastUr:i.UrBar;firstReduction=0;firstReductionKnown=false;lastStep=0;passiveFirstPending=true;releaseColdFactor=1;
            kind=i.ObservedKm==6?TrainerTrainBrakeKind.Emergency:i.ApplicationKind;
            hasApplication=true;state=TrainerTrainBrakeCycleState.Holding;holdSince=-1;
        }
        if(passiveFirstPending&&i.ObservedCommandsKnown&&(i.ObservedKm==4||i.ObservedKm==1)) {
            double measured=applicationUr-(i.ObservedKm==1?lastUr:i.UrBar);
            if(measured>0){firstReduction=measured;firstReductionKnown=true;}
            if(i.ObservedKm==4)holdSince=i.Seconds;passiveFirstPending=false;
        }
        if(hasApplication&&i.ObservedCommandsKnown&&i.ObservedKm==1)RecordRelease(i);
        // If an effective-position sample is unavailable, complete release gives
        // a conservative later anchor. It must never grant an earlier interval.
        if(hasApplication&&!previousAutoReleased&&i.AllAutoReleased&&releaseSince<0)RecordRelease(i);
        if(releaseSince>=0&&stopped)releaseWasStopped=true;
        if(hasApplication&&stopped)stoppedLatch=true;
        if(releaseSince>=0&&!owns&&i.ObservedCommandsKnown&&(i.ObservedKm==1||i.ObservedKm==2))
            state=i.ObservedKm==1?TrainerTrainBrakeCycleState.ReleaseBoost:TrainerTrainBrakeCycleState.Recharging;
        if(hasApplication&&!owns&&!i.AllAutoReleased&&state==TrainerTrainBrakeCycleState.Idle) {
            // Attaching to an already applied native brake cannot invent another
            // first step. Hold its measured state; first-step history is unknown.
            state=TrainerTrainBrakeCycleState.Holding;holdSince=i.Seconds;targetUr=i.UrBar;
        }
        previousAutoReleased=i.AllAutoReleased;lastUr=i.UrBar;lastStopped=stopped;
    }
    bool BeginBrake(TrainerTrainBrakeCycleInput i,bool additional,out string error) {
        error="";
        double reduction=i.HoldRecoveryOnDescent?i.RecoveryStepBar:additional?i.AdditionalStepBar:FirstReduction;
        if(!additional&&!i.HoldRecoveryOnDescent&&i.IsDownhill&&hasApplication&&(releaseSince<0||i.Seconds-releaseSince<RechargeSeconds)) {
            if(!firstReductionKnown){error="Первая ступень предыдущего торможения неизвестна; обычное повторное торможение требует подтверждённой истории";return false;}
            reduction=Math.Max(reduction,firstReduction+.3);
        }
        if(i.UrBar-reduction<=0) { error="Недозарядка: требуемая ступень торможения недостижима";return false; }
        double cumulative=additional&&applicationUr>0?applicationUr-(i.UrBar-reduction):reduction;
        if(cumulative>1+PressureSlack&&!i.SandConfirmed) {
            pendingAdditional=additional;state=TrainerTrainBrakeCycleState.AwaitingSand;
            error="Усиление УР более1.0 требует подтверждённой подачи песка; требуется остановочная процедура при отсутствии песка";return false;
        }
        if(!additional)stoppedLatch=Stopped(i);
        if(!additional) {
            firstReduction=reduction;firstReductionKnown=true;applicationUr=i.UrBar;firstStage=true;passiveFirstPending=false;
            kind=i.ApplicationKind==TrainerTrainBrakeKind.Unknown?TrainerTrainBrakeKind.Service:i.ApplicationKind;
        } else firstStage=false;
        lastStep=reduction;targetUr=i.UrBar-reduction;hasApplication=true;
        releaseSince=-1;releaseRequestedSince=-1;releaseWasStopped=false;restartApproved=false;releaseColdFactor=1;
        kvtBandSince=-1;previousKvtBand=false;kvtReleased=false;holdSince=-1;lapPending=false;applicationStarted=i.Seconds;
        state=TrainerTrainBrakeCycleState.Applying;return true;
    }
    double RequiredDelay(TrainerTrainBrakeCycleInput i) {
        double observedCold=ColdFactor(i);
        if(releaseSince>=0)releaseColdFactor=Math.Max(releaseColdFactor,observedCold);
        bool stoppedWait=releaseWasStopped||stoppedLatch;
        // The winter multiplier belongs only to the post-stop interval.
        // Moving release always waits60s; a subsequent stop extends the SAME I.
        lastColdFactor=stoppedWait?(releaseSince>=0?releaseColdFactor:observedCold):1;
        double delay=stoppedWait?(kind==TrainerTrainBrakeKind.Service?90:kind==TrainerTrainBrakeKind.FullService?120:360):MovingReleaseDelaySeconds;
        return delay*lastColdFactor;
    }
    bool TractionGate(TrainerTrainBrakeCycleInput i) {
        if(!i.Ready||i.BrakeDemand||i.StopRequired||i.Ended||!i.AllAutoReleased||!i.AllBrakesReleased||faultHeld)return false;
        if(!hasApplication)return true;
        if(stoppedLatch&&!restartApproved)return false;
        lastReleaseDelay=RequiredDelay(i);
        return releaseSince>=0&&i.Seconds-releaseSince>=lastReleaseDelay;
    }
    TrainerTrainBrakeCycleDecision Result(TrainerTrainBrakeCycleInput i,bool owns,int km,int kvt,string reason,bool canTraction) {
        lastCommandedKvt=owns?kvt:lastCommandedKvt;
        double delay=i==null?lastReleaseDelay:RequiredDelay(i);
        if(i!=null&&!i.HasAmbientTemperature)reason+=(reason.Length==0?"":"; ")+"температура неизвестна; консервативный коэффициент1.5 для остановочного интервала";
        return new TrainerTrainBrakeCycleDecision {State=state,Km=owns?km:0,Kvt=owns?kvt:0,OwnsCommands=owns,
            RequestCoast=owns&&!canTraction,CanApplyTraction=canTraction,RequestSand=owns&&state==TrainerTrainBrakeCycleState.AwaitingSand,Reason=reason,TargetUrBar=targetUr,
            FirstReductionBar=firstReduction,LastStepReductionBar=lastStep,ReleaseStartedSeconds=releaseSince,
            TractionAllowedAtSeconds=releaseSince<0?-1:releaseSince+delay,KvtBandStartedSeconds=kvtBandSince,
            ColdDelayFactor=lastColdFactor,LastApplicationKind=kind};
    }
    TrainerTrainBrakeCycleDecision BrakeRefused(TrainerTrainBrakeCycleInput i,string reason) {
        if(state==TrainerTrainBrakeCycleState.AwaitingSand)return Result(i,true,4,stoppedLatch?6:3,reason,false);
        return FailHold(i,true,reason);
    }
    TrainerTrainBrakeCycleDecision FailHold(TrainerTrainBrakeCycleInput i,bool owns,string reason) {
        faultHeld=true;kvtBandSince=-1;previousKvtBand=false;
        if(state!=TrainerTrainBrakeCycleState.SensorHold)holdSince=-1;
        state=TrainerTrainBrakeCycleState.SensorHold;
        return Result(i,owns,4,stoppedLatch||lastStopped?6:lastCommandedKvt==6?6:3,reason,false);
    }
    TrainerTrainBrakeCycleDecision Evaluate(TrainerTrainBrakeCycleInput i,bool owns) {
        string error=Validate(i);if(error.Length!=0)return FailHold(i,owns,error);
        lastSeconds=i.Seconds;ObserveHistory(i,owns);
        if(lapPending&&i.ObservedCommandsKnown&&i.ObservedKm==4){holdSince=i.Seconds;lapPending=false;}
        bool restart=i.ExplicitRestart&&!previousRestart,additional=i.AdditionalStepDemand&&!previousAdditional;
        previousRestart=i.ExplicitRestart;previousAdditional=i.AdditionalStepDemand;
        bool demand=i.BrakeDemand||i.StopRequired||i.Ended;
        bool newDemand=demand&&!previousDemand;previousDemand=demand;
        if(!i.Ready)return FailHold(i,owns,"Неполная готовность: удержание тормоза и выбег");
        if(!owns)return Result(i,false,0,0,"Наблюдение: ручное управление и защита имеют приоритет",TractionGate(i));
        if(faultHeld) {
            // Recovery cannot silently return traction after an unknown interval.
            if(!restart&&!newDemand)return FailHold(i,true,"Показания восстановлены; требуется явный запрос");
            faultHeld=false;state=hasApplication?TrainerTrainBrakeCycleState.Holding:TrainerTrainBrakeCycleState.Idle;holdSince=hasApplication?i.Seconds:-1;
        }
        if(state==TrainerTrainBrakeCycleState.AwaitingSand&&!demand)state=TrainerTrainBrakeCycleState.Holding;
        if(state==TrainerTrainBrakeCycleState.AwaitingSand) {
            if(!i.SandConfirmed)return Result(i,true,4,stoppedLatch?6:3,"Ожидание подтверждённого песка; автоматический отпуск и тяга запрещены",false);
            if(!BeginBrake(i,pendingAdditional,out error))return BrakeRefused(i,error);
        }
        if(restart) {
            if(demand)return Result(i,true,4,stoppedLatch?6:3,"Запуск запрещён, пока требуется торможение/остановка",false);
            restartApproved=true;
            if(hasApplication) {
                // Re-arming an already observed I/II release preserves its anchor.
                if(releaseSince>=0)state=i.ObservedCommandsKnown&&i.ObservedKm==1?TrainerTrainBrakeCycleState.ReleaseBoost:TrainerTrainBrakeCycleState.Recharging;
                else state=lapPending||holdSince>=0&&i.Seconds-holdSince<MinimumLapSeconds?TrainerTrainBrakeCycleState.Holding:TrainerTrainBrakeCycleState.PreparingRelease;
            }
        }
        if(newDemand&&(state==TrainerTrainBrakeCycleState.Idle||state==TrainerTrainBrakeCycleState.ReleaseBoost||
           state==TrainerTrainBrakeCycleState.Recharging||state==TrainerTrainBrakeCycleState.PreparingRelease||releaseSince>=0)) {
            if(!BeginBrake(i,false,out error))return BrakeRefused(i,error);
        }
        if(state==TrainerTrainBrakeCycleState.Idle&&demand) {
            if(!BeginBrake(i,false,out error))return BrakeRefused(i,error);
        }
        if(state==TrainerTrainBrakeCycleState.Applying) {
            if(applicationUr-i.UrBar>=1.5-PressureSlack)RaiseKind(TrainerTrainBrakeKind.FullService);
            if(i.UrBar>targetUr+PressureSlack) {
                if(applicationStarted>=0&&i.Seconds-applicationStarted>=15){lastCommandedKvt=6;return FailHold(i,true,"Ступень УР не достигнута за15с: удержание IV/VI и выбег");}
                return Result(i,true,5,stoppedLatch?6:2,"Достичь измеренной ступени УР",false);
            }
            if(firstStage)firstReduction=Math.Max(firstReduction,applicationUr-i.UrBar);
            state=stoppedLatch?TrainerTrainBrakeCycleState.StoppedHold:TrainerTrainBrakeCycleState.Holding;holdSince=-1;lapPending=true;
            if(i.ObservedCommandsKnown&&i.ObservedKm==4){holdSince=i.Seconds;lapPending=false;}
        }
        if(hasApplication&&Stopped(i)&&!restartApproved&&state!=TrainerTrainBrakeCycleState.Applying)
            state=TrainerTrainBrakeCycleState.StoppedHold;
        if(state==TrainerTrainBrakeCycleState.StoppedHold) {
            return Result(i,true,4,6,"Полное КВТ и перекрыша после остановки; нужен явный запуск",false);
        }
        if(state==TrainerTrainBrakeCycleState.Holding) {
            if(additional||demand&&!Stopped(i)&&holdSince>=0&&i.Seconds-holdSince>=ContinuousHoldSeconds) {
                if(holdSince<0||i.Seconds-holdSince<MinimumLapSeconds)return Result(i,true,4,stoppedLatch?6:2,"Минимальная выдержка перекрыши6с",false);
                if(!BeginBrake(i,true,out error))return BrakeRefused(i,error);
                return Result(i,true,5,stoppedLatch?6:2,"Добавочная ступень торможения",false);
            }
            if(holdSince<0||i.Seconds-holdSince<MinimumLapSeconds)return Result(i,true,4,stoppedLatch?6:2,"Минимальная выдержка перекрыши6с",false);
            if(demand)return Result(i,true,4,stoppedLatch?6:2,"Сохраняется требование торможения/остановки",false);
            state=TrainerTrainBrakeCycleState.PreparingRelease;
        }
        if(state==TrainerTrainBrakeCycleState.PreparingRelease) {
            if(demand)return Result(i,true,4,stoppedLatch?6:3,"Самостоятельный отпуск при обязательной остановке запрещён",false);
            bool movingPreRelease=!Stopped(i)&&i.LoadedTrain&&i.Axles>100;
            if(movingPreRelease) {
                bool band=KvtBand(i);
                if(!band){kvtBandSince=-1;previousKvtBand=false;return Result(i,true,4,3,"Подтвердить КВТ1.0–2.2 и все тележечные ТЦ перед отпуском",false);}
                if(!previousKvtBand||kvtBandSince<0)kvtBandSince=i.Seconds;previousKvtBand=true;
            }
            // Issue I now after pressure confirmation. Timer starts only when
            // an effective native I is subsequently observed (or conservatively
            // when actual automatic release is complete without that sample).
            releaseRequestedSince=i.Seconds;releaseBaselineUr=i.UrBar;releaseColdFactor=Math.Max(releaseColdFactor,ColdFactor(i));
            state=TrainerTrainBrakeCycleState.ReleaseBoost;
        }
        if(state==TrainerTrainBrakeCycleState.ReleaseBoost||state==TrainerTrainBrakeCycleState.Recharging) {
            if(demand)return Result(i,true,4,stoppedLatch?6:3,"Обязательная остановка/торможение не допускает автоматического отпуска",false);
            if(i.ObservedCommandsKnown&&i.ObservedKm==1)RecordRelease(i);
            if(releaseSince>=0&&Stopped(i))releaseWasStopped=true;
            bool needKvt=!Stopped(i)&&i.LoadedTrain&&i.Axles>100;
            bool holdKvt=false;
            if(needKvt&&!kvtReleased) {
                bool band=KvtBand(i);
                if(!band){kvtBandSince=-1;previousKvtBand=false;}
                else if(!previousKvtBand||kvtBandSince<0){kvtBandSince=i.Seconds;previousKvtBand=true;}
                holdKvt=!i.AllAutoReleased||kvtBandSince<0||i.Seconds-kvtBandSince<LocomotiveHoldSeconds;
                if(!holdKvt)kvtReleased=true;
            }
            double delay=RequiredDelay(i);lastReleaseDelay=delay;
            bool stoppedWait=stoppedLatch&&(!restartApproved||releaseSince<0||i.Seconds-releaseSince<delay||!i.AllAutoReleased);
            int kvt=stoppedWait?6:holdKvt?3:2;
            if(state==TrainerTrainBrakeCycleState.ReleaseBoost&&i.UrBar<i.ChargeBar+.3-PressureSlack)
                return Result(i,true,1,kvt,"Отпуск I до зарядного+.3; тяга запрещена",false);
            state=TrainerTrainBrakeCycleState.Recharging;
            bool traction=TractionGate(i)&&!holdKvt&&kvt==2;
            return Result(i,true,2,kvt,traction?"Тормоза отпущены, временные условия выполнены":"Дозарядка II; дождаться времени и всех отпущенных ТЦ",traction);
        }
        return Result(i,true,2,2,"Готовность без активного тормозного цикла",TractionGate(i));
    }
}
