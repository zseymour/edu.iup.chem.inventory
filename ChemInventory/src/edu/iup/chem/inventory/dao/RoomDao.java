package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ROOM;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.Access;
import edu.iup.chem.inventory.db.inventory.tables.Room;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;

public class RoomDao {

	private static final Logger	LOG	= Logger.getLogger(RoomDao.class);

	public static List<RoomRecord> fetchUserRooms(final Integer uid) {
		List<RoomRecord> rooms = new ArrayList<>();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			rooms = create.select(Room.ROOM.ROOM_).from(Room.ROOM)
					.join(Access.ACCESS)
					.on(Room.ROOM.ROOM_.equal(Access.ACCESS.ROOM))
					.where(Access.ACCESS.UID.equal(uid))
					.fetchInto(RoomRecord.class);
		} catch (final SQLException e) {
			LOG.error("Error fetching list of user's rooms.");
		}
		return rooms;
	}

	public static List<String> getAllRoomNames() {
		final List<String> rooms = new ArrayList<>();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			rooms.addAll(create.select(ROOM.ROOM_).from(ROOM).fetch(ROOM.ROOM_));
		} catch (final SQLException e) {
			LOG.error("Error fetching all rooms.");
		}

		return rooms;
	}

	public static List<RoomRecord> getAllRoomRecords() {

		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			return create.select().from(ROOM).fetch().into(RoomRecord.class);
		} catch (final SQLException e) {
			LOG.error("Error fetching all rooms.");
		}

		return new ArrayList<>();
	}

	public static RoomRecord getDefaultRoom(final String room) {
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			return create.select().from(ROOM).where(ROOM.ROOM_.equal(room))
					.fetchOne().into(RoomRecord.class);
		} catch (final SQLException e) {
			LOG.error("Error fetching default room.");
		}
		return null;
	}

}
