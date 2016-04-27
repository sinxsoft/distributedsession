package org.ct.plat.session.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.ct.plat.session.SessionManager;
import org.ct.plat.session.config.Configuration;
import org.ct.plat.session.pool.ZookeeperPoolManager;
import org.ct.plat.session.servlet.ContainerRequestWrapper;
import org.ct.plat.session.zookeeper.DefaultZooKeeperClient;
import org.ct.plat.session.zookeeper.ZooKeeperClient;
import org.ct.plat.session.zookeeper.handler.CreateGroupNodeHandler;

public abstract class DistributedSessionFilter extends BaseSessionFilter {
	
	protected static final Logger LOGGER = Logger.getLogger(DistributedSessionFilter.class);

	protected ZooKeeperClient client = DefaultZooKeeperClient.getInstance();

	public void init(FilterConfig filterConfig) throws ServletException {
		Configuration conf = Configuration.getInstance();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("1. 读取系统配置属性成功，" + conf);
		}
		ServletContext sc = filterConfig.getServletContext();
		sc.setAttribute(".cfg.properties", conf);

		ZookeeperPoolManager.getInstance().init(conf);
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("2. 初始化ZK客户端对象池完成");
		}
		try {
			this.client.execute(new CreateGroupNodeHandler());
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("3. 创建SESSIONS组节点完成");
			}
		} catch (Exception ex) {
			LOGGER.error("创建组节点时发生异常，", ex);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = new ContainerRequestWrapper(request, this.sessionManager);
		chain.doFilter(req, response);
	}

	public void destroy() {
		if (this.sessionManager != null) {
			try {
				this.sessionManager.stop();
			} catch (Exception ex) {
				LOGGER.error("关闭Session管理器时发生异常，", ex);
			}
		}
		ZookeeperPoolManager.getInstance().close();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("DistributedSessionFilter.destroy completed.");
		}
	}
}
