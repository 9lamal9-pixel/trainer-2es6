unit ConstHardware;

interface

uses Windows,Classes,SysUtils;

 //-----------------------------------------------------------------------------
 //
 // Переменные аппаратного обеспечения тренажера для локомотива серии 2ЭС6
 //
 //-----------------------------------------------------------------------------

const

  //Переменные ВВ (кнопки)
  RB_Btn :integer = 0;  // РБ
  RBS_Btn :integer = 1;  // РБС
  Pedal_1_Btn :integer = 2;  // Педаль 1
  Pedal_2_Btn :integer = 3;  // Педаль 2
  Radiost_Btn :integer = 4;  // Включение радиостанции
  MegaOm_Btn :integer = 5;  // Мегоомметр
  VCU_On_Btn :integer = 6;  // ВЦУ
  Tokopr_1_Btn :integer = 7;  // Токоприемники. Секция 1
  Tokopr_2_Btn :integer = 8;  // Токоприемники. Секция 2
  Tokopr_3_Btn :integer = 9;  // Токоприемники. Секция 3
  Tokopr_4_Btn :integer = 10;  // Токоприемники. Секция 4
  Extr_Torm_Btn :integer = 11;  // Клапан экстренного торможения
  Otpusk_1_Btn :integer = 12;  // Отпуск тормозов
  BV_Btn :integer = 13;  // БВ
  Vybeg_Btn :integer = 14;  // Выбег
  Pesok_Btn :integer = 15;  // Песок
  Svistok_Btn :integer = 16;  // Свисток
  Tifon_Btn :integer = 17;  // Тифон
  Vent_Btn :integer = 18;  // Вентиляторы
  Kompr_Btn :integer = 19;  // Компрессоры
  Revers_Fwd_Btn :integer = 20;  // Реверсивка
  SAUT_Mode_Btn :integer = 21;  // Режим САУТ
  Shtorki_1_Btn :integer = 22;  // Шторка
  Vozb_Btn :integer = 23;  // Возбуждение
  Projector_Yarko_Btn :integer = 24;  // Прожектор
  Stekloomyv_Btn :integer = 25;  // Стеклоомыватель
  Stekloochist_1_Btn :integer = 26;  // Стеклоочиститель
  Produvka_Rez_Btn :integer = 27;  // Продувка резервуаров
  Bufer_Left_Red_Btn :integer = 28;  // Буферный левый красный
  Bufer_Left_White_Btn :integer = 29;  // Буферный левый белый
  Bufer_Right_White_Btn :integer = 30;  // Буферный правый белый
  Bufer_Right_Red_Btn :integer = 31;  // Буферный правый красный
  TD_1_Btn :integer = 32;  // Тяговые двигатели 1 секции
  TD_2_Btn :integer = 33;  // Тяговые двигатели 2 секции
  TD_3_Btn :integer = 34;  // Тяговые двигатели 3 секции
  TD_4_Btn :integer = 35;  // Тяговые двигатели 4 секции
  MSUD1_1_Btn :integer = 36;  // МСУД 1
  MSUD2_1_Btn :integer = 37;  // МСУД 2
  Fault_btn :integer = 38;  // Неисправность
  Exit_btn :integer = 39;  // Выход
  Down_btn :integer = 40;  // Вниз
  Enter_btn :integer = 41;  // Ввод
  KRU_Otpusk_btn :integer = 42;  // КРУ
  EPK_Key_Btn :integer = 43;  // Ключ ЭПК
  Trubka_btn :integer = 44;  // Трубка
  FixSpeed_Btn :integer = 45;  // Фиксация скорости
  RBP_btn :integer = 46;  // РБП
  KompressorHand_btn :integer = 47;  // Принудит включение компрессоров
  VspomKompr_btn :integer = 48;  // Вспомогательный компрессор
  RezhSecGolPric1 :integer = 49;  // Режим работы секций (головная/прицепная) 1
  RezhSecGolPric2 :integer = 50;  // Режим работы секций (головная/прицепная) 2
  RadioDNCWhite_btn :integer = 51;  // Радио ДНЦ белая
  RadioTCHMWhite_btn :integer = 52;  // Радио ТЧМ белая
  RadioDSPWhite_btn :integer = 53;  // Радио ДСП белая
  RadioReshWhite_btn :integer = 54;  // Радио Решетка белая
  RadioDNCYell_btn :integer = 55;  // РадиоДНЦ желтая
  RadioTCHMYell_btn :integer = 56;  // Радио ТЧМ желтая
  RadioDSPYell_btn :integer = 57;  // Радио ДСП желтая
  RadioHren_btn :integer = 58;  // Радио хрень
  TifonPom_btn :integer = 59;  // Тифон Помощник
  SvistPom_btn :integer = 60;  // Свисток Помощник
  Extr_Torm2_btn :integer = 61;  // Клапан экстренного торможения пом.
  Blok_P_btn :integer = 62;  // Блок
  KLUB_Btn :integer = 63;  // Клуб. Автомат
  SAUT_Btn :integer = 64;  // САУТ. Автомат
  TSKBM_Btn :integer = 65;  // ТСКБМ. Автомат

  //Переменные ВСТ (лампочки)
  TM_Ind :integer = 200;  // ТМ
  Sbros_Prib1 :integer = 201;  // Сброс прибора давления 1
  Sbros_Prib2 :integer = 202;  // Сброс прибора давления 2
  Sbros_Prib3 :integer = 203;  // Сброс прибора давления 3
  ALSN_Light :integer = 204;  // АЛСН Машиниста
  ALSNPom_Light :integer = 205;  // АЛСН Машиниста

  //Переменные ЦАП (приборы)
  P_ur_pr :integer = 400;  // Давление в УР
  P_tm_pr :integer = 401;  // Давление в TM
  P_pm_pr :integer = 402;  // Давление в ПM
  P_tc1_pr :integer = 403;  // Давление в ТЦ 1
  P_tc2_pr :integer = 404;  // Давление в ТЦ 2
  U_CepiUpr_Prib :integer = 405;  // Напряжение цепей управления
  I_AB_Prib :integer = 406;  // Ток АБ

  //Переменные АЦП (управление)
  KVT_254 :integer = 600;  // Кран вспомогательного тормоза
  KM_130 :integer = 601;  // Кран машиниста
  I_vozb_Plus_Btn :integer = 602;  // Задатчик Силы
  Tyga_Plus1_Btn :integer = 603;  // Тяга

  //Переменные ВЫВ (штучки)

implementation

end.
