# МПСУиД: источники и фактические поля экранов

Дата: 2026-10-11. Область: «Машинист», СИ, индикация ЭДТ/рекуперации, САУТ, ввод параметров автоведения и СМЕ. Production не изменялся, native Run/Test этим субагентом не выполнялся. Это карта renderer и предложения детерминированных проверок; она не заменяет проверку физической модели и нормативную приёмку по РЭ.

## Основание и границы

Текущая DLL: `work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1/MSUL_MON.dll`, SHA256 **95E3FB14CBC7FF2B5E9C61DC2215CFA7C2F23161FE21D075ADF04F45907B653B**. RVA ниже относятся исключительно к этой версии. Смещения объектов относятся к x86 Delphi ABI. Адрес = фактическая база загруженного модуля + RVA.

Первичные технические доказательства: bytes текущей DLL, `work/msul_emulator/native_record_schema.json`, опубликованные field tables и DFM текущего модуля, перечисленные в `dev/docs/audit_parts/MPSUD_SCREEN_FIELDS.json`. Узкий свежий disassembly и SHA находятся в `work/mpsud_screen_values/decode_manifest.json` и отдельных `.asm` того же каталога. Линейный decoder включает inline data; значения switch-веток проверяются с действительного target, не по случайно декодированным байтам таблицы переходов.

Для нормативных требований используются источники и ограничения общего `dev/docs/MPSUID_2ES6_AUDIT.md`. Точная редакция РЭ/комплектация №137+ не установлена. Полный заводской том с подробным описанием экранов получить не удалось; таблица ожидаемых значений ниже означает **ожидаемое поведение текущего renderer**, а не «правильное по РЭ». Статические константы, повторение секций и несогласованные источники подтверждены на уровне компонента. Системный дефект фиксируется окончательно после свежей runtime проверки фактического виджета и проверки нормативного назначения поля.

Режим fixture: сохранить входные bytes, записать контролируемые входы, вызвать только существующий путь обновления UI на его UI thread, прочитать фактический объект, сохранить expected/observed/assertions, восстановить входы и заново обновить UI. `ProceedTyga`, физический `Proceed`, запуск движения и принятие алгоритма САУТ/автоведения такой проверкой не подтверждаются. Перед чтением обязательны SHA/VMT guards, ненулевые и валидные pointers; не писать Delphi string pointers руками.

## Чтение actual widgets

Read-only helper: `work/mpsud_screen_values/RuntimeValueHelpers.cs.txt`. Это заготовка для изолированного fixture host, не production source.

| Класс / поле | Смещение в объекте | Тип / источник доказательства |
|---|---:|---|
| `TIndicatorPribor.Value` | +0x1F8 | Double; `SetValue` RVA 0x1C9C2C |
| `TIndicatorPribor.Min/Max` | +0x200 / +0x208 | Double; setter ограничивает Value этим диапазоном |
| Связанный `TDataCaption` индикатора | +0x26C | x86 pointer, может быть null |
| `TDataCaption.DataStr` | +0x1EC | UnicodeString pointer, длина UInt32 перед данными по адресу −4 |
| `TDataCaption` integer cache | +0x1F0 | Int32; обновляется только integer setter RVA 0x1C68D4 |
| `TDataCaption` Double cache | +0x1F8 | Double; обновляется Double setter RVA 0x1C64AC |
| `TDataCaption.CountAfterPoint` | +0x200 | Int32; Double formatter строит формат `%.[N]f` |
| `TDataCaption.State` | +0x239 | Byte; setter RVA 0x1C643C |
| `TLabel/TControl` caption buffer | +0x17C | **PWideChar UTF16 с NUL**, не UnicodeString; `DefaultHandler` RVA 0x94194 делает StrNew/StrDispose/PWCharLen |
| `TSAUTArrowPribor.Value` | +0x298 | Double, clamp собственными Min/Max; setter RVA 0x1CFA84 |
| `TSAUTArrowPribor.PIndicator/PLabel` | +0x30C / +0x310 | x86 pointers |
| вложенный SAUT `TIndicator.IValue/IValue2` | +0x20 / +0x28 | Double: скорость / ограничение; setters 0x1CF0EC / 0x1CF1B8 |
| вложенный SAUT `TIndicator.Min/Max` | +0x48 / +0x40 | Double; выход за диапазон вызывает exception |
| вложенный SAUT `TPriborLabel.PLCaption` | +0x8 | UnicodeString; setter 0x1CEDDC |

