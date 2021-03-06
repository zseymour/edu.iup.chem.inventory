/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.5.1"},
                            comments = "This class is generated by jOOQ")
public class Access extends org.jooq.impl.UpdatableTableImpl<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord> {

	private static final long serialVersionUID = 1269805904;

	/**
	 * The singleton instance of inventory.access
	 */
	public static final edu.iup.chem.inventory.db.inventory.tables.Access ACCESS = new edu.iup.chem.inventory.db.inventory.tables.Access();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord> getRecordType() {
		return edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord.class;
	}

	/**
	 * The table column <code>inventory.access.rid</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord, java.lang.Integer> RID = createField("rid", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.access.uid</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord, java.lang.Integer> UID = createField("uid", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.access.name</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The table column <code>inventory.access.room</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord, java.lang.String> ROOM = createField("room", org.jooq.impl.SQLDataType.VARCHAR, this);

	public Access() {
		super("access", edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY);
	}

	public Access(java.lang.String alias) {
		super(alias, edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY, edu.iup.chem.inventory.db.inventory.tables.Access.ACCESS);
	}

	@Override
	public org.jooq.Identity<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord, java.lang.Integer> getIdentity() {
		return edu.iup.chem.inventory.db.inventory.Keys.IDENTITY_ACCESS;
	}

	@Override
	public org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord> getMainKey() {
		return edu.iup.chem.inventory.db.inventory.Keys.KEY_ACCESS_PRIMARY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.AccessRecord>>asList(edu.iup.chem.inventory.db.inventory.Keys.KEY_ACCESS_PRIMARY);
	}

	@Override
	public edu.iup.chem.inventory.db.inventory.tables.Access as(java.lang.String alias) {
		return new edu.iup.chem.inventory.db.inventory.tables.Access(alias);
	}
}
