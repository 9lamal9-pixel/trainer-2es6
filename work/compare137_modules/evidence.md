# Статические особенности текущего модуля 2ЭС6

Исследовались только байты файлов; DLL/EXE не запускались. Текущая копия: `work/trenager_static/WORK_TRENAGER_3D/RESOURCES/LOCOMOTIVES/1`. Названия процедур восстановлены из TD32/FB09, поля структур — из Delphi extended RTTI. ImageBase текущих DLL: `0x400000`; ниже адреса RVA, если не указано VA.

SHA256 Communication1.dll: `27178a089ef33692d16b1d46ae0dd7567135b327a24be9d7d0ca84af8df3db7f`.
SHA256 Pult1.dll: `7dac59bf94276309e012a6eb72602c08a921fe1910a4169f9f0ec27008f0f8f5`.

## Аппаратные приборные выходы

`Communication1.dll`, `TComm2ES6.UpdatePriborsHW`: RVA `0x2b1dc4`, длина `0x130`. Семь прямых вызовов `THardwares.SetDblValule` (RVA `0x19cddc`). Глобальные ID разрешены через два чтения указателя в самом PE; соответствие с `ConstHardware.pas`:

| ID | Имя | Выражение до калибровочного преобразователя |
|---:|---|---|
| 400 | P_ur_pr | `TPult.Eng.LocP.Pnevmo.PSend.Pur` |
| 401 | P_tm_pr | `TPult.Eng.VagP.VSend.Ptm` |
| 402 | P_pm_pr | `TPult.Eng.LocP.Pnevmo.PSend.Pgr` |
| 403 | P_tc1_pr | `TPult.Eng.LocP.Pnevmo.PSend.PtcLoc` |
| 404 | P_tc2_pr | `TPult.Eng.LocP.Pnevmo.PSend.PtcLoc - 0.8` |
| 405 | U_CepiUpr_Prib | literal `110.0` |
| 406 | I_AB_Prib | `TPult.Prib.I_AB * 100.0` |

Указатели глобальных ID (VA -> VA значения -> ID):
`0x6c7804 -> 0x6c56e8 -> 400`, `0x6c795c -> 0x6c56ec -> 401`,
`0x6c7c4c -> 0x6c56f0 -> 402`, `0x6c75e0 -> 0x6c56f4 -> 403`,
`0x6c765c -> 0x6c56f8 -> 404`, `0x6c72e4 -> 0x6c56fc -> 405`,
`0x6c7228 -> 0x6c5700 -> 406`.

Поля RTTI:
- `TPult` (VA `0x5a18ac`): Eng offset `0x4`, Prib offset `0x55`.
- `TEngineParam` (VA `0x5963d8`): LocP offset `0`, VagP offset `4`.
- `TLocParam` (VA `0x596300`): Pnevmo offset `5`.
- `TPnevmo` (VA `0x595f60`): PSend offset `0`.
- `TPnevmoSend` (VA `0x595e94`): Pgr offset `0`, Pur offset `4`, PtcLoc offset `8`.
- `TVagonParam` (VA `0x5955fc`): VSend offset `0`; `TVagonSend` (VA `0x5954b8`): Ptm offset `0`.
- `TPribors_2ES6` (VA `0x5a1128`): I_AB offset `8`.

Доказательства значимых упрощений:

1. TC2: последовательность `8b 40 04 d9 40 0d db 2d e4 1e 6b 00 de e9` — LocP, Single по offset `0x0d`, загрузка Extended по VA `0x6b1ee4`, `fsubp st(1),st(0)`. Extended 80-bit bytes `cd cc cc cc cc cc cc cc fb 3f` = 0.8. Нет условного перехода/сравнения/обрезки между вычитанием и `SetDblValule`. Значение до конвертера отрицательное при PtcLoc < 0.8. Из этого нельзя заключить, что физическая стрелка уйдёт ниже нуля: аппаратный ConvertorFloat и его поведение вне шкалы здесь не разобраны.
2. U управления: `68 00 80 5b 40 6a 00` перед ID405 — push старшего DWORD `0x405b8000` и младшего DWORD `0`; IEEE754 Double = 110.0. В этой функции нет чтения динамического напряжения для данного выхода.
3. I_AB: `d9 40 5d d8 05 f0 1e 6b 00` — чтение TPult.Prib.I_AB и умножение на Single по VA `0x6b1ef0`; bytes `00 00 c8 42` = 100.0. Значения I_AB задаёт Pult1, см. ниже.

