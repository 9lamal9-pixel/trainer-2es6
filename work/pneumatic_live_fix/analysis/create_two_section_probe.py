from pathlib import Path

root = Path(__file__).resolve().parents[3]
src = (root / 'work/pneumatic_live_fix/analysis/NativeBvrBtoProbe.cs').read_text(encoding='utf-8-sig')
src = src.replace('class NativeBvrBtoProbe', 'class NativeTwoBvrBtoProbe')
src = src.replace('static PressureSelector selector;static IdealRelay kvt;', 'static PressureSelector selector;static IdealRelay kvt;static IntPtr second;static Reservoir automatic2,signal2;static BtoSection bto2;static PressureSelector selector2;static List<IntPtr> allocations=new List<IntPtr>();\n [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetModuleHandle(string name);\n [DllImport("kernel32.dll",CharSet=CharSet.Ansi)]static extern IntPtr GetProcAddress(IntPtr module,string name);\n [UnmanagedFunctionPointer(CallingConvention.StdCall)]delegate byte InitFn(IntPtr train);')
src = src.replace('+bto.TotalMass;', '+bto.TotalMass+automatic2.Mass+signal2.Mass+bto2.TotalMass;')
src = src.replace('Write(car,4,(float)automatic.Gauge);\n  float beforePm', 'Write(car,4,(float)automatic.Gauge);Write(second,4,(float)automatic2.Gauge);float beforeAuto2=Read(second,4);\n  float beforePm')
src = src.replace('boundary+=pmDelta+autoDelta;', 'double delta2=(Read(second,4)-beforeAuto2)*Flow.Kgf*automatic2.Volume/(Flow.R*automatic2.Temperature);automatic2.ApplyBoundaryMass(delta2);boundary+=pmDelta+autoDelta+delta2;')
src = src.replace('vent+=bto.Step(pm,tm,signal,.01);', 'vent+=bto.Step(pm,tm,signal,.01);selector2.Step(automatic2,pilot,signal2,.01);vent+=bto2.Step(pm,tm,signal2,.01);')
src = src.replace('Write(car,4,(float)automatic.Gauge);Write(loc,5', 'Write(car,4,(float)automatic.Gauge);Write(second,4,(float)automatic2.Gauge);Write(loc,5')
src = src.replace('tc2=bto.Cylinder[1].Gauge', 'tc2=bto.Cylinder[1].Gauge,section2_tc1=bto2.Cylinder[0].Gauge,section2_tc2=bto2.Cylinder[1].Gauge')
needle='  Double(car,0xd7,20);'
setup='''  var cars=(IntPtr[])typeof(TrainerIntegrationHost).GetField("cars",flags).GetValue(host);
  var train=(IntPtr)typeof(TrainerIntegrationHost).GetField("trainData",flags).GetValue(host);
  second=Marshal.AllocHGlobal(243);allocations.Add(second);byte[] copy=new byte[243];Marshal.Copy(car,copy,0,243);Marshal.Copy(copy,0,second,243);
  var vr=Marshal.AllocHGlobal(136);allocations.Add(vr);copy=new byte[136];Marshal.Copy(Marshal.ReadIntPtr(car,0xe8),copy,0,136);Marshal.Copy(copy,0,vr,136);Marshal.WriteIntPtr(second,0xe8,vr);
  foreach(var c in new[]{car,second}){Marshal.WriteInt32(c,0x25,100);Double(c,0xcb,10.4);Double(c,0xd7,20);Double(c,0xdf,1);Marshal.WriteByte(c,0x18,1);Marshal.WriteByte(c,0x1a,0);}
  Marshal.WriteByte(second,0x20,1);Marshal.WriteByte(second,0x22,1);
  var array=Marshal.AllocHGlobal(77*8);allocations.Add(array);for(int i=0;i<77*8;i++)Marshal.WriteByte(array,i,0);
  Marshal.WriteIntPtr(array,0,loc);Marshal.WriteIntPtr(array,4,car);Marshal.WriteIntPtr(array,12,second);
  for(int i=1;i<76;i++)Marshal.WriteIntPtr(array,(i+1)*8+4,cars[i]);
  Marshal.WriteInt32(train,0,77);Marshal.WriteIntPtr(train,0x42,array);
  var init=(InitFn)Marshal.GetDelegateForFunctionPointer(GetProcAddress(GetModuleHandle("TormModel.dll"),"InitSostavClass"),typeof(InitFn));if(init(train)!=1)throw new Exception("Two BVR initialization");
'''
src=src.replace(needle, setup+needle)
src=src.replace('initial=Mass();', 'automatic2=new Reservoir(1,0,293.15);signal2=new Reservoir(1,0,293.15);bto2=new BtoSection(8,1,1,12,6,6,6,6,.7,.001);selector2=new PressureSelector(6,.7);initial=Mass();')
src=src.replace('double autoTc=bto.Cylinder[0].Gauge;', 'double autoTc=bto.Cylinder[0].Gauge,autoTc2=bto2.Cylinder[0].Gauge;')
src=src.replace('double combinedTc=bto.Cylinder[0].Gauge;', 'double combinedTc=bto.Cylinder[0].Gauge,combinedTc2=bto2.Cylinder[0].Gauge;')
src=src.replace('double retainedTc=bto.Cylinder[0].Gauge;', 'double retainedTc=bto.Cylinder[0].Gauge,retainedTc2=bto2.Cylinder[0].Gauge;')
src=src.replace('double releasedTc=bto.Cylinder[0].Gauge;', 'double releasedTc=bto.Cylinder[0].Gauge,releasedTc2=bto2.Cylinder[0].Gauge;')
src=src.replace('if(autoTc<.5', 'if(autoTc2<.5||combinedTc2<3.8||retainedTc2<.5||releasedTc2>.05)throw new Exception("Second section combination failed");\n  if(autoTc<.5')
src=src.replace('One native483A BVR with20l ZR and1l test outlet feeding one BTO section with pressure feedback; finite native BVR boundary exchange; two-section implementation and electrical cycle absent', 'Two native483A records with20l ZR each feeding two BTO sections and four TC groups; native boundary flux accounted explicitly, not whole native mass balance; chamber volumes and passages are test assumptions; electrical cycle and motion absent')
src=src.replace('automatic_tc=autoTc,', 'second_automatic_tc=autoTc2,second_combined_tc=combinedTc2,second_retained_tc=retainedTc2,second_released_tc=releasedTc2,automatic_tc=autoTc,')
src=src.replace('Trainer_native_BVR_BTO_validation.json', 'Trainer_native_two_BVR_BTO_validation.json')
src=src.replace('static int Main(){', 'static int Main(string[] args){bool baseline=args.Length>0&&args[0]=="baseline";')
src=src.replace('Phase(300,4,6);', 'Phase(300,4,baseline?2:6);')
src=src.replace('combinedTc2<3.8', '(!baseline&&combinedTc2<3.8)')
src=src.replace('combinedTc<3.8', '(!baseline&&combinedTc<3.8)')
src=src.replace('passed=true,installed=false,', 'passed=true,baseline=baseline,installed=false,')
src=src.replace('"Trainer_native_two_BVR_BTO_validation.json"', '(baseline?"Trainer_native_two_BVR_BTO_baseline.json":"Trainer_native_two_BVR_BTO_validation.json")')
src=src.replace('((Form)host).Dispose();', '((Form)host).Dispose();foreach(var p in allocations)Marshal.FreeHGlobal(p);')
# Use the same reusable circuit that can be connected to the live host, rather than a probe-only implementation.
src=src.replace('static double initial,boundary,vent,maxError;', 'static TwoSectionAirChain circuit;static double initial,boundary,vent,maxError;')
start=src.index('  double pmDelta=')
end=src.index('  // Feedback',start)
src=src[:start]+'''  circuit.ApplyNativePressureChanges(Read(loc,5)-beforePm,Read(car,4)-beforeAuto,Read(second,4)-beforeAuto2);
  circuit.StepIndependent(code,.1,true);
  boundary=circuit.ExternalMass;vent=circuit.ExhaustedMass;
'''+src[end:]
start=src.index('  pm=new Reservoir(')
end=src.index('  Phase(1000',start)
src=src[:start]+'''  circuit=new TwoSectionAirChain(new TwoSectionAirParameters(2000,100,1,20,1,1,1,12,6,.7,.001),8,5,8);
  pm=circuit.Pm;tm=circuit.Tm;pilot=circuit.Independent;automatic=circuit.Automatic[0];automatic2=circuit.Automatic[1];
  signal=circuit.Signal[0];signal2=circuit.Signal[1];bto=circuit.Sections[0];bto2=circuit.Sections[1];initial=Mass();
'''+src[end:]
src=src.replace('max_boundary_mass_error=maxError,', 'shared_circuit=true,max_boundary_mass_error=maxError,')
src=src.replace('static PressureSelector selector;static IdealRelay kvt;', '')
src=src.replace('static PressureSelector selector2;', '')
(root/'work/pneumatic_live_fix/analysis/NativeTwoBvrBtoProbe.cs').write_text(src,encoding='utf-8-sig')
