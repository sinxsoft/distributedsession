package org.ct.plat.session.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Configuration {
	public static final String SERVERS = "servers";
	public static final String MAX_IDLE = "maxIdle";
	public static final String INIT_IDLE_CAPACITY = "initIdleCapacity";
	public static final String SESSION_TIMEOUT = "sessionTimeout";
	public static final String TIMEOUT = "timeout";
	public static final String POOLSIZE = "poolSize";
	public static final String CFG_NAME = ".cfg.properties";
	private static Configuration instance;
	private Properties config;

	protected Configuration() {
		this.config = new Properties();

		String basedir = System.getProperty("user.home");
		File file = new File(basedir, CFG_NAME);
		try {
			boolean exist = file.exists();
			if (!exist) {
				file.createNewFile();
			}
			this.config.load(new FileInputStream(file));
			if (!exist) {
				this.config.setProperty(SERVERS, "127.0.0.1");
				this.config.setProperty(MAX_IDLE, "8");
				this.config.setProperty(INIT_IDLE_CAPACITY, "4");
				this.config.setProperty(SESSION_TIMEOUT, "5");
				this.config.setProperty(TIMEOUT, "5000");
				this.config.setProperty(POOLSIZE, "5000");
				this.config.store(new FileOutputStream(file), "");
			}
		} catch (Exception ex) {
		}
	}

	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}

	public String getString(String key, String defaultValue) {
		if (this.config != null) {
			return this.config.getProperty(key) != null ? this.config.getProperty(key) : defaultValue;
		}
		return defaultValue;
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String toString() {
		return "Configuration [config=" + this.config + "]";
	}
}
