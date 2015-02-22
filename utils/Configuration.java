package utils;

public class Configuration extends java.util.HashMap<String, String>
{
	public static boolean verbose_mode = false;

	public static void verbose(String string)
	{
		if (verbose_mode)
			System.out.println(string);
	}

	public int getInt(String key)
	{
		return Integer.parseInt(this.get(key));
	}

	public static Configuration loadConfiguration(String filename) throws java.io.IOException
	{
		verbose("Loading configuration from " + filename);
		try
		{
			Configuration map = new Configuration();
			java.util.Scanner scanner = new java.util.Scanner(java.nio.file.Paths.get(filename));
			String linefeed = System.getProperty("line.separator");
			scanner.useDelimiter(linefeed + "|=|//.*?" + linefeed);
			while (scanner.hasNext())
			{
				String head = scanner.next();
				if (scanner.hasNext())
				{
					String tail = scanner.next();
					map.put(head.trim(), tail.trim());
				}
				else
				{
					map.put(head, "");
				}
			}
			return map;
		}
		catch (java.util.InputMismatchException exc_obj)
		{
			verbose(exc_obj.getMessage());
		}
		return null;
	}

	public static Configuration loadDefaultConfiguration() throws java.io.IOException
	{
		settings = loadConfiguration(config_file);
		return settings;
	}

	public static utils.Configuration settings;

	private static String config_file = "settings.conf";
}