Integer, Double и string setter имеют отдельные caches. Нельзя подтверждать обновление строкового поля чтением старого Int32 cache. Для округляемого тока проверять одновременно Indicator.Value и DataCaption.DataStr. Для СИ проверять строки буфера, который читает DrawCell; записанный в fixture input ещё не является наблюдением UI.

Уточнение по фактическому callback: `Ija1_12ChangeState` RVA1CB9B0 и `UksChangeState` RVA1CC188 делают ROUND → обратное преобразование в Double → **Double setter1C64AC**, а не integer setter. Поэтому numeric caption cache+1F8 содержит округлённое Double; cache+1F0 у этих показаний не является актуальным наблюдением. Из helper MashSnapshot правильны строка+1EC и исходное Indicator.Double+1F8.

## ABI источников

`TMSUL2ES6` packed: +1 pointer TPult, +5 pointer TTygaParam, +9 pointer TShema. TPult: +0 pointer TCommonBtn, +4 pointer TLocParam, +8 pointer TVagonParam, +C pointer TInputParamPult, +18 pointer TOutParamAllTRen. Ниже offsets от **базы названной структуры**, а не от объекта frame.

| Структура | Поле / offset | Тип / назначение |
|---|---|---|
| TTygaParam | +0 V | Double; физическая скорость signed m/s, см. motion audit |
| TInputParamPult | +1 Vcur | Single; переданная host скорость km/h |
| TTygaParam | +C Pos, +10 PosRec | Int32 |
| TTygaParam | +27 ResT | pointer TResTyga |
| TResTyga | +11 Uset | Single, V; источник Uкс в проверенных renderer |
| TTygaParam | +33 Udvig | Double; проверенная строка UЭДТ СИ его **не читает** |
| TTygaParam | +3B raw Tok[8], +5B Tok_av[8] | массивы Single, шаг группы 4 bytes, шаг секции 8 bytes |
| TTygaParam | +7B raw Iv[8], +9B Iv_av[8] | массивы Single, тот же шаг |
| TTygaParam | +DC SoedTED | Byte: 0 ` - `, 1 `С`, 2 `СП`, 3 `П` по DecodeSoedTED |
| TTygaParam | +DD ProcReal, +E1 ProcTygaUst | Int32, % |
| TTygaParam | +10D VklTD[8] | массив Int32; первый используется СМЕ «МВ включен» |
| TTygaParam | +12D ElectrTorm, +12E EDTVkl | Bool, по одному byte |
| TShema | +B6 MVProcent | Int32, % |
| TShema | +125 Useti | Int32, V; иной источник, не выбран ProceedCur/CI.UpdateData |
| TShema | +242 PSN[4] | Int32, секционный шаг 4 bytes |
| TLocParam | +5 GR, +D TC | Single; gauge kgf/cm² |
| TVagonParam | +0 TM | Single; головная TM, gauge kgf/cm² |
| TOutParamAllTRen | +1 SAUT_TetaR | Double |
| TOutParamAllTRen | +A SAUT_VOgr, +10 SAUT_Dist | Int32; km/h и m соответственно |
| TLocParam | +99 EPKPos.Kluch | Byte |
| TCommonBtn / TPBBtn | +F TPBBtn; затем +5 ALS, +6 SAUTVKL | pointer / два Byte; enabled только при обоих ==1 |

## «Машинист» и ЭДТ

Private pMSUL frame+0x460. `ProceedFrame` RVA 0x1CBFB8 вызывает `ProceedCur`, `UpdatePos`, `UpdateBox`, `UpdateOborud`, `UpdateMessages`. Current reading path подтверждён `ProceedCur` 0x1CBC08 и `UpdatePos` 0x1CD66C.

