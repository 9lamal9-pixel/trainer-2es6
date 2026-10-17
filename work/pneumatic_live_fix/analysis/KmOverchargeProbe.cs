using System;using System.IO;using System.Reflection;using System.Collections.Generic;using System.Runtime.InteropServices;using System.Web.Script.Serialization;using System.Windows.Forms;
class KmOverchargeProbe {
 static readonly BindingFlags Flags=BindingFlags.Instance|BindingFlags.NonPublic;static TrainerIntegrationHost host;static IntPtr loc;static IntPtr[] cars;static int cycles;static double maxTc;static List<object> trace=new List<object>();
 static object Field(string name){return typeof(TrainerIntegrationHost).GetField(name,Flags).GetValue(host);}
 static void Call(string name){typeof(TrainerIntegrationHost).GetMethod(name,Flags,null,Type.EmptyTypes,null).Invoke(host,null);}
 static float Read(IntPtr p,int offset){var b=new byte[4];Marshal.Copy(IntPtr.Add(p,offset),b,0,4);return BitConverter.ToSingle(b,0);}
 static double ReadD(IntPtr p,int offset){var b=new byte[8];Marshal.Copy(IntPtr.Add(p,offset),b,0,8);return BitConverter.ToDouble(b,0);}
 static void Select(int km){typeof(TrainerIntegrationHost).GetMethod("TestBrake",Flags).Invoke(host,new object[]{km});}
 static void Step(){Call("ElectricalCycle");Call("ApplyTestInputs");Marshal.WriteByte(loc,0x7e,2);for(int k=0x80;k<=0x87;k++)Marshal.WriteByte(loc,k,0);Call("PneumaticCycle");cycles++;}
 static void Observe(){for(int w=1;w<cars.Length;w++){double tc=Read(cars[w],4);if(double.IsNaN(tc)||double.IsInfinity(tc)||tc<0)throw new Exception("Invalid wagon TC");maxTc=Math.Max(maxTc,tc);}if(cycles%100==0)trace.Add(new{seconds=cycles*.1,ur=Read(loc,9),pm=Read(loc,5),head_tm=Read(cars[0],0),tail_tm=Read(cars[75],0),max_tc=maxTc});}
 static void Require(bool condition,string reason){if(!condition)throw new Exception(reason);}
 [STAThread]static int Main(){Application.EnableVisualStyles();try{
  host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);var handle=host.Handle;Call("StartNative");((Timer)Field("timer")).Stop();loc=((Dictionary<string,IntPtr>)Field("mem"))["TLocParam"];cars=(IntPtr[])Field("cars");
  double setpoint=ReadD(loc,0x13);int timeLikvid=Marshal.ReadInt32(loc,0x1b);Select(2);for(int n=0;n<1000;n++)Step();double startingUr=Read(loc,9);
  Select(1);int chargeCycles=0;while(Read(loc,9)<6.4&&chargeCycles<1000){Step();chargeCycles++;}double overcharge=Read(loc,9);Require(overcharge>=6.4,"KM I could not reach UR6.4 without pressure fixtures");Select(2);
  double start=cycles*.1,at58=-1,at56=-1,returnAt=-1,previous=Read(loc,9);int risingSteps=0;double largestRise=0;maxTc=0;bool returned=false;
  for(int n=0;n<14000;n++){
   double before=Read(loc,9);Step();double after=Read(loc,9),time=cycles*.1;
   if(before>5.8&&after<=5.8)at58=time-.1+.1*(before-5.8)/(before-after);
   if(before>5.6&&after<=5.6)at56=time-.1+.1*(before-5.6)/(before-after);
   if(after>previous+1e-5){risingSteps++;largestRise=Math.Max(largestRise,after-previous);}previous=after;Observe();
   if(after<=setpoint+.02&&at56>=0){returned=true;returnAt=time;break;}
  }
  double interval=at56-at58,urAtReturn=Read(loc,9),tmAtReturn=Read(cars[0],0),tailAtReturn=Read(cars[75],0);Select(2);for(int n=0;n<2000;n++){Step();Observe();}
  var failures=new List<string>();if(at58<0||at56<0)failures.Add("UR thresholds5.8/5.6 not crossed");if(interval<80||interval>120)failures.Add("UR5.8->5.6 outside80..120s");if(!returned)failures.Add("UR did not return to actualPzar+0.02 within1400s");if(maxTc>.1)failures.Add("Diagnostic unwanted wagon braking TC>0.1");if(Math.Abs(Read(loc,9)-setpoint)>.05)failures.Add("Diagnostic steady UR differs from setpoint by>0.05");
  var result=new{passed=failures.Count==0,failure_reasons=failures,actual_setpoint=setpoint,native_time_likvid=timeLikvid,starting_ur=startingUr,charge_cycles=chargeCycles,overcharge_ur=overcharge,ur58_seconds=at58-start,ur56_seconds=at56-start,liquidation58_to56_seconds=interval,standard80_120_matched=interval>=80&&interval<=120,increased_length100_120_matched=interval>=100&&interval<=120,returned=returned,return_seconds=returnAt-start,ur_at_return=urAtReturn,head_tm_at_return=tmAtReturn,tail_tm_at_return=tailAtReturn,final_ur=Read(loc,9),final_head_tm=Read(cars[0],0),final_tail_tm=Read(cars[75],0),final_pm=Read(loc,5),wagon_tc_max=maxTc,ur_rising_steps=risingSteps,largest_ur_rise=largestRise,native_cycles=cycles,visual_tm_sensor_signal_verified=false,norm_source="Rules83 appendix65 §3.2.8 PDF20–21; chargeI toUR6.4, thenII, measureUR5.8→5.6",scope="75 native wagons, preserved geometry, actual userPzar, finite preparedPM, compressors off. No pressure fixtures, repeated InitNew/InitSostav or flow edits. UR/TC return tolerances diagnostic; visual pressure-sensor indication not verified. Nominal model step0.1s; crossing interpolated within step.",trace=trace};
  File.WriteAllText(TrainerSession.ResultPath("Trainer_km_overcharge_validation.json"),new JavaScriptSerializer().Serialize(result));Console.WriteLine("KM overcharge:passed="+(failures.Count==0)+",5.8->5.6="+interval+"s,return="+(returnAt-start)+"s,maxTC="+maxTc+",PM="+Read(loc,5));return failures.Count==0?0:1;
 }catch(Exception e){Console.WriteLine(e);return 1;}finally{if(host!=null){host.Close();host.Dispose();}}}
}
