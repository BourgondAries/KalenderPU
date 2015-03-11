package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.io.IOException;
//import java.utils.Configuration;

public class Room
{

	public int room_id, size;
	public String location, room_name;
	public Database db;
	ArrayList<Integer> available_rooms;
	public static class CouldNotConnectAndSetupDatabaseConnection extends Exception { CouldNotConnectAndSetupDatabaseConnection(Throwable exc) { super(exc); } }
	

	public Room() throws Exception
	{
		this.db = new Database(utils.Configuration.settings.get("DBConnection"));
		this.available_rooms = new ArrayList<Integer>();
	}
	public Room(int room_id, int size, String location, String room_name) throws Exception
	{
		this.room_id = room_id;
		this.size = size;
		this.location = location;
		this.room_name = room_name;
		this.available_rooms = new ArrayList<Integer>();

		db = new Database(utils.Configuration.settings.get("DBConnection"));
	}

	
	public static Room getRoom(int room_id, Database db) throws Exception
	{
		try
		{
			PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM ROOM WHERE roomId=?");
			prep_statement.setInt(1, room_id);
			String result_string = db.resultToString(prep_statement.executeQuery());
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(result_string);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			
			return new Room(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), parts.get(2), parts.get(3));
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}
	}


	public static void addRoom(int size, String location, String room_name, Database db) throws Exception
	{
		try
		{
			PreparedStatement prep_statement = db.getPreparedStatement("INSERT INTO Room (size, location, roomName) VALUES (?, ?, ?)");
			prep_statement.setInt(1, size);
			prep_statement.setString(2, location);
			prep_statement.setString(3, room_name);
			prep_statement.executeUpdate();	
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}
	}

	public ArrayList<Integer> findAvailableRoom(String start_time, String end_time, Database db) throws Exception
	{

		ArrayList<Integer> av_rooms = new ArrayList<Integer>();

		PreparedStatement prep_statement = db.getPreparedStatement("SELECT DISTINCT roomId FROM Booking WHERE (timeBegin<? OR timeEnd>?)");
		prep_statement.setTimestamp(1, java.sql.Timestamp.valueOf(end_time));
		prep_statement.setTimestamp(2, java.sql.Timestamp.valueOf(start_time));
		String selection_result = Database.resultToString(prep_statement.executeQuery());
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
		int columns = Integer.parseInt(parts.get(0));
		for (int i = columns + 1; i < parts.size(); i++)
		{
			av_rooms.add(Integer.parseInt(parts.get(i)));
		}

		this.available_rooms = av_rooms;

		return av_rooms;
	}

	public static boolean isAvailable(ArrayList<Integer> available_rooms, int room_id) 
	{
		return available_rooms.contains(room_id);
	}


// hente bookings som er i det tidsintervallet, finn alle opptatte rom. Gaa inn paa room og "fjern" de rommene som er opptatte og returner en oversikt
// gammel_greie = "SELECT * FROM BOOKING (timeBegin, timeEnd) WHERE timeBegin<? AND timeEnd>?
//			(SELECT * FROM ROOM WHERE ROOM.roomId != BOOKING.roomId)"

	


}
