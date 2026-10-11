from pathlib import Path
p=Path('outputs/TrainerIntegrationHost.cs');s=p.read_text(encoding='utf-8-sig')
s=s.replace('Ptr(output,0x32,frame);Ptr(output,0x3e,IntPtr.Zero);','if(!centerSaved){SaveDisplay("BLOK_center",frame);centerSaved=true;}Ptr(output,0x32,frame);Ptr(output,0x3e,IntPtr.Zero);')
p.write_text(s,encoding='utf-8-sig')
