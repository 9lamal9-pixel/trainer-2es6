using System;
using RailwayDynamics.Couplers;

namespace RailwayDynamics.Simulation {
    /// <summary>
    /// Типы тормозных колодок железнодорожного подвижного состава
    /// </summary>
    public enum BrakeShoeType {
        StandardCastIron = 1,       // Чугунные стандартные колодки (Формула 56 ПТР)
        PhosphorusCastIronP10 = 2,  // Historical API name: PTR phosphorus / published M reference curve; no OEM or UIC identification.
        Composite = 3               // Композиционные колодки (Формула 58 ПТР)
    }

    /// <summary>
    /// Трибологические расчеты трения тормозных колодок по «Правилам тяговых расчетов» (ПТР ВНИИЖТ)
    /// </summary>
    public static class BrakeTribologyPtr {
        /// <summary>
        /// Действительный коэффициент трения тормозной колодки о колесо по ПТР (Формулы 56-58)
        /// в зависимости от скорости v и силы нажатия на одну колодку K1
        /// </summary>
        public static double ComputeBrakeShoeFriction(BrakeShoeType type, double speedMps, double forcePerShoeNewton) {
            if (Double.IsNaN(speedMps) || Double.IsInfinity(speedMps)) throw new ArgumentOutOfRangeException("speedMps");
            if (Double.IsNaN(forcePerShoeNewton) || Double.IsInfinity(forcePerShoeNewton) || forcePerShoeNewton < 0)
                throw new ArgumentOutOfRangeException("forcePerShoeNewton");
            double vKmh = Math.Abs(speedMps) * 3.6;
            if (Double.IsInfinity(vKmh) || vKmh > Double.MaxValue / 5.0) throw new ArgumentOutOfRangeException("speedMps");
            double k1Kn = forcePerShoeNewton / 1000.0; // Actual normal force on ONE shoe, kN (not vehicle total).

            switch (type) {
                case BrakeShoeType.StandardCastIron:
                    // SI form of PTR standard cast iron: K in kN, speed in km/h.
                    return 0.60 * ((1.6 * k1Kn + 100.0) / (8.0 * k1Kn + 100.0)) * ((vKmh + 100.0) / (5.0 * vKmh + 100.0));

                case BrakeShoeType.PhosphorusCastIronP10:
                    // PTR phosphorus cast iron (1.0-1.4% P), SI form; this is not an OEM material identification.
                    return 0.50 * ((1.6 * k1Kn + 100.0) / (5.2 * k1Kn + 100.0)) * ((vKmh + 100.0) / (5.0 * vKmh + 100.0));

                case BrakeShoeType.Composite:
                    // GOST 34434-2018 8.3.2.6 (14): actual K in kN, not calculated/equivalent pressing.
                    return 0.44 * ((0.1 * k1Kn + 20.0) / (0.4 * k1Kn + 20.0)) * ((vKmh + 150.0) / (2.0 * vKmh + 150.0));

                default:
                    throw new ArgumentOutOfRangeException("type");
            }
        }

        /// <summary>
        /// Расчетный коэффициент трения колодок по ПТР (Формулы 59-60)
        /// </summary>
        public static double ComputeCalculatedShoeFriction(BrakeShoeType type, double speedMps) {
            if (Double.IsNaN(speedMps) || Double.IsInfinity(speedMps)) throw new ArgumentOutOfRangeException("speedMps");
            double vKmh = Math.Abs(speedMps) * 3.6;
            if (Double.IsInfinity(vKmh) || vKmh > Double.MaxValue / 5.0) throw new ArgumentOutOfRangeException("speedMps");
            switch (type) {
                case BrakeShoeType.StandardCastIron:
                case BrakeShoeType.PhosphorusCastIronP10:
                    // Формула 59 ПТР: phi_кр = 0.27 * (v + 100) / (5v + 100)
                    return 0.27 * (vKmh + 100.0) / (5.0 * vKmh + 100.0);

                case BrakeShoeType.Composite:
                    // Формула 60 ПТР: phi_кр = 0.36 * (v + 150) / (2v + 150)
                    return 0.36 * (vKmh + 150.0) / (2.0 * vKmh + 150.0);

                default:
                    throw new ArgumentOutOfRangeException("type");
            }
        }
    }

    /// <summary>
    /// Параметры и динамическое состояние единицы подвижного состава (локомотива или вагона)
    /// </summary>
    public struct VehicleDynamicsNode {
        public double MassKg;                       // Масса экипажа (кг)
        public double RotaryInertiaGamma;           // Коэффициент инерции вращающихся частей (гамма)
        public double LengthM;                      // Длина по осям автосцепок (м)
        public bool IsLocomotive;                   // Признак локомотива

        public double PositionX;                    // Centre coordinate, metres in fixed +X (not the front end).
        public double VelocityV;                    // Скорость (м/с)
        public double AccelerationA;                // Продольное ускорение (м/с^2)

