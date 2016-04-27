package org.ct.plat.session.jetty;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.AbstractSessionManager.Session;
import org.ct.plat.session.DefaultSessionManager;
import org.ct.plat.session.config.Configuration;
import org.ct.plat.session.helper.CookieHelper;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.CreateNodeHandler;
import org.ct.plat.session.zookeeper.handler.RemoveNodeHandler;
import org.ct.plat.session.zookeeper.handler.UpdateMetadataHandler;

public class JettyDistributedSessionManager extends DefaultSessionManager {
	public JettyDistributedSessionManager(ServletContext sc) {
		super(sc);
	}

	public HttpSession getHttpSession(String id, HttpServletRequest request) {
		if (!(request instanceof Request)) {
			LOGGER.warn("不是Jetty容器下的Request对象");
			return null;
		}
		Request req = (Request) request;
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
		session = new JettyDistributedSession((AbstractSessionManager) req.getSessionManager(),
				System.currentTimeMillis(), id, this);

		addHttpSession(session);
		return session;
	}

	public HttpSession newHttpSession(HttpServletRequest request) {
		if (!(request instanceof Request)) {
			LOGGER.warn("不是Jetty容器下的Request对象");
			return null;
		}
		Request req = (Request) request;
		AbstractSessionManager.Session session = new JettyDistributedSession(
				(AbstractSessionManager) req.getSessionManager(), request, this);

		String id = session.getId();

		Cookie cookie = CookieHelper.writeSessionIdToCookie(id, req, req.getConnection().getResponse(), 31536000);
		if ((cookie != null) && (LOGGER.isInfoEnabled())) {
			LOGGER.info("Wrote sid to Cookie,name:[" + cookie.getName() + "],value:[" + cookie.getValue() + "]");
		}
		SessionMetaData metadata = new SessionMetaData();
		metadata.setId(id);
		long sessionTimeout = NumberUtils.toLong(this.config.getString("sessionTimeout")) * 60L * 1000L;
		metadata.setMaxIdle(Long.valueOf(sessionTimeout));
		try {
			this.client.execute(new CreateNodeHandler(id, metadata));
		} catch (Exception ex) {
			LOGGER.error("创建节点时发生异常，", ex);
		}
		addHttpSession(session);
		return session;
	}
}
