package server;

import static org.junit.Assert.*;
//@Test

public class TestUser
{
/*
	//Database db = null;
	//User random_user = null;
	//User user_root = null;
 

	static 	int rank = 1, user_id = 7;
	static 	String username = "oar", fname = "Ole Andreas", lname = "Ramsdal", hashed_password = "hash";
	static User user;
	

	@org.junit.Test
	public static void fieldsShouldBeSet()
	{
		 user = new User(user_id,rank,username,fname,lname,hashed_password);

		//Tests
		assertEquals(user_id, user.user_id); 
		assertEquals(rank, user.rank);
		assertEquals(username, user.username);
		assertEquals(fname, user.fname);
		assertEquals(lname, user.lname);
		assertEquals(hashed_password, user.hashed_password);
	}

	public static void setFields(User user) 
	{
		user.user_id = user_id;
		user.rank = rank;
		user.username = username;
		user.fname = fname; 
		user.lname = lname;
		user.hashed_password = hashed_password;
	}
	public static void setFields(User user, int user_id, int rank, String username, String firstname, String lastname, String hashed_password) 
	{
		user.user_id = user_id;
		user.rank = rank;
		user.username = username;
		user.fname = firstname; 
		user.lname = lastname;
		user.hashed_password = hashed_password;
	}
	

	@org.junit.Test
	public static void getUserShouldReturnUser() throws java.sql.SQLException
	{
		User user = new User(user_id,rank,username,fname,lname,hashed_password);
		assertEquals(user,user.getUser());
	}


@org.junit.Test
	public static void getUserFromDatabase(User user) throws java.sql.SQLException
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));

		setFields(user);
		user.saveNewUser( hashed_password);
		//user.updateSystemUser();  
		setFields(user, 666, 2, "feilUsername", "feilFirstname", "feilLastname", "feilPassord" );
		
		try 
		{
			user.loadSystemUser(user_id);
		} 
		catch(java.sql.SQLException exc)
		{
			throw new java.sql.SQLException(exc);
		}
		assertEquals(user_id, user.user_id); 
		assertEquals(rank, user.rank);
		assertEquals(username, user.username);
		assertEquals(fname, user.fname);
		assertEquals(lname, user.lname);
		assertEquals(hashed_password, user.hashed_password);
		db.closeDatabase();
	}
	
	@org.junit.Test
	public static void main(String[] args) 	
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//utils.Configuration.loadDefaultConfigurations();
		User user = new User(user_id, rank, username, fname, lname, hashed_password);
		
		System.out.println("Hello Test"); 
		
		user.saveNewUser("hash"); 
		db.closeDatabase();
		
		//getUserFromDatabase(user); 
	}
	*/
}