| Виджет | Published pointer offset | DataCaption pointer | Источник / преобразование |
|---|---:|---:|---|
| Uкс | +2A8 | +3CC | ResT.Uset Single; indicator Double; caption Round |
| Ia сек1 1–2 / 3–4 | +2AC / +2B0 | +3A4 / +3A8 | Abs(Tok_av +5B/+5F); caption Round |
| Iv сек1 1–2 / 3–4 | +2B4 / +2B8 | +3AC / +3B0 | Abs(Iv_av +9B/+9F); caption Round |
| Ia / Iv сек2 | +2BC / +2C0 | +3B4 / +3B8 | raw Tok+43 !=0: first average +63/+A3; иначе second +67/+A7; Abs |
| Ia / Iv сек3 | +2C4 / +2C8 | +3BC / +3C0 | raw Tok+4B !=0: +6B/+AB; иначе +6F/+AF; Abs |
| Ia / Iv сек4 | +368 / +36C | +3C4 / +3C8 | raw Tok+53 !=0: +73/+B3; иначе +77/+B7; Abs |
| Режим | — | +414 | GetNameRezhim RVA 0x1CB7F0 |
| Позиция / фактический % / заданный % / МВ % | — | +418 / +41C / +434 / +420 | Pos / ProcReal / ProcTygaUst / Shema.MVProcent |

Секции 2–4 выбирают **одну группу**, не среднее двух групп. Условие проверяет ненулевой абсолютный raw current (константа compare 0 подтверждена), не средний ток. Отрицательный raw current ненулевой и выбирает first group.

Режим: ElectrTorm=1,EDTVkl=0 даёт `Рекуперация [соединение]`; EDTVkl=1 — `ЭДТ [соединение]`; иначе Pos!=0 — `Тяга [соединение]`; Pos=0 при валидном pMSUL — только строка соединения. `Выбег` существует в fallback при null pMSUL. Это точно описывает DLL; нормативность нулевой подписи требует отдельного источника.

Fixture M: Pos=23, SoedTED=1, ProcReal=67, ProcTygaUst=42, MVProcent=55; ResT.Uset=3123.25, Shema.Useti=3299 (различные источники); Ia1 −311.25/+322.25; Iv1 411.25/422.25. Expected indicators 3123.25,311.25,322.25,411.25,422.25; captions `3123`,`311`,`322`,`411`,`422`; position `23`, проценты `67`,`42`,`55`. Ia2 first/second=433/444, Iv2=533/544: raw+43=1 →433/533; raw+43=0 →444/544. Числа внутри Min/Max, не на .5 tie. Separate fixture переключает flags и проверяет mode prefix, оставляя physics остановленной.

Digit2: TShema.ShowSpisok +322/+323 (per monitor). `UpdateMessages` 0x1CC3A4 читает Shema[321+frame.monitorNumber(+490)] и вызывает SetVisible у Panel5 pointer frame+454, опубликованный контейнер списка сообщений. ProceedCur/UpdatePos не используют этот flag. Для проверки собственного экрана достаточно читать Visible контейнера через правильный VCL accessor; TPanel Caption через helper для TLabel не читать.

## СИ

`UpdateData` RVA 0x1DE9FC обновляет только section=1,2 (loop end 3), независимо от SectionCount. Private pMSUL frame+2DC. Буферы — **UnicodeString pointers**, не TDataCaption. DrawGrid2/3 читает эти же буферы.

Формулы от frame: block1 `0x2B4 + 0x30*section + 4*row`, block2 `0x344 + 0x30*section + 4*row`; stride **48 decimal bytes =0x30**, не0x48. Готовые bases: block1 сек1=2E4, сек2=314; block2 сек1=374, сек2=3A4. Row=0..11.

| row | block1 | block2 |
|---:|---|---|
| 0 | Round(ResT.Uset), blank при U<=0 | blank |
| 1 | Round(Tok_av first), signed | I генератора: PSN==0 →0, иначе5 |
| 2 | Round(Tok_av second), signed | U бортсети: всегда110 |
| 3 | Round(**raw Iv first**), signed | U АБ: PSN==0 →110, иначе140 |
| 4 | Round(**raw Iv second**), signed | I АБ: PSN==0 →−5, иначе0 |
| 5 | UЭДТ = Round(**Pos*2800/65**) | Riso ТЭД1–2 = fixed33.3 |
| 6 | I собственных нужд =fixed0 | Riso ТЭД3–4 =fixed33.1 |
| 7 | I ПТР1 =Round(Tok_av first/4) | Riso ЦУ =fixed0.46, текст0.5 |
| 8 | I ПТР2 =Round(Tok_av second/4) | ТЦ1 =Loc.TC |
| 9 | GR =Loc.GR | ТЦ2 =тот же Loc.TC |
| 10 | TM =головная TVagon.TM | скорость =Round(Tyga.V), **без*3.6** |
| 11 | CU =**Loc.GR−1** | blank |

