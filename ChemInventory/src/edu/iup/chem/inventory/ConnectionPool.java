package edu.iup.chem.inventory;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionPool {
	private static ComboPooledDataSource	pool	= null;

	public static void closePool() {
		if (pool != null) {
			pool.close();
		}
	}

	public static Connection getConnection() throws SQLException {
		if (pool != null) {
			return pool.getConnection();
		}

		return null;
	}

	public static ComboPooledDataSource getPool() {
		return pool;
	}

	public static void initializePool() {
		try {
			pool = new ComboPooledDataSource();
			pool.setDriverClass("com.mysql.jdbc.Driver");
			pool.setJdbcUrl("jdbc:mysql://sage.nsm.iup.edu:3306/inventory?zeroDateTimeBehavior=round");
			pool.setUser("app");
			pool.setPassword("inventory@pp");

			// the settings below are optional -- c3p0 can work with defaults
			pool.setMinPoolSize(5);
			pool.setAcquireIncrement(5);
			pool.setMaxPoolSize(15);
			// pool.setDebugUnreturnedConnectionStackTraces(true);
			pool.setUnreturnedConnectionTimeout(120);
		} catch (final PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // loads the jdbc driver

	}
}
