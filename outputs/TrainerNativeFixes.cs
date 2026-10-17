using System;
using System.IO;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
partial class TrainerIntegrationHost {
 [DllImport("kernel32.dll",SetLastError=true)]static extern bool VirtualProtect(IntPtr address,UIntPtr size,uint protect,out uint oldProtect);
 [DllImport("kernel32.dll",SetLastError=true)]static extern bool FlushInstructionCache(IntPtr process,IntPtr address,UIntPtr size);
 [DllImport("kernel32.dll")]static extern IntPtr GetCurrentProcess();
 IntPtr signedSpeedCopySite;
 void SetNativeInstruction(IntPtr site,byte second){
  uint previous;if(!VirtualProtect(site,(UIntPtr)2,0x40,out previous))throw new Exception("Native speed guard: VirtualProtect="+Marshal.GetLastWin32Error());
  try{Marshal.WriteByte(site,1,second);if(!FlushInstructionCache(GetCurrentProcess(),site,(UIntPtr)2))throw new Exception("Native speed guard: cache flush failed");}
  finally{uint ignored;if(!VirtualProtect(site,(UIntPtr)2,previous,out ignored))throw new Exception("Native speed guard: protection restore failed");}
 }
 void EnableSignedTractionSpeed(IntPtr communication){
  string hash;using(var algorithm=SHA256.Create())using(var stream=File.OpenRead(Path.Combine(resources,@"LOCOMOTIVES\1\Communication1.dll")))hash=BitConverter.ToString(algorithm.ComputeHash(stream)).Replace("-","");
  if(hash!="27178A089EF33692D16B1D46AE0DD7567135B327A24BE9D7D0CA84AF8DF3DB7F")throw new Exception("Неизвестная сборка Communication1: знаковая скорость не подключена");
  var site=IntPtr.Add(communication,0x2975ef);
  byte[] expected={0xdd,0x18,0x9b,0x8b,0x45,0xfc};for(int i=0;i<expected.Length;i++)if(Marshal.ReadByte(site,i)!=expected[i])throw new Exception("Native speed copy instruction guard failed");
  // Original Proceed overwrites only the traction Double with abs(InputV)/3.6.
  // Discard that x87 value with a balanced pop; the host supplies signed POEZD V.
  // Safety, sound and display inputs retain the absolute speed. Disk DLL is untouched.
  SetNativeInstruction(site,0xd8);signedSpeedCopySite=site;
  Dbl(mem["TTygaParam"],0,signedVelocity);Log("Signed traction speed connected: runtime-only DD18 -> DDD8 at Communication RVA2975EF; disk DLL unchanged");
 }
 void RestoreSignedTractionSpeed(){if(signedSpeedCopySite==IntPtr.Zero)return;SetNativeInstruction(signedSpeedCopySite,0x18);signedSpeedCopySite=IntPtr.Zero;}
 [DllImport("kernel32.dll",CharSet=CharSet.Unicode,SetLastError=true)]static extern uint GetModuleFileName(IntPtr module,System.Text.StringBuilder path,int capacity);
 const string MpsudMonitorSha="95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B";
 static readonly byte[] MpsudWagonOriginal={0x8b,0x80,0xe8,0x02,0x00,0x00},MpsudWagonCorrected={0x8b,0x80,0xf4,0x02,0x00,0x00};
 IntPtr mpsudWagonPatchSite,mpsudWagonModule;uint mpsudWagonOriginalProtection;bool mpsudWagonProtectionPending;
 static bool NativeBytesEqual(IntPtr site,byte[] expected){for(int i=0;i<expected.Length;i++)if(Marshal.ReadByte(site,i)!=expected[i])return false;return true;}
 void RestoreMpsudWagonProtection(){if(!mpsudWagonProtectionPending)return;uint ignored;if(!VirtualProtect(mpsudWagonPatchSite,(UIntPtr)MpsudWagonOriginal.Length,mpsudWagonOriginalProtection,out ignored))throw new Exception("SC09 protection restore failed="+Marshal.GetLastWin32Error());mpsudWagonProtectionPending=false;}
 void WriteMpsudWagonDisplacement(byte value){
  uint previous;if(!VirtualProtect(mpsudWagonPatchSite,(UIntPtr)MpsudWagonOriginal.Length,0x40,out previous))throw new Exception("SC09 VirtualProtect failed="+Marshal.GetLastWin32Error());
  if(!mpsudWagonProtectionPending){mpsudWagonOriginalProtection=previous;mpsudWagonProtectionPending=true;}
  try{Marshal.WriteByte(mpsudWagonPatchSite,2,value);if(!FlushInstructionCache(GetCurrentProcess(),mpsudWagonPatchSite,(UIntPtr)MpsudWagonOriginal.Length))throw new Exception("SC09 cache flush failed="+Marshal.GetLastWin32Error());}
  finally{RestoreMpsudWagonProtection();}
 }
 void EnableMpsudAvWagonFieldFix(){
  var module=GetModuleHandle("MSUL_MON.dll");if(module==IntPtr.Zero)throw new Exception("SC09 MSUL_MON module absent");
  var loadedPath=new System.Text.StringBuilder(32768);uint pathLength=GetModuleFileName(module,loadedPath,loadedPath.Capacity);if(pathLength==0||pathLength>=loadedPath.Capacity)throw new Exception("SC09 loaded module path unavailable");
  var expectedPath=Path.GetFullPath(Path.Combine(resources,@"LOCOMOTIVES\1\MSUL_MON.dll"));if(!String.Equals(Path.GetFullPath(loadedPath.ToString()),expectedPath,StringComparison.OrdinalIgnoreCase))throw new Exception("SC09 MSUL_MON loaded path differs from runtime");
  string hash;using(var sha=SHA256.Create())using(var input=File.OpenRead(loadedPath.ToString()))hash=BitConverter.ToString(sha.ComputeHash(input)).Replace("-","");if(hash!=MpsudMonitorSha)throw new Exception("SC09 unknown MSUL_MON build");
  var site=IntPtr.Add(module,0x1e460d);
  if(mpsudWagonPatchSite!=IntPtr.Zero){if(module!=mpsudWagonModule||site!=mpsudWagonPatchSite||!NativeBytesEqual(site,MpsudWagonCorrected)||mpsudWagonProtectionPending)throw new Exception("SC09 active patch guard failed");return;}
  if(!NativeBytesEqual(site,MpsudWagonOriginal))throw new Exception("SC09 original instruction guard failed");
  // Remember ownership before the first write so a failed write/flush/protect can be undone.
  mpsudWagonModule=module;mpsudWagonPatchSite=site;
  try{WriteMpsudWagonDisplacement(0xf4);if(!NativeBytesEqual(site,MpsudWagonCorrected))throw new Exception("SC09 corrected instruction verification failed");}
  catch(Exception failure){try{RestoreMpsudAvWagonFieldFix();}catch(Exception cleanup){throw new AggregateException("SC09 patch failed and cleanup failed",failure,cleanup);}throw;}
  Log("SC09 wagon preview uses its own label: MSUL_MON RVA1E460D displacement2E8 -> 2F4; runtime only");
 }
 void RestoreMpsudAvWagonFieldFix(){
  if(mpsudWagonPatchSite==IntPtr.Zero)return;
  if(GetModuleHandle("MSUL_MON.dll")!=mpsudWagonModule)throw new Exception("SC09 restore module guard failed");
  bool original=NativeBytesEqual(mpsudWagonPatchSite,MpsudWagonOriginal),corrected=NativeBytesEqual(mpsudWagonPatchSite,MpsudWagonCorrected);if(!original&&!corrected)throw new Exception("SC09 restore instruction guard failed");
  if(corrected)WriteMpsudWagonDisplacement(0xe8);else RestoreMpsudWagonProtection();
  if(!NativeBytesEqual(mpsudWagonPatchSite,MpsudWagonOriginal)||mpsudWagonProtectionPending)throw new Exception("SC09 original instruction restoration verification failed");
  mpsudWagonPatchSite=IntPtr.Zero;mpsudWagonModule=IntPtr.Zero;Log("SC09 original MSUL_MON instruction restored before native close");
 }

}
