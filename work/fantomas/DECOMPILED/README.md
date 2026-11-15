# «Фантомас»: декомпилированный код

Получено 07.11.2026 из копии тренажёра `фантомас/` (исходная установка не менялась). Это восстановленный код, а не исходники разработчика: имена локальных переменных, комментарии и часть типов утрачены. Открытых исходников в интернете по уникальным именам (`ScenarioCreater`, `TrainModuleImport`, `Te116ModuleImport`, `CargoPnevmoSystem`) не нашлось.

| Папка | Что | Инструмент |
|---|---|---|
| `dotnet/` | C#-проекты .NET-сборок (по файлу на тип) | ILSpy `ilspycmd` 11.2.0.10050 (nuget.org, `ilspycmd.win-x64`): `ilspycmd -p -o <папка> --nested-directories -r <папка сборки> <сборка>` |
| `java/Block_v0.2.6/` | Java-код БЛОК (`block/Block_v0.2.6.jar`); собственный код — `base/`, остальное — встроенный JavaFX | CFR 0.152 (Maven Central, SHA-1 48ef4892…): `java -jar cfr-0.152.jar Block_v0.2.6.jar --outputdir <папка>` |
| `native/` | Псевдо-C нативных DLL и EXE, все функции; рядом `.ghidra.log` и `.export.json` | Ghidra 12.2 headless + `ExportCore.java` из конвейера 2ЭС6 (`tools/ghidra_batch.py`) |

Главное для модели 2ТЭ116 — `native/work/Release/Lib/TrainModule.dll.c` (`FUN_180002f90` — шаг движения поезда), `dotnet/work_Release__instructor/IWP/TrainMovementController.cs`, `Train.cs`, `Wagon.cs`; разбор — в `dev/fantomas/brake_proxy/README.md`, раздел «Движение поезда».

## .NET

| Сборка | Платформа | Размер | SHA-256 | Проект |
|---|---|---:|---|---|
| `DatabaseManager/ATR.exe` | x86 | 35840 | 5D3896DB1F88BF11… | `dotnet/DatabaseManager__ATR/` (11 .cs) |
| `DatabaseManager/ELS.exe` | x86 | 77312 | DD9DC94BE423A1F5… | `dotnet/DatabaseManager__ELS/` (10 .cs) |
| `video_tren/Calibrator V2.exe` | x86 | 89600 | 407EC04695E6CA49… | `dotnet/video_tren__Calibrator_V2/` (5 .cs) |
| `work/Release/FantomasBrakes.dll` | x86 | 73216 | 76ED44F09EC87907… | наша модель, исходники в `dev/fantomas/brake_proxy` |
| `work/Release/Lib/2Te116.dll` | x64 | 10752 | CB3304B8428C24D8… | наша модель, исходники в `dev/fantomas/brake_proxy` |
| `work/Release/NetLib.dll` | x86 | 8704 | 35D0065E47007186… | `dotnet/work_Release__NetLib/` (8 .cs) |
| `work/Release/ScenarioCreater.exe` | x64 | 617472 | 2B995EA164D96A18… | `dotnet/work_Release__ScenarioCreater/` (26 .cs) |
| `work/Release/TcpAsyncNetLib.dll` | x64 | 17920 | 5ADE89E4D0FBAB94… | `dotnet/work_Release__TcpAsyncNetLib/` (11 .cs) |
| `work/Release/instructor.exe` | x64 | 725504 | 0750AA6E20A013F1… | `dotnet/work_Release__instructor/` (88 .cs) |
| `work/Release/instructor20211119.exe` | x64 | 726016 | 698F3D069504A105… | `dotnet/work_Release__instructor20211119/` (88 .cs) |
| `work/Release/instructor_.exe` | x64 | 725504 | 921D4314A6596AE1… | `dotnet/work_Release__instructor_/` (88 .cs) |

Версии инструктора:
- `instructor.exe` и `instructor_.exe` — один и тот же код (различаются только метаданные сборки);
- `instructor20211119.exe` — отладочная сборка: в `Train.Update` после тяги вставлено `if (Speed < 250f) WagonSetPull(0, 2000f)`, поезд разгоняется сам. Для занятий её не запускать.

