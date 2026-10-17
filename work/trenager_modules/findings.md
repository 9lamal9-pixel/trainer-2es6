# DLL WORK_TRENAGER_3D — статические выводы

Образцы не запускались. Исследованы PE-метаданные, импорты/экспорты и строки. metadata.json и *.strings.txt содержат доказательства и смещения.

| Подсистема | Проверенные доказательства | Вывод |
|---|---|---|
| Динамика поезда | POEZD.DLL экспортирует Poezd, SetPlan, SetProf, SetSostav, SetTorm, SetInitState, GetCurVal, GetMaxA, GetMaxS | Расчёт движения состава с планом/профилем пути и торможением. Формулы/точность без исходников не оценены. |
| Грузовые тормоза | TormModelGruz.dll ProductName: «Модель работы автотормозов грузового поезда»; экспорт ConnectVagonToSost, InitSostav, ProceedTormNew, SetNeshtSitGruz | Модель автотормозов грузового состава, присоединение вагона и нештатные ситуации. |
| Пассажирские/пригородные тормоза | Torm_Pass_Dll.dll и TormModelPrig.dll ProductName: «Модель работы тормозов пассажирского поезда»; Proceed_Torm_Pass/Prig, SetEPK_Sriv/Prig, SetNeshtSitPass/Prig | Раздельные тормозные модели, сценарии неисправностей и срыва ЭПК. Название ProductName у Prig общее с пассажирской моделью. |
| Общая тормозная модель | TormModel.dll: InitSostavClass, ProceedTormClass, GetCurNeshtSitGruz, SetNeshtSitGruz | Классовый интерфейс тормозной модели и нештатных ситуаций. |
| Локомотив 2ЭС6 | LOCOMOTIVES/1/Pult1.dll содержит TPult2ES6, TMSUL2ES6; методы UpdatePribors, UpdateKont, UpdateLight, UpdateOborud и экспорт SetNeispravPult | Пульт, приборы, контроллер, освещение и неисправности оборудования 2ЭС6. |
| Тяга 2ЭС6 | LOCOMOTIVES/1/Tyga1.dll: TTyga2ES6, namespace Tyga_2es6; CalcRecup, ChooseSoedTEDRecup, CalcIvozbMaxMin, GetProcentTygaReal, SetVCur, SetFlagRecup, SetEDT | Расчёт тяги и электрического торможения/рекуперации с токами возбуждения и состоянием ТЭД. |
| МСУЛ 2ЭС6 | LOCOMOTIVES/1/MSUL_MON.dll: TMSUL2ES6; «Давление ГР/ТМ/ЦУ/ТЦ1/ТЦ2», «Скорость от БЭС» и диагностические сообщения об отказах | Экран контроля параметров и диагностики локомотива. |
| Согласование подсистем | Communication1.dll экспорт InitCommunication, ExecuteCommunication, GetNumCurNeispr, SetNeispavnost; сообщения о ненайденных библиотеках тяги, МСУД/МСУЛ, пульта, звуков; TComm2ES6 | Объединяет работу модели локомотива, пульта, звуков и неисправностей. |
| Приборы/системы | KLUB_U_LCD_XE2.dll: InitKlub_U_LCD, ProceedKlub, AddOgranKlub, GetBmpKLUB; Saut_Dll.dll: Init_Saut_Dll, Proceed_saut, AddOgranSaut, AddProfilSaut; BLOK.dll: InitBLOK, ProceedBLOK; KPD_3PV.dll: InitKPD/ProceedKPD; SpeedMeterEM.dll: InitSpeedMetr/ProceedSpeedMetr | Отдельные приборные/системные модули КЛУБ-У, САУТ, БЛОК, КПД-3ПВ, скоростемера. |
| Аппаратные устройства | Cbndll.dll, DLL_BBT.dll, da10.dll импортируют CreateFileA, ReadFile, WriteFile, SetCommState/SetCommTimeouts/GetCommState; JoyLib.dll импортирует DirectInput8Create | Поддержка последовательных портов и устройств ввода; фактически подключённое оборудование не проверялось. |
| Динамическая платформа | MVCONTROL_DLL.dll: PL_GetMotorsData, PL_PowerControl, PL_SetStartPosition, PL_Start, PL_ShutDown; SetCommState/PurgeComm/ClearCommError. Communication содержит TDinPlatform и методы ProceedPlatform, StoppedDinPlatform, LoadPlatfDll | Поддерживается аппаратная платформа с управлением моторами/движением. Наличие/работоспособность оборудования не установлены. |
| Звук и видео | SoundsTren/SoundsLoc/SoundsMSUL — воспроизведение, громкость/частота, записи; VideoModulScen ProductName «Видеомодуль», описание отображения размеченного видео участков профиля пути; PlayVideo — OpenPlay/Play/Pause/SetPosition | Звуковое сопровождение, видео маршрута и графическое отображение. |

Комплект из пяти DLL в LOCOMOTIVES/1/2ES6/1 побайтно совпадает по SHA256 с Communication1.dll, MSUL_MON.dll, Pult1.dll, SoundsMSUL.dll и Tyga1.dll непосредственно в LOCOMOTIVES/1. Это дополнительное подтверждение предназначения текущего комплекта для 2ЭС6.

Все 49 DLL в RESOURCES и вложенных LOCOMOTIVES получили статус Authenticode NotSigned. 44 PE (без пяти проверенных дубликатов) разобраны; machine 0x14c, то есть x86. Многие большие DLL содержат несколько МБ .debug; размер сам по себе не характеризует сложность или опасность.

Метаданные тормозных библиотек называют производителя ЗАО НПЦ «СПЕКТР», г. Екатеринбург. Модули разных поколений: VCL/Delphi и C++ с зависимостями от MSVCR80/MSVCP80. Оценка совместимости при запуске не проводилась.