        public double DistanceFromHeadM;            // Расстояние от головы поезда (м)
        public double BrakePipePressurePa;          // Давление в тормозной магистрали вагона (Па)
        public double BrakeCylinderPressurePa;      // Давление в тормозном цилиндре (Па)
        public double MaxCylinderPressurePa;        // Максимальное давление в ТЦ (~3.8e5 Па)
        public double FillingTimeConstantSec;       // Постоянная времени наполнения ТЦ (с)
        public double CylinderPistonAreaM2;         // Площадь поршня ТЦ (м^2)
        public double ReturnSpringForceN;           // Сопротивление отпускной пружины ТЦ (Н)
        public double RiggingRatioEta;              // Передаточное число рычажной передачи
        public int BrakeShoeCount;                  // Число тормозных колодок
        public BrakeShoeType ShoeType;              // Тип колодок (чугун / композит)

        public double TractiveForceN;               // Тяговое усилие (Н)
        public double BrakeForceN;                  // Реализованная тормозная сила колодок (Н)
        public double RunningResistanceN;           // Основное сопротивление движению (Н)
        public int AxleCount;                       // 0 keeps the legacy default (8 loco, 4 wagon).
        public int CylinderCount;                   // 0 keeps the legacy single equivalent cylinder.
        public double GradeForceN;                  // Signed external gravity/profile force in +X.
        public double CurveResistanceN;             // Applied signed passive curve resistance.
        public double DynamicBrakeForceN;           // Signed dissipative impulse / substep; no static holding.
    }

    /// <summary>
    /// Centre-coordinate, fixed +X longitudinal kernel. Production pressures are
    /// measured gauge Pa inputs; this class never synthesizes pneumatic propagation
    /// in Step(dt). Rotary inertia and brake hardware are explicit caller profiles.
    /// Backward Euler couplers plus a Coulomb proximal solve prevent passive drag
    /// from reversing a vehicle. Accepted steps obey a work/energy inequality.
    /// </summary>
    public sealed class LongitudinalTrainDynamicsSolver {
        private readonly VehicleDynamicsNode[] _vehicles;
        private readonly CouplerState[] _couplerStates;
        private readonly double[] _couplerForces;
        private readonly FrictionDraftGearModel _draftGearModel;
        private readonly double _waveSpeedAirMps;
        private readonly double[] _effectiveMass, _brakeOverride, _curveMagnitude, _dynamicBrakeCapacity, _oldVelocity;
        private readonly double[] _velocity, _solution, _couplerNet, _stiffness, _diagonal, _rhs;
        private readonly double[] _brakeCapacity, _rollingCapacity, _passiveCapacity, _trialForces;
        private readonly double[] _gradeInput, _gradeTrial, _curveTrial, _externalTrial;
        private readonly CouplerState[] _trialStates;
        private Func<int, double, double> _brakeForceProvider, _gradeForceProvider, _curveForceProvider;
        private readonly System.Collections.Generic.List<LegacyCommand> _legacyCommands = new System.Collections.Generic.List<LegacyCommand>();
        private struct LegacyCommand { public double Time, Reduction; }
        private double _time, _externalWork, _dissipated;
        private bool _legacyUsed;
        private bool _stepping, _runningResistanceEnabled;
        // Irreversible tensile fracture. Compression overload is a warning,
        // not a command to pull contacting vehicles apart.
        private readonly bool[] _broken, _trialBroken;
        private readonly double[] _failureDemand, _failureTime, _trialFailureDemand;
        private readonly bool[] _compressionOverload;
        private double _fractureTensionLimitN, _designCompressionLimitN;
        public int BrokenCouplerCount { get { int n=0;foreach(bool b in _broken)if(b)n++;return n; } }
        public int ComponentCount { get { return BrokenCouplerCount+1; } }
        public double FractureTensionLimitN { get { return _fractureTensionLimitN; } }
        public void ConfigureCouplerIntegrity(double tensileFailureN,double compressionDesignN) {
            EnsureInputsMutable();Nonnegative(tensileFailureN,"tensileFailureN");Nonnegative(compressionDesignN,"compressionDesignN");
            _fractureTensionLimitN=tensileFailureN;_designCompressionLimitN=compressionDesignN;
        }
        public bool IsCouplerBroken(int index) { CheckLink(index);return _broken[index]; }
        public bool HasCompressionOverload(int index) { CheckLink(index);return _compressionOverload[index]; }
        public double GetCouplerFailureTensionNewtons(int index) { CheckLink(index);return _failureDemand[index]; }
        public double GetCouplerFailureTimeSeconds(int index) { CheckLink(index);return _failureTime[index]; }
        public int GetVehicleComponentIndex(int index) { CheckIndex(index);int n=0;for(int i=0;i<index;i++)if(_broken[i])n++;return n; }
        private void CheckLink(int i) { if(i<0||i>=_couplerForces.Length)throw new ArgumentOutOfRangeException("index"); }
        // Observation only: freeze the last accepted force endpoint, because
        // input setters may change live node fields before the next Step.
        private readonly VehicleDynamicsNode[] _lastAcceptedForceVehicles;
        private readonly double[] _lastAcceptedForceCouplers;
        private bool _lastAcceptedForceAvailable;
        private double _lastAcceptedForceDt, _lastAcceptedForceTime;
        private double _maximumSubstepSeconds;
        public bool RunningResistanceEnabled { get { return _runningResistanceEnabled; } set { EnsureInputsMutable(); _runningResistanceEnabled = value; } }
        public double MaximumSubstepSeconds { get { return _maximumSubstepSeconds; } set { EnsureInputsMutable(); RequireFinite(value, "MaximumSubstepSeconds"); if (value < 1e-6 || value > .05) throw new ArgumentOutOfRangeException("MaximumSubstepSeconds"); _maximumSubstepSeconds = value; } }
        public int LastSubstepCount { get; private set; }
        public int LastRejectedSubstepCount { get; private set; }

