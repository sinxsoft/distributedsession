package org.ct.plat.session.servlet;

import java.security.Principal;
import java.util.Enumeration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;

/**
 * 从ServletRequestWrapper包装扩展开来。
 * @author xiaxilin
 *
 */
public class ContainerRequestWrapper extends ServletRequestWrapper implements HttpServletRequest {
	protected Logger log = Logger.getLogger(getClass());
	private SessionManager sessionManager;
	private HttpSession session;

	public ContainerRequestWrapper(ServletRequest request, SessionManager sessionManager) {
		super(request);
		this.sessionManager = sessionManager;
	}

	private HttpServletRequest getHttpServletRequest() {
		return (HttpServletRequest) super.getRequest();
	}

	/*
	 * 实现getSession方法
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		HttpServletRequest request = (HttpServletRequest) getRequest();
		if ((this.sessionManager == null) && (create)) {
			throw new IllegalStateException("No SessionHandler or SessionManager");
		}
		if ((this.session != null) && (this.sessionManager != null)) {
			return this.session;
		}
		this.session = null;

		String id = this.sessionManager.getRequestSessionId(request);
		this.log.debug("获取客户端的Session ID:[" + id + "]");
		if ((id != null) && (this.sessionManager != null)) {
			this.session = this.sessionManager.getHttpSession(id, request);
			if ((this.session == null) && (!create)) {
				return null;
			}
		}
		if ((this.session == null) && (this.sessionManager != null) && (create)) {
			this.session = this.sessionManager.newHttpSession(request);
		}
		return this.session;
	}

	public String getAuthType() {
		return getHttpServletRequest().getAuthType();
	}

	public Cookie[] getCookies() {
		return getHttpServletRequest().getCookies();
	}

	public long getDateHeader(String name) {
		return getHttpServletRequest().getDateHeader(name);
	}

	public String getHeader(String name) {
		return getHttpServletRequest().getHeader(name);
	}

	public Enumeration getHeaders(String name) {
		return getHttpServletRequest().getHeaders(name);
	}

	public Enumeration getHeaderNames() {
		return getHttpServletRequest().getHeaderNames();
	}

	public int getIntHeader(String name) {
		return getHttpServletRequest().getIntHeader(name);
	}

	public String getMethod() {
		return getHttpServletRequest().getMethod();
	}

	public String getPathInfo() {
		return getHttpServletRequest().getPathInfo();
	}

	public String getPathTranslated() {
		return getHttpServletRequest().getPathTranslated();
	}

	public String getContextPath() {
		return getHttpServletRequest().getContextPath();
	}

	public String getQueryString() {
		return getHttpServletRequest().getQueryString();
	}

	public String getRemoteUser() {
		return getHttpServletRequest().getRemoteUser();
	}

	public boolean isUserInRole(String role) {
		return getHttpServletRequest().isUserInRole(role);
	}

	public Principal getUserPrincipal() {
		return getHttpServletRequest().getUserPrincipal();
	}

	public String getRequestedSessionId() {
		return getHttpServletRequest().getRequestedSessionId();
	}

	public String getRequestURI() {
		return getHttpServletRequest().getRequestURI();
	}

	public StringBuffer getRequestURL() {
		return getHttpServletRequest().getRequestURL();
	}

	public String getServletPath() {
		return getHttpServletRequest().getServletPath();
	}

	public HttpSession getSession() {
		return getSession(true);
	}

	public boolean isRequestedSessionIdValid() {
		return getHttpServletRequest().isRequestedSessionIdValid();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return getHttpServletRequest().isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return getHttpServletRequest().isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdFromUrl() {
		return getHttpServletRequest().isRequestedSessionIdFromUrl();
	}
}
