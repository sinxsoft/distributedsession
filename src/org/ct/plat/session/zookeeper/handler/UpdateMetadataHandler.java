package org.ct.plat.session.zookeeper.handler;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.utils.SerializationUtils;

public class UpdateMetadataHandler extends GetMetadataHandler {
	
	public UpdateMetadataHandler(String id) {
		super(id);
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			SessionMetaData metadata = (SessionMetaData) super.handle();
			if (metadata != null) {
				updateMetadata(metadata, this.zookeeper);
				return (T) metadata.getValidate();
			}
		}
		return (T) Boolean.FALSE;
	}

	protected void updateMetadata(SessionMetaData metadata, ZooKeeper zk) throws Exception {
		if (metadata != null) {
			String id = metadata.getId();
			Long now = Long.valueOf(System.currentTimeMillis());

			Long timeout = Long.valueOf(metadata.getLastAccessTm().longValue() + metadata.getMaxIdle().longValue());
			if (timeout.longValue() < now.longValue()) {
				metadata.setValidate(Boolean.valueOf(false));
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Session节点已超时[" + id + "]");
				}
			}
			metadata.setLastAccessTm(now);

			String path = "/SESSIONS/" + id;
			byte[] data = SerializationUtils.serialize(metadata);
			zk.setData(path, data, metadata.getVersion());
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("更新Session节点的元数据完成[" + path + "]");
			}
		}
	}
}
