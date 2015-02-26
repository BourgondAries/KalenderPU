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
			String firstword = null;
			if (query.contains(" "))
				firstword = query.substring(0, query.indexOf(" "));
			if (firstword == null)
				firstword = query;

			switch (firstword)
			{
				case "REGISTER":
					if (user.rank < 10)
					{
						query = query.substring(query.indexOf(" ") + 1);
						java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
						java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
						statement.setString(1, parts.get(0));
						statement.setInt(2, Integer.parseInt(parts.get(1)));
						statement.setString(3, parts.get(2));
						statement.setString(4, parts.get(3));
						statement.setString(5, PasswordHash.createHash(parts.get(4)));
						return String.valueOf(statement.executeUpdate());
					}
					else
					{
						return "You do not have the privilege to register users.";
					}
				case "CHANGE_PASSWORD_OF":
				{
					if (user.username.equals("root"))
					{
						java.util.ArrayList<String> components = utils.Utils.splitAndUnescapeString(query);
						java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE systemUserId=?");
						statement.setString(1, PasswordHash.createHash(components.get(1)));
						statement.setInt(2, user.user_id);
						return String.valueOf(statement.executeUpdate());
					}
					else
					{
						return "Only root can change other users' passwords.";
					}
				}
				case "CHANGE_PASSWORD":
				{
					java.util.ArrayList<String> components = utils.Utils.splitAndUnescapeString(query);
					java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE systemUserId=?");
					statement.setString(1, PasswordHash.createHash(components.get(1)));
					statement.setInt(2, user.user_id);
					return String.valueOf(statement.executeUpdate());
				}
				case "NEW_EVENT":
				{
					java.util.ArrayList<String> components = utils.Utils.splitAndUnescapeString(query);
					java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO PersonalEvent (description, time, systemUserId) VALUES (?, ?, ?)");
					statement.setString(1, components.get(1));
					statement.setTimestamp(2, java.sql.Timestamp.valueOf(components.get(2)));
					statement.setInt(3, user.user_id);
					return String.valueOf(statement.executeUpdate());
				}
				case "GET_EVENTS":
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("SELECT description, time FROM PersonalEvent WHERE systemUserId=? ORDER BY time ASC");
					statement.setInt(1, user.user_id);
					return resultToString(statement.executeQuery());
				}
				case "REGISTER_ROOM":
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Room (roomName, size, location) VALUES (?, ?, ?)");
					java.util.ArrayList<String> components = utils.Utils.splitAndUnescapeString(query);
					statement.setString(1, components.get(1));
					statement.setInt(2, Integer.parseInt(components.get(2)));
					statement.setString(3, components.get(3));
					return String.valueOf(statement.executeUpdate());
				}
				case "FIND_PERSON":
				{
					java.util.ArrayList<String> components = utils.Utils.splitAndUnescapeString(query);
					java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
					statement.setString(1, components.get(1));
					statement.setString(2, components.get(1));
					return resultToString(statement.executeQuery());
				}
				default:
					return "No more information provided.";
			}
		}
		catch (Exception exc)
		{
			verbose("An exception ocurred during execution: " + exc.toString());
			return exc.toString();
		}
	}
}