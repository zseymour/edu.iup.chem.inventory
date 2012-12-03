/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.5.1"},
                            comments = "This class is generated by jOOQ")
public class Inventory extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -1430572121;

	/**
	 * The singleton instance of inventory
	 */
	public static final Inventory INVENTORY = new Inventory();

	/**
	 * No further instances allowed
	 */
	private Inventory() {
		super("inventory");
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			edu.iup.chem.inventory.db.inventory.tables.Access.ACCESS,
			edu.iup.chem.inventory.db.inventory.tables.Cancer.CANCER,
			edu.iup.chem.inventory.db.inventory.tables.Chemical.CHEMICAL,
			edu.iup.chem.inventory.db.inventory.tables.FieldDataFieldName.FIELD_DATA_FIELD_NAME,
			edu.iup.chem.inventory.db.inventory.tables.FieldDataFieldRoom.FIELD_DATA_FIELD_ROOM,
			edu.iup.chem.inventory.db.inventory.tables.Jchemproperties.JCHEMPROPERTIES,
			edu.iup.chem.inventory.db.inventory.tables.JchempropertiesCr.JCHEMPROPERTIES_CR,
			edu.iup.chem.inventory.db.inventory.tables.Location.LOCATION,
			edu.iup.chem.inventory.db.inventory.tables.Role.ROLE,
			edu.iup.chem.inventory.db.inventory.tables.Room.ROOM,
			edu.iup.chem.inventory.db.inventory.tables.Structures.STRUCTURES,
			edu.iup.chem.inventory.db.inventory.tables.StructuresUl.STRUCTURES_UL,
			edu.iup.chem.inventory.db.inventory.tables.StructTemp.STRUCT_TEMP,
			edu.iup.chem.inventory.db.inventory.tables.Synonym.SYNONYM,
			edu.iup.chem.inventory.db.inventory.tables.User.USER,
			edu.iup.chem.inventory.db.inventory.tables.Users.USERS);
	}
}
