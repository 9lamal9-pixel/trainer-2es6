using System;using System.IO;using System.Diagnostics;using System.Windows.Forms;using System.Drawing;
class TrainerStart:Form{
 Label info; Process child;Timer timer;
 TrainerStart(){Text="Полный запуск тренажёра 2ЭС6";ClientSize=new Size(650,180);info=new Label{Dock=DockStyle.Fill,Font=new Font("Segoe UI",11),Padding=new Padding(15)};Controls.Add(info);Shown+=(s,e)=>Run();}
 void Log(string s){info.Text=s;File.AppendAllText(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,"Trainer_start_log.txt"),DateTime.Now.ToString("O")+" "+s+Environment.NewLine);}
 void Run(){try{string folder=Path.GetFullPath(Path.Combine(AppDomain.CurrentDomain.BaseDirectory,@"..\work\trenager_static\WORK_TRENAGER_3D"));child=Process.Start(new ProcessStartInfo(Path.Combine(folder,"Trenager_3D.exe")){WorkingDirectory=folder,UseShellExecute=false});Log("Основной тренажёр запущен, PID "+child.Id+"\nРабочая папка: "+folder);timer=new Timer{Interval=1000};timer.Tick+=(s,e)=>{if(child.HasExited){timer.Stop();Log("Основной процесс завершился. Код: 0x"+child.ExitCode.ToString("X8"));}};timer.Start();}catch(Exception ex){Log(ex.ToString());}}
 [STAThread]static void Main(){Application.EnableVisualStyles();Application.Run(new TrainerStart());}}