        public LongitudinalTrainDynamicsSolver(
            VehicleDynamicsNode[] vehicles,
            FrictionDraftGearModel draftGearModel,
            double waveSpeedAirMps = 280.0
        ) {
            if (vehicles == null || vehicles.Length == 0) throw new ArgumentException("A nonempty consist is required", "vehicles");
            if (draftGearModel == null) throw new ArgumentNullException("draftGearModel");
            RequireFinite(waveSpeedAirMps, "waveSpeedAirMps");
            if (waveSpeedAirMps <= 0) throw new ArgumentOutOfRangeException("waveSpeedAirMps");
            _vehicles = (VehicleDynamicsNode[])vehicles.Clone();
            _draftGearModel = draftGearModel;
            _waveSpeedAirMps = waveSpeedAirMps;
            _couplerStates = new CouplerState[vehicles.Length - 1];
            _couplerForces = new double[vehicles.Length - 1];
            _broken=new bool[vehicles.Length-1];_trialBroken=new bool[vehicles.Length-1];
            _compressionOverload=new bool[vehicles.Length-1];
            _failureDemand=new double[vehicles.Length-1];_failureTime=new double[vehicles.Length-1];_trialFailureDemand=new double[vehicles.Length-1];
            int n = vehicles.Length;
            _lastAcceptedForceVehicles = new VehicleDynamicsNode[n];
            _lastAcceptedForceCouplers = new double[n - 1];
            _effectiveMass = new double[n]; _brakeOverride = new double[n]; _curveMagnitude = new double[n];
            _dynamicBrakeCapacity = new double[n]; _oldVelocity = new double[n];
            _velocity = new double[n]; _solution = new double[n]; _couplerNet = new double[n];
            _stiffness = new double[n - 1]; _diagonal = new double[n]; _rhs = new double[n];
            _brakeCapacity = new double[n]; _rollingCapacity = new double[n]; _passiveCapacity = new double[n];
            _trialForces = new double[n - 1]; _trialStates = new CouplerState[n - 1];
            _gradeInput = new double[n]; _gradeTrial = new double[n]; _curveTrial = new double[n]; _externalTrial = new double[n];
            double frontDistance = 0;
            for (int i = 0; i < n; i++) {
                ValidateVehicle(_vehicles[i]);
                _effectiveMass[i] = _vehicles[i].MassKg * (1 + _vehicles[i].RotaryInertiaGamma);
                RequireFinite(_effectiveMass[i], "effectiveMass");
                _brakeOverride[i] = double.NaN;
                _gradeInput[i] = _vehicles[i].GradeForceN;
                _vehicles[i].DistanceFromHeadM = frontDistance;
                frontDistance += _vehicles[i].LengthM;
            }
            RunningResistanceEnabled = true; MaximumSubstepSeconds = .005;
            InitializeCouplers();
            _legacyCommands.Add(new LegacyCommand { Time = 0, Reduction = 0 });
        }

        public int VehicleCount {
            get { return _vehicles.Length; }
        }

        public VehicleDynamicsNode GetVehicle(int index) {
            return _vehicles[index];
        }

        /// <summary>Copies the actual last accepted substep, not pending inputs.
        /// Positive rear force is compression on the leading body. Reset makes
        /// the force endpoint unavailable until another successful Step.</summary>
        public bool TryGetLastAcceptedForceStep(out VehicleDynamicsNode[] vehicles,
            out double[] rearCouplerForcesN, out double acceptedSubstepSeconds, out double endTimeSeconds) {
            if (_stepping) throw new InvalidOperationException("Force observation requires a completed Step.");
            if (!_lastAcceptedForceAvailable) {
                vehicles = null; rearCouplerForcesN = null; acceptedSubstepSeconds = endTimeSeconds = 0; return false;
            }
            vehicles = (VehicleDynamicsNode[])_lastAcceptedForceVehicles.Clone();
            rearCouplerForcesN = (double[])_lastAcceptedForceCouplers.Clone();
            acceptedSubstepSeconds = _lastAcceptedForceDt; endTimeSeconds = _lastAcceptedForceTime;
            return true;
        }

        public double GetCouplerForce(int index) {
            return _couplerForces[index];
        }

