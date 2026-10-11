using System;
using System.IO;
using System.Reflection;
using System.Collections.Generic;
using System.Runtime.InteropServices;
class IntegrationContractProbe {
 static BindingFlags flags=BindingFlags.Instance|BindingFlags.NonPublic;
 static void Check(bool b,string message){if(!b)throw new Exception(message);}
 [STAThread]static void Main(string[] args){
  var host=(TrainerIntegrationHost)Activator.CreateInstance(typeof(TrainerIntegrationHost),true);
  var type=typeof(TrainerIntegrationHost);var mem=(Dictionary<string,IntPtr>)type.GetField("mem",flags).GetValue(host);
  var loc=Marshal.AllocHGlobal(264);var input=Marshal.AllocHGlobal(52);var pult=Marshal.AllocHGlobal(269);var pb=Marshal.AllocHGlobal(26);Marshal.Copy(new byte[264],0,loc,264);Marshal.Copy(new byte[52],0,input,52);Marshal.Copy(new byte[269],0,pult,269);Marshal.Copy(new byte[26],0,pb,26);mem["TLocParam"]=loc;mem["TInputParamPult"]=input;mem["TPult"]=pult;mem["TPBBtn"]=pb;
  // Regression: test buttons previously depended on an unrelated command file.
  type.GetMethod("TestBrake",flags).Invoke(host,new object[]{5});
  type.GetMethod("ApplyTestInputs",flags).Invoke(host,null);
  Check(Marshal.ReadByte(loc,0x7d)==5&&Marshal.ReadByte(loc,0x90)==0,"Brake command without command file and without bypassing blocking");
  type.GetMethod("ReleaseTestInputs",flags).Invoke(host,null);Marshal.WriteByte(loc,0x7d,2);
  type.GetMethod("ApplyTestInputs",flags).Invoke(host,null);Check(Marshal.ReadByte(loc,0x7d)==2,"Native cab after test release");
  var setSpeed=type.GetMethod("SetInitialSpeed",flags);foreach(float v in new float[]{float.NaN,float.PositiveInfinity,-1,121})setSpeed.Invoke(host,new object[]{v});
  Check(BitConverter.ToSingle(Read(input,1,4),0)==0,"Invalid speed reached native state");setSpeed.Invoke(host,new object[]{60f});Check(BitConverter.ToSingle(Read(input,1,4),0)==60,"Valid speed rejected");
  var route=RouteModel.Load(args[0]);Check(route.curves.Count==103&&route.unknown_curves.Count==1&&route.unknown_curves[0].radius_m==null,"Known/unknown radius contract");
  var grade=route.profile[0];double old=grade.grade_permille;grade.grade_permille=double.NaN;bool rejected=false;try{route.Validate();}catch{rejected=true;}grade.grade_permille=old;Check(rejected,"Nonfinite grade accepted");
  double start=route.profile[1].km_start;route.profile[1].km_start-=.0005;rejected=false;try{route.Validate();}catch{rejected=true;}route.profile[1].km_start=start;Check(rejected,"Submetre coverage gap accepted");route.Validate();
  File.WriteAllText(args[1],"PASS: brake button without command file; native control after release; nonfinite/out-of-range speed rejected; known/unknown radii; nonfinite grade and coverage gap rejected.\n");Console.WriteLine("Integration contracts PASS");
  host.Dispose();Marshal.FreeHGlobal(loc);Marshal.FreeHGlobal(input);Marshal.FreeHGlobal(pult);Marshal.FreeHGlobal(pb);
 }
 static byte[] Read(IntPtr p,int offset,int n){var b=new byte[n];Marshal.Copy(IntPtr.Add(p,offset),b,0,n);return b;}
}
