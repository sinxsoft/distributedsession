package org.ct.plat.session.zookeeper.handler;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.utils.SerializationUtils;

public class RemoveDataHandler extends GetDataHandler {
	
	public RemoveDataHandler(String id, String key) {
		super(id, key);
	}

	public <T> T handle() throws Exception {
		Object value = null;
		if (this.zookeeper != null) {
			String path = "/SESSIONS/" + this.id;

			Stat stat = this.zookeeper.exists(path, false);
			if (stat != null) {
				String dataPath = path + "/" + this.key;
				stat = this.zookeeper.exists(dataPath, false);
				if (stat != null) {
					byte[] data = this.zookeeper.getData(dataPath, false, null);
					if (data != null) {
						value = SerializationUtils.deserialize(data);
					}
					this.zookeeper.delete(dataPath, -1);
				}
			}
		}
		return (T) value;
	}
}