Токи и давления секций используют общие/повторённые источники, кроме секционных Ia/Iv и PSN. Для Ia/Iv секционный offset +8*(section−1). UЭДТ вообще не использует Udvig. Fixed Riso использует `%-10.1f`, строки padded до10characters и decimal separator текущего FormatSettings; сравнивать `.Trim()` либо хранить exact native string. GR/TM/CU/TC форматируются `FloatToStrF(format=0,precision=1,digits=1)` — это не гарантированная одна цифра после запятой. Для initial fixture выбрать целые8/5/2, а fractional precision проверять отдельным наблюдением.

Энергия: consumed/recuperated сек1 frame+404/+408, сек2+40C/+410; UpdateData всегда пишет `0`. Различение fixed и source-based показаний важно для трактовки зелёного assertion: оно доказывает текущую заглушку, не измеритель.

Fixture CI: Pos23 и Udvig1777.25 → обе UЭДТ строки `991`, несмотря на Udvig; Ia1 −311.25/322.25 →`−311`/`322` (actual ASCII `-311`); raw Iv1=123.25/234.25, Iv_av=411.25/422.25 →СИ`123`/`234`, «Машинист»411/422. PSN1=0, PSN2=1 →генератор0/5, UAB110/140, IAB−5/0, Uboard110/110. Riso `.Trim()`=33.3/33.1/0.5, energy четыре`0`. Loc.GR8,TM5,TC2 →GR8,TM5,CU7,TC1/TC2=2. Tyga.V=−17.5 m/s плюс Input.Vcur=63 km/h: renderer CI Round в зависимости от FPU rounding; вместо tie лучше V=−17.25 и Vcur=62.1 →CI`-17`, SAUT62. Изменить каждый unrelated input и доказать unchanged stub одновременно с изменившимся source field.

## САУТ

`UpdateSAUTPrib` RVA0x1D1D84; pMSUL frame+2DC. Arrow pointer+298; ограничение TLabel+2BC; расстояние Indicator+2C0, DC+2C4; САУТState DC+2C8; ЭПКState DC+2CC; ThetaR DC+2D0. Speed source TPult.Input.Vcur+1: сначала Round, затем Abs; обе стрелка/числовой indicator и вложенная speed label получают это значение. Ограничение TPult.Out+А отображается всегда. Distance/Theta/SautState обнуляются если хотя бы один из ALS/SAUTVKL buttons не равен1. EPK state берётся непосредственно из Loc+99.

Fixture S: Input.Vcur=−63.25, Out.VOgr=80,Dist=1234,ThetaR=0.625,Loc.EPKkey=1, buttons5/6=1: actual Arrow.Value=63, nestedIValue63, nestedIValue2=80, speedtext`63`, limittext`80`, Distance1234/DC`1234`, ThetaDouble0.625 (текст зависит CountAfterPoint), SAUTState1, EPKState1. Выключение одной кнопки: Distance0/Theta0/SAUTState0; speed63/limit80/EPK1 сохраняются. Перед пробой проверить пределы Distance и Arrow/TIndicator. Эта проверка не подтверждает вычисление реальной кривой САУТ.

## Автоведение: редактор параметров

`ProceedFrame` RVA0x1E420C, pMSUL frame+30C. TShema.AutovedNastr+324: curPos Int32+324, VvodFlagBool+328, embedded **ShortString[10]** +329 (11 bytes: lengthByte + CP1251characters), modeByte+334, intensityInt32+335, numberInt32+339, massInt32+33D, wagonCountInt32+341, porozhBool+345.

