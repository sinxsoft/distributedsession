package org.ct.plat.session.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

public class CookieHelper {
	private static final String DISTRIBUTED_SESSION_ID = "CT_JSESSIONID";
	protected static Logger log = Logger.getLogger(CookieHelper.class);

	public static Cookie writeSessionIdToNewCookie(String id, HttpServletResponse response, int expiry) {
		Cookie cookie = new Cookie(DISTRIBUTED_SESSION_ID, id);
		cookie.setMaxAge(expiry);
		response.addCookie(cookie);
		return cookie;
	}

	public static Cookie writeSessionIdToCookie(String id, HttpServletRequest request, HttpServletResponse response,
			int expiry) {
		Cookie cookie = findCookie(DISTRIBUTED_SESSION_ID, request);
		if (cookie == null) {
			return writeSessionIdToNewCookie(id, response, expiry);
		}
		cookie.setValue(id);
		cookie.setMaxAge(expiry);
		response.addCookie(cookie);

		return cookie;
	}

	public static String findCookieValue(String name, HttpServletRequest request) {
		Cookie cookie = findCookie(name, request);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}

	public static Cookie findCookie(String name, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		int i = 0;
		for (int n = cookies.length; i < n; i++) {
			if (cookies[i].getName().equalsIgnoreCase(name)) {
				return cookies[i];
			}
		}
		return null;
	}

	public static String findSessionId(HttpServletRequest request) {
		return findCookieValue(DISTRIBUTED_SESSION_ID, request);
	}
}
