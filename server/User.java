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

		db = new Database(utils.Configuration.settings.get("DBConnection"));
		rs = new ResultSet();
		prepStatement = PreparedStatement();
	}
/*
	public static User getUser(int systemUserId)
	{
		prepStatement = db.prepareStatement("SELECT * FROM SystemUser WHERE systemUserId =?");
		prepStatement.setString(1, groupID);
		rs = prepStatement.executeQuery(prepStatement);
	}
	
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