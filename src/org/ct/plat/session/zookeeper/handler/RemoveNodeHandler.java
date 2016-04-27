package org.ct.plat.session.zookeeper.handler;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.session.zookeeper.AbstractZookeeperHandler;
import org.ct.plat.utils.SerializationUtils;

public class RemoveNodeHandler
  extends AbstractZookeeperHandler
{
  public RemoveNodeHandler(String id)
  {
    super(id);
  }
  
  public <T> T handle()
    throws Exception
  {
    Map<String, Serializable> datas = new HashMap();
    if (this.zookeeper != null)
    {
      String path = "/SESSIONS/" + this.id;
      
      Stat stat = this.zookeeper.exists(path, false);
      if (stat != null)
      {
        List<String> nodes = this.zookeeper.getChildren(path, false);
        if (nodes != null) {
          for (String node : nodes)
          {
            String dataPath = path + "/" + node;
            
            byte[] data = this.zookeeper.getData(dataPath, false, null);
            if (data != null)
            {
              Object obj = SerializationUtils.deserialize(data);
              datas.put(node, (Serializable)obj);
            }
            this.zookeeper.delete(dataPath, -1);
          }
        }
        this.zookeeper.delete(path, -1);
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("删除Session节点完成:[" + path + "]");
        }
      }
    }
    return (T) datas;
  }
}
