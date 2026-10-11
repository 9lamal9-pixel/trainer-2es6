from pathlib import Path
p=Path('outputs/TrainerIntegrationHost.cs');s=p.read_text(encoding='utf-8-sig').replace('class TrainerIntegrationHost:Form','partial class TrainerIntegrationHost:Form')
s=s.replace('F(mem["TInputParamPult"],1,(float)speed.Value);','SetInitialSpeed((float)speed.Value);')
s=s.replace('Ptr(c,0x1d,mem["PWagonArray"]);','Ptr(c,0x1d,mem["PWagonArray"]);InitializePhysics();')
s=s.replace('0x32);commStep();','0x32);PneumaticCycle();commStep();MotionCycle();')
s=s.replace('CheckDisplays();Log(', 'CheckDisplays();LogPhysics();Log(')
s=s.replace('Масса в данных: 6000 т, 75 вагонов. Пневматика и поездная динамика ещё не подключены.','Пневматика и движение поезда подключены: 6000 т вагонов + 200 т электровоза.')
p.write_text(s,encoding='utf-8-sig')
