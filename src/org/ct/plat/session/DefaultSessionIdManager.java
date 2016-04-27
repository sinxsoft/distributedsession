package org.ct.plat.session;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

public class DefaultSessionIdManager implements SessionIdManager {
	private static final String __NEW_SESSION_ID = "my.ct.newSessionId";
	protected static final String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
	protected static final String SESSION_ID_RANDOM_ALGORITHM_ALT = "IBMSecureRandom";
	private Logger log = Logger.getLogger(getClass());
	protected Random random;
	private boolean weakRandom;
	private boolean started = false;
	private boolean stopped = false;

	public void start() throws Exception {
		if (isStarted()) {
			return;
		}
		if (this.random == null) {
			try {
				this.random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				try {
					this.random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM_ALT);
					this.weakRandom = false;
				} catch (NoSuchAlgorithmException e_alt) {
					this.log.warn("Could not generate SecureRandom for session-id randomness", e);
					this.random = new Random();
					this.weakRandom = true;
				}
			}
		}
		this.random.setSeed(
				this.random.nextLong() ^ System.currentTimeMillis() ^ hashCode() ^ Runtime.getRuntime().freeMemory());

		this.started = true;
	}

	public void stop() throws Exception {
		this.stopped = true;
	}

	public boolean isStarted() {
		return this.started;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public String newSessionId(HttpServletRequest request, long created) {
		synchronized (this) {
			String requestedId = request.getRequestedSessionId();
			if (requestedId != null) {
				return requestedId;
			}
			String newId = (String) request.getAttribute(__NEW_SESSION_ID);
			if (newId != null) {
				return newId;
			}
			String id = null;
			while ((id == null) || (id.length() == 0)) {
				long r = this.weakRandom ? hashCode() ^ Runtime.getRuntime().freeMemory() ^ this.random.nextInt()
						^ request.hashCode() << 32 : this.random.nextLong();

				r ^= created;
				if ((request != null) && (request.getRemoteAddr() != null)) {
					r ^= request.getRemoteAddr().hashCode();
				}
				if (r < 0L) {
					r = -r;
				}
				id = Long.toString(r, 36);
			}
			request.setAttribute(__NEW_SESSION_ID, id);
			return id;
		}
	}
}