Intensity label+2D0 обновляется всегда. Number label+2DC, mass label+2E8, count label+2F4 обновляются **только при соответствующем curPos=2,3,4**. Mode0/1/2 меняет выделение «обычный/по удалению/нагон», porozh0/1 — «нет/да». Они не подтверждают включение/работу физического контроллера автоведения.

Fixture A: VvodFlag0, mode2,intensity7,number2319,mass6000,count75,porozh1. Последовательно обновить curPos2/3/4: ожидаются labelstrings`2319`,`6000`,`75`, intensity`7`. Нельзя проверять все три сразу после единственного обновления curPos2: два остальных остаются прежними.

**Подтверждённое статическое отклонение назначения поля:** curPos4 («вагоны»),VvodFlag1,ShortString`77`: ветка RVA0x1E45ED..0x1E4613 берёт ShortString, но передаёт SetText объекту **frame+2E8 (mass label)**, а normal count branch frame+2F4. Сейчас mass становится`77`, count остаётся`75`; ожидаемое назначение внутри собственного editor: mass`6000`,count`77`. Runtime fixture нужен для фактической приёмки дефекта. Исправлять production bytes этим отчётом не разрешалось; их не меняли. Восстанавливать все11ShortStringbytes, curPos/VvodFlag после пробы.

## СМЕ

`UpdateData` RVA0x1E01BC; pMSUL frame+2BC. Внутренняя матрица Byte frame+`0x2BB+8*row+column`, row1..27,column1..8. Row0 читать/писать нельзя: там private pointers. Обновляются columns1/2 и4/5;3 — gap. PaintContacts и DrawCell используют результаты, поэтому проверка matrix bytes является проверкой фактического UI buffer, а не inputs. VMT RVA1DF398, instance_size0x3AC: допустимый диапазон матрицы2C4..39B, конец39C меньше3AC. Границу проверять перед Marshal чтением.

| Row / подпись | columns1/2 текущий источник |
|---|---|
| 1 «Бл.ВВК» | constant1 |
| 2–4 разъединитель/заземлитель/контрольТКП | notAny(KEP1Kont Shema bytes4C..4F) |
| 5 БВ | Any(QF1 Shema bytes62..65) |
| 6–8 ПСН2ст/ПСН включен/готовностьПСН | Any(PSN[4] Int32==1), одно aggregate для всех columns |
| 9 готовностьМК | constant1 |
| 10–11 РДМК/МК включен | (TPultbyte9C или9D !=0) и Locbyte67==1 |
| 12 «МВ включен» | **Tyga.VklTD[0] Int32+10D !=0**, не состояние вентилятора |
| 13–14 «БлВперёд/Назад» | TygaInt32+1B ==1/−1 |
| 15–17 ОтклСТПР/режСВ/режНВ | constant0 |
| 18–19 БлК36/БлК30 | ShemaByte30A/2F2 |
| 20 УУБК | constant1 |
| 21 жалюзиПТР | Abs(row12−1) |
| 22–27 аварииПСН/боксование/конецЭПТ/ДРВМ/ДРТД/БК | constant0 |

Columns4/5 повторяют агрегаты PSN/QF1 и первый VklTD, но row1=0,row4=0,row21=0. Reverse-ветки RVA1E0A16..1E0AAA используют hardcoded columns: row13 col4=(Tyga+1B==1),col5=NOT этого; row14 col4=(+1B==−1),col5=NOT этого. При reverse0 обе строки дают col4=0,col5=1, хотя columns1/2 имеют0/0. Секционная достоверность отсутствует у этих конкретных aggregate mappings; native физическая работа секций этим выводом не опровергается.

Fixture SME: VklTD[0]=0→1 меняет row12 columns1/2/4/5 на1; row21 columns1/2 становится0,columns4/5 остаётся0. VklTD[1] изменение не влияет на «МВ». Единственный PSN1=1 при PSN2/3/4=0 включает rows6/7/8 во всех обновляемых columns. Assertions дополнить постоянными row1(firstpair1/secondpair0),row9=1,row20=1,rows22/23/24/25/26/27=0. Эти PASS будут означать обнаруженную заглушку/агрегацию, не нормальную диагностику СМЕ.

## Статусы результатов этой части

