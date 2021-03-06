/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value = { "http://www.jooq.org", "2.5.1" }, comments = "This class is generated by jOOQ")
public class Location
		extends
		org.jooq.impl.UpdatableTableImpl<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord> {

	private static final long																								serialVersionUID	= 765753265;

	/**
	 * The singleton instance of inventory.location
	 */
	public static final edu.iup.chem.inventory.db.inventory.tables.Location													LOCATION			= new edu.iup.chem.inventory.db.inventory.tables.Location();

	/**
	 * The table column <code>inventory.location.cas</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	CAS					= createField(
																																						"cas",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.Integer>	CID					= createField(
																																						"cid",
																																						org.jooq.impl.SQLDataType.INTEGER,
																																						this);

	/**
	 * The table column <code>inventory.location.room</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	ROOM				= createField(
																																						"room",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.shelf</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	SHELF				= createField(
																																						"shelf",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.bottle_no</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.Integer>	BOTTLE_NO			= createField(
																																						"bottle_no",
																																						org.jooq.impl.SQLDataType.INTEGER,
																																						this);
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	TYPE				= createField(
																																						"type",
																																						org.jooq.impl.SQLDataType.CHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.instructor</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	INSTRUCTOR			= createField(
																																						"instructor",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.amount</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.Double>	AMOUNT				= createField(
																																						"amount",
																																						org.jooq.impl.SQLDataType.DOUBLE,
																																						this);

	/**
	 * The table column <code>inventory.location.units</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	UNITS				= createField(
																																						"units",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.arrival</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.sql.Date>		ARRIVAL				= createField(
																																						"arrival",
																																						org.jooq.impl.SQLDataType.DATE,
																																						this);

	/**
	 * The table column <code>inventory.location.expiration</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.sql.Date>		EXPIRATION			= createField(
																																						"expiration",
																																						org.jooq.impl.SQLDataType.DATE,
																																						this);

	/**
	 * The table column <code>inventory.location.part_no</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.Integer>	PART_NO				= createField(
																																						"part_no",
																																						org.jooq.impl.SQLDataType.INTEGER,
																																						this);

	/**
	 * The table column <code>inventory.location.supplier</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	SUPPLIER			= createField(
																																						"supplier",
																																						org.jooq.impl.SQLDataType.VARCHAR,
																																						this);

	/**
	 * The table column <code>inventory.location.active</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.Byte>		ACTIVE				= createField(
																																						"active",
																																						org.jooq.impl.SQLDataType.TINYINT,
																																						this);
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord, java.lang.String>	DESCRIPTION			= createField(
																																						"description",
																																						org.jooq.impl.SQLDataType.CLOB,
																																						this);

	public Location() {
		super("location",
				edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY);
	}

	public Location(final java.lang.String alias) {
		super(alias, edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY,
				edu.iup.chem.inventory.db.inventory.tables.Location.LOCATION);
	}

	@Override
	public edu.iup.chem.inventory.db.inventory.tables.Location as(
			final java.lang.String alias) {
		return new edu.iup.chem.inventory.db.inventory.tables.Location(alias);
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord>> getKeys() {
		return java.util.Arrays
				.<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord>> asList(edu.iup.chem.inventory.db.inventory.Keys.KEY_LOCATION_PRIMARY);
	}

	@Override
	public org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord> getMainKey() {
		return edu.iup.chem.inventory.db.inventory.Keys.KEY_LOCATION_PRIMARY;
	}

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord> getRecordType() {
		return edu.iup.chem.inventory.db.inventory.tables.records.LocationRecord.class;
	}
}
