package org.ct.plat.session.zookeeper.handler;

import org.apache.zookeeper.ZooKeeper;
import org.ct.plat.session.metadata.SessionMetaData;

public class GetNodeNames extends GetMetadataHandler {
	public GetNodeNames(String id) {
		super(id);
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			String path = "/SESSIONS/" + this.id;

			SessionMetaData metadata = (SessionMetaData) super.handle();
			if ((metadata == null) || (!metadata.getValidate().booleanValue())) {
				return null;
			}
			return (T) this.zookeeper.getChildren(path, false);
		}
		return (T) null;
	}
}
