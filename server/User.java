package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class User
{
	
	public int systemUserI, rank;
	public int user_id;
	public String username, fname, lname, hashedPW;

	public User(int systemUserId, int rank, String username, String fname, String lname, String hashedPW)
	{
		this.systemUserI = systemUserId;
		this.rank = rank;
		this.username = username;
		this.fname = fname;
		this.lname = lname;
		this.hashedPW = hashedPW;
		user_id = systemUserId;

		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//ResultSet rs = new ResultSet();
		//PreparedStatement prepStatement = new PreparedStatement();
	}
/*
	public static User getUser(int systemUserId)
	{
		prepStatement = db.prepareStatement("SELECT * FROM SystemUser WHERE systemUserId =?");
		prepStatement.setString(1, groupID);
	}
	public static Group getGroup(int groupID){
		prepStatement = db.prepareStatement("SELECT * FROM SystemGroup WHERE groupID =?");
		prepStatement.setString(1, groupID);
		rs = prepStatement.executeQuery(prepStatement);
		return rs;
	}

	void saveUser(){
		assertEquals(db.execute("nonexistingname", "pass", "query"), "Login username 'nonexistingname' does not exist.");
		
		ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		java.sql.PreparedStatement statement = connection.prepareStatement
					(
						"INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", 
						java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
					);
					statement.execute();

	}

	// husk at du holder paa med user-klassen, og ikke group! :P
	public static void removeGroup(Group gruppe, int groupID, String groupName, User groupMember){
			query = query.substring(query.indexOf(" ") + 1);
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
			if (user == Group.groupAdmin) {
				prepStatement = db.prepareStatement("DELETE * FROM SystemGroup (groupID, groupName, groupAdmin) WHERE VALUES (?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
				gruppe.subGroups.re
			}
			else {
				String errMessage = "You are not permitted to delete this group";

			}
			


		
	public void getUserFromDatabase(int systemUserId)
	{

		Database db = new Database(utils.Configuration.settings.get("DBConnection"));

		return ;

	}
*/
}