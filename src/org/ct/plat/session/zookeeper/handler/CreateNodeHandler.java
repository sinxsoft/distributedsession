package org.ct.plat.session.zookeeper.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.session.zookeeper.AbstractZookeeperHandler;
import org.ct.plat.utils.SerializationUtils;

public class CreateNodeHandler extends AbstractZookeeperHandler {
	private SessionMetaData metadata;

	public CreateNodeHandler(String id, SessionMetaData metadata) {
		super(id);
		this.metadata = metadata;
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			String path = this.id;
			if (!StringUtils.startsWithIgnoreCase(this.id, "/SESSIONS")) {
				path = "/SESSIONS/" + this.id;
			}
			Stat stat = this.zookeeper.exists(path, false);
			if (stat == null) {
				byte[] arrData = null;
				if (this.metadata != null) {
					arrData = SerializationUtils.serialize(this.metadata);
				}
				String createPath = this.zookeeper.create(path, arrData, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("创建节点完成:[" + createPath + "]");
				}
			} else if (LOGGER.isInfoEnabled()) {
				LOGGER.info("组节点已存在，无需创建[" + path + "]");
			}
		}
		return (T) null;
	}
}

/*
 * Location:
 * D:\googledownload\www.dssz.com_distributed-session-core-1.0.0.jar!\org\
 * storevm\toolkits\session\zookeeper\handler\CreateNodeHandler.class Java
 * compiler version: 6 (50.0) JD-Core Version: 0.7.1
 */