package org.ct.plat.session.helper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ConnectionWatcher implements Watcher {
	
	private static final int SESSION_TIMEOUT = 5000;
	
	private CountDownLatch signal = new CountDownLatch(1);
	
	private Logger log = Logger.getLogger(getClass());

	public ZooKeeper connection(String servers) {
		return connection(servers, SESSION_TIMEOUT);
	}

	public ZooKeeper connection(String servers, int sessionTimeout) {
		try {
			ZooKeeper zk = new ZooKeeper(servers, sessionTimeout, this);
			this.signal.await();
			return zk;
		} catch (IOException e) {
			this.log.error(e);
		} catch (InterruptedException e) {
			this.log.error(e);
		}
		return null;
	}

	public void process(WatchedEvent event) {
		Watcher.Event.KeeperState state = event.getState();
		if (state == Watcher.Event.KeeperState.SyncConnected) {
			this.signal.countDown();
		}
	}
}
