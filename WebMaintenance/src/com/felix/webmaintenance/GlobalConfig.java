package com.felix.webmaintenance;

import java.io.File;



import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.felix.util.KeyValues;
import com.felix.util.logging.Log4JLogger;
import com.felix.util.logging.LoggerInterface;

public class GlobalConfig extends KeyValues {
	public final static String APPLICATION_SCOPE_NAME = "global";
	private static GlobalConfig globalRef = null; // reference to the instance
	private String _contextName = null, _appRootPath = null,
			_confFilePath = null;
	private LoggerInterface _logger;

	/**
	 * Constructor (can be used for JSPs or java applications) Object won't be
	 * initialized!
	 */
	public GlobalConfig(String confFilePath) {
		super(confFilePath, "=");
		_confFilePath = confFilePath;
		globalRef = this;
	}

	public String getContextName() {
		return _contextName;
	}

	public static GlobalConfig getInstance() {
		return globalRef;
	}

	public LoggerInterface getLogger() {
		return _logger;
	}

	/**
	 * Constructor (can be used for Servlets). Initializes the object und reads
	 * the configuration data from the specified directory.
	 * 
	 * @param servletContext
	 *            object of the corresponding ServletContext
	 * @param configDirectory
	 *            absolute or relative Path (e.g. 'web-inf/conf') to the
	 *            configuration directory. If 'null', the default directory will
	 *            be used.
	 */
	public GlobalConfig(ServletContext sc, String configDirectory) {
		this(sc.getRealPath("/") + File.separator + configDirectory);

		try {
			_appRootPath = sc.getRealPath("/");
			if ((_appRootPath != null)
					&& !_appRootPath.endsWith(File.separator)) {
				_appRootPath += File.separator;
			}
			this.setPathBase(_appRootPath);

			_contextName = sc.getServletContextName();
			DOMConfigurator.configure(getPathValue("loggerConfig"));
			_logger = new Log4JLogger(Logger.getLogger(getString("loggerName")));
			if (_logger.isDebugEnabled()) {
				_logger.debug("Global() - Initialize Global");
			}
		} catch (Exception e) {
			_logger.error("Global(ServletContext, String)");
		}
	}

	public String getAppRootPath() {
		return _appRootPath;
	}

	public String getConfFilePath() {
		return _confFilePath;
	}
}
