package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class User
{

	public int systemUserId, rank;	
	public int systemUserI, rank;
	public int user_id;

	public String username, fname, lname, hashedPW;
	Database db;
	ResultSet rs;
	PreparedStatement prepStatement;

	public User(int systemUserId, int rank, String username, String fname, String lname, String hashedPW)
	{
		this.systemUserId = systemUserId;
		this.rank = rank;
		this.username = username;
		this.fname = fname;
		this.lname = lname;
		this.hashedPW = hashedPW;S

	}

	void setDatabase(Database db)
	{
		this.db = db;
	}


	void saveUser(String password)
	{
		//ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
	
		try
        {
        	boolean create_new = false;
        	if (this.systemUserId != null)
        	{
	          	java.sql.PreparedStatement prepstatement = db.connection.prepareStatement("SELECT * FROM SystemUser WHERE systemUserId=?");
				prepstatement.setInt(1, this.systemUserId);
				java.sql.ResultSet result = prepstatement.executeQuery();

				if (result.next() == true)
				{
					java.sql.PreparedStatement statement 
						= connection.prepareStatement
							(
								"UPDATE SystemUser 
								SET rank=?, username=?, fname=?, lname=?, hashedPW=?
								WHERE systemUserId=?"
							);
							statement.setInt(1, this.rank);
							statement.set.String(2, this.username);
							statement.setString(3, this.fname);
							statement.setString(4, this.lname);
							statement.setString(5, PasswordHash.createHash(password) );
							statement.setInt(6, this.systemUserId);

					statement.execute();
				}	
				else
					create_new = true;
        	}
        	else
        		create_new = true;

			if (create_new)
			{
	        	java.sql.PreparedStatement statement 
					= connection.prepareStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)"
						);
						statement.setInt(1, this.rank);
						statement.set.String(2, this.username);
						statement.setString(3, this.fname);
						statement.setString(4, this.lname);
						statement.setString(5, PasswordHash.createHash(password) );

				statement.execute();
			}
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }

    }  
/*
	prepStatement.setInt(1, parts.get(0));
		prepStatement.setInt(2, parts.get(1));
		prepStatement.setString(3, parts.get(2));
		prepStatement.setString(4, parts.get(3));

	
	public static Group getGroup(int groupID)
	{
		prepStatement = db.prepareStatement("SELECT * FROM SystemGroup WHERE groupID =?");
		prepStatement.setString(1, groupID);
		rs = prepStatement.executeQuery(prepStatement);
		return rs;
	}
S
	void saveUser()
	{
		assertEquals(db.execute("nonexistingname", "pass", "query"), "Login username 'nonexistingname' does not exist.");
		
		ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		java.sql.PreparedStatement statement = connection.prepareStatement
		(
			"INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", 
			java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
		);
		statement.execute();

	}
	*/	
/*

		statement.execute();
	
	public static void removeSystemUser(SystemUser systemUser, int systemUserId, String username)
	{
			String query = query.substring(query.indexOf(" ") + 1);
			ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
			
				prepStatement = db.prepareStatement
				(
					"DELETE * FROM SystemUser (systemUserId, rank, username, lname, fname, hashedPW) WHERE VALUES (?, ?, ?)", 
					java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
				);
	
	}


		
	public void loadSystemUser(int systemUserId)
	{

		db = new Database(utils.Configuration.settings.get("DBConnection"));

		return ;

	}
*/
}