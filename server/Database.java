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
					User user = new User(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6));
					return executeWithValidUser(user, query);
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
		try
		{
			String firstword = null;
			if (query.contains(" "))
				firstword = query.substring(0, query.indexOf(" "));
			if (firstword == null)
				return "No separable input.";

			switch (firstword)
			{
				case "REGISTER": 		// REGISTER johndoe 20 john doe abcPass123
					if (user.rank < 10)
					{
						query = query.substring(query.indexOf(" ") + 1);
						String[] parts = query.split(" ");
						java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
						statement.setString(1, parts[0]);
						statement.setInt(2, Integer.parseInt(parts[1]));
						statement.setString(3, parts[2]);
						statement.setString(4, parts[3]);
						statement.setString(5, PasswordHash.createHash(parts[4]));
						return String.valueOf(statement.executeUpdate());
					}
					else
					{
						return "You do not have the privilege to register users.";
					}
			}

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
					{
						String tmp = "";
						for (int i = 0; i < columns; ++i)
						{
							tmp += " " + utils.Utils.escapeSpaces(result.getString(i + 1));
						}
						answer += " " + utils.Utils.escapeSpaces(tmp);
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
		catch (Exception exc)
		{
			return exc.toString();
		}
	}
}