package org.ct.plat.session.zookeeper.handler;

public class CreateGroupNodeHandler
  extends CreateNodeHandler
{
  public CreateGroupNodeHandler()
  {
    this("/SESSIONS");
  }
  
  protected CreateGroupNodeHandler(String id)
  {
    super(id, null);
  }
}
