package server;
import java.io.FileWriter;
import java.sql.Date;
import java.util.Calendar;
import java.io.*;
public class Logger
{
	
	public static void Log(String message, String type, String stringSeverity) throws IOException, IllegalArgumentException
	{
		ErrorType errorType;
		Severity severity;
		try 
		{
			errorType = ErrorType.valueOf(type);
		}
		catch (IllegalArgumentException e )
		{
			errorType = ErrorType.UNDEFINED;
		}

		try 
		{
			severity = Severity.valueOf(stringSeverity);
		}
		catch (IllegalArgumentException e)
		{
			severity = Severity.UNDEFINED;
		}
		
		try
		{
		FileWriter fw = new FileWriter(utils.Configuration.settings.get("logfile") , true);
		
		java.sql.Date timeNow = new Date(Calendar.getInstance().getTimeInMillis());

		fw.write("" + timeNow + ": " + errorType +", " + severity +", " + message + "\n");
		fw.flush();
		fw.close();
		}

		catch(IOException e)
		{
			System.err.println("Error: " + e.getMessage());		
		}		
	}
}