**Подтверждено статически, высокая уверенность:** record paths/типы, component offsets, setter storage, source selection/abs/round, СИ фиксированные значения и energy0, editor неверный destination, SME aggregate/constant mappings. Доказательства текущей SHA сохранены. Новых functional Run/Test собственным субагентом не выполнялось.

**Нужен fresh end-to-end UI result от root:** actual widgets expected/observed после безопасного UI update; fixture restoration; screenshots рядом с assertion JSON; module identity; независимое переключение двух мониторов и hidden views. Fresh root capture PASS сам по себе не доказывает перечисленные numerical assertions.

**Открыто нормативно:** требуемая секционность/погрешность/единицы СИ и СМЕ точной комплектации; factory destination для отдельных auxiliary captions; active algorithm САУТ/автоведения. Current-component discrepancy и нормативный дефект не объединять без первичного требования.

## Дополнение: цвет показаний и потеря данных

Свежая root fixture `test-20261011T123154502Z-0365d110/MpsudValuesDriver` завершилась PASS, 45 checks. Компактный результат `results/MPSUD_screens_runtime.json` прочитан выборочно; PNG `results/MSUL_runtime_driver_known_values_0.png` визуально проверен. Actual widgets: Uкс3000; Ia1 abs111/222; Iv1 51/62; Ia2/3/4 311/433/555. В парных входах вторые группы322/444/566 больше, но выбран первый group. Runtime тем самым подтвердил source-selection отклонение, сохранив ограничение: это изолированный renderer fixture, не полная приёмка physics. На PNG числовые токи жёлтые и при ненулевых значениях; State у этих DC остаётся0.

Это **не является подтверждением потери данных**. Текущий DFM всех Ia/Iv DC имеет `FontValue.Color=clYellow`, `OnOffFlag=false`, `State=0`, `FontColorOff=clBlack`; UksDC имеет FontValue.Color=clSilver. `TDataCaption.Paint` RVA1C546B..1C54AA при OnOffFlag0 выбирает обычный FontValue и не учитывает State. Setter OnOffFlag RVA1C62CC устанавливает Byte+238; FontValue pointer+1B8; TFont.Color Int32+18; OffColor Int32+244; State Byte+239. Read-only цветовой assertion: DC+238==0 и `[[DC+1B8]+18]==0x0000FFFF` для тока. State0 в таком режиме не означает NoData.

Бар индикатора имеет иной независимый цвет: `TIndicatorPribor.BarColor` Int32+230 (`SetBArColor` RVA1C9558). `SetRecuperatia` RVA1CC094 меняет только Ia bars: traction COLORREF0x0067FF31 (RGB31,FF,67), recup COLORREF0x00FFB331 (RGB31,B3,FF). Числовые DC этот метод не перекрашивает. Значение COLORREF не трактовать как ARGB. DFM Iv BarColor9800738, Uкс16757553; их динамическая смена этим методом не предусмотрена.

Есть функция `GetColorOfDef` RVA1CB784, но её нельзя принимать за код потери каналов Ia/Iv. Она возвращает две палитры для codes0/1/2/default: FGgreen/BGblack; FGblack/BGdarkgreen; FGblack/BGred; FGblack/BGgray соответственно. 64 её вызова в `UpdateOborud` адресуют оборудование и ТМ/ТЦ DC offsets2CC..358, **не** current/Uкс DC3A4..3CC. Узкие callsites сохранены `work/mpsud_screen_values/Mash_Equipment_ColorOfDef_calls.txt`; это не утверждение, что native equipment вообще не имеет диагностического loss code.

В разобранном тракте `ProceedCur → numeric callbacks → SetValue` нет чтения valid/channel-loss flag, переключения FontValue.Color, отдельной ветви yellowzero; Tok/Iv представлены массивами Single без именованного validity field в данном ABI record. `Abs` не является декодированием missing-data code. **Статус:** постоянная жёлтая стилизация токов и отсутствие ветви validity в этом компоненте подтверждены; реальное нормативное событие потери источника/протокольный missing marker и end-to-end yellowzero остаются не проверены. Не имитировать loss повреждением pointers, NaN либо отключением communication без установленного безопасного сценария.
