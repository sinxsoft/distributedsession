package org.ct.plat.session.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;
import org.ct.plat.session.catalina.CatalinaDistributedSessionManager;

public class CatalinaDistributedSessionFilter extends DistributedSessionFilter {
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		this.sessionManager = new CatalinaDistributedSessionManager(filterConfig.getServletContext());
		try {
			this.sessionManager.start();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("DistributedSessionFilter.init completed.");
			}
		} catch (Exception ex) {
			LOGGER.error("过滤器初始化失败，", ex);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		this.sessionManager.setHttpServletResponse((HttpServletResponse) response);
		super.doFilter(request, response, chain);
	}
}
