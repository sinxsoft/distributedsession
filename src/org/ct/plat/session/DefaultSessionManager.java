package org.ct.plat.session;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.ct.plat.session.config.Configuration;
import org.ct.plat.session.helper.CookieHelper;
import org.ct.plat.session.zookeeper.DefaultZooKeeperClient;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.TimeoutHandler;

public abstract class DefaultSessionManager
  implements SessionManager
{
  protected static final Logger LOGGER = Logger.getLogger(DefaultSessionManager.class);
  protected Map<String, HttpSession> sessions;
  protected ExecutorService executor;
  protected Configuration config;
  private boolean started = false;
  private boolean stopped = false;
  private SessionIdManager sessionIdManager;
  private ServletContext sc;
  private HttpServletResponse response;
  protected ZooKeeperClient client = DefaultZooKeeperClient.getInstance();
  
  public DefaultSessionManager(ServletContext sc)
  {
    this.sc = sc;
    this.config = ((Configuration)sc.getAttribute(".cfg.properties"));
  }
  
  public void start()
    throws Exception
  {
    if (!isStarted())
    {
      if (this.sessions == null) {
        this.sessions = new ConcurrentHashMap();
      }
      if (this.sessionIdManager == null)
      {
        this.sessionIdManager = new DefaultSessionIdManager();
        this.sessionIdManager.start();
      }
      int poolSize = NumberUtils.toInt(this.config.getString("poolSize"));
      this.executor = Executors.newFixedThreadPool(poolSize);
      this.started = true;
    }
  }
  
  public void stop()
    throws Exception
  {
    if (!isStopped())
    {
      if (this.sessions != null)
      {
        for (HttpSession s : this.sessions.values()) {
          s.invalidate();
        }
        this.sessions.clear();
      }
      this.sessions = null;
      if (this.sessionIdManager != null) {
        this.sessionIdManager.stop();
      }
      this.executor.shutdown();
      try
      {
        if (!this.executor.awaitTermination(60L, TimeUnit.MILLISECONDS))
        {
          this.executor.shutdownNow();
          if (!this.executor.awaitTermination(60L, TimeUnit.MILLISECONDS)) {
            System.err.println("Pool did not terminate");
          }
        }
      }
      catch (InterruptedException ie)
      {
        this.executor.shutdownNow();
        
        Thread.currentThread().interrupt();
      }
      finally
      {
        this.stopped = true;
      }
    }
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  
  public boolean isStopped()
  {
    return this.stopped;
  }
  
  public ServletContext getServletContext()
  {
    return this.sc;
  }
  
  public void setServletContext(ServletContext sc)
  {
    this.sc = sc;
  }
  
  public HttpServletResponse getResponse()
  {
    return this.response;
  }
  
  public void setHttpServletResponse(HttpServletResponse response)
  {
    this.response = response;
  }
  
  public String getNewSessionId(HttpServletRequest request)
  {
    if (this.sessionIdManager != null) {
      return this.sessionIdManager.newSessionId(request, System.currentTimeMillis());
    }
    return null;
  }
  
  public String getRequestSessionId(HttpServletRequest request)
  {
    return CookieHelper.findSessionId(request);
  }
  
  public void removeHttpSession(HttpSession session)
  {
    if (session != null)
    {
      String id = session.getId();
      if (StringUtils.isNotBlank(id)) {
        this.sessions.remove(id);
      }
    }
  }
  
  public void addHttpSession(HttpSession session)
  {
    if (session == null) {
      return;
    }
    String id = session.getId();
    if (!this.sessions.containsKey(id))
    {
      this.sessions.put(id, session);
      
      this.executor.submit(new checkSessionTimeoutTask(session));
    }
  }
  
  protected class checkSessionTimeoutTask
    implements Callable<Boolean>
  {
    private HttpSession session;
    private static final long SLEEP_TIMEOUT = 10L;
    
    public checkSessionTimeoutTask(HttpSession session)
    {
      this.session = session;
    }
    
    public Boolean call()
      throws Exception
    {
      if (this.session == null) {
        return Boolean.valueOf(false);
      }
      boolean running = true;
      while (running) {
        try
        {
          Boolean timeout = (Boolean)DefaultSessionManager.this.client.execute(new TimeoutHandler(this.session.getId()));
          if (timeout.booleanValue())
          {
            this.session.invalidate();
            
            break;
          }
          TimeUnit.SECONDS.sleep(10L);
        }
        catch (Exception ex)
        {
          DefaultSessionManager.LOGGER.error("Session超时定时任务发生异常，", ex);
        }
      }
      return Boolean.valueOf(true);
    }
  }
}
