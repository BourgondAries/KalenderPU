package server;
import java.util.ArrayList; 
java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;


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
	Database con = new Database("jdbc:derby:database");
	Statement stmt = new con.createStatement();
	ResultSet rs = new ResultSet(string sql);
	}


	//save- og get metoder
	public Group getGroup(int groupID, Database con){
		string sqlSelect = "SELECT * FROM SystemGroup WHERE groupID =?";
		Group gr = rs(sqlSelect);
		return gr;
	}

	public void saveGroup(int groupID){
		---
	}


}