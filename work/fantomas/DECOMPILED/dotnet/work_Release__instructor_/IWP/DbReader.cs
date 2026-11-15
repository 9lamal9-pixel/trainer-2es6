using System;
using System.Collections.Generic;
using System.Data.SQLite;

namespace IWP;

public class DbReader
{
	private const string Path = "RTS/database/";

	private const string DbName = "Program_data.db";

	public List<Wagon> GetWagons(int sostavCompozitionId)
	{
		List<Wagon> list = new List<Wagon>();
		SQLiteConnection sQLiteConnection = new SQLiteConnection("DataSource=RTS/database/Program_data.db");
		sQLiteConnection.Open();
		string commandText = $"SELECT * FROM Wagons WHERE Composition_id == {sostavCompozitionId}";
		SQLiteCommand sQLiteCommand = new SQLiteCommand(commandText, sQLiteConnection);
		SQLiteDataReader sQLiteDataReader = sQLiteCommand.ExecuteReader();
		while (sQLiteDataReader.Read())
		{
			int @int = sQLiteDataReader.GetInt32(1);
			int num = sQLiteDataReader.GetInt32(2);
			float length = 0f;
			int int2 = sQLiteDataReader.GetInt32(3);
			List<int> list2 = new List<int>();
			for (double num2 = (double)sQLiteDataReader.GetInt32(4) / 10.0; num2 >= 0.1; num2 = Math.Truncate(num2) / 10.0)
			{
				list2.Add((int)Math.Round((num2 - Math.Truncate(num2)) * 10.0));
			}
			list2.Reverse();
			bool airDiffuserOn = list2[0] == 1;
			AirDiffuserMode airDiffuserMode = GetAirDiffuserMode(list2[1]);
			AirDiffuserRegime airDiffuserRegime = GetAirDiffuserRegime(list2[2]);
			bool autoMode = sQLiteDataReader.GetInt32(5) == 1;
			int int3 = sQLiteDataReader.GetInt32(6);
			string commandText2 = $"SELECT Mass, Length FROM Basic_wagons WHERE Wagon_id == {@int}";
			SQLiteCommand sQLiteCommand2 = new SQLiteCommand(commandText2, sQLiteConnection);
			SQLiteDataReader sQLiteDataReader2 = sQLiteCommand2.ExecuteReader();
			while (sQLiteDataReader2.Read())
			{
				num += sQLiteDataReader2.GetInt32(0);
				length = (float)sQLiteDataReader2.GetInt32(1) * 0.01f;
			}
			list.Add(new Wagon(int3, new WagonData(GetWagonType(@int), num, length, autoMode, int2, airDiffuserOn, airDiffuserMode, airDiffuserRegime)));
		}
		return list;
	}

	private WagonType GetWagonType(int key)
	{
		return key switch
		{
			207 => WagonType.Roofed, 
			302 => WagonType.Hopper, 
			330 => WagonType.Dumpcar, 
			400 => WagonType.Platform, 
			600 => WagonType.HalfWagon, 
			748 => WagonType.Tank, 
			928 => WagonType.CarCarrier, 
			_ => WagonType.Roofed, 
		};
	}

	private static AirDiffuserMode GetAirDiffuserMode(int key)
	{
		return key switch
		{
			1 => AirDiffuserMode.Empty, 
			2 => AirDiffuserMode.Medium, 
			3 => AirDiffuserMode.Loaded, 
			_ => AirDiffuserMode.Medium, 
		};
	}

	private static AirDiffuserRegime GetAirDiffuserRegime(int key)
	{
		return key switch
		{
			1 => AirDiffuserRegime.Plain, 
			2 => AirDiffuserRegime.Mountain, 
			_ => AirDiffuserRegime.Plain, 
		};
	}
}
