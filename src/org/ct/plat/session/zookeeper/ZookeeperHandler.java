package org.ct.plat.session.zookeeper;

import org.apache.zookeeper.ZooKeeper;

public abstract interface ZookeeperHandler
{
  public static final String GROUP_NAME = "/SESSIONS";
  public static final String NODE_SEP = "/";
  
  public abstract <T> T handle()
    throws Exception;
  
  public abstract void setZooKeeper(ZooKeeper paramZooKeeper);
}
