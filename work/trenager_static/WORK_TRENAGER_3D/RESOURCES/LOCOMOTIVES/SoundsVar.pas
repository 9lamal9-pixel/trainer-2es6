unit SoundsVar;

interface

uses
Windows, Classes;

const
      PiskKLUB:integer=0;  // Звук Писк КЛУБ - Одиночный звук
      CiklPiskKLUB:integer=1;  // Звук Цикличный ПИСК - Циклический звук


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