        // Rear coupler force ON vehicle i: compression is +X, tension is -X.
        // The last vehicle has no rear coupler and therefore returns zero.
        public double GetCouplerForceSignedNewtons(int vehicleIndex) {
            CheckIndex(vehicleIndex);
            return vehicleIndex == _vehicles.Length - 1 ? 0 : _couplerForces[vehicleIndex];
        }
        public double GetVehicleFrontPosition(int index) { CheckIndex(index); return _vehicles[index].PositionX + .5 * _vehicles[index].LengthM; }
        public double SimulationTimeSeconds { get { return _time; } }
        public bool LegacyPneumaticFixtureUsed { get { return _legacyUsed; } }
        public double CumulativeExternalWorkJ { get { return _externalWork; } }
        public double CumulativeDissipatedEnergyJ { get { return _dissipated; } }
        public double TotalLengthM { get { double length = 0; foreach (var v in _vehicles) length += v.LengthM; return length; } }
        public double TotalEnergyJ { get { return KineticEnergy(null) + StoredCouplerEnergy(_couplerStates); } }
        public double EffectiveMomentumKgMps { get { double p = 0; for (int i = 0; i < VehicleCount; i++) p += _effectiveMass[i] * _vehicles[i].VelocityV; return p; } }

        public void SetInitialState(double headFrontM, double signedSpeedMps) {
            EnsureInputsMutable();
            RequireFinite(headFrontM, "headFrontM"); RequireFinite(signedSpeedMps, "signedSpeedMps");
            _lastAcceptedForceAvailable = false; _lastAcceptedForceDt = _lastAcceptedForceTime = 0;
            double front = headFrontM;
            for (int i = 0; i < VehicleCount; i++) {
                _vehicles[i].PositionX = front - _vehicles[i].LengthM * .5;
                _vehicles[i].VelocityV = signedSpeedMps; _vehicles[i].AccelerationA = 0;
                _vehicles[i].BrakeForceN = _vehicles[i].RunningResistanceN = _vehicles[i].CurveResistanceN = 0;
                _vehicles[i].DynamicBrakeForceN = 0;
                front -= _vehicles[i].LengthM;
            }
            _time = _externalWork = _dissipated = 0;
            // Existing damage is an initial condition of the new time epoch,
            // not a future event stamped with the previous epoch's clock.
            for(int i=0;i<_broken.Length;i++)if(_broken[i])_failureTime[i]=0;
            LastSubstepCount = LastRejectedSubstepCount = 0;
            _legacyUsed = false; _legacyCommands.Clear(); _legacyCommands.Add(new LegacyCommand { Time = 0, Reduction = 0 });
            InitializeCouplers();
        }
        public void SetBrakeCylinderPressure(int index, double pressurePa) {
            EnsureInputsMutable(); CheckIndex(index); Nonnegative(pressurePa, "pressurePa");
            _vehicles[index].BrakeCylinderPressurePa = pressurePa;
        }
        public void SetVehicleForce(int index, double forceN) { EnsureInputsMutable(); CheckIndex(index); RequireFinite(forceN, "forceN"); _vehicles[index].TractiveForceN = forceN; }
        public void SetVehicleBrakeForceMagnitude(int index, double forceN) { EnsureInputsMutable(); CheckIndex(index); Nonnegative(forceN, "forceN"); _brakeOverride[index] = forceN; }
        public void ClearVehicleBrakeForceMagnitude(int index) { EnsureInputsMutable(); CheckIndex(index); _brakeOverride[index] = double.NaN; }
        public void SetVehicleDynamicBrakeForceMagnitude(int index, double forceN) { EnsureInputsMutable(); CheckIndex(index); Nonnegative(forceN, "forceN"); _dynamicBrakeCapacity[index] = forceN; }
        // Providers must be pure and finite. Brake provider receives signed m/s;
        // route providers receive the body's CENTRE coordinate, not its front.
        // A non-null brake provider outranks the fixed capacity and BC fallback.
        public void SetBrakeForceProvider(Func<int, double, double> provider) { EnsureInputsMutable(); _brakeForceProvider = provider; }
        public void SetRouteForceProviders(Func<int, double, double> gradeForceProvider, Func<int, double, double> curveResistanceProvider) { EnsureInputsMutable(); _gradeForceProvider = gradeForceProvider; _curveForceProvider = curveResistanceProvider; }
        public void SetRouteForces(int index, double gradeForceN, double curveResistanceN) {
            EnsureInputsMutable(); CheckIndex(index); RequireFinite(gradeForceN, "gradeForceN"); Nonnegative(curveResistanceN, "curveResistanceN");
            _gradeInput[index] = _vehicles[index].GradeForceN = gradeForceN; _curveMagnitude[index] = curveResistanceN;
        }

        public void SetLocomotiveTraction(double forceN) {
            SetVehicleForce(0, forceN);
        }

        private static double Clamp(double val, double min, double max) {
            return val < min ? min : (val > max ? max : val);
        }

