package server;

import static org.junit.Assert.*;

public class TestRoom 
{

	Database db_test = null;
	User user_root = null;

	@org.junit.Test
	public void testGetRoom() throws Exception
	{
		// Register room to get
		utils.Configuration.loadDefaultConfiguration();
		setup();
		String random_str = utils.Utils.makeRandomString(8);

		db_test.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RegisterRoomCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("0")
			+ " "
			+ utils.Utils.escapeSpaces("location")
		);
		java.sql.PreparedStatement prep_statement = db_test.getPreparedStatement("SELECT roomId FROM room WHERE roomName=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
		int columns = Integer.parseInt(parts.get(0));
		parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));

		// Get room
		Room room = new Room();
		room = room.getRoom(Integer.parseInt(parts.get(0)), room.db);
		int room_id_test = Integer.parseInt(parts.get(0));

		assertEquals(room_id_test, room.room_id);

		room.db.closeDatabase();
		db_test.closeDatabase();
	}

	//@org.junit.Test
	public void testAddRoom()
	{
		//TODO
	}

	@org.junit.Test
	public void testFindAvailableRoom() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		Room room = new Room();
		room.findAvailableRoom("2014-03-03 12:00:00", "2015-03-03 12:00:00", room.db);

		assertTrue(room.available_rooms.size() > 0);

		db_test.closeDatabase();
		room.db.closeDatabase();
	}
	private void setup() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		db_test = new Database(utils.Configuration.settings.get("DBConnection"));
		java.sql.PreparedStatement prepstatement = db_test.getPreparedStatement("SELECT * FROM SystemUser WHERE username=?");
		prepstatement.setString(1, "root");
		java.sql.ResultSet result = prepstatement.executeQuery();
		if (result.next())
		{
			user_root = new User(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6));
		}
	}
	
}