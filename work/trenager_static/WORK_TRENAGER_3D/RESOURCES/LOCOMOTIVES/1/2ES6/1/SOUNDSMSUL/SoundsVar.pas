unit SoundsVar;

interface

uses
Windows, Classes;

const
      BoxFirstOsFirstSec:integer=0;  // Звук Боксов первой оси первой секции - Одиночный звук
      BoxFirstOsSecondSec:integer=1;  // Звук Боксов первой оси второй секции - Одиночный звук
      BoxFirstOsTreSec:integer=2;  // Звук Боксов первой оси третьей секции - Одиночный звук
      BoxFirstOsFourSec:integer=3;  // Звук Боксов первой оси четвертой секции - Одиночный звук
      BoxSecondOsFirstSec:integer=4;  // Звук Боксов второй оси первой секции - Одиночный звук
      BoxSecondOsSecondSec:integer=5;  // Звук Боксов второй оси второй секции - Одиночный звук
      BoxSecondOsTreSec:integer=6;  // Звук Боксов второй оси третьей секции - Одиночный звук
      BoxSecondOsFourSec:integer=7;  // Звук Боксов второй оси четвертой секции - Одиночный звук
      BoxTriOsFirstSec:integer=8;  // Звук Боксов третьей оси первой секции - Одиночный звук
      BoxTriOsSecondSec:integer=9;  // Звук Боксов третьей оси второй секции - Одиночный звук
      BoxTriOsTriSec:integer=10;  // Звук Боксов третьей оси третьей секции - Одиночный звук
      BoxTriOsFourSec:integer=11;  // Звук Боксов третьей оси четвертой секции - Одиночный звук
      BoxFourOsFirstSec:integer=12;  // Звук Боксов четвертой оси первой секции - Одиночный звук
      BoxFourOsSecondSec:integer=13;  // Звук Боксов четвертой оси второй секции - Одиночный звук
      BoxFourOsTreSec:integer=14;  // Звук Боксов четвертой оси третьей секции - Одиночный звук
      BoxFourOsFourSec:integer=15;  // Звук Боксов четвертой оси четвертой секции - Одиночный звук
      BoxFiveOsFirstSec:integer=16;  // Звук Боксов пятой оси первой секции - Одиночный звук
      BoxFiveOsSecondSec:integer=17;  // Звук Боксов пятой оси второй секции - Одиночный звук
      BoxFiveOsTreSec:integer=18;  // Звук Боксов пятой оси третьей секции - Одиночный звук
      BoxFiveOsFourSec:integer=19;  // Звук Боксов пятой оси четвертой секции - Одиночный звук
      BoxSixOsFirstSec:integer=20;  // Звук Боксов шестой оси первой секции - Одиночный звук
      BoxSixOsSecondSec:integer=21;  // Звук Боксов шестой оси второй секции - Одиночный звук
      BoxSixOsTreSec:integer=22;  // Звук Боксов шестой оси третьей секции - Одиночный звук
      BoxSixOsFourSec:integer=23;  // Звук Боксов шестой оси четвертой секции - Одиночный звук
      SrabBVFirstSec:integer=24;  // Звук Срабатывание БВ первой секции - Одиночный звук
      SrabBVSecondSec:integer=25;  // Звук Срабатывание БВ второй секции - Одиночный звук
      SrabBVTreSec:integer=26;  // Звук Срабатывание БВ третьей секции - Одиночный звук
      SrabBVFourSec:integer=27;  // Звук Срабатывание БВ четвертой секции - Одиночный звук
      SrabDifReleFirstSec:integer=28;  // Звук Срабатывание дифреле первой секции - Одиночный звук
      SrabDifReleSecondSec:integer=29;  // Звук Срабатывание дифреле второй секции - Одиночный звук
      SrabDifReleTreSec:integer=30;  // Звук Срабатывание дифреле третьей секции - Одиночный звук
      SrabDifReleFourSec:integer=31;  // Звук Срабатывание дифреле четвертой секции - Одиночный звук
      SrabDifReleVMFirstSec:integer=32;  // Звук Срабатывание дифреле вспом машин первой секции - Одиночный звук
      SrabDifReleVMSecondSec:integer=33;  // Звук Срабатывание дифреле вспом машин второй секции - Одиночный звук
      SrabDifReleVMTreSec:integer=34;  // Звук Срабатывание дифреле вспом машин третьей секции - Одиночный звук
      SrabDifReleVMFourSec:integer=35;  // Звук Срабатывание дифреле вспом машин четвертой секции - Одиночный звук
      LowNaprKS:integer=36;  // Звук Пониженное напряжение КС - Одиночный звук
      NetZarABFirstSec:integer=37;  // Звук Нет заряда АБ первой секции - Одиночный звук
      NetZarABSecondSec:integer=38;  // Звук Нет заряда АБ второй секции - Одиночный звук
      NetZarABTreSec:integer=39;  // Звук Нет заряда АБ третьей секции - Одиночный звук
      NetZarABFourSec:integer=40;  // Звук Нет заряда АБ четвертой секции - Одиночный звук
      NezapKompFirstSec:integer=41;  // Звук Незапуск Компр. первой секции - Одиночный звук
      NezapKompSecondSec:integer=42;  // Звук Незапуск Компр. второй секции - Одиночный звук
      NezapKompTreSec:integer=43;  // Звук Незапуск Компр. третьей секции - Одиночный звук
      NezapKompFourSec:integer=44;  // Звук Незапуск Компр. четвертой секции - Одиночный звук
      PeregrDvigFirstSec:integer=45;  // Звук Перегрузка двигателей первой секции - Одиночный звук
      PeregrDvigSecondSec:integer=46;  // Звук Перегрузка двигателей второй секции - Одиночный звук
      PeregrDvigTreSec:integer=47;  // Звук Перегрузка двигателей третьей секции - Одиночный звук
      PeregrDvigFourSec:integer=48;  // Звук Перегрузка двигателей четвертой секции - Одиночный звук
      UpNaprKS:integer=49;  // Звук Повышенное напряжение КС - Одиночный звук
      VentFirstSecVykl:integer=50;  // Звук Вентилятор первой секции выключен - Одиночный звук
      VentSecondSecVykl:integer=51;  // Звук Вентилятор второй секции выключен - Одиночный звук
      VentTreSecVykl:integer=52;  // Звук Вентилятор третьей секции выключен - Одиночный звук
      VentFourSecVykl:integer=53;  // Звук Вентилятор четвертой секции выключен - Одиночный звук


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
