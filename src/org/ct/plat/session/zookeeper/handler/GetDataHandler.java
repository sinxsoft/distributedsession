package org.ct.plat.session.zookeeper.handler;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.session.zookeeper.AbstractZookeeperHandler;
import org.ct.plat.utils.SerializationUtils;

public class GetDataHandler extends AbstractZookeeperHandler {
	protected String key;

	protected GetDataHandler(String id) {
		super(id);
	}

	public GetDataHandler(String id, String key) {
		this(id);
		this.key = key;
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			String path = "/SESSIONS/" + this.id;

			Stat stat = this.zookeeper.exists(path, false);
			if (stat != null) {
				String dataPath = path + "/" + this.key;
				stat = this.zookeeper.exists(dataPath, false);
				Object obj = null;
				if (stat != null) {
					byte[] data = this.zookeeper.getData(dataPath, false, null);
					if (data != null) {
						obj = SerializationUtils.deserialize(data);
					}
				}
				return (T) obj;
			}
		}
		return (T) null;
	}
}
