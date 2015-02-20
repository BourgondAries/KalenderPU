package utils;

public class Utils
{
	public static String fileToString(String filename) throws java.io.IOException
	{
		return new java.util.Scanner(new java.io.File(filename)).useDelimiter("\\Z").next();
	}
}