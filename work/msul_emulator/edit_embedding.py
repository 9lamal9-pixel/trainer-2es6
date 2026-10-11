from pathlib import Path
p=Path('outputs/TrainerIntegrationHost.cs');s=p.read_text(encoding='utf-8-sig')
s=s.replace('F(wagon,0x25,80)','I(wagon,0x25,80)')
s=s.replace('KlubHandle=(uint)kh.ToInt32(),Visible=1','KlubHandle=(uint)kh.ToInt32(),Visible=0,HandleFlag=1')
s=s.replace('mem["TPult"]=IntPtr.Add(owner,0x24);','mem["CommOwner"]=owner;mem["TPult"]=IntPtr.Add(owner,0x24);')
s=s.replace('if(ticks==10||ticks%100==0)Log(', 'if(ticks==10||ticks%100==0){CheckDisplays();Log(')
s=s.replace('+FloatAt(mem["TInputParamPult"],1));status.Text=', '+FloatAt(mem["TInputParamPult"],1));}status.Text=')
pos=s.index(' [STAThread]')
s=s[:pos]+''' void CheckDisplays(){
  var wrap=Marshal.ReadIntPtr(mem["CommOwner"],0x2bc);var form=wrap==IntPtr.Zero?IntPtr.Zero:Marshal.ReadIntPtr(wrap,0xc);
  Log("Display feeds: MSUL1="+Marshal.ReadIntPtr(mem["TPult"],0x105).ToString("X")+" MSUL2="+Marshal.ReadIntPtr(mem["TPult"],0x109).ToString("X")+" PB="+Marshal.ReadIntPtr(mem["TOutParamAllTRen"],0x32).ToString("X"));
  if(form!=IntPtr.Zero)for(int i=0;i<3;i++){var view=Marshal.ReadIntPtr(form,0x500+4*i);Log("Cab display "+i+" bitmap="+Marshal.ReadIntPtr(view,0x298).ToString("X"));}
 }
'''+s[pos:]
p.write_text(s,encoding='utf-8-sig')
