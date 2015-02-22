package server;

import static utils.Configuration.verbose;

public class Database
{
	private static String db_url = null;
	private static java.sql.Connection connection = null;

	public Database(String db_url)
	{
		this.db_url = db_url;
		try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            connection = java.sql.DriverManager.getConnection(db_url);

            java.sql.PreparedStatement prepstatement = connection.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstatement.setString(1, "root");
			java.sql.ResultSet result = prepstatement.executeQuery();
			if (result.next() == false)
			{
	        	java.sql.PreparedStatement statement 
					= connection.prepareStatement
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

	public void closeDatabase() throws java.sql.SQLException
	{
		connection.close();
	}

	public String execute(String username, String password, String query)
	{
		try
		{
			verbose("Delegating input to the input handler.");

			java.sql.PreparedStatement prepstatement = connection.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstatement.setString(1, username);
			java.sql.ResultSet result = prepstatement.executeQuery();
			if (result.next())
			{
				if (PasswordHash.validatePassword(password, result.getString("hashedPW")))
				{
					return executeWithValidUser(new User(), query);
				}
				else 
					return "Invalid password for user '" + username + "'.";
			}
			else
			{
				return "User '" + username + "' does not exist.";
			}
			
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
			return "Internal database error.";
		}
	}

	public String executeWithValidUser(User user, String query)
	{
		verbose("Executing query " + query);
		try
		{
			if 
			(
				query.startsWith("UPDATE") 
				|| query.startsWith("INSERT")
				|| query.startsWith("EXECUTE")
				|| query.startsWith("INSERT")
			)
			{
				java.sql.PreparedStatement statement = connection.prepareStatement(query, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
				return String.valueOf(statement.executeUpdate());
			}
			else if (query.startsWith("SELECT"))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement(query);
				java.sql.ResultSet result = statement.executeQuery();
				java.sql.ResultSetMetaData resultmetadata = result.getMetaData();
				int columns = resultmetadata.getColumnCount();

				String answer = String.valueOf(columns);
				while (result.next())
					for (int i = 0; i < columns; ++i)
					{
						answer += " " + result.getString(i + 1);
					}
				return answer;
			}
			else
			{
				return "Invalid query provided";
			}
		}
		catch (Exception exc)
		{
			verbose("An exception ocurred during execution: " + exc.toString());
		}

		return null;
	}
}