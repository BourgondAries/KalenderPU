package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class User
{
	public int user_id, rank;	
	public String username, fname, lname, hashed_password;
	public static Database db;
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

	public void saveNewUser(String password) throws Exception
	{
		try
        {
        	utils.Configuration.loadDefaultConfiguration();
			this.db = new Database(utils.Configuration.settings.get("DBConnection"));
        	if (this.user_id == 0)
        	{	
        		System.out.println("user_id = 0");
        	}
        	else 
        	{
        		System.out.println("SAVING USER PLZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        		System.out.println("USER ID PLZZ :" + user_id);
	        	java.sql.PreparedStatement statement 
					= db.getPreparedStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)"
						);
				statement.setInt(1, this.rank);
				statement.setString(2, this.username);
				statement.setString(3, this.fname);
				statement.setString(4, this.lname);
				statement.setString(5, PasswordHash.createHash(password) );
				statement.executeUpdate();
									

				System.out.println(this.username);
				System.out.println("Prøvde å lagre");
				System.out.println(this.user_id);
			}
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
        db.closeDatabase();
    }  

    public void updateSystemUser() throws Exception
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
					= db.getPreparedStatement
						("UPDATE SystemUser SET rank=?, username=?, fname=?, lname=?, hashedPW=? WHERE systemUserId=?");
					
					statement.setInt(1, this.rank);
					statement.setString(2, this.username);
					statement.setString(3, this.fname);
					statement.setString(4, this.lname);
					statement.setString(5, this.hashed_password);
					statement.setInt(6, this.user_id);
					statement.executeUpdate();
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
        
    	db.closeDatabase();
	}
 

	public void loadSystemUser(int user_id) throws Exception
	{ 
		
		if (user_id != 0)
        {
        	try 
			{
				utils.Configuration.loadDefaultConfiguration();
				db = new Database(utils.Configuration.settings.get("DBConnection")); 

          		java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE systemUserId=?");
			
				prepstatement.setInt(1, this.user_id);
				java.sql.ResultSet result = prepstatement.executeQuery();
			
				if (/*result.next() ==*/ true)
				{
					String query = Database.resultToString(result);

					System.out.println(query);
				
					java.util.ArrayList<String> res = utils.Utils.splitAndUnescapeString(query);
						//tester for å finne ut hva som komer ut fra database slik at vi kan lagre informasjon med rette indekser. 
					for (int i = 0; i < res.size(); ++i) 
						System.out.println(i + res.get(i));
					// #antall felt 6, systemUserId, rank, username, fname, lname, hashedPW		
					// user_id, rank, username, 
					int index = 0;

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
			catch (Exception exc)
			{
				exc.printStackTrace();
			}	
    	}
    	else
    		System.out.println("It appears that " + user_id +" do not match systemUserId in database." );    	
    	db.closeDatabase();		
	}
	public void loadSystemUser(String username) throws Exception
	{ 
		
		if (true)
        {
        	try 
			{
				utils.Configuration.loadDefaultConfiguration();
			this.db = new Database(utils.Configuration.settings.get("DBConnection"));

          		java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT user_id, rank, username, fname, lname FROM SystemUser WHERE systemUser.username like ?");
			
				prepstatement.setString(1, this.username);
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
					System.out.println("It seems like the database is empty for this user " + username + " .");
			}	
			catch (Exception exc)
			{
				exc.printStackTrace();
			}		
    	}
    	else
    		System.out.println("It appears that " + username +" do not match systemUserId in database." );    	
    	db.closeDatabase();		
	}

	public static void eraseSystemUser(String username) throws Exception
	{    	
		utils.Configuration.loadDefaultConfiguration();
		db = new Database(utils.Configuration.settings.get("DBConnection"));
		java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE username=?");
		prepstatement.setString(1, username);
		java.sql.ResultSet result = prepstatement.executeQuery();
	
		if (result.next() == true)
		{
			System.out.println("Found user in database, " + username + ". Now ready for deleting.");
  			prepstatement = db.getPreparedStatement("DELETE FROM SystemUser WHERE username=?");
			prepstatement.setString(1, username);
			prepstatement.executeUpdate();
		}
		else 
			System.out.println("Something wrong happened, did not delete: " + username + ".");
		
		prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE username=?");
		prepstatement.setString(1, username);
		result = prepstatement.executeQuery();
	
		if (result.next() == false)
			System.out.println("It seems like the database is empty for this user " + username + ". Deletion of systemUser was a sucsess.");
			 	
    	db.closeDatabase();		
	}


	public User getUser()
	{
		return this;
	}

	public static User getUser(int user_id) throws java.sql.SQLException
	{
		User user = new User(user_id, 0, "", "", "", "");
		try 
		{
		user.loadSystemUser(user_id);
		}
		catch (Exception exc)
			{
				exc.printStackTrace();
			}	
		return user;
	}
	public static User getUser(String username) throws java.sql.SQLException
	{
		User user = new User(1,1, username, "", "", "");
		try 
		{
			user.loadSystemUser(username);
		}
		catch (Exception exc)
			{
				exc.printStackTrace();
			}	
		return user;
	}

	public static void main(String[] args) 
	{
		System.out.println("User main");
	}
	
	
}
