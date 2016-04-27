package org.ct.plat.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.ct.plat.component.LifeCycle;

public abstract interface SessionManager extends LifeCycle {
	
	public static final int COOKIE_EXPIRY = 31536000;

	public abstract HttpSession getHttpSession(String paramString, HttpServletRequest paramHttpServletRequest);

	public abstract HttpSession newHttpSession(HttpServletRequest paramHttpServletRequest);

	public abstract String getRequestSessionId(HttpServletRequest paramHttpServletRequest);

	public abstract void addHttpSession(HttpSession paramHttpSession);

	public abstract void removeHttpSession(HttpSession paramHttpSession);

	public abstract String getNewSessionId(HttpServletRequest paramHttpServletRequest);

	public abstract ServletContext getServletContext();

	public abstract void setServletContext(ServletContext paramServletContext);

	public abstract HttpServletResponse getResponse();

	public abstract void setHttpServletResponse(HttpServletResponse paramHttpServletResponse);
}
