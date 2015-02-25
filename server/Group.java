package server;
import java.util.ArrayList; 
java.sql.Connection;

public class Group{

	public int groupID, groupRank;
	public String groupName;
	public User groupAdmin; 
	public ArrayList<User> groupMembers;
	public ArrayList<Group> subGroups;
	private Database database;	

	public Group(){
	ArrayList<User> groupMembers = new ArrayList<User>();
	ArrayList<Group> subGroups = new ArrayList<Group>();
	this.groupID = groupID;
	this.groupRank = groupRank;
	this.groupName = groupName;
	this.groupAdmin = groupAdmin;
	this.database = database(jdbc:derby:database);
	}

	//save- og get metoder
	public Group getGroup(groupID, connection){
		Group fetch = 
		return fetch;
	}

	public void saveGroup(groupID)


}