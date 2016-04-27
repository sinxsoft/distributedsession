package org.ct.plat.session.catalina;

import javax.servlet.http.HttpSession;
import org.apache.catalina.session.StandardSessionFacade;

public class CatalinaDistributedSessionFacade extends StandardSessionFacade {
	public CatalinaDistributedSessionFacade(HttpSession session) {
		super(session);
	}
}
