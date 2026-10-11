from pathlib import Path
p=Path('outputs/TrainerIntegrationHost.cs');s=p.read_text(encoding='utf-8-sig')
s=s.replace('commStep();klubStep();blokStep();ticks++;','commStep();klubStep();blokStep();RouteBlokDisplay();ticks++;')
s=s.replace('" PB="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32).ToString("X")','" PB="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32).ToString("X")+" BLOK="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x3e).ToString("X")')
s=s.replace('  var wrap=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);','  if(ticks==10){SaveDisplay("MSUL_left",Marshal.ReadIntPtr(mem["TPult"],0x105));SaveDisplay("MSUL_right",Marshal.ReadIntPtr(mem["TPult"],0x109));SaveDisplay("BLOK_center",Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32));}\n  var wrap=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);')
pos=s.index(' void CheckDisplays()')
s=s[:pos]+''' // The cab consumes THndlBmp.KLUB (+0x32); the BLOK module produces THndlBmp.BLOK (+0x3e).
 // Transfer ownership only when the cab has consumed the preceding frame. Its native Paint deletes each HBITMAP.
 void RouteBlokDisplay(){var output=mem["TOutParamAllTRen"];var frame=Marshal.ReadIntPtr(output,0x3e);if(frame!=IntPtr.Zero&&Marshal.ReadIntPtr(output,0x32)==IntPtr.Zero){Ptr(output,0x32,frame);Ptr(output,0x3e,IntPtr.Zero);}}
 void SaveDisplay(string name,IntPtr bitmap){if(bitmap==IntPtr.Zero){Log(name+" frame absent");return;}using(var picture=Image.FromHbitmap(bitmap)){picture.Save(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,name+".png"),System.Drawing.Imaging.ImageFormat.Png);Log(name+" native frame="+picture.Width+"x"+picture.Height);}}
'''+s[pos:]
p.write_text(s,encoding='utf-8-sig')
