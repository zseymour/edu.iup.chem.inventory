/**
 * This class is generated by jOOQ
 */
package edu.iup.chem.inventory.db.inventory.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.5.1"},
                            comments = "This class is generated by jOOQ")
public class Structures extends org.jooq.impl.UpdatableTableImpl<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord> {

	private static final long serialVersionUID = -2040266344;

	/**
	 * The singleton instance of inventory.structures
	 */
	public static final edu.iup.chem.inventory.db.inventory.tables.Structures STRUCTURES = new edu.iup.chem.inventory.db.inventory.tables.Structures();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord> getRecordType() {
		return edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord.class;
	}

	/**
	 * The table column <code>inventory.structures.cd_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_ID = createField("cd_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_structure</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, byte[]> CD_STRUCTURE = createField("cd_structure", org.jooq.impl.SQLDataType.BLOB, this);

	/**
	 * The table column <code>inventory.structures.cd_smiles</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.String> CD_SMILES = createField("cd_smiles", org.jooq.impl.SQLDataType.CLOB, this);

	/**
	 * The table column <code>inventory.structures.cd_formula</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.String> CD_FORMULA = createField("cd_formula", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The table column <code>inventory.structures.cd_sortable_formula</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.String> CD_SORTABLE_FORMULA = createField("cd_sortable_formula", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The table column <code>inventory.structures.cd_molweight</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Double> CD_MOLWEIGHT = createField("cd_molweight", org.jooq.impl.SQLDataType.DOUBLE, this);

	/**
	 * The table column <code>inventory.structures.cd_hash</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_HASH = createField("cd_hash", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_flags</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.String> CD_FLAGS = createField("cd_flags", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * The table column <code>inventory.structures.cd_timestamp</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.sql.Timestamp> CD_TIMESTAMP = createField("cd_timestamp", org.jooq.impl.SQLDataType.TIMESTAMP, this);

	/**
	 * The table column <code>inventory.structures.cd_pre_calculated</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Byte> CD_PRE_CALCULATED = createField("cd_pre_calculated", org.jooq.impl.SQLDataType.TINYINT, this);

	/**
	 * The table column <code>inventory.structures.cd_fp1</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP1 = createField("cd_fp1", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp2</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP2 = createField("cd_fp2", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp3</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP3 = createField("cd_fp3", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp4</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP4 = createField("cd_fp4", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp5</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP5 = createField("cd_fp5", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp6</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP6 = createField("cd_fp6", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp7</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP7 = createField("cd_fp7", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp8</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP8 = createField("cd_fp8", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp9</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP9 = createField("cd_fp9", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp10</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP10 = createField("cd_fp10", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp11</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP11 = createField("cd_fp11", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp12</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP12 = createField("cd_fp12", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp13</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP13 = createField("cd_fp13", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp14</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP14 = createField("cd_fp14", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp15</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP15 = createField("cd_fp15", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * The table column <code>inventory.structures.cd_fp16</code>
	 */
	public final org.jooq.TableField<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> CD_FP16 = createField("cd_fp16", org.jooq.impl.SQLDataType.INTEGER, this);

	public Structures() {
		super("structures", edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY);
	}

	public Structures(java.lang.String alias) {
		super(alias, edu.iup.chem.inventory.db.inventory.Inventory.INVENTORY, edu.iup.chem.inventory.db.inventory.tables.Structures.STRUCTURES);
	}

	@Override
	public org.jooq.Identity<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord, java.lang.Integer> getIdentity() {
		return edu.iup.chem.inventory.db.inventory.Keys.IDENTITY_STRUCTURES;
	}

	@Override
	public org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord> getMainKey() {
		return edu.iup.chem.inventory.db.inventory.Keys.KEY_STRUCTURES_PRIMARY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.iup.chem.inventory.db.inventory.tables.records.StructuresRecord>>asList(edu.iup.chem.inventory.db.inventory.Keys.KEY_STRUCTURES_PRIMARY);
	}

	@Override
	public edu.iup.chem.inventory.db.inventory.tables.Structures as(java.lang.String alias) {
		return new edu.iup.chem.inventory.db.inventory.tables.Structures(alias);
	}
}