        /// <summary>
        /// Выполняет шаг интегрирования по времени продольного движения состава
        /// </summary>
        public void Step(double dt) { StepCore(dt, false); }

        /// <summary>Legacy standalone pneumatic fixture only. It includes an
        /// explicit transport delay and exponential filling, not native brakes.</summary>
        [Obsolete("Legacy pneumatic fixture only. Production supplies measured BC/force and calls Step(dt).")]
        public void Step(double dt, double headBrakePipePressureReductionPa) {
            EnsureInputsMutable();
            Nonnegative(headBrakePipePressureReductionPa, "headBrakePipePressureReductionPa");
            ValidateDt(dt);
            _legacyUsed = true;
            if (_legacyCommands[_legacyCommands.Count - 1].Reduction != headBrakePipePressureReductionPa)
                _legacyCommands.Add(new LegacyCommand { Time = _time, Reduction = headBrakePipePressureReductionPa });
            StepCore(dt, true);
        }

        private void StepCore(double dt, bool legacy) {
            EnsureInputsMutable();
            ValidateDt(dt);
            RequireFinite(MaximumSubstepSeconds, "MaximumSubstepSeconds");
            if (MaximumSubstepSeconds < 1e-6 || MaximumSubstepSeconds > .05) throw new ArgumentOutOfRangeException("MaximumSubstepSeconds");
            var savedVehicles = (VehicleDynamicsNode[])_vehicles.Clone();
            var savedStates = (CouplerState[])_couplerStates.Clone();
            var savedForces = (double[])_couplerForces.Clone();
            var savedBroken=(bool[])_broken.Clone();var savedCompression=(bool[])_compressionOverload.Clone();
            var savedFailureDemand=(double[])_failureDemand.Clone();var savedFailureTime=(double[])_failureTime.Clone();
            double savedTime = _time, savedWork = _externalWork, savedDissipation = _dissipated;
            bool savedForceAvailable = _lastAcceptedForceAvailable;
            double savedForceDt = _lastAcceptedForceDt, savedForceTime = _lastAcceptedForceTime;
            var savedForceVehicles = savedForceAvailable ? (VehicleDynamicsNode[])_lastAcceptedForceVehicles.Clone() : null;
            var savedForceCouplers = savedForceAvailable ? (double[])_lastAcceptedForceCouplers.Clone() : null;
            LastSubstepCount = LastRejectedSubstepCount = 0;
            _stepping = true;
            try {
                double remaining = dt;
                while (remaining > Math.Max(1e-13, dt * 1e-12)) {
                    if (LastSubstepCount >= 1000000) throw new InvalidOperationException("LTD adaptive step budget exceeded");
                    double h = Math.Min(remaining, StableSubstep());
                    bool accepted = false;
                    for (int retry = 0; retry < 24; retry++) {
                        if (TrySubstep(h, legacy)) { accepted = true; break; }
                        LastRejectedSubstepCount++; h *= .5;
                        if (h < 1e-9) break;
                    }
                    if (!accepted) throw new InvalidOperationException("LTD implicit/passivity convergence failed");
                    _time += h; remaining -= h; LastSubstepCount++;
                }
            } catch {
                Array.Copy(savedVehicles, _vehicles, VehicleCount); Array.Copy(savedStates, _couplerStates, _couplerStates.Length);
                Array.Copy(savedForces, _couplerForces, _couplerForces.Length);
                Array.Copy(savedBroken,_broken,_broken.Length);Array.Copy(savedCompression,_compressionOverload,_broken.Length);
                Array.Copy(savedFailureDemand,_failureDemand,_broken.Length);Array.Copy(savedFailureTime,_failureTime,_broken.Length);
                _time = savedTime; _externalWork = savedWork; _dissipated = savedDissipation;
                _lastAcceptedForceAvailable = savedForceAvailable; _lastAcceptedForceDt = savedForceDt; _lastAcceptedForceTime = savedForceTime;
                if (savedForceAvailable) {
                    Array.Copy(savedForceVehicles, _lastAcceptedForceVehicles, VehicleCount);
                    Array.Copy(savedForceCouplers, _lastAcceptedForceCouplers, _couplerForces.Length);
                }
                throw;
            } finally { _stepping = false; }
        }

        private double StableSubstep() {
            double bound = 0;
            for (int i = 0; i < _stiffness.Length; i++) {
                _stiffness[i] = _draftGearModel.GetTangentStiffness(Deflection(i));
                Nonnegative(_stiffness[i], "coupler tangent stiffness");
            }
            for (int i = 0; i < VehicleCount; i++) {
                double row = (i > 0 ? _stiffness[i - 1] : 0) + (i < _stiffness.Length ? _stiffness[i] : 0);
                bound = Math.Max(bound, 2 * row / _effectiveMass[i]);
            }
            return bound > 0 ? Math.Min(MaximumSubstepSeconds, .20 / Math.Sqrt(bound)) : MaximumSubstepSeconds;
        }

