package org.ct.plat.session.filter;

import javax.servlet.Filter;

import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;

public abstract class BaseSessionFilter implements Filter {

	protected static final Logger LOGGER = Logger.getLogger(DistributedSessionFilter.class);
	
	protected SessionManager sessionManager;
	
}
