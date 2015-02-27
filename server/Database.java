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

	// Only call this method from within the server. Runs a query without bounds. For sysadmin purposes ONLY!
	public String runQuery(String query)
	{
		try
		{
			java.sql.PreparedStatement statement = connection.prepareStatement(query);
			if 
			(
				query.startsWith("UPDATE")
				|| query.startsWith("DELETE")
				|| query.startsWith("INSERT INTO")
				|| query.startsWith("ALTER TABLE")
				|| query.startsWith("DROP")
			)
				return String.valueOf(statement.executeUpdate());
			else
				return resultToString(statement.executeQuery());
		}
		catch (Exception exc)
		{
			return exc.toString();
		}
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
					//System.out.println(query);
					return executeWithValidUser(user, query);
				}
				else 
					return "Invalid password for user '" + username + "'.";
			}
			else
			{
				return "Login username '" + username + "' does not exist.";
			}
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
			return "Unable to execute the query. Please contact the system administrator.";
		}
	}

	public String resultToString(java.sql.ResultSet result) throws Exception
	{
		java.sql.ResultSetMetaData resultmetadata = result.getMetaData();
		int columns = resultmetadata.getColumnCount();

		String answer = String.valueOf(columns);
		while (result.next())
		{
			String tmp = "";
			if (columns > 0)
			{
				tmp = utils.Utils.escapeSpaces(result.getString(1));
			}
			for (int i = 1; i < columns; ++i)
			{
				tmp += " " + utils.Utils.escapeSpaces(result.getString(i + 1));
			}
			answer += " " + utils.Utils.escapeSpaces(tmp);
		}
		return answer;
	}

	public String executeWithValidUser(User user, String query)
	{
		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
			for (int i = 0; i < parts.size(); ++i)
				verbose("Parts: '" + parts.get(i) + "'.");
			utils.Configuration coms = utils.Configuration.settings;

			if (parts.get(0).equals(coms.get("RegisterCommand")))
			{
				if (user.rank < 10)
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
					statement.setString(1, parts.get(1));
					statement.setInt(2, Integer.parseInt(parts.get(2)));
					statement.setString(3, parts.get(3));
					statement.setString(4, parts.get(4));
					statement.setString(5, PasswordHash.createHash(parts.get(5)));
					try
					{
						int n = statement.executeUpdate();
						if (n == 1)
						{
							return "User '" + parts.get(1) + "' registered!";
						}
					}
					catch (java.sql.SQLException exc)
					{
						return "It's likely that the user you're trying to add (" + parts.get(1) + ") already exists.";
					}
				}
				else
				{
					return "You do not have the privilege to register users.";
				}
			}
			else if (parts.get(0).equals(coms.get("ChangePassOfCommand")))
			{
				if (user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE username=?");
					statement.setString(1, PasswordHash.createHash(parts.get(2)));
					statement.setString(2, parts.get(1));
					return String.valueOf(statement.executeUpdate());
				}
				else
				{
					return "Only root can change other users' passwords.";
				}
			}
			else if (parts.get(0).equals(coms.get("ChangePassCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE systemUserId=?");
				statement.setString(1, PasswordHash.createHash(parts.get(1)));
				statement.setInt(2, user.user_id);
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("NewEventCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO PersonalEvent (description, time, systemUserId) VALUES (?, ?, ?)");
				statement.setString(1, parts.get(1));
				statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(2)));
				statement.setInt(3, user.user_id);
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("GetEventsCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT description, time FROM PersonalEvent WHERE systemUserId=? ORDER BY time ASC");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("RegisterRoomCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Room (roomName, size, location) VALUES (?, ?, ?)");
				statement.setString(1, parts.get(1));
				statement.setInt(2, Integer.parseInt(parts.get(2)));
				statement.setString(3, parts.get(3));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("FindPersonCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
				statement.setString(1, parts.get(1));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("PassCheck")))
			{
				return "Password is valid.";
			}
			else
				return "No handler for the command: " + parts.get(0);
		}
		catch (Exception exc)
		{
			verbose("An exception ocurred during execution: " + exc.toString());
			return exc.toString();
		}
		return "Impossible.";
	}
}