        private bool TrySubstep(double h, bool legacy) {
            Array.Copy(_broken,_trialBroken,_broken.Length);Array.Copy(_failureDemand,_trialFailureDemand,_broken.Length);
            return TryIntegritySubstep(h,legacy,0);
        }
        private bool TryIntegritySubstep(double h, bool legacy,int fracturePass) {
            int n = VehicleCount;
            double[] oldPressures = legacy ? new double[n] : null;
            for (int i = 0; i < n; i++) {
                _oldVelocity[i] = _vehicles[i].VelocityV;
                // An EDT impulse removes existing momentum only. It never
                // supplies a static holding reaction against a new drive/grade.
                _velocity[i] = _solution[i] = SoftThreshold(_oldVelocity[i], h * _dynamicBrakeCapacity[i] / _effectiveMass[i]);
                if (legacy) {
                    oldPressures[i] = _vehicles[i].BrakeCylinderPressurePa;
                    double delayed = _time + h - _vehicles[i].DistanceFromHeadM / _waveSpeedAirMps;
                    double reduction = 0;
                    for (int c = _legacyCommands.Count - 1; c >= 0; c--) if (_legacyCommands[c].Time <= delayed) { reduction = _legacyCommands[c].Reduction; break; }
                    double target = Clamp((reduction - 20000) / 140000, 0, 1) * _vehicles[i].MaxCylinderPressurePa;
                    double tau = Math.Max(.5, _vehicles[i].FillingTimeConstantSec);
                    _vehicles[i].BrakeCylinderPressurePa = target + (oldPressures[i] - target) * Math.Exp(-h / tau);
                }
            }
            double oldEnergy = KineticEnergy(null) + StoredCouplerEnergy(_couplerStates);
            bool converged = false;
            for (int iteration = 0; iteration < 40; iteration++) {
                EvaluateCouplers(_solution, h);
                for (int i = 0; i < n; i++) {
                    double left = i > 0 ? _stiffness[i - 1] : 0, right = i < n - 1 ? _stiffness[i] : 0;
                    double kv = left * (_solution[i] - (i > 0 ? _solution[i - 1] : 0)) + right * (_solution[i] - (i < n - 1 ? _solution[i + 1] : 0));
                    _diagonal[i] = _effectiveMass[i] + h * h * (left + right);
                    _rhs[i] = _effectiveMass[i] * _velocity[i] + h * (External(i) + _couplerNet[i]) + h * h * kv;
                }
                // Convex tridiagonal quadratic + per-vehicle Coulomb |v|.
                for (int sweep = 0; sweep < 40; sweep++) {
                    double change = 0;
                    for (int i = 0; i < n; i++) {
                        double b = _rhs[i];
                        if (i > 0) b += h * h * _stiffness[i - 1] * _solution[i - 1];
                        if (i < n - 1) b += h * h * _stiffness[i] * _solution[i + 1];
                        double next = SoftThreshold(b, h * _passiveCapacity[i]) / _diagonal[i];
                        change = Math.Max(change, Math.Abs(next - _solution[i])); _solution[i] = next;
                    }
                    if (change < 1e-12) break;
                }
                EvaluateCouplers(_solution, h);
                double residual = 0, scale = 1;
                for (int i = 0; i < n; i++) {
                    double expected = SoftThreshold(_effectiveMass[i] * _velocity[i] + h * (External(i) + _couplerNet[i]), h * _passiveCapacity[i]) / _effectiveMass[i];
                    residual = Math.Max(residual, Math.Abs(expected - _solution[i])); scale = Math.Max(scale, Math.Abs(_solution[i]));
                }
                if (residual <= 1e-10 * scale) { converged = true; break; }
            }
            double work = 0;
            for (int i = 0; i < n; i++) work += h * External(i) * _solution[i];
            double energy = KineticEnergy(_solution) + StoredCouplerEnergy(_trialStates);
            double tolerance = 1e-9 + 1e-10 * Math.Max(1, Math.Max(Math.Abs(oldEnergy), Math.Abs(work)));
            if (!converged || !Finite(energy) || !Finite(work) || energy > oldEnergy + work + tolerance) {
                if (legacy) for (int i = 0; i < n; i++) _vehicles[i].BrakeCylinderPressurePa = oldPressures[i];
                return false;
            }
            bool fracture=false;
            if(_fractureTensionLimitN>0)for(int i=0;i<_trialForces.Length;i++) {
                if(!_trialBroken[i]&&-_trialForces[i]>_fractureTensionLimitN) {
                    _trialBroken[i]=true;_trialFailureDemand[i]=-_trialForces[i];fracture=true;
                }
            }
            if(fracture) {
                // Re-solve this substep with the failed tensile constraints
                // removed. A rejected Newton/adaptive attempt never breaks a
                // real link. Event timing is resolved to the mechanical h.
                if(fracturePass>=_broken.Length)throw new InvalidOperationException("Fracture topology solve bound");
                if(legacy)for(int i=0;i<n;i++)_vehicles[i].BrakeCylinderPressurePa=oldPressures[i];
                return TryIntegritySubstep(h,legacy,fracturePass+1);
            }
            for (int i = 0; i < n; i++) {
                double drag = Clamp(External(i) + _couplerNet[i] - _effectiveMass[i] * (_solution[i] - _velocity[i]) / h, -_passiveCapacity[i], _passiveCapacity[i]);
                double sign = Math.Sign(drag), amount = Math.Abs(drag);
                double brake = Math.Min(amount, _brakeCapacity[i]); amount -= brake;
                double roll = Math.Min(amount, _rollingCapacity[i]); amount -= roll;
                _vehicles[i].BrakeForceN = sign * brake;
                _vehicles[i].RunningResistanceN = sign * roll;
                _vehicles[i].CurveResistanceN = sign * Math.Min(amount, _curveTrial[i]);
                _vehicles[i].GradeForceN = _gradeTrial[i];
                _vehicles[i].DynamicBrakeForceN = _effectiveMass[i] * (_oldVelocity[i] - _velocity[i]) / h;
                _vehicles[i].AccelerationA = (_solution[i] - _oldVelocity[i]) / h;
                _vehicles[i].VelocityV = _solution[i]; _vehicles[i].PositionX += h * _solution[i];
            }
            Array.Copy(_trialStates, _couplerStates, _trialStates.Length);
            Array.Copy(_trialForces, _couplerForces, _trialForces.Length);
            for(int i=0;i<_broken.Length;i++) {
                if(!_broken[i]&&_trialBroken[i]){_failureDemand[i]=_trialFailureDemand[i];_failureTime[i]=_time+h;}
                _broken[i]=_trialBroken[i];
                if(_designCompressionLimitN>0&&_trialForces[i]>_designCompressionLimitN)_compressionOverload[i]=true;
            }
            _externalWork += work; _dissipated += Math.Max(0, oldEnergy + work - energy);
            Array.Copy(_vehicles, _lastAcceptedForceVehicles, VehicleCount);
            Array.Copy(_couplerForces, _lastAcceptedForceCouplers, _couplerForces.Length);
            _lastAcceptedForceDt = h; _lastAcceptedForceTime = _time + h; _lastAcceptedForceAvailable = true;
            return true;
        }

