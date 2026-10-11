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
}
