package net.irisshaders.iris;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrisLogging {
	public static final boolean ENABLE_SPAM = false;

	private final Logger logger;

	public IrisLogging(String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	private String fmt(String msg) {
		if (msg == null) return "";
		if (msg.startsWith("[Quasar]")) return msg;
		return "[Quasar] " + msg;
	}

	public void fatal(String fatal) {
		this.logger.error(LogUtils.FATAL_MARKER, fmt(fatal));
	}

	public void fatal(String fatal, Throwable t) {
		this.logger.error(LogUtils.FATAL_MARKER, fmt(fatal), t);
	}

	public void error(String error) {
		this.logger.error(fmt(error));
	}

	public void error(String error, Object... o) {
		this.logger.error(fmt(error), o);
	}

	public void error(String error, Throwable t) {
		this.logger.error(fmt(error), t);
	}

	public void warn(String warning) {
		this.logger.warn(fmt(warning));
	}

	public void warn(String warning, Object... object) {
		this.logger.warn(fmt(warning), object);
	}

	public void warn(String warning, Throwable t) {
		this.logger.warn(fmt(warning), t);
	}

	public void warn(Throwable o) {
		this.logger.warn("[Quasar]", o);
	}

	public void info(String info) {
		this.logger.info(fmt(info));
	}

	public void info(String info, Object... o) {
		this.logger.info(fmt(info), o);
	}

	public void debug(String debug) {
		this.logger.debug(fmt(debug));
	}

	public void debug(String debug, Throwable t) {
		this.logger.debug(fmt(debug), t);
	}
}
