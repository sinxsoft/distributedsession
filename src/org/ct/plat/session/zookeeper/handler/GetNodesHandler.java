package org.ct.plat.session.zookeeper.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.ct.plat.session.metadata.SessionMetaData;
import org.ct.plat.utils.SerializationUtils;

public class GetNodesHandler
  extends GetMetadataHandler
{
  public GetNodesHandler(String id)
  {
    super(id);
  }
  
  public <T> T handle()
    throws Exception
  {
    Map<String, Object> nodeMap = new HashMap();
    String path;
    if (this.zookeeper != null)
    {
      path = "/SESSIONS/" + this.id;
      
      SessionMetaData metadata = (SessionMetaData)super.handle();
      if ((metadata == null) || (!metadata.getValidate().booleanValue())) {
        return null;
      }
      List<String> nodes = this.zookeeper.getChildren(path, false);
      for (String node : nodes)
      {
        String dataPath = path + "/" + node;
        Stat stat = this.zookeeper.exists(dataPath, false);
        if (stat != null)
        {
          byte[] data = this.zookeeper.getData(dataPath, false, null);
          if (data != null) {
            nodeMap.put(node, SerializationUtils.deserialize(data));
          } else {
            nodeMap.put(node, null);
          }
        }
      }
    }
    return (T) nodeMap;
  }
}
