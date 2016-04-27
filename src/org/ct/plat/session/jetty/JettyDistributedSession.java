package org.ct.plat.session.jetty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.AbstractSessionManager;
import org.mortbay.jetty.servlet.AbstractSessionManager.Session;
import org.ct.plat.session.SessionManager;
import org.ct.plat.session.zookeeper.DefaultZooKeeperClient;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.GetDataHandler;
import org.ct.plat.session.zookeeper.handler.PutDataHandler;
import org.ct.plat.session.zookeeper.handler.RemoveDataHandler;
import org.ct.plat.session.zookeeper.handler.RemoveNodeHandler;

public class JettyDistributedSession extends AbstractSessionManager.Session {
	private static final long serialVersionUID = -6089477971984554624L;
	private static final Logger LOGGER = Logger.getLogger(JettyDistributedSession.class);
	private SessionManager sessionManager;
	private ZooKeeperClient client = DefaultZooKeeperClient.getInstance();

	public JettyDistributedSession(AbstractSessionManager sessionManager, HttpServletRequest request,
			SessionManager sm) {
		sessionManager.super(request);
		this.sessionManager = sm;
	}

	public JettyDistributedSession(AbstractSessionManager sessionManager, long create, String id, SessionManager sm) {
		sessionManager.super(create, id);
		this.sessionManager = sm;
	}

	protected Map newAttributeMap() {
		return new HashMap(3);
	}

	public Object getAttribute(String name) {
		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			try {
				return this.client.execute(new GetDataHandler(id, name));
			} catch (Exception ex) {
				LOGGER.error("调用getAttribute方法时发生异常，", ex);
			}
		}
		return null;
	}

	public void removeAttribute(String name) {
		Object value = null;

		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			try {
				value = this.client.execute(new RemoveDataHandler(id, name));
			} catch (Exception ex) {
				LOGGER.error("调用removeAttribute方法时发生异常，", ex);
			}
		}
		fireHttpSessionUnbindEvent(name, value);
	}

	public void setAttribute(String name, Object value) {
		if (!(value instanceof Serializable)) {
			LOGGER.warn("对象[" + value + "]没有实现Serializable接口，无法保存到分布式Session中");
			return;
		}
		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			try {
				value = this.client.execute(new PutDataHandler(id, name, (Serializable) value));
			} catch (Exception ex) {
				LOGGER.error("调用setAttribute方法时发生异常，", ex);
			}
		}
		fireHttpSessionBindEvent(name, value);
	}

	public void invalidate() throws IllegalStateException {
		Map<String, Object> sessionMap;
		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			try {
				sessionMap = (Map) this.client.execute(new RemoveNodeHandler(id));
				if (sessionMap != null) {
					Set<String> keys = sessionMap.keySet();
					for (String key : keys) {
						Object value = sessionMap.get(key);
						fireHttpSessionUnbindEvent(key, value);
					}
				}
			} catch (Exception ex) {
				LOGGER.error("调用invalidate方法时发生异常，", ex);
			}
		}
		if (this.sessionManager != null) {
			this.sessionManager.removeHttpSession(this);
		}
	}

	protected void fireHttpSessionBindEvent(String name, Object value) {
		if ((value != null) && ((value instanceof HttpSessionBindingListener))) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
			((HttpSessionBindingListener) value).valueBound(event);
		}
	}

	protected void fireHttpSessionUnbindEvent(String name, Object value) {
		if ((value != null) && ((value instanceof HttpSessionBindingListener))) {
			HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
			((HttpSessionBindingListener) value).valueUnbound(event);
		}
	}
}
