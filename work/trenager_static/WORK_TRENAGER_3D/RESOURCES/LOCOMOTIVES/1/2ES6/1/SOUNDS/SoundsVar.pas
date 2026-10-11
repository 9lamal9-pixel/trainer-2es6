unit SoundsVar;

interface

uses
Windows, Classes;

const
      ConnectVagon_snd:integer=0;  // Звук Соединение вагонов - Одиночный звук
      SryvEPK_snd:integer=1;  // Звук Срыв ЭПК - Составной звук
      Kran254_1_snd:integer=2;  // Звук Кран 254_1 - Составной звук
      Kran254_3_snd:integer=3;  // Звук Кран 254_3 - Составной звук
      Kran394_6_snd:integer=4;  // Звук Кран 394_6 - Циклический звук
      Compressor2ES6_snd:integer=5;  // Звук Компрессор 2ЭС6 - Составной звук
      Svist_2es6_snd:integer=6;  // Звук Свисток 2ЭС6 - Циклический звук
      tifon_2es6_snd:integer=7;  // Звук Тифон 2ЭС6 - Циклический звук
      trans_2es6:integer=8;  // Звук Трансформатор - Циклический звук
      vent_2es6_snd:integer=9;  // Звук Вентилятор 2ЭС6 - Составной звук
      pesok_snd:integer=10;  // Звук Песок - Циклический звук
      box_snd:integer=11;  // Звук Боксование сигнал - Циклический звук
      VG_on_snd:integer=12;  // Звук ГВ включение 2ЭС6 - Одиночный звук
      GV_off_snd:integer=13;  // Звук ГВ выключение 2ЭС6 - Одиночный звук
      Tok_UP_snd:integer=14;  // Звук Поднятие токоприемника 2ЭС6 - Одиночный звук
      tok_down_snd:integer=15;  // Звук Опускание токоприемника 2ЭС6 - Одиночный звук
      Svist_EPK_snd:integer=16;  // Звук Свисток ЭПК - Циклический звук
      VspomComp_snd:integer=17;  // Звук VspomComp - Составной звук


//При первом открытии SoundsVar.pas не забудте взять в кавычки(апостроф) имя звуковой ДЛЛ

function Init_SoundsTrenDLL(SndLib:PChar;WndHandle:THandle):boolean; stdcall;external 'SoundsTren.dll'; //Инициализация звука DirectX
//Вызывается один раз перед началом работы
function CloseSounds():boolean;stdcall; external 'SoundsTren.dll';//Остановка всех звуков и убивание объектов
//Вызываеся один раз перед окончанием работы
function PlaySound(Num:integer):boolean;stdcall;external 'SoundsTren.dll';// Проиграть звук
function StopSound(Num:integer):boolean;stdcall;external 'SoundsTren.dll';// Остановить проигрывание звука. Только для циклических и составных звуков
function SetPositionSound(Num:integer;X:integer;Y:integer;Z:integer):boolean;stdcall;external 'SoundsTren.dll';
// Установить позицию звука. Х - справа/слева Y - вперед/назад Z - верх/низ
function SetFrequencySound(Num:integer;Freq:integer):boolean;stdcall;external 'SoundsTren.dll'; //Установка частотных характеристик звука
function GetStatusSound(Num:integer):integer;stdcall;external 'SoundsTren.dll';//0-не играется 1,2 - проигрывается


implementation


end.
