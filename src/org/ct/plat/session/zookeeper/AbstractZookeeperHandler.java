package org.ct.plat.session.zookeeper;

import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;

public abstract class AbstractZookeeperHandler  implements ZookeeperHandler
{
	  protected static final Logger LOGGER = Logger.getLogger(ZookeeperHandler.class);
	  protected ZooKeeper zookeeper;
	  protected String id;
	  
	  public AbstractZookeeperHandler(String id)
	  {
		  this.id = id;
	  }
	  
	  public void setZooKeeper(ZooKeeper zookeeper)
	  {
		  this.zookeeper = zookeeper;
	  }
}
