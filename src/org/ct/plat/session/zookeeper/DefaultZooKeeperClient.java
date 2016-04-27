package org.ct.plat.session.zookeeper;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.ct.plat.session.pool.ZookeeperPoolManager;

public class DefaultZooKeeperClient implements ZooKeeperClient {
	private static final Logger LOGGER = Logger.getLogger(ZooKeeperClient.class);
	private static ZooKeeperClient instance;
	private ZookeeperPoolManager pool;

	protected DefaultZooKeeperClient() {
		if (this.pool == null) {
			this.pool = ZookeeperPoolManager.getInstance();
		}
	}

	public static ZooKeeperClient getInstance() {
		if (instance == null) {
			instance = new DefaultZooKeeperClient();
		}
		return instance;
	}

	public <T> T execute(ZookeeperHandler handler) throws Exception {
		ZooKeeper zk = this.pool.borrowObject();
		if (zk != null) {
			try {
				handler.setZooKeeper(zk);
				return (T) handler.handle();
			} catch (KeeperException ex) {
				LOGGER.error("执行ZK节点操作时发生异常: ", ex);
			} catch (InterruptedException ex) {
				LOGGER.error("执行ZK节点操作时发生异常: ", ex);
			} finally {
				this.pool.returnObject(zk);
			}
		}
		return (T) null;
	}
}

