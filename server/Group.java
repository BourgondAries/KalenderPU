package server;
import java.util.ArrayList; 
java.sql.Connection;
//import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Group{

	public int groupID, groupRank;
	public String groupName;
	public User groupAdmin; 
	public ArrayList<User> groupMembers;
	public ArrayList<Group> subGroups;
	private Database database;	

	public Group(int groupID, int groupRank, String groupName, User groupAdmin){
	ArrayList<User> groupMembers = new ArrayList<User>();
	ArrayList<Group> subGroups = new ArrayList<Group>();
	this.groupID = groupID;
	this.groupRank = groupRank;
	this.groupName = groupName;
	this.groupAdmin = groupAdmin;
	Database con = new Database("jdbc:derby:database");
	//Statement stmt = new con.createStatement();
	ResultSet rs = new ResultSet(string sql);
	PreparedStatement prepStatement = new PreparedStatement();
	}


	//save- og get metoder
	public Group getGroup(int groupID){
		prepStatement = connection.prepareStatement("SELECT * FROM SystemGroup WHERE groupID =?");
		prepStatement.setString(1, groupID);
		rs = executeQuery();
		return rs;
	}

	public void addGroup(int groupID, int groupRank, String groupName, User groupAdmin){
		query = query.substring(query.indexOf(" ") + 1);
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		prepStatement = connection.prepareStatement("INSERT INTO SystemGroup (groupID, groupRank, groupName, groupAdmin) VALUES (?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		prepstatement.setInt(1, parts.get(0));
		prepStatement.setInt(2, parts.get(1));
		prepStatement.setString(3, parts.get(2));
		prepStatement.setString(4, parts.get(3));
		rs = prepStatement.executeQuery();
		
	}


}