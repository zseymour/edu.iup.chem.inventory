/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.tables;

/**
 * This class is generated by jOOQ.
 *
 * Stores user roles.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.5.1"},
                            comments = "This class is generated by jOOQ")
public class Role extends org.jooq.impl.UpdatableTableImpl<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord> {

	private static final long serialVersionUID = -83008083;

	/**
	 * The singleton instance of inventory.role
	 */
	public static final edu.iup.chem.inventory.db.inventory.tables.Role ROLE = new edu.iup.chem.inventory.db.inventory.tables.Role();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord> getRecordType() {
		return edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord.class;
	}

	/**
	 * Primary Key: Unique role ID.
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord, org.jooq.tools.unsigned.UInteger> RID = createField("rid", org.jooq.impl.SQLDataType.INTEGERUNSIGNED, this);

	/**
	 * Unique role name.
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The weight of this role in listings and the user interface.
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord, java.lang.Integer> WEIGHT = createField("weight", org.jooq.impl.SQLDataType.INTEGER, this);

	public Role() {
		super("role", edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY);
	}

	public Role(java.lang.String alias) {
		super(alias, edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY, edu.iup.chem.inventory.db.inventory.tables.Role.ROLE);
	}

	@Override
	public org.jooq.Identity<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord, org.jooq.tools.unsigned.UInteger> getIdentity() {
		return edu.iup.chem.inventory.db.inventory.Keys.IDENTITY_ROLE;
	}

	@Override
	public org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord> getMainKey() {
		return edu.iup.chem.inventory.db.inventory.Keys.KEY_ROLE_PRIMARY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.RoleRecord>>asList(edu.iup.chem.inventory.db.inventory.Keys.KEY_ROLE_PRIMARY, edu.iup.chem.inventory.db.inventory.Keys.KEY_ROLE_NAME);
	}

	@Override
	public edu.iup.chem.inventory.db.inventory.tables.Role as(java.lang.String alias) {
		return new edu.iup.chem.inventory.db.inventory.tables.Role(alias);
	}
}
