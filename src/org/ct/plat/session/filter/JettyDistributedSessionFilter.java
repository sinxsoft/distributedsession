package org.ct.plat.session.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;
import org.ct.plat.session.jetty.JettyDistributedSessionManager;

public class JettyDistributedSessionFilter extends DistributedSessionFilter {
	private Logger log = Logger.getLogger(getClass());

	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		this.sessionManager = new JettyDistributedSessionManager(filterConfig.getServletContext());
		try {
			this.sessionManager.start();
			this.log.debug("DistributedSessionFilter.init completed.");
		} catch (Exception e) {
			this.log.error(e);
		}
	}
}