## Нативные модули

Очередь — от маленьких к большим, 4 потока. Модули с тем же SHA-256, что у 2ЭС6, взяты из `ORIGINAL_DECOMPILED`.

| Модуль | Платформа | Размер | SHA-256 | Результат | Где |
|---|---|---:|---|---|---|
| `TrainSim-release/bin/AppWall_double_x64.dll` | x64 | 92160 | 66715DC80950B445… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/FbxImporter_double_x64.dll` | x64 | 7931392 | 84F3C10F6149F670… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/Geodetics_double_x64.dll` | x64 | 33280 | E3252DF52AD598D2… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/Interface_double_x64.dll` | x64 | 84480 | B5C4CEF0F840342B… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/NCSEcw.dll` | x64 | 9610808 | 58ACF911087DE2F0… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/Noesis.dll` | x64 | 4307312 | 358848EF78FA57BD… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/RuMetroBinaries/BrakeMetro.dll` | x64 | 3668992 | 08DA70384A457BB4… | 14338 функций, не восстановлено 1, 7.0 МБ | `native/TrainSim-release/bin/RuMetroBinaries/BrakeMetro.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/ControlAlgoritmes.dll` | x64 | 1725952 | B9417F81CFE5691E… | 6869 функций, 3.3 МБ | `native/TrainSim-release/bin/RuMetroBinaries/ControlAlgoritmes.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/EditMarshruts.exe` | x86 | 13112901 | 82D407C4351C6A9E… | 7518 функций, не восстановлено 1, 5.5 МБ | `native/TrainSim-release/bin/RuMetroBinaries/EditMarshruts.exe.c` |
| `TrainSim-release/bin/RuMetroBinaries/EngineMetro.dll` | x64 | 3645440 | 26F4A6B4E0BD67D1… | 14311 функций, 7.0 МБ | `native/TrainSim-release/bin/RuMetroBinaries/EngineMetro.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/ForceMetro.dll` | x64 | 1639424 | CC8C075684D5357E… | 6597 функций, 3.2 МБ | `native/TrainSim-release/bin/RuMetroBinaries/ForceMetro.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/GenerateFileLicense64.exe` | x64 | 14704446 | A032F374C52F2FC4… | 15743 функций, не восстановлено 1, 8.3 МБ | `native/TrainSim-release/bin/RuMetroBinaries/GenerateFileLicense64.exe.c` |
| `TrainSim-release/bin/RuMetroBinaries/Integration.dll` | x64 | 5414912 | 36427BD56CEFE90B… | 16965 функций, 8.3 МБ | `native/TrainSim-release/bin/RuMetroBinaries/Integration.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/Skif.dll` | x64 | 5159424 | B34CBB5A7B0CDF33… | 17359 функций, не восстановлено 1, 8.3 МБ | `native/TrainSim-release/bin/RuMetroBinaries/Skif.dll.c` |
| `TrainSim-release/bin/RuMetroBinaries/TestMetro.exe` | x64 | 24171193 | 51804B550AFD1A98… | 27140 функций, 13.7 МБ | `native/TrainSim-release/bin/RuMetroBinaries/TestMetro.exe.c` |
| `TrainSim-release/bin/Syncker_double_x64.dll` | x64 | 1134592 | AB8693FB46897AFB… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/TrainSim.exe` | x64 | 7942656 | 0C6B0B6132104603… | 22726 функций, не восстановлено 2, 30.3 МБ | `native/TrainSim-release/bin/TrainSim.exe.c` |
| `TrainSim-release/bin/TrainSimLauncher.exe` | x64 | 1307136 | 54FEDFEECC284499… | 4647 функций, 5.5 МБ | `native/TrainSim-release/bin/TrainSimLauncher.exe.c` |
| `TrainSim-release/bin/Unigine_double_x64.dll` | x64 | 22612992 | 3F43907DB4BC3D02… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/UnitTests.exe` | x64 | 1560576 | 2AAC97BF35F2462E… | 4448 функций, 6.4 МБ | `native/TrainSim-release/bin/UnitTests.exe.c` |
| `TrainSim-release/bin/example-vdisplay-provider.dll` | x64 | 72704 | 7E2F1DA50A0E7B74… | 347 функций, 0.2 МБ | `native/TrainSim-release/bin/example-vdisplay-provider.dll.c` |
| `TrainSim-release/bin/gdal.dll` | x64 | 13219328 | CDB9BD7109B048E1… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/gdalplugins/gdal_ECW_JP2ECW.dll` | x64 | 184320 | 7C284854ACAED47E… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/openal32.dll` | x64 | 891904 | 9A7220F9FD73D393… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/vccorlib140.dll` | x64 | 330120 | D79236E5EF69F842… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/virtual-display.dll` | x64 | 11776 | DAC6B751AA8F4DFB… | 79 функций, 0.0 МБ | `native/TrainSim-release/bin/virtual-display.dll.c` |
| `TrainSim-release/bin/weather_double_x64.dll` | x64 | 156160 | 2870B6B2D510F22F… | сторонний, не декомпилировался | — |
| `TrainSim-release/bin/zlib.dll` | x64 | 124928 | 3E299663B76C0EBE… | сторонний, не декомпилировался | — |
| `video_tren/PultTester2.exe` | x86 | 12800247 | 16D42BAB9CD9350D… | 6677 функций, 4.8 МБ | `native/video_tren/PultTester2.exe.c` |
| `video_tren/RESOURCES/BLOK.dll` | x86 | 558080 | 4BC9CF8F27C13EB8… | 2840 функций, 1.9 МБ | `native/video_tren/RESOURCES/BLOK.dll.c` |
| `video_tren/RESOURCES/DLL_BBT.dll` | x86 | 299008 | C3DF17C2456662D8… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/c3df17c2456662d87324fe775aa73227dc333946424fee6c5a0ea7328a45dc1c/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/DLL_BBT.dll) |
| `video_tren/RESOURCES/InfoDisplay.dll` | x86 | 3298816 | 484AF0A93C11E2A7… | 4110 функций, 3.2 МБ | `native/video_tren/RESOURCES/InfoDisplay.dll.c` |
| `video_tren/RESOURCES/KLUB_U_LCD.dll` | x86 | 12258816 | B62D274283F899B8… | 2664 функций, 2.4 МБ | `native/video_tren/RESOURCES/KLUB_U_LCD.dll.c` |
| `video_tren/RESOURCES/KPD_3PV.dll` | x86 | 8396800 | AEEAA28A37A44471… | 2616 функций, 2.1 МБ | `native/video_tren/RESOURCES/KPD_3PV.dll.c` |
| `video_tren/RESOURCES/LOCOMOTIVES/1/Communication1.dll` | x86 | 4077568 | E4E7CC76AEA4BE43… | 2437 функций, 1.8 МБ | `native/video_tren/RESOURCES/LOCOMOTIVES/1/Communication1.dll.c` |
| `video_tren/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll` | x86 | 2658816 | B4753B2D0CDFB142… | 3066 функций, 2.4 МБ | `native/video_tren/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll.c` |
| `video_tren/RESOURCES/LOCOMOTIVES/1/Pult1.dll` | x86 | 474624 | 229F1B1D67910AA1… | 2272 функций, не восстановлено 1, 1.7 МБ | `native/video_tren/RESOURCES/LOCOMOTIVES/1/Pult1.dll.c` |
| `video_tren/RESOURCES/LOCOMOTIVES/1/SOUNDSMSUL/SoundList.exe` | x86 | 556544 | 53EAA5B23AE40820… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/53eaa5b23ae408207813fa60c6da4138c1349c31d70dd4cbe39d1f31dc47e17e/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1_Old/SOUNDSMSUL/SoundList.exe) |
| `video_tren/RESOURCES/LOCOMOTIVES/1/TestMSUL.exe` | x86 | 564224 | 7C975D62C515543C… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/7c975d62c515543cff06a0dea1faba53a29d622ba025d2ec6a56b37ee9e55f2b/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/1/TestMSUL.exe) |
| `video_tren/RESOURCES/LOCOMOTIVES/1/Tyga1.dll` | x86 | 440832 | AD3D8DDDC32E739E… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/ad3d8dddc32e739e43c24fa088ebf012ee24c245b1a1b005709185ed94491ee8/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/1/Tyga1.dll) |
| `video_tren/RESOURCES/LOCOMOTIVES/SoundList.exe` | x86 | 556544 | 5F82A4D9926E25FF… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/5f82a4d9926e25ff3c3280f0313a2fb1c9c5cf4bf88682dcb5ef27e4b29631bb/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/SOUNDSTOYKA/SoundList.exe) |
| `video_tren/RESOURCES/LOCOMOTIVES/SoundsLoc.dll` | x86 | 431104 | C1EDE01F79F2822A… | 2330 функций, 1.6 МБ | `native/video_tren/RESOURCES/LOCOMOTIVES/SoundsLoc.dll.c` |
| `video_tren/RESOURCES/MVCONTROL_DLL.dll` | x86 | 2132480 | 2BF32C43D1DA4E8D… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/2bf32c43d1da4e8d6cbb070a6343592b3d8dbd194a66e2d0f2c85f93939d6403/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/MVCONTROL_DLL_.dll) |
| `video_tren/RESOURCES/POEZD.DLL` | x86 | 123904 | 822B6D17CE45F801… | 644 функций, 0.5 МБ | `native/video_tren/RESOURCES/POEZD.DLL.c` |
| `video_tren/RESOURCES/RADIO_RV11M.dll` | x86 | 2475520 | 6EE934455D985C62… | 2222 функций, 1.5 МБ | `native/video_tren/RESOURCES/RADIO_RV11M.dll.c` |
| `video_tren/RESOURCES/Radio/RADIO_RV11M.dll` | x86 | 2475520 | B0DC59A6AB185A25… | 2223 функций, 1.5 МБ | `native/video_tren/RESOURCES/Radio/RADIO_RV11M.dll.c` |
| `video_tren/RESOURCES/Radio/Radio.dll` | x86 | 204288 | 579FE9904E99BA41… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/579fe9904e99ba4186d4fa91ddc78d8795087873e7b553da761a3e3da1d5ec57/pseudocode.c` (WORLD3DSERVER/UPDATELAUNCHER/RESOURCES/RADIO/Radio.dll) |
| `video_tren/RESOURCES/Radio/RadioClient.exe` | x86 | 495104 | A4F0DFDB2D0CBCEB… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/a4f0dfdb2d0cbceb5418fc2f08a42444ec518cb49335267df80936fde49253e9/pseudocode.c` (WORLD3DSERVER/UPDATELAUNCHER/RESOURCES/RADIO/RadioClient.exe) |
| `video_tren/RESOURCES/Saut_Dll.dll` | x86 | 96256 | A00EA55CE5CF1FE1… | 534 функций, 0.4 МБ | `native/video_tren/RESOURCES/Saut_Dll.dll.c` |
| `video_tren/RESOURCES/SaveParam.dll` | x86 | 396800 | 2FF69345EAF6ED2A… | 2171 функций, 1.5 МБ | `native/video_tren/RESOURCES/SaveParam.dll.c` |
| `video_tren/RESOURCES/SoundsTren.dll` | x86 | 431104 | 42A82A3F41B711CC… | 2327 функций, 1.6 МБ | `native/video_tren/RESOURCES/SoundsTren.dll.c` |
| `video_tren/RESOURCES/SoundsVideo.dll` | x86 | 431104 | 256524C04D1BDCCD… | 2327 функций, 1.6 МБ | `native/video_tren/RESOURCES/SoundsVideo.dll.c` |
| `video_tren/RESOURCES/TalkSound.dll` | x86 | 405504 | DDE2659BC20C9FAD… | 2192 функций, 1.5 МБ | `native/video_tren/RESOURCES/TalkSound.dll.c` |
| `video_tren/RESOURCES/TormModelGruz.dll` | x86 | 413696 | 5704818B49C5C6E1… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/5704818b49c5c6e16947bdfe3a834c3da1ad053febb57babfe705b719c821bd9/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/TormModelGruz.dll) |
| `video_tren/RESOURCES/TormModelPrig.dll` | x86 | 419840 | 331B93FE9FD260AF… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/331b93fe9fd260af2d3070299abe9f032cae339a2eb40097610066d8ad4befcb/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/TormModelPrig.dll) |
| `video_tren/RESOURCES/Torm_Pass_Dll.dll` | x86 | 419328 | B6B91DA78088E96D… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/b6b91da78088e96d36816c294e1529c4abb247cb994f0d0d639ce4f54ab966f0/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/Torm_Pass_Dll.dll) |
| `video_tren/RESOURCES/VideoModul.dll` | x86 | 559616 | B6E0B12699954132… | 2637 функций, 2.1 МБ | `native/video_tren/RESOURCES/VideoModul.dll.c` |
| `video_tren/RESOURCES/VideoModulScen.dll` | x86 | 551936 | 31D69803D533F68D… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/31d69803d533f68dbedb947d6357f61c16564793eb0541f735e0c1359bdd16b8/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/VideoModulScen.dll) |
| `video_tren/RESOURCES/VideoModul_1.dll` | x86 | 541184 | 2E5065B0A9F8EE49… | 2612 функций, 2.0 МБ | `native/video_tren/RESOURCES/VideoModul_1.dll.c` |
| `video_tren/RESOURCES/VideoModul__.dll` | x86 | 542720 | E5B0B4740CC68595… | 2614 функций, 2.0 МБ | `native/video_tren/RESOURCES/VideoModul__.dll.c` |
| `video_tren/RESOURCES/VideoStream.dll` | x86 | 248320 | F0800A941ACB26C0… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/f0800a941acb26c0517f2ddf826da92fff4fe3a650fa08a52d36d55d45b4df05/pseudocode.c` (WORK_TRENAGER_3D/RESOURCES/VideoStream.dll) |
| `video_tren/SmartLauncher.exe` | x86 | 33086049 | 5EEBECB250B415F3… | 16888 функций, не восстановлено 1, 12.3 МБ | `native/video_tren/SmartLauncher.exe.c` |
| `video_tren/SmartVideo.exe` | x86 | 13048832 | 5D7486DF173FA7CB… | 22539 функций, не восстановлено 3, 16.6 МБ | `native/video_tren/SmartVideo.exe.c` |
| `video_tren/UID/jre-8u221-windows-x64.exe` | x64 | 79607256 | 61CCC93957A931D6… | сторонний, не декомпилировался | — |
| `video_tren/WORK_TRENAGER_3D/Install/Launcher/Launcher.exe` | x86 | 562176 | 6F959BACA1E5BCD3… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/6f959baca1e5bcd32d6a699318b9d42597f32f8f2f73946ee5bcd66afb48c72e/pseudocode.c` (WORK_TRENAGER_3D/Install/Launcher/Launcher.exe) |
| `video_tren/WORK_TRENAGER_3D/Install/oalinst.exe` | x86 | 809496 | B8F39714D41E009F… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/b8f39714d41e009f75efb183c37100f2cbabb71784bbd243be881ac5b42d86fd/pseudocode.c` (WORK_TRENAGER_3D/Install/oalinst.exe) |
| `video_tren/WORK_TRENAGER_3D/Install/vcredist_x86.exe` | x86 | 4216840 | 41F45A46EE56626F… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/41f45a46ee56626ff2699d525bb56a3bb4718c5ca5f4fb5b3b38add64584026b/pseudocode.c` (WORK_TRENAGER_3D/Install/vcredist_x86.exe) |
| `video_tren/WORK_TRENAGER_3D/InstructorOption.exe` | x86 | 602624 | 299D9A41733E9433… | 2663 функций, 2.1 МБ | `native/video_tren/WORK_TRENAGER_3D/InstructorOption.exe.c` |
| `video_tren/WORK_TRENAGER_3D/OPTIONS/EditNarushBall.exe` | x86 | 538624 | BCDF039447CD0898… | 2740 функций, 2.0 МБ | `native/video_tren/WORK_TRENAGER_3D/OPTIONS/EditNarushBall.exe.c` |
| `video_tren/WORK_TRENAGER_3D/OPTIONS/EditObuch.exe` | x86 | 893952 | 39E7B4FCFE1BC8AE… | 3760 функций, 2.8 МБ | `native/video_tren/WORK_TRENAGER_3D/OPTIONS/EditObuch.exe.c` |
| `video_tren/WORK_TRENAGER_3D/OPTIONS/EditScen.exe` | x86 | 1029120 | 3285C8A0EBC7D15F… | 4056 функций, 3.1 МБ | `native/video_tren/WORK_TRENAGER_3D/OPTIONS/EditScen.exe.c` |
| `video_tren/WORK_TRENAGER_3D/OPTIONS/Instruction.exe` | x86 | 485888 | 0C7E898E60D40C38… | 2528 функций, 1.8 МБ | `native/video_tren/WORK_TRENAGER_3D/OPTIONS/Instruction.exe.c` |
| `video_tren/WORK_TRENAGER_3D/RAZMETKA/Ekaterinburg-VerhNeyvinsk/VideoReg.exe` | x86 | 529408 | 1A70DA6C414DFAEE… | 2530 функций, 1.8 МБ | `native/video_tren/WORK_TRENAGER_3D/RAZMETKA/Ekaterinburg-VerhNeyvinsk/VideoReg.exe.c` |
| `video_tren/WORK_TRENAGER_3D/RAZMETKA/TYmen-Tobolsk/VideoReg.exe` | x86 | 529408 | 8BE9AD46D037EB10… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/8be9ad46d037eb10287ecc102bcdca955c7d910690190ed55c4409b1fd0f5034/pseudocode.c` (WORLD3DSERVER/SERVICE/SettingVideo/VideoReg/VideoReg.exe) |
| `video_tren/WORK_TRENAGER_3D/RESOURCES/VideoModul.dll` | x86 | 11370956 | D40CF21803DF0238… | 6064 функций, 4.2 МБ | `native/video_tren/WORK_TRENAGER_3D/RESOURCES/VideoModul.dll.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/Diagram.exe` | x86 | 607744 | C9A44C10074E8D8F… | 2555 функций, 1.8 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/Diagram.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/EditNarushList.exe` | x86 | 758272 | FBB44030C32EEF2B… | 2952 функций, 2.3 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/EditNarushList.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/LocoParam.exe` | x86 | 514048 | 5A461F130282B484… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/5a461f130282b484cab2b666814940214916ca895c553803391f9edc268d6b87/pseudocode.c` (WORLD3DSERVER/SERVICE/LocoParam.exe) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/LocomotiveSettings.exe` | x86 | 685568 | B38C8B959F5BBFF9… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/b38c8b959f5bbff95e3c384be68c5a15bebf81345b463ec443f470c15c0e11e2/pseudocode.c` (WORLD3DSERVER/SERVICE/LocomotiveSettings.exe) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/NeisprEditor.exe` | x86 | 770048 | 4AF1B8773CED54F9… | 2977 функций, 2.3 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/NeisprEditor.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/Platform_Test.exe` | x86 | 575488 | D774330BFFD6F28F… | 2773 функций, не восстановлено 1, 2.0 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/Platform_Test.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/ReestrTren.exe` | x86 | 662528 | C62659A3D6F1586A… | 2829 функций, 2.1 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/ReestrTren.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingHardwareTrenager.exe` | x86 | 656384 | 36FC4823BEA811B3… | 2830 функций, 2.2 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/SettingHardwareTrenager.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/DATA/SoundList.exe` | x86 | 556032 | 472DA955B97DA97A… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/472da955b97da97ae6fae06af104a5447bbfb2bd0f0220352a563c5fc035fdde/pseudocode.c` (WORLD3DSERVER/SERVICE/SettingVideo/DATA/SoundList.exe) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/Dxvideo8_333_conv_02_2008.exe` | x86 | 1204736 | CDF2D19A496951E6… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/cdf2d19a496951e6d9f0b781a29b74138c41a6923bb0cbfea3a27a781dc5ca70/pseudocode.c` (WORLD3DSERVER/SERVICE/SettingVideo/Dxvideo8_333_conv_02_2008.exe) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/Projects/123/VideoReg.exe` | x86 | 528896 | 22E60F0617F9C271… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/22e60f0617f9c271bcb0e7e7d1f7f703fe8cce6a829e1b1e9f6068e6f90be76d/pseudocode.c` (WORLD3DSERVER/SERVICE/SettingVideo/Projects/TestFulHD/VideoReg.exe) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/Projects/probaB/VideoReg.exe` | x86 | 526848 | 3024018D999ABA7C… | 2527 функций, 1.8 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/Projects/probaB/VideoReg.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/SettingVideo.exe` | x86 | 1253376 | 1808468471D100E7… | 4301 функций, 3.5 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/SettingVideo.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SettingVideo/SoundsTren.dll` | x86 | 431104 | 280B19DA1D1D786B… | совпадает с 2ЭС6 | `ORIGINAL_DECOMPILED/FULL/modules/280b19da1d1d786b0d2651e448278662f80d2ebaa662aeb7721880b497497d83/pseudocode.c` (WORLD3DSERVER/SERVICE/SettingVideo/SoundsTren.dll) |
| `video_tren/WORK_TRENAGER_3D/SERVICE/SoundList.exe` | x86 | 556544 | 406C777D54D34BF1… | 2578 функций, 1.9 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/SoundList.exe.c` |
| `video_tren/WORK_TRENAGER_3D/SERVICE/ThemesEditor.exe` | x86 | 587776 | 1CD6C1012EF4A899… | 2653 функций, 1.9 МБ | `native/video_tren/WORK_TRENAGER_3D/SERVICE/ThemesEditor.exe.c` |
| `video_tren/WORK_TRENAGER_3D/Trenager_3D.exe` | x86 | 1574400 | 2381B2C8128EBD7B… | 4543 функций, не восстановлено 2, 4.2 МБ | `native/video_tren/WORK_TRENAGER_3D/Trenager_3D.exe.c` |
| `video_tren/_PultTester.exe` | x86 | 12719898 | DC5099A6365894A6… | 6612 функций, 4.7 МБ | `native/video_tren/_PultTester.exe.c` |
| `work/Release/LauncherRussia.exe` | x64 | 16668672 | C27B9DD451D260C4… | 2371 функций, не восстановлено 1, 3.5 МБ | `native/work/Release/LauncherRussia.exe.c` |
| `work/Release/Lib/2Te116_orig.dll` | x64 | 39936 | 75407B17D37A0E4A… | 189 функций, 0.2 МБ | `native/work/Release/Lib/2Te116_orig.dll.c` |
| `work/Release/Lib/CargoPnevmoSystem.dll` | x64 | 111616 | 0DA5CF8DB5415679… | 571 функций, 0.3 МБ | `native/work/Release/Lib/CargoPnevmoSystem.dll.c` |
| `work/Release/Lib/TrainModule.dll` | x64 | 25088 | 5ECCABCE02B989AC… | 130 функций, 0.1 МБ | `native/work/Release/Lib/TrainModule.dll.c` |

Итого нативных: 59 декомпилировано, 23 совпадают с 2ЭС6, 15 сторонних, не получилось — 0.

## Как повторить

`tools/`: `inventory.py` (опись PE: управляемый или нет, платформа, SHA-256), `ghidra_batch.py` (очередь Ghidra с защитой свободного места на диске), `pedis.py` (быстрый дизассемблер capstone с подписью констант), `queue_native.json`, `fantomas_pe.json`. Ghidra 12.2 и JDK 25 — в `%TEMP%\opencode` (из конвейера 2ЭС6).
