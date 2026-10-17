# Статический анализ модулей LAUNCH_CAM

Образцы не запускались. Python/pefile читали только байты файлов. Метаданные и строки со смещениями сохранены в этой папке.

## Camera3D.dll

- Размер 8 517 110 байт; PE32/x86 DLL; версия 1.0.0.0; PE timestamp 2020-03-10 08:36:58 UTC; авторская строка `Developed by Brylin Alexey 2010-2016`.
- Экспорты только `InitClientDll`, `CloseClientDll`.
- `.debug` занимает 6 744 054 байта, поэтому размер библиотеки в основном обусловлен отладочными сведениями. Найдены исходное имя `Camera3DClass.pas` и типы VCL/System.
- Поддержка камеры виртуального 3D мира: `TCamera3DClass.GetCommandRecord`, `TCamera3DClass.RunCommand`, `TCamera3DType`, `CAMERA3D_CID`, `c3Dt_Front`, `c3Dt_Left`, `c3Dt_Right`, `c3Dt_Back`, `c3Dt_Free`, `c3Dt_Former`, `c3Dt_VagonMan` (смещения 0x65b651–0x65b7eb). Общий интерфейс содержит `scc_CorrectionCamera3D` (0x65bd95).
- Обмен с сервером и приложением: `TClientDllOwner.FromServerData`, `ToServerData`, `SendCommandToServer`, `PostMessagesToForm`, `TListenerThread`, `WorkTo3D`, `WorkToVideo`.
- Общие перечисления содержат роли instructor/DSP/VAGON/SOSTAVITEL/PRIEMSDAT/DNC/TREN и команды начала/конца игры, экзамена, перезагрузки/выключения. Это названия из общего протокола, не доказательство фактического выполнения каждой команды этой DLL.
- Нет основания по одному имени LAUNCH_CAM заключать о веб-камере или видеонаблюдении: извлечённые имена описывают виртуальную камеру и железнодорожный тренажёр.

## BusinessPlay.dll

- PE32/x86 DLL; единственный экспорт `Examen`.
- Назначение — экзаменатор/контроль знаний в деловой игре. Строки `Деловая игра` (0xbf7d4), `Экзаменатор` (0xbf7ec), `Выберите правильные ответы.` (0x71e00), `Укажите правильный порядок ответов` (0x71e24), `Время экзамена истекло` (0x72a0c), `Количество правильных ответов: %d` (0xbd558), `Оценка` (0xbef6c).
- Типы `TExamen`, `TExamenResult`, `TExamenator`; отчетные компоненты QuickReport и импорты печати (winspool, PrintDlgA) поддерживают вывод результатов на печать.
- Timestamp 1992-06-19 выглядит как стандартное/неинформативное значение старого Delphi, его нельзя использовать как достоверную дату создания продукта.

## Radio.dll и RadioClient.exe

- Оба PE32/x86; DLL timestamp 2016-12-02, клиент 2019-09-16; версия обоих 1.0.0.0.
- Экспорты DLL: `Call`, `ChangeChannal`, `Connect`, `Disconnect`, `GetStat`, `GetUserCount`, `GetVolume`, `SetVolume`, `StartLog`, `StopLog`, `TestConnect`, `Voice`.
- Уверенно голосовая связь/имитация радиостанции: `TRadioStation` содержит `UDPServer`, `Capturer`, `Calls`, `VizovPlayer`, `FInPack`, `FOutPack`; используются Indy `IdUDPServer`, `CapSound`, `DirectSound9`, а строки API включают `DirectSoundCaptureCreate` и `DirectSoundFullDuplexCreate8`.
- Клиент подгружает `Radio.dll`, читает `Radio.ini`, `COMMON`, `Server_IP`, `Channal`, имеет тип `TRadioParams` и поля `FPort`, `FServerIP`, `FChannal`, `FRole`, `FTangenta`.
- UI: `Радиостанция` (0x17dfb8), `Вызов` (0x17e0ef), `Разговор` (0x17e464), `Tangenta_BtnMouseDown`/`Tangenta_BtnMouseUp` — кнопка PTT/тангенты. Голос предполагает использование микрофона во время работы радиосвязи; условия включения в реальном запуске не проверялись.
- `StartLog`/`StopLog` и `logger` подтверждают возможность логирования; содержимое/формат записи без запуска не установлен.

## Подписи и ограничения

`Get-AuthenticodeSignature` возвращает `NotSigned` для Camera3D.dll, BusinessPlay.dll, Radio.dll и RadioClient.exe. PE certificate directory для всех пуст. Наличие неподписанных файлов само по себе не доказывает вредоносность. Анализ не включал запуск, поведенческий мониторинг или антивирусную проверку.