`THardwares.SetDblValule` классифицирует ID, записывает переданный Double в описание параметра и вызывает виртуальный метод аппаратного объекта; непосредственно переданный Double не обрезает. Для TC2 корневой Hardware1.cbn содержит ConvertorFloat и две калибровочные точки `mark 0 -> data 472`, `mark 5 -> data 259`; явного Min/Clamp/LowerBound нет (проверка configs). Поэтому доказано только значение до конвертера.

## Ток АБ: двухуровневая индикация

`Pult1.dll`, `TPult2ES6.UpdatePribors`: RVA `0x18ef44`, длина `0x49`:

```text
558bec518945fc8b45fce8d9c9ffff83b8420200000175118b45fce8c8c9ffff83b8460200000174108b45fc8b4004c7405d0000a0c0595dc38b45fc8b4004c7405d0000a041595dc3
```

Оба вызова разрешаются в `TPult2ES6.GetShema` (RVA `0x18b92c`); функция возвращает адрес поля схемы объекта (`self+0x18`).

`TShema` RTTI в Pult1: VA `0x587d98`, массив PSN offset `0x242`, AB offset `0x272`. Код сравнивает DWORD по `0x242` и `0x246` с `1`.

Эквивалент:

```text
if Shema.PSN[0] == 1 and Shema.PSN[1] == 1:
    Pult.Prib.I_AB = 20.0
else:
    Pult.Prib.I_AB = -5.0
```

Литералы stores: `c7 40 5d 00 00 a0 c0` = Single -5.0; `c7 40 5d 00 00 a0 41` = Single 20.0. Затем аппаратный выход получает -500 либо 2000. Это два состояния для индикации, а не доказанная непрерывная модель заряда/разряда АБ. Перевод значений аппаратного конвертера в видимые амперы требует учитывать его калибровку.

Проверка configs: корневая шкала I_AB_Prib mark/data `0 -> 1006`, `50 -> 738`, `100 -> 502`, `150 -> 252`, `175 -> 106`, MaxValue 1024, ConvertorFloat. Альтернатива 2ES6/1: `0 -> 69`, `50 -> 289`, `100 -> 513`, `150 -> 735`, `175 -> 957`. Передаваемые -500/2000 вне указанной шкалы; это потенциальное несоответствие масштаба, а не доказанная ошибка: поведение ConvertorFloat и внутренние единицы аппаратной DLL не восстановлены.

Это действительная часть цикла Pult: `TPult2ES6.Proceed` RVA `0x18ba1c` напрямую вызывает UpdatePribors в RVA `0x18ba4e`, а также UpdateKont/UpdateButton/UpdateFlags/UpdateLight/Proceed_Kontaktors/Proceed_Messages/UpdateOborud/UpdateLastParam. В том же цикле при наличии объекта обновления монитора вызывается `TUpdateMSUL.Proceed` (CALL в `0x18ba9e`). Поэтому экран МСУЛ обновляется через отдельный объект, а аппаратные приборы — через Communication1. Одинаковость всех показаний этих двух путей здесь не доказана.

## Тяга и рекуперация: что подтверждается

В текущей Tyga1 есть специализированный `Tyga_2es6.TTygaCalcClass`, с процедурой `CalcRecup`, `ChooseSoedTEDRecup`, `CalcIvozbMaxMin`, `CalcRealProcent`, `SetEDT`, `SetFlagRecup`, `GetPodgotovkaElTorm`, `GetUDvig`, `GetUSet`, `GetCountVklTED`.
Структуры содержат ток якоря/возбуждения, напряжение ТД, момент и тягу ТД; режимы S/SP/P и последовательное/независимое возбуждение. Два независимых набора команд: `PlusOdin/APlus/MinusOdin/AMinus/Vybeg` и `PlusOV/MinusOV/FiksSpeed/NullOV`. Наличие численной модели и режима рекуперации подтверждается; соответствие её ограничений и коэффициентов заводскому РЭ пока не доказано. Конкретные численные ограничения тяги в этом этапе не реконструированы.

## Поколения файлов и системы

