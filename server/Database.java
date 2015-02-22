package server;

import static utils.Configuration.verbose;

public class Database
{
	private static String db_url = "jdbc:derby:database";
	private static java.sql.Connection conn = null;
    private static java.sql.Statement stmt = null;

	public Database(String db_url)
	{
		this.db_url = db_url;
		try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = java.sql.DriverManager.getConnection(db_url);

            java.sql.PreparedStatement prepstmt = conn.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstmt.setString(1, "root");
			java.sql.ResultSet result = prepstmt.executeQuery();
			if (result.next() == false)
			{
	        	java.sql.PreparedStatement statement 
					= conn.prepareStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashedPW) VALUES ("
							+ "0, 'root', '', '', '" + PasswordHash.createHash("root") + "')"
							, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
						);
				statement.execute();
			}
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
	}

	public String execute(String username, String password, String query)
	{
		try
		{
			verbose("Delegating input to the input handler.");

			java.sql.PreparedStatement prepstmt = conn.prepareStatement("SELECT hashedPW FROM SystemUser WHERE username=?");
			prepstmt.setString(1, username);
			java.sql.ResultSet result = prepstmt.executeQuery();
			if (result.next())
			{
				if (PasswordHash.validatePassword(password, result.getString(1)))
				{
					return "OK";
				}
				else 
					return "Invalid password for user '" + username + "'.";
			}
			else
			{
				return "User '" + username + "' does not exist.";
			}
			/*
			System.out.println("Executing query " + last_message);
			try
			{
				if 
				(
					last_message.startsWith("UPDATE") 
					|| last_message.startsWith("INSERT")
					|| last_message.startsWith("EXECUTE")
					|| last_message.startsWith("INSERT")
				)
				{
					java.sql.PreparedStatement statement = conn.prepareStatement(last_message, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
					statement.execute();
				}
				else
				{
					stmt = conn.createStatement(); //(last_message, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
					java.sql.ResultSet result = stmt.executeQuery(last_message);
					while (result.next())
						System.out.println("'" + result.getInt(1) + ", " + result.getString(2) + "'");	
				}
			}
			catch (Exception exc)
			{
				System.out.println("An exception ocurred during execution: " + exc.toString());
			}
			*/
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
			return "Internal database error.";
		}
	}
}