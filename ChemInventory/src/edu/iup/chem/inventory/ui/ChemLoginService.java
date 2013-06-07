package edu.iup.chem.inventory.ui;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.auth.LoginService;

import edu.iup.chem.inventory.Constants;
import edu.iup.chem.inventory.dao.UserDao;
import edu.iup.chem.inventory.db.inventory.tables.records.UserRecord;

public class ChemLoginService extends LoginService {

	private static UserRecord	user	= null;
	private static final Logger	LOG		= Logger.getLogger(ChemLoginService.class);

	@Override
	public boolean authenticate(final String name, final char[] password,
			final String server) throws Exception {

		user = UserDao.getByUsernamePassword(name, password);

		if (user != null) {
			Constants.CURRENT_USER = user;
			LOG.debug("New user (" + user.getName() + ") logged in with role "
					+ user.getRoleName());
			return true;
		} else {
			LOG.debug("Fetched user is null.");
		}

		return false;
	}

	@Override
	public String[] getUserRoles() {
		if (user == null) {
			return null;
		}

		return new String[] { user.getRoleName() };
	}

}
