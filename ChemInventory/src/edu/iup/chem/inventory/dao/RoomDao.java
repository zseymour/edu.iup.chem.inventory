package edu.iup.chem.inventory.dao;

import static edu.iup.chem.inventory.db.inventory.Tables.ROOM;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.iup.chem.inventory.ConnectionPool;
import edu.iup.chem.inventory.db.inventory.InventoryFactory;
import edu.iup.chem.inventory.db.inventory.tables.records.RoomRecord;

public class RoomDao {

	private static final Logger	LOG	= Logger.getLogger(RoomDao.class);

	public static List<String> getAllRoomNames() {
		final List<String> rooms = new ArrayList<>();
		try (Connection conn = ConnectionPool.getConnection()) {
			final InventoryFactory create = new InventoryFactory(conn);
			rooms.addAll(create.select(ROOM.ROOM_).from(ROOM).fetch()
					.into(String.class));
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

}
