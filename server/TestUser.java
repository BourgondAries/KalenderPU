package server;

import static org.junit.Assert.*;


public class TestUser
{

	//Database db = null;
	//User random_user = null;
	//User user_root = null;
 

	static 	int rank = 1, user_id = 7;
	static 	String username = "oar", fname = "Ole Andreas", lname = "Ramsdal", hashed_password = "hash";
	static User user;
	

	@org.junit.Test
	public void fieldsShouldBeSet()
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

	public void setFields(User user) 
	{
		user.user_id = user_id;
		user.rank = rank;
		user.username = username;
		user.fname = fname; 
		user.lname = lname;
		user.hashed_password = hashed_password;
	}
	public void setFields(User user, int user_id, int rank, String username, String firstname, String lastname, String hashed_password) 
	{
		user.user_id = user_id;
		user.rank = rank;
		user.username = username;
		user.fname = firstname; 
		user.lname = lastname;
		user.hashed_password = hashed_password;
	}

	@org.junit.Test
	public void getUserShouldReturnUser() throws java.sql.SQLException
	{
		User user = new User(user_id,rank,username,fname,lname,hashed_password);
		assertEquals(user,user.getUser());
	}

	public User newTestUser()
	{
		User user = new User(user_id, rank, username, fname, lname, hashed_password);
		return user;
	}

/*
@org.junit.Test
	public static void getUserFromDatabase(User user) throws java.sql.SQLException
	{
		Database db;
		utils.Configuration.loadDefaultConfiguration();
		try
		{
			db = new Database(utils.Configuration.settings.get("DBConnection"));
		}
		catch ( Exception CouldNotConnectAndSetupDatabaseConnection)
		{
			CouldNotConnectAndSetupDatabaseConnection.printStackTrace();
		}
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
	} */
	
@org.junit.Test
	public void getUserFromDatabase() throws Exception
	{
		User user = newTestUser();
		User.eraseSystemUser("oar");
		setFields(user);
		user.saveNewUser(hashed_password);
		//user.updateSystemUser();  
		setFields(user, 666, 2, "feilUsername", "feilFirstname", "feilLastname", "feilPassord" );
		
		user.loadSystemUser(user_id);

		System.out.println(user.user_id);
		
		assertEquals(user_id, user.user_id); 
		assertEquals(rank, user.rank);
		assertEquals(username, user.username);
		assertEquals(fname, user.fname);
		assertEquals(lname, user.lname);
		assertEquals(hashed_password, user.hashed_password);
		
	}	
	
/*
	@org.junit.Test
	public static void main(String[] args) 	throws Exception
	{
		System.out.println("Testing User.java"); 
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//utils.Configuration.loadDefaultConfigurations();
		System.out.println("Deleting testuser from db:"); 
		User.eraseSystemUser("oar");
		System.out.println("Making testuser."); 

		getUserShouldReturnUser();

		User user = newTestUser();
		System.out.println("Saving testuser to db"); 

		getUserFromDatabase(user);


		db.closeDatabase();
		//getUserShouldReturnUser();
		//getUserFromDatabase(user); 
	}
	*/
}

