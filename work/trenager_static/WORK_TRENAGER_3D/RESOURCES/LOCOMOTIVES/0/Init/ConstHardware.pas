unit ConstHardware;

interface

uses Classes,SysUtils;

 //-----------------------------------------------------------------------------
 //
 // Переменные аппаратного обеспечения тренажера для локомотива серии 
 //
 //-----------------------------------------------------------------------------

const

  //Переменные ВВ (кнопки)
  Up_Button :integer = 0;  // Вверх
  Neispr_Button :integer = 1;  // Неисправность
  Enter_Button :integer = 2;  // Ввод
  Down_Button :integer = 3;  // Вниз
  Svistok_Button :integer = 4;  // Свисток
  Signal_Button :integer = 5;  // Сигнал
  Pesok_Button :integer = 6;  // Песок
  RB_1_Button :integer = 7;  // РБ
  RB_2_Button :integer = 8;  // РБС
  Pedal_Left_Button :integer = 9;  // Педаль левая
  Pedal_Right_Button :integer = 10;  // Педаль правая
  Avar_Pit_Button :integer = 11;  // Аварийное питание
  Bditelnost_Button :integer = 12;  // Бдительность
  Otkl_SPU_1_Button :integer = 13;  // Отключение СПУ1
  Otkl_SPU_2_Button :integer = 14;  // Отключение СПУ2
  Otkl_PSN_Button :integer = 15;  // Отключение ПСН
  Otkl_Vent_Button :integer = 16;  // Отключение вентиляторов
  SQ1_Button :integer = 17;  // SQ1
  Sign_Rab_Mesto_Button :integer = 18;  // Сигн. рабочего места
  Bez_DGU_Button :integer = 19;  // Без ДГУ
  Compr_1_Button :integer = 20;  // Компрессор 1
  Compr_2_Button :integer = 21;  // Компрессор 2
  SU_Button :integer = 22;  // Система управления
  Rezervuar_1_Button :integer = 23;  // Резервуар 1
  Rezervuar_2_Button :integer = 24;  // Резервуар 2
  Maslootdel_Button :integer = 25;  // Маслоотделитель
  Vkl_U_Gen_Button :integer = 26;  // Вкл. напряжения генератора
  Vozb_Gen_Button :integer = 27;  // Возбуждение генератора
  Pusk_Disel_Button :integer = 28;  // Пуск дизеля
  Otkl_U_Gen_Button :integer = 29;  // Откл. напряжения генератора
  Prokachka_Button :integer = 30;  // Прокачка масла
  Projector_Per_Yarko_Button :integer = 31;  // Прожектор передний Ярко
  Projector_Per_Tusklo_Button :integer = 32;  // Прожектор передний Тускло
  Projector_Zad_Yarko_Button :integer = 33;  // Прожектор задний Ярко
  Projector_Zad_Tusklo_Button :integer = 34;  // Прожектор задний Тускло
  Tok_Centr_Per_Button :integer = 35;  // Токоприемник Центр. Передний
  Tok_Centr_Zad_Button :integer = 36;  // Токоприемник Центр. Задний
  Tok_Bok_Left_Button :integer = 37;  // Токоприемник бок. Левый
  Tok_Bok_Right_Button :integer = 38;  // Токоприемник бок. Правый
  Compr_Button :integer = 39;  // Компрессоры
  Signalization_Button :integer = 40;  // Сигнализация
  BV_Button :integer = 41;  // БВ
  VZ_Button :integer = 42;  // Возврат защиты
  Autoscep_Per_Button :integer = 43;  // Автосцепка передняя
  Autoscep_Zad_Button :integer = 44;  // Автосцепка задняя
  Hand_Compr_Button :integer = 45;  // Ручной компрессор
  Stop_PM_Button :integer = 46;  // Стоп платф. пульта машиниста
  Key_PM_Button :integer = 47;  // Ключ платф. пульта машиниста
  Platform_On_PM_Button :integer = 48;  // Работа  платформы
  Ventil_Zaluzi_Button :integer = 49;  // Вентиль жалюзи
  Prib_Disel_Button :integer = 50;  // Приборы дизеля
  Micr_Button :integer = 51;  // Микрофон
  Electro_Tyga_Button :integer = 52;  // Електровоз
  Disel_Tyga_Button :integer = 53;  // Дизель
  Revers_Forvard_Button :integer = 54;  // Реверс вперед
  Revers_Back_Button :integer = 55;  // Реверс назад
  Buf_Disel_Left_Button :integer = 56;  // Буф. фонари дизеля левый
  Buf_Disel_Right_Button :integer = 57;  // Буф. фонари дизеля правый
  White_Red_Disel_Btn_Button :integer = 58;  // Буф. фонари дизеля Белый/Красный
  Buf_Left_Button :integer = 59;  // Буф. фонари левый
  Buf_Right_Button :integer = 60;  // Буф. фонари правый
  White_Red_Btn_Button :integer = 61;  // Буф. фонари белый/Красный

  //Переменные ВСТ (лампочки)
  BV_Light :integer = 200;  // БВ(пульт)
  DV_Light :integer = 201;  // ДБ(пульт)
  Vedusch_Light :integer = 202;  // Ведущая
  Vedom_Light :integer = 203;  // Ведомая
  LC_Light :integer = 204;  // ЛК
  ZB_Light :integer = 205;  // ЗБ
  TM_Light :integer = 206;  // ТМ
  Otpusk_Light :integer = 207;  // Отпуск
  BUTP_1_Light :integer = 208;  // БУТП 1
  BUTP_2_Light :integer = 209;  // БУТП 2
  Peregr_Light :integer = 210;  // Перегр
  DB_Light :integer = 211;  // ДБ
  U_Light :integer = 212;  // Сеть
  KMB_Light :integer = 213;  // КМБ
  MV_Light :integer = 214;  // МВ
  MK_1_Light :integer = 215;  // МК
  RD_31_Light :integer = 216;  // РДЗ 1
  RD_32_Light :integer = 217;  // РДЗ 2
  COU_Light :integer = 218;  // СОУ
  DGU_Light :integer = 219;  // ДГУ
  BTR_Light :integer = 220;  // БТР
  DMK_1_Light :integer = 221;  // ДМК 1
  DMK_2_Light :integer = 222;  // ДМК 2
  PP_PSN_Light :integer = 223;  // ПП ПСН
  PP_TP_Light :integer = 224;  // ПП ТР
  PSN_Light :integer = 225;  // ПСН
  Start_Platform_L :integer = 226;  // Старт платформы
  Stop_Platform_L :integer = 227;  // Стоп платформы
  SbrosBiPribors_L :integer = 228;  // Сброс двухстрелочных приборов

  //Переменные ЦАП (приборы)
  P_ur :integer = 400;  // Уравнительный резервуар
  P_tm :integer = 401;  // Тормозная магистраль
  P_rm :integer = 402;  // Разгрузочная магистраль
  P_gr :integer = 403;  // Питательная магистраль
  P_tc_Loc :integer = 404;  // ТЦ локомотива
  P_tc :integer = 405;  // ТЦ состава
  I_td1 :integer = 406;  // Ток ТД1
  I_td3 :integer = 407;  // Ток ТД3
  U_Vspom_Mach :integer = 408;  // Напряжение Вспом. машин
  U_seti :integer = 409;  // Сеть
  Frec :integer = 410;  // Обороты дизеля
  P_Maslo :integer = 411;  // Давление масла
  T_Water :integer = 412;  // Температура воды
  kreslo_frw :integer = 413;  // Кресло вперед
  kreslo_bck :integer = 414;  // Кресло назад

  //Переменные АЦП (управление)
  Kran_395 :integer = 600;  // Кран 395
  Kran_254 :integer = 601;  // Кран 254
  Toplivn_Reika :integer = 602;  // Топливная рейка

  //Переменные ВЫВ (штучки)
  KLUB_VYV0 :integer = 800;  // Клуб 0
  KLUB_VYV1 :integer = 801;  // Клуб 1

implementation

end.
