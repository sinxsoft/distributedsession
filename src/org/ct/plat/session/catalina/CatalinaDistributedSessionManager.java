package org.ct.plat.session.catalina;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.ct.plat.session.DefaultSessionManager;
import org.ct.plat.session.config.Configuration;
import org.ct.plat.session.helper.CookieHelper;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.CreateNodeHandler;
import org.ct.plat.session.zookeeper.handler.RemoveNodeHandler;
import org.ct.plat.session.zookeeper.handler.UpdateMetadataHandler;

public class CatalinaDistributedSessionManager extends DefaultSessionManager {
	public CatalinaDistributedSessionManager(ServletContext sc) {
		super(sc);
	}

	public HttpSession getHttpSession(String id, HttpServletRequest request) {
		HttpSession session = (HttpSession) this.sessions.get(id);

		Boolean valid = Boolean.FALSE;
		try {
			valid = (Boolean) this.client.execute(new UpdateMetadataHandler(id));
		} catch (Exception ex) {
			LOGGER.error("更新节点元数据时发生异常，", ex);
		}
		if (!valid.booleanValue()) {
			if (session != null) {
				session.invalidate();
			} else {
				try {
					this.client.execute(new RemoveNodeHandler(id));
				} catch (Exception ex) {
					LOGGER.error("删除节点元数据时发生异常，", ex);
				}
			}
			return null;
		}
		if (session != null) {
			return session;
		}
		CatalinaDistributedSession sess = new CatalinaDistributedSession(this, id);
		sess.access();
		session = new CatalinaDistributedSessionFacade(sess);
		addHttpSession(session);
		return session;
	}

	public HttpSession newHttpSession(HttpServletRequest request) {
		String id = getNewSessionId(request);
		CatalinaDistributedSession sess = new CatalinaDistributedSession(this, id);
		HttpSession session = new CatalinaDistributedSessionFacade(sess);

		Cookie cookie = CookieHelper.writeSessionIdToCookie(id, request, getResponse(), 31536000);
		if ((cookie != null) && (LOGGER.isInfoEnabled())) {
			LOGGER.info("Wrote sid to Cookie,name:[" + cookie.getName() + "],value:[" + cookie.getValue() + "]");
		}
		SessionMetaData metadata = new SessionMetaData();
		metadata.setId(id);
		Long sessionTimeout = Long.valueOf(NumberUtils.toLong(this.config.getString("sessionTimeout")));
		metadata.setMaxIdle(Long.valueOf(sessionTimeout.longValue() * 60L * 1000L));
		try {
			this.client.execute(new CreateNodeHandler(id, metadata));
		} catch (Exception ex) {
			LOGGER.error("创建节点时发生异常，", ex);
		}
		addHttpSession(session);
		return session;
	}
}
