package org.ct.plat.session.zookeeper;

public abstract interface ZooKeeperClient
{
	public abstract <T> T execute(ZookeeperHandler paramZookeeperHandler)
			throws Exception;
}
