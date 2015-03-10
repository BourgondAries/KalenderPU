package server;

import java.io.FileWriter;
import java.sql.Date;

public enum Logger
{
	DEBUGGING, MESSAGING;

	public static void Log(String message, Logger type)
	{
		FileWriter fw = new FileWriter(utils.Configuration.settings.get("logfile") , true);
		Date date = new Date();
		fw.write(type +": " + message + " @" +date+ "\n");

	}
}