from pathlib import Path
p=Path('outputs/TrainerIntegrationHost.cs');s=p.read_text(encoding='utf-8-sig')
s=s.replace('int ticks;string root,resources;','int ticks,centerFrames;bool leftSaved,rightSaved,centerSaved;string root,resources;')
s=s.replace('commStep();klubStep();blokStep();RouteBlokDisplay();ticks++;','var pending=Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32);commStep();if(pending!=IntPtr.Zero&&Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32)==IntPtr.Zero)centerFrames++;klubStep();blokStep();RouteBlokDisplay();ticks++;')
s=s.replace('  if(ticks==10){SaveDisplay("MSUL_left",Marshal.ReadIntPtr(mem["TPult"],0x105));SaveDisplay("MSUL_right",Marshal.ReadIntPtr(mem["TPult"],0x109));SaveDisplay("BLOK_center",Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32));}', '''  if(!leftSaved&&Marshal.ReadIntPtr(mem["TPult"],0x105)!=IntPtr.Zero){SaveDisplay("MSUL_left",Marshal.ReadIntPtr(mem["TPult"],0x105));leftSaved=true;}
  if(!rightSaved&&Marshal.ReadIntPtr(mem["TPult"],0x109)!=IntPtr.Zero){SaveDisplay("MSUL_right",Marshal.ReadIntPtr(mem["TPult"],0x109));rightSaved=true;}
  if(!centerSaved&&Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32)!=IntPtr.Zero){SaveDisplay("BLOK_center",Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32));centerSaved=true;}
  Log("Center frames consumed by native cab="+centerFrames);''')
p.write_text(s,encoding='utf-8-sig')
