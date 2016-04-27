package org.ct.plat.session;

import javax.servlet.http.HttpServletRequest;
import org.ct.plat.component.LifeCycle;

public abstract interface SessionIdManager  extends LifeCycle
{
	public abstract String newSessionId(HttpServletRequest paramHttpServletRequest, long paramLong);
}
