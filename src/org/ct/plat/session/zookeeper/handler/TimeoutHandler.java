package org.ct.plat.session.zookeeper.handler;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.utils.SerializationUtils;

public class TimeoutHandler extends GetMetadataHandler {
	
	public TimeoutHandler(String id) {
		super(id);
	}

	public <T> T handle() throws Exception {
		if (this.zookeeper != null) {
			String path = "/SESSIONS/" + this.id;

			SessionMetaData metadata = (SessionMetaData) super.handle();
			if (metadata == null) {
				return (T) Boolean.TRUE;
			}
			if (!metadata.getValidate().booleanValue()) {
				return (T) Boolean.TRUE;
			}
			Long now = Long.valueOf(System.currentTimeMillis());

			Long timeout = Long.valueOf(metadata.getLastAccessTm().longValue() + metadata.getMaxIdle().longValue());
			if (timeout.longValue() < now.longValue()) {
				metadata.setValidate(Boolean.valueOf(false));

				byte[] data = SerializationUtils.serialize(metadata);
				this.zookeeper.setData(path, data, metadata.getVersion());
			}
			String timeoutStr = DateFormatUtils.format(timeout.longValue(), "yyyy-MM-dd HH:mm");
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("session超时检查:[" + timeoutStr + "]");
			}
		}
		return (T) Boolean.FALSE;
	}
}
