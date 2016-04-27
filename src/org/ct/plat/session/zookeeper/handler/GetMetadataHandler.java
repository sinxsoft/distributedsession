package org.ct.plat.session.zookeeper.handler;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.session.zookeeper.AbstractZookeeperHandler;
import org.ct.plat.utils.SerializationUtils;

public class GetMetadataHandler
  extends AbstractZookeeperHandler
{
  public GetMetadataHandler(String id)
  {
    super(id);
  }
  
  public <T> T handle()
    throws Exception
  {
    if (this.zookeeper != null)
    {
      String path = "/SESSIONS/" + this.id;
      
      Stat stat = this.zookeeper.exists(path, false);
      if (stat == null) {
        return null;
      }
      byte[] data = this.zookeeper.getData(path, false, null);
      if (data != null)
      {
        Object obj = SerializationUtils.deserialize(data);
        if ((obj instanceof SessionMetaData))
        {
          SessionMetaData metadata = (SessionMetaData)obj;
          
          metadata.setVersion(stat.getVersion());
          return (T) metadata;
        }
      }
    }
    return (T)null;
  }
}
