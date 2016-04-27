package org.ct.plat.session.pool;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.ct.plat.session.config.Configuration;
import org.ct.plat.session.helper.ConnectionWatcher;

public class ZookeeperPoolableObjectFactory implements PoolableObjectFactory<ZooKeeper> {

	private static final Logger LOGGER = Logger.getLogger(ZookeeperPoolableObjectFactory.class);

	private Configuration config;

	public ZookeeperPoolableObjectFactory(Configuration config) {
		this.config = config;
	}

	public ZooKeeper makeObject() throws Exception {
		ConnectionWatcher cw = new ConnectionWatcher();

		String servers = this.config.getString("servers");
		int timeout = NumberUtils.toInt(this.config.getString("timeout"));
		ZooKeeper zk = cw.connection(servers, timeout);
		if (zk != null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("实例化ZK客户端对象，zk.sessionId=" + zk.getSessionId());
			}
		} else {
			LOGGER.warn("实例化ZK客户端对象失败");
		}
		return zk;
	}

	public void destroyObject(ZooKeeper obj) throws Exception {
		if (obj != null) {
			obj.close();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("ZK客户端对象被关闭，zk.sessionId=" + obj.getSessionId());
			}
		}
	}

	public boolean validateObject(ZooKeeper obj) {
		if ((obj != null) && (obj.getState() == ZooKeeper.States.CONNECTED)) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("ZK客户端对象验证通过，zk.sessionId=" + obj.getSessionId());
			}
			return true;
		}
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ZK客户端对象验证不通过，zk.sessionId=" + obj.getSessionId());
		}
		return false;
	}

	public void activateObject(ZooKeeper obj) throws Exception {
	}

	public void passivateObject(ZooKeeper obj) throws Exception {
	}
}