Current root DLLs и `2ES6/1` совпадают побайтно (Communication1, Pult1, Tyga1, MSUL_MON, SoundsMSUL). Поэтому это не две разные модели; аппаратные конфигурации различаются.
Корневые PE timestamp: Communication1 `2020-07-16 09:27:49Z`, MSUL_MON `09:27:58Z`, Pult1 `09:25:33Z`, Tyga1 `09:25:34Z`. FileVersion всех 1.0.0.0. Dummy timestamp 1992 у старых файлов не датирует их действительную сборку.

Старое `1/1` имеет другой API: Communication1 экспортирует дополнительные `SetStrelkaForDinPlatform`, `StopDinPlatform`; MSUL_MON использует InitMSULMon/UpdateMSULMon, а current InitMSUL/ProceedMsul/GetHandleMSUL. Старое Tyga1 Init_Tyga_Dll против current Init_Tyga. Их нельзя смешивать с текущими модулями без анализа вызывающего интерфейса.

Имена файлов и классов current соответствуют модели МСУЛ для 2ЭС6 (`TMSUL2ES6`, `Msulstructure`); имена аппаратных кнопок MSUD1/MSUD2 сохраняют историческое обозначение и сами по себе не доказывают моделирование старой МСУД. В корневой hardware карте два клавиатурных входа МСУЛ/МСУД и БЛОК-keypad привязаны; KLUB_Btn/SAUT_Btn/TSKBM_Btn Existing0 (нет привязки физических автоматов). Это не доказывает фактический программный выбор БЛОК вместо КЛУБ. Выбор зависит от реестра локомотива/другого конфигурационного слоя.

Локальный `BLOK.dll` поддерживает нужные экранные величины: у `TUpdateFormBLOK` присутствуют RTTI поля FPTC/FPTM/FPUR (file offsets `0x1ad6af/0x1ad6bf/0x1ad6cf`), свойства PTC/PTM/PUR (`0x1adcda/0x1adcf8/0x1add16`), FVCur/FVCel/FVOgr, FNumWay, FStation, FCoord, FCurTime, FDistToCel, FCelType, FAlarmBdit, FAlarmTSKBM. Подтверждается состав поддерживаемого экранного интерфейса; выбранность BLOK этой записью не подтверждается.

## Последняя проверка КВТ: имя 254 не определяет тип крана

`Communication1.dll`, `TComm2ES6.UpdatePosHW`, RVA `0x2b1d70`, length `0x54`:

```text
558bec51538945fc8b153c7c6c008b128b45fc8b4010e8a58ceeff8a586e8b45fce80e4cfeff8b400488587e8b1534746c008b128b45fc8b4010e8818ceeff8a586e8b45fce8ea4bfeff8b400488587d5b595dc3
```

Первая половина читает ID600 через `THardwares.GetValueParam`, берёт byte преобразованного значения `[eax+0x6e]` и записывает его непосредственно в `[LocP+0x7e]`. RTTI `TLocParam.Pos` находится по `0x7d`, `TPositionLoc.KvT` имеет offset1 и тип **Byte**, без enum названий/смыслов. Вторая половина аналогично читает ID601 и записывает KM по offset `0x7d`.

Таким образом, дополнительного перенумерования значений КВТ здесь нет. Корневые аппаратные карты задают пять преобразуемых mark: **2,3,4,5,6** (подтверждение root/configs), а mark1 отсутствует. Это действительно пять аппаратных положений, несмотря на историческое имя входа `KVT_254`.

В визуальной форме модуль называет орган `Pos_Kran215`; его DFM по file offset `0xc58362` содержит общий набор подписей `6,5,4,3,2,1` (`Rukoyat.ItemsPosPos` начинается около `0xc58680`). GUI-обработчик `Pos_Kran215ChangeState` RVA `0x2aeef4` также пишет выбранное byte значение в LocP+0x7e. Наличие шестипозиционного общего GUI-компонента не доказывает, что аппаратный тренажёр использует все шесть положений.

`TormModel.dll` RTTI сохраняет KvT как Byte. В TormModelGruz код читает его и использует для индексации таблиц (например RVA `0x5529e`), но семантика каждого кода и конкретные давления за это ограниченное исследование не восстановлены. Поэтому заключение «вместо215 ошибочно смоделирован254» по имени входа необоснованно; подтверждены пять аппаратных кодов2..6, а тип и точное соответствие давлениям РЭ требуют следующей проверки.
