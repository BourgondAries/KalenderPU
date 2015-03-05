package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class GroupSettings{
	public int groupID, groupRank;
	public String groupName;
	public User groupAdmin, groupMember; //hvor staar dette om i SQL?
	public ArrayList<User> groupMembers;
	public ArrayList<Group> subGroups;
	private Database database;	


	public GroupSettings(){
		ArrayList<User> groupMembers = new ArrayList<User>();
		ArrayList<Group> subGroups = new ArrayList<Group>();
		this.groupID = groupID;
		this.groupRank = groupRank;
		this.groupName = groupName;
		this.groupAdmin = groupAdmin;
		//this.groupMember = groupMember;
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		ResultSet rs = new ResultSet();
		PreparedStatement prepStatement = new PreparedStatement();
	}



	public static void removeGroup(Group gruppe, int groupID, String groupName, User groupMember){
			query = query.substring(query.indexOf(" ") + 1);
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
			if (user == Group.groupAdmin) {
				prepStatement = db.prepareStatement("DELETE * FROM SystemGroup (groupID, groupName, groupAdmin) VALUES (?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
				// hvordan sletter man subGroup-lista???
				group.subGroups.clear();
			}
			else {
				String errMessage = "You are not permitted to delete this group";

			}
			

	public static void addMember(User user, Group gruppe){
		gruppe.groupMembers.add(user);
		update(User user, Group gruppe);


	}

	public void update(User user, Group gruppe){
		prepStatement = db.prepareStatement("INSERT INTO GroupMember (UserID, groupID) VALUES (?,?)");
		prepStatement.setInt(1, user.user_id);
		prepStatement.setInt(2, gruppe.groupID);
		prepStatement.executeQuery();

	}

}