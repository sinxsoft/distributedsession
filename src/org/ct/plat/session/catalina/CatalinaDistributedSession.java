package org.ct.plat.session.catalina;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;
import org.ct.plat.session.zookeeper.DefaultZooKeeperClient;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.GetDataHandler;
import org.ct.plat.session.zookeeper.handler.GetNodeNames;
import org.ct.plat.session.zookeeper.handler.PutDataHandler;
import org.ct.plat.session.zookeeper.handler.RemoveDataHandler;
import org.ct.plat.session.zookeeper.handler.RemoveNodeHandler;

public class CatalinaDistributedSession implements HttpSession {
	private static final Logger LOGGER = Logger.getLogger(CatalinaDistributedSession.class);
	private SessionManager sessionManager;
	private String id;
	private long creationTm;
	private long lastAccessedTm;
	private int maxInactiveInterval;
	private boolean newSession;
	private ZooKeeperClient client = DefaultZooKeeperClient.getInstance();

	public CatalinaDistributedSession(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
		this.creationTm = System.currentTimeMillis();
		this.lastAccessedTm = this.creationTm;
		this.newSession = true;
	}

	public CatalinaDistributedSession(SessionManager sessionManager, String id) {
		this(sessionManager);
		this.id = id;
	}

	public long getCreationTime() {
		return this.creationTm;
	}

	public String getId() {
		return this.id;
	}

	public long getLastAccessedTime() {
		return this.lastAccessedTm;
	}

	public ServletContext getServletContext() {
		return this.sessionManager.getServletContext();
	}

	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
	}

	public int getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	@Deprecated
	public HttpSessionContext getSessionContext() {
		return null;
	}

	public Object getAttribute(String name) {
		access();

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

	public Object getValue(String name) {
		return getAttribute(name);
	}

	public Enumeration getAttributeNames() {
		access();

		String id = getId();
		if (StringUtils.isNotBlank(id)) {
			try {
				List<String> names = (List) this.client.execute(new GetNodeNames(id));
				if (names != null) {
					return Collections.enumeration(names);
				}
			} catch (Exception ex) {
				LOGGER.error("调用getAttributeNames方法时发生异常，", ex);
			}
		}
		return null;
	}

	public String[] getValueNames() {
		List<String> names = new ArrayList();
		Enumeration n = getAttributeNames();
		while (n.hasMoreElements()) {
			names.add((String) n.nextElement());
		}
		return (String[]) names.toArray(new String[0]);
	}

	public void setAttribute(String name, Object value) {
		if (!(value instanceof Serializable)) {
			LOGGER.warn("对象[" + value + "]没有实现Serializable接口，无法保存到分布式Session中");
			return;
		}
		access();

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

	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	public void removeAttribute(String name) {
		access();
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

	public void removeValue(String name) {
		removeAttribute(name);
	}

	public void invalidate() {
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
		this.sessionManager.removeHttpSession(this);
	}

	public boolean isNew() {
		return this.newSession;
	}

	public void access() {
		this.newSession = false;
		this.lastAccessedTm = System.currentTimeMillis();
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