        private void EvaluateCouplers(double[] velocity, double h) {
            for (int i = 0; i < VehicleCount; i++) {
                double centre = _vehicles[i].PositionX + h * velocity[i];
                _gradeTrial[i] = _gradeForceProvider == null ? _gradeInput[i] : _gradeForceProvider(i, centre);
                _curveTrial[i] = _curveForceProvider == null ? _curveMagnitude[i] : _curveForceProvider(i, centre);
                RequireFinite(_gradeTrial[i], "grade force provider"); Nonnegative(_curveTrial[i], "curve force provider");
                _externalTrial[i] = _vehicles[i].TractiveForceN + _gradeTrial[i]; RequireFinite(_externalTrial[i], "external force sum");
                _brakeCapacity[i] = BrakeCapacity(i, velocity[i]); _rollingCapacity[i] = RollingCapacity(i, velocity[i]);
                _passiveCapacity[i] = _brakeCapacity[i] + _rollingCapacity[i] + _curveTrial[i];
                Nonnegative(_brakeCapacity[i], "brake force provider"); Nonnegative(_rollingCapacity[i], "rolling capacity"); Nonnegative(_passiveCapacity[i], "passive capacity");
            }
            Array.Clear(_couplerNet, 0, _couplerNet.Length);
            for (int i = 0; i < _couplerForces.Length; i++) {
                double dx = Deflection(i) + h * (velocity[i + 1] - velocity[i]);
                CouplerState state = _couplerStates[i];
                if(_trialBroken[i]) {
                    // A broken coupling cannot carry tension or relatch. Keep
                    // only unilateral compressive contact to prevent vehicles
                    // passing through one another on subsequent recontact.
                    dx=Math.Max(0,dx);
                    if(!_broken[i])state=_draftGearModel.InitializeState(0);
                }
                double force = _draftGearModel.ComputeCouplerForce(dx, velocity[i + 1] - velocity[i], ref state);
                RequireFinite(force, "coupler force");
                _trialStates[i] = state; _trialForces[i] = force;
                _couplerNet[i] += force; _couplerNet[i + 1] -= force;
            }
        }
        private double BrakeCapacity(int i, double velocity) {
            if (_brakeForceProvider != null) return _brakeForceProvider(i, velocity);
            if (!double.IsNaN(_brakeOverride[i])) return _brakeOverride[i];
            var v = _vehicles[i]; int cylinders = v.CylinderCount > 0 ? v.CylinderCount : 1;
            double normal = Math.Max(0, v.BrakeCylinderPressurePa * v.CylinderPistonAreaM2 - v.ReturnSpringForceN) * v.RiggingRatioEta * cylinders;
            double perShoe = normal / Math.Max(1, v.BrakeShoeCount);
            return normal * BrakeTribologyPtr.ComputeBrakeShoeFriction(v.ShoeType, velocity, perShoe);
        }
        private double RollingCapacity(int i, double velocity) {
            if (!RunningResistanceEnabled) return 0;
            var v = _vehicles[i]; double kmh = Math.Abs(velocity) * 3.6;
            int axles = v.AxleCount > 0 ? v.AxleCount : (v.IsLocomotive ? 8 : 4);
            double w = v.IsLocomotive ? 1.9 + .01 * kmh + .0003 * kmh * kmh : .7 + (3 + .1 * kmh + .0025 * kmh * kmh) / (v.MassKg / 1000 / axles);
            return v.MassKg * 9.80665 * .001 * w;
        }
        private double External(int i) { return _externalTrial[i]; }
        private double Deflection(int i) { return .5 * (_vehicles[i].LengthM + _vehicles[i + 1].LengthM) - (_vehicles[i].PositionX - _vehicles[i + 1].PositionX); }
        private void InitializeCouplers() {
            for (int i = 0; i < _couplerStates.Length; i++) { _couplerStates[i] = _draftGearModel.InitializeState(_broken[i]?Math.Max(0,Deflection(i)):Deflection(i)); _couplerForces[i] = 0; }
            RequireFinite(TotalEnergyJ, "initial total energy");
        }
        private double StoredCouplerEnergy(CouplerState[] states) { double sum = 0; foreach (var state in states) sum += _draftGearModel.GetStoredEnergy(state); return sum; }
        private double KineticEnergy(double[] velocity) { double sum = 0; for (int i = 0; i < VehicleCount; i++) { double v = velocity == null ? _vehicles[i].VelocityV : velocity[i]; sum += .5 * _effectiveMass[i] * v * v; } return sum; }
        private static double SoftThreshold(double v, double limit) { return v > limit ? v - limit : (v < -limit ? v + limit : 0); }
        private void CheckIndex(int i) { if (i < 0 || i >= VehicleCount) throw new ArgumentOutOfRangeException("index"); }
        private void EnsureInputsMutable() { if (_stepping) throw new InvalidOperationException("LTD inputs cannot change or reenter during a step/provider callback"); }
        private static bool Finite(double x) { return !double.IsNaN(x) && !double.IsInfinity(x); }
        private static void RequireFinite(double x, string name) { if (!Finite(x)) throw new ArgumentOutOfRangeException(name, "Finite value required"); }
        private static void Nonnegative(double x, string name) { RequireFinite(x, name); if (x < 0) throw new ArgumentOutOfRangeException(name); }
        private static void ValidateDt(double dt) { RequireFinite(dt, "dt"); if (dt <= 0 || dt > 60) throw new ArgumentOutOfRangeException("dt"); }
        private static void ValidateVehicle(VehicleDynamicsNode v) {
            RequireFinite(v.MassKg, "MassKg"); RequireFinite(v.LengthM, "LengthM"); Nonnegative(v.RotaryInertiaGamma, "RotaryInertiaGamma");
            if (v.MassKg <= 0 || v.LengthM <= 0 || v.AxleCount < 0 || v.CylinderCount < 0 || v.BrakeShoeCount < 0) throw new ArgumentException("Invalid vehicle geometry/hardware");
            RequireFinite(v.PositionX, "PositionX"); RequireFinite(v.VelocityV, "VelocityV"); RequireFinite(v.TractiveForceN, "TractiveForceN"); RequireFinite(v.GradeForceN, "GradeForceN");
            Nonnegative(v.BrakeCylinderPressurePa, "BrakeCylinderPressurePa"); Nonnegative(v.MaxCylinderPressurePa, "MaxCylinderPressurePa");
            Nonnegative(v.FillingTimeConstantSec, "FillingTimeConstantSec"); Nonnegative(v.CylinderPistonAreaM2, "CylinderPistonAreaM2");
            Nonnegative(v.ReturnSpringForceN, "ReturnSpringForceN"); Nonnegative(v.RiggingRatioEta, "RiggingRatioEta");
        }

        /// <summary>76-body geometry only; no factory brake calibration is implied.
        /// The host supplies validated brake capacities or explicit hardware data.</summary>
        public static VehicleDynamicsNode[] CreateFreightConsist(int wagonCount = 75, double rotaryInertiaGamma = 0) {
            if (wagonCount < 0 || wagonCount > 1000) throw new ArgumentOutOfRangeException("wagonCount");
            Nonnegative(rotaryInertiaGamma, "rotaryInertiaGamma");
            var result = new VehicleDynamicsNode[wagonCount + 1]; double front = 0;
            for (int i = 0; i < result.Length; i++) {
                double length = i == 0 ? 31.93 : 14.59;
                result[i] = new VehicleDynamicsNode { MassKg = i == 0 ? 200000 : 80000, LengthM = length, IsLocomotive = i == 0,
                    AxleCount = i == 0 ? 8 : 4, RotaryInertiaGamma = rotaryInertiaGamma, PositionX = front - length * .5,
                    DistanceFromHeadM = -front, ShoeType = BrakeShoeType.Composite };
                front -= length;
            }
            return result;
        }
    }
}
