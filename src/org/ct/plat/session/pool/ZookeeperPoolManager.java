package org.ct.plat.session.pool;

import java.util.NoSuchElementException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.ct.plat.session.config.Configuration;

public class ZookeeperPoolManager {
	private static final Logger LOGGER = Logger.getLogger(ZookeeperPoolManager.class);
	protected static ZookeeperPoolManager instance;
	private ObjectPool<ZooKeeper> pool;

	public static ZookeeperPoolManager getInstance() {
		if (instance == null) {
			instance = new ZookeeperPoolManager();
		}
		return instance;
	}

	public void init(Configuration config) {
		PoolableObjectFactory<ZooKeeper> factory = new ZookeeperPoolableObjectFactory(config);

		int maxIdle = NumberUtils.toInt(config.getString("maxIdle"));
		int initIdleCapacity = NumberUtils.toInt(config.getString("initIdleCapacity"));

		this.pool = new StackObjectPool(factory, maxIdle, initIdleCapacity);
	}

	public ZooKeeper borrowObject() {
		if (this.pool != null) {
			try {
				ZooKeeper zk = (ZooKeeper) this.pool.borrowObject();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("从ZK对象池中返回实例，zk.sessionId=" + zk.getSessionId());
				}
				return zk;
			} catch (NoSuchElementException ex) {
				LOGGER.error("出借ZK池化实例时发生异常：", ex);
			} catch (IllegalStateException ex) {
				LOGGER.error("出借ZK池化实例时发生异常：", ex);
			} catch (Exception e) {
				LOGGER.error("出借ZK池化实例时发生异常：", e);
			}
		}
		return null;
	}

	public void returnObject(ZooKeeper zk) {
		if ((this.pool != null) && (zk != null)) {
			try {
				this.pool.returnObject(zk);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("将ZK实例返回对象池中，zk.sessionId=" + zk.getSessionId());
				}
			} catch (Exception ex) {
				LOGGER.error("返回ZK池化实例时发生异常：", ex);
			}
		}
	}

	public void close() {
		if (this.pool != null) {
			try {
				this.pool.close();
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("关闭ZK对象池完成");
				}
			} catch (Exception ex) {
				LOGGER.error("关闭ZK对象池时发生异常：", ex);
			}
		}
	}
}

