namespace IWP;

internal class TrafficLight
{
	public static void GetMode(int mode, out string nameOfMode, out int blokMode)
	{
		nameOfMode = "";
		blokMode = -1;
		switch (mode)
		{
		case 0:
			nameOfMode = "Зеленый";
			blokMode = 1;
			break;
		case 1:
			nameOfMode = "Красный";
			blokMode = 3;
			break;
		case 2:
			nameOfMode = "Желтый";
			blokMode = 2;
			break;
		case 3:
			nameOfMode = "";
			blokMode = 5;
			break;
		case 4:
			nameOfMode = "Белый";
			blokMode = 0;
			break;
		case 5:
			nameOfMode = "";
			blokMode = 0;
			break;
		case 6:
			nameOfMode = "Зеленый миг.";
			blokMode = 1;
			break;
		case 7:
			nameOfMode = "Зеленый миг. и желтый";
			blokMode = 1;
			break;
		case 8:
			nameOfMode = "Желтый миг.";
			blokMode = 1;
			break;
		case 9:
			nameOfMode = "Желтый миг. и белый";
			blokMode = 2;
			break;
		case 10:
			nameOfMode = "2 желтых";
			blokMode = 2;
			break;
		case 11:
			nameOfMode = "2 зеленых";
			blokMode = 1;
			break;
		case 12:
			nameOfMode = "3 желтых";
			blokMode = 2;
			break;
		case 13:
			nameOfMode = "2 желтых - верхний миг.";
			blokMode = 2;
			break;
		case 14:
			nameOfMode = "Красный и белый";
			blokMode = 3;
			break;
		case 15:
			nameOfMode = "Красный и белый миг.";
			blokMode = 3;
			break;
		case 16:
			nameOfMode = "Желтый и зеленый";
			blokMode = 2;
			break;
		case 17:
			nameOfMode = "Желтый и белый";
			blokMode = 2;
			break;
		case 18:
			nameOfMode = "Зеленый и белый";
			blokMode = 1;
			break;
		case 19:
			nameOfMode = "Не горит";
			blokMode = 5;
			break;
		case 20:
			nameOfMode = "Все горят";
			blokMode = 5;
			break;
		case 21:
			nameOfMode = "Не понятный сигнал";
			blokMode = 0;
			break;
		default:
			nameOfMode = "Вообще не понятный сигнал";
			blokMode = 0;
			break;
		}
	}
}
