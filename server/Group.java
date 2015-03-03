package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Group{

	public int groupID, groupRank;
	public String groupName;
	public User groupAdmin; //hvor staar dette om i SQL?
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
	Database db = new Database(utils.Configuration.settings.get("DBConnection"));
	//Statement stmt = new con.createStatement();
	ResultSet rs = new ResultSet();
	PreparedStatement prepStatement = new PreparedStatement();
	}


	//save- og get metoder
	public Group getGroup(int groupID){
		prepStatement = db.prepareStatement("SELECT * FROM SystemGroup WHERE groupID =?");
		prepStatement.setString(1, groupID);
		rs = prepStatement.executeQuery(prepStatement);
		return rs;
	}

	public void addGroup(int groupID, int groupRank, String groupName, User groupAdmin){
		query = query.substring(query.indexOf(" ") + 1);
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		prepStatement = db.prepareStatement("INSERT INTO SystemGroup (groupID, groupRank, groupName, groupAdmin) VALUES (?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		prepStatement.setInt(1, parts.get(0));
		prepStatement.setInt(2, parts.get(1));
		prepStatement.setString(3, parts.get(2));
		prepStatement.setString(4, parts.get(3));
		rs = prepStatement.executeQuery(); //hvordan execute'er man disse?
		return rs;
	}

	public void removeGroup(int groupID, String groupName, User groupAdmin){
		query = query.substring(query.indexOf(" ") + 1);
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		prepStatement = db.prepareStatement("INSERT INTO SystemGroup (groupID, groupRank, groupName, groupAdmin) VALUES (?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		
	}


}