package org.ct.plat.session.zookeeper.handler;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.utils.SerializationUtils;

public class PutDataHandler extends GetDataHandler {
	private Serializable data;

	public PutDataHandler(String id, String key, Serializable data) {
		super(id, key);
		this.data = data;
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			String path = "/SESSIONS/" + this.id;

			Stat stat = this.zookeeper.exists(path, false);
			if (stat != null) {
				String dataPath = path + "/" + this.key;
				stat = this.zookeeper.exists(dataPath, false);
				if (stat == null) {
					this.zookeeper.create(dataPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("创建数据节点完成[" + dataPath + "]");
					}
				}
				if ((this.data instanceof Serializable)) {
					int dataNodeVer = -1;
					if (stat != null) {
						dataNodeVer = stat.getVersion();
					}
					byte[] arrData = SerializationUtils.serialize(this.data);
					stat = this.zookeeper.setData(dataPath, arrData, dataNodeVer);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("更新数据节点数据完成[" + dataPath + "][" + this.data + "]");
					}
					return (T) Boolean.TRUE;
				}
			}
		}
		return (T) Boolean.FALSE;
	}
}
