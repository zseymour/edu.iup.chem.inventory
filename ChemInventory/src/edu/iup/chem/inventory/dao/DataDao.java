package edu.iup.chem.inventory.dao;

import java.util.List;

import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.impl.UpdatableRecordImpl;

@SuppressWarnings("rawtypes")
public abstract class DataDao<E extends UpdatableRecordImpl> {
	public abstract List<E> getAll();

	public abstract Cursor<Record> getAllLazy();
}
