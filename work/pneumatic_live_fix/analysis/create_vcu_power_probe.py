from pathlib import Path
p=Path('work/pneumatic_live_fix/analysis/VcuBoundaryProbe.cs').read_text(encoding='utf-8-sig')
helpers=p[p.index('static int InvokeRegister'):p.index('static BindingFlags flags')]
s=Path('work/pneumatic_live_fix/analysis/Sf3ControlAirHostProbe.cs').read_text(encoding='utf-8-sig')
s=s.replace('class Sf3ControlAirHostProbe','class VcuPowerCabProbe')
s=s.replace(' static BindingFlags f=', '[UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate int NativeCall();\n'+helpers+'\n static BindingFlags f=')
a=s.index('  for(int sample=0;');b=s.index(' }catch(Exception e)',a)
body='''  for(int i=0;i<100;i++){Call(h,"ElectricalCycle");Call(h,"ApplyTestInputs");Call(h,"PneumaticCycle");}
  var communication=GetModuleHandle("Communication1.dll");InvokeRegister(cab,1,IntPtr.Add(communication,0x2ae994));var tumba=Marshal.ReadIntPtr(cab,0x10);var tumbaForm=Marshal.ReadIntPtr(tumba,0xc);var vcu=Marshal.ReadIntPtr(tumbaForm,0x3a4);
  var samples=new List<object>();
  foreach(int position in new[]{1,2,1,3}){
   // Explicit pressure fixture permits physical native position3. No pneumatic cycle runs during this fixture.
   if(position==3){var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",f).GetValue(h);Marshal.Copy(BitConverter.GetBytes(.7f),0,cars[0],4);Marshal.Copy(BitConverter.GetBytes(3.5d),0,IntPtr.Add(loc,0xec),8);Call(h,"UpdateVcuInterlock");}
   SetPosition(vcu,IntPtr.Add(communication,0x2a4944),position);
   for(int i=0;i<100;i++)Call(h,"ElectricalCycle");
   if(Marshal.ReadByte(loc,0x8f)!=position)throw new Exception("Native VCU event did not accept position");
   samples.Add(new{position=position,key=Marshal.ReadByte(schema,0x37),km10=Marshal.ReadByte(schema,0x1aa),km11=Marshal.ReadByte(schema,0x1b6),contacts=new byte[]{Marshal.ReadByte(schema,0x1ae),Marshal.ReadByte(schema,0x1b2),Marshal.ReadByte(schema,0x1ba),Marshal.ReadByte(schema,0x1be)}});
  }
  string text=new JavaScriptSerializer().Serialize(new{passed=true,samples=samples,scope="Native SetPosition/event,100 electrical cycles per phase; position3 allowed by explicit TM0.7/IM3.5 fixture; complete pneumatic cab change not validated"});File.WriteAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_vcu_native_power_positions.json"),text);Console.WriteLine(text);return 0;
'''
s=s[:a]+body+s[b:]
Path('work/pneumatic_live_fix/analysis/VcuPowerCabProbe.cs').write_text(s,encoding='utf8')
p=Path('outputs/BuildSf3ControlAirHostProbe.ps1').read_text(encoding='utf-8-sig').replace('Sf3ControlAirHostProbe','VcuPowerCabProbe')
Path('outputs/BuildVcuPowerCabProbe.ps1').write_text(p,encoding='utf8')



