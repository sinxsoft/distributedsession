package org.ct.plat.session.metadata;

import java.io.Serializable;

public class SessionMetaData implements Serializable {
	private static final long serialVersionUID = -6446174402446690125L;
	private String id;
	private Long createTm;
	private Long maxIdle;
	private Long lastAccessTm;
	private Boolean validate = Boolean.valueOf(false);
	private int version = 0;

	public SessionMetaData() {
		this.createTm = Long.valueOf(System.currentTimeMillis());
		this.lastAccessTm = this.createTm;
		this.validate = Boolean.valueOf(true);
	}

	public Long getCreateTm() {
		return this.createTm;
	}

	public void setCreateTm(Long createTm) {
		this.createTm = createTm;
	}

	public Long getMaxIdle() {
		return this.maxIdle;
	}

	public void setMaxIdle(Long maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Long getLastAccessTm() {
		return this.lastAccessTm;
	}

	public void setLastAccessTm(Long lastAccessTm) {
		this.lastAccessTm = lastAccessTm;
	}

	public Boolean getValidate() {
		return this.validate;
	}

	public void setValidate(Boolean validate) {
		this.validate = validate;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
