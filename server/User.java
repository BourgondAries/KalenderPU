package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


public class User
{

	public int user_id, rank;	
	public String username, fname, lname, hashed_password;
	Connection connection;
	Database db;
	ResultSet rs;
	PreparedStatement prepStatement;

	public User(int user_id, int rank, String username, String fname, String lname, String hashed_password)
	{
		this.user_id = user_id;
		this.rank = rank;
		this.username = username;
		this.fname = fname;
		this.lname = lname;
		this.hashed_password = hashed_password;
	}

	/*

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public void saveNewUser(String password) throws(exception)
	{
		try
        {
        	utils.Configuration.loadDefaultConfiguration();
			this.db = new Database(utils.Configuration.settings.get("DBConnection"));
        	boolean create_new = false;
        	if (this.user_id == 0)
        	{	
        		System.out.println("user_id = 0");
        	}
        	else 
        	{
	        	java.sql.PreparedStatement statement 
					= db.getPreparedStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashed_password) VALUES (?, ?, ?, ?, ?)"
						);
				statement.setInt(1, this.rank);
				statement.setString(2, this.username);
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
        catch (IOException exc)
		{
			exc.printStackTrace();
		}
        db.closeDatabase();
    }  

    public void updateSystemUser()
    {
		try
        {
        	utils.Configuration.loadDefaultConfiguration();
			this.db = new Database(utils.Configuration.settings.get("DBConnection"));
        	if (this.user_id != 0)
        	{
	          	java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE systemUserId=?");
				prepstatement.setInt(1, this.user_id);
				java.sql.ResultSet result = prepstatement.executeQuery();

				if (result.next() == true)
				{
					java.sql.PreparedStatement statement 
					= db.prepareStatement
						("UPDATE SystemUser SET rank=?, username=?, fname=?, lname=?, hashed_password=? WHERE systemUserId=?");
					
					statement.setInt(1, this.rank);
					statement.setString(2, this.username);
					statement.setString(3, this.fname);
					statement.setString(4, this.lname);
					statement.setString(5, this.hashed_password);
					statement.setInt(6, this.user_id);
					statement.execute();
				}	
				else
					System.out.println("user_id: " + user_id + "has empty database which can not be updated, try saveUser()" );
        	}
        	else
        		System.out.println("user_id: " + user_id + "Seems to be 0 which is not valid for updating database" );
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
        catch (IOException exc)
		{
			exc.printStackTrace();
		}
    	db.closeDatabase();
}
 
	public void loadSystemUser(int user_id) throws java.sql.SQLException
	{ 
		
		if (this.user_id != 0)
        {
        	try 
			{
          		java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE systemUserId=?");
			
				prepstatement.setInt(1, this.user_id);
				java.sql.ResultSet result = prepstatement.executeQuery();
			
				if (result.next() == true)
				{
					String query = Database.resultToString(result);
				
					java.util.ArrayList<String> res = utils.Utils.splitAndUnescapeString(query);
						//tester for å finne ut hva som komer ut fra database slik at vi kan lagre informasjon med rette indekser. 
					for (int i = 0; i < res.size(); ++i) 
						System.out.println(i + res.get(i));
					// #antall felt 6, systemUserId, rank, username, fname, lname, hashedPW		
					// user_id, rank, username, 
					int index = 7;

					this.user_id = Integer.parseInt(res.get(index));
					this.rank = Integer.parseInt(res.get(index+1));
					this.username = res.get(index+2);
					this.fname = res.get(index+3);
					this.lname = res.get(index+4);
					this.hashed_password = res.get(index+5);
				}
				else 
					System.out.println("It seems like the database is empty for this user " + user_id + " .");
			}		
			catch (java.sql.SQLException exc)
			{
				exc.printStackTrace();
			}	
			catch (IOException exc)
			{
				exc.printStackTrace();
			}
    	}
    	else
    		System.out.println("It appears that " + user_id +" do not match systemUserId in database." );    	
    	db.closeDatabase();		
	}
	

	public User getUser()
	{
		return this;
	}

	public static User getUser(int user_id) throws java.sql.SQLException
	{
		User user = new User(user_id, 0, "", "", "", "");
		user.loadSystemUser(user_id);
		return user;
	}
	public static User getUser(String username) throws java.sql.SQLException
	{
		User user = new User(1,1, username, "", "", "");
		//user.loadSystemUser(username);
		return user;
	}

	public static void main(String[] args) 
	{
		System.out.println("Gisle er jelly");
	}
	
	*/
}
