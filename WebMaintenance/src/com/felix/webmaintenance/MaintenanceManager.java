package com.felix.webmaintenance;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.felix.util.KeyValue;
import com.felix.util.KeyValues;
import com.felix.util.Preprocessor;
import com.felix.util.StringUtil;
import com.felix.util.logging.LoggerInterface;

/**
 * A class to maintain configuration of a web-server by a maintenance web
 * server.
 * 
 * @author burkhardt.felix
 * 
 */
public class MaintenanceManager {
	public static final String FILEDESCRIPTION_SEPARATOR_INTERN = ",";
	public static final String FILEDESCRIPTION_SEPARATOR = ";";
	public static final String MAINTENANCE_LINKS = "maintenanceLinks";
	public static final String MAINTENANCE_ACTIONS = "maintenanceActions";
	public static final String MAINTENANCE_FILES = "maintenanceFiles";
	public static final String MAINTENANCE_UPLOADS = "maintenanceUploads";
	public static final String PREPRO_PATH = "preproRules";
	public final static String EXECUTE_TRIGGER = "exec";
	public final static String SHOWINFO_TRIGGER = "show";

	public final static String READ_ONLY = "r";
	public final static String READ_WRITE = "w";
	public final static String FILTER = "f";
	public final static String REVERSE_ORDER = "r";
	public final static String REFRESH_FILES = "refreshFiles";
	public final static String REINITIALIZE = "init";
	public final static String CONFIG_DIR = "configDir";
	public final static String CONTEXT_NAME = "contextName";

	private Vector<KeyValue> _links;
	private Vector<KeyValue> _actions;
	private Vector<KeyValue> _files;
	private Vector<KeyValue> _uploads;
	private Vector<ConfigFile> _configFiles;
	private KeyValues _config;
	private ConfigFile _actFile;
	private Preprocessor _preproFilter = null;
	private String _configPath, _pathBase;
	public MaintainedServer _maintainedServer = null;
	private LoggerInterface _logger = null;

	/**
	 * The constructor.
	 * 
	 * @param configPath
	 *            Path to configuration.
	 * @param pathBase
	 *            The pathbase of the files to be configured.
	 * @param l
	 *            A logger.
	 */
	public MaintenanceManager(String configPath, String pathBase,
			LoggerInterface l) {
		_configPath = configPath;
		_pathBase = pathBase;
		_logger = l;
		_config = new KeyValues(_configPath);
		_config.setPathBase(_pathBase);
	}

	public void addAction(KeyValue act) {
		if (_actions == null)
			_actions = new Vector<KeyValue>();
		_actions.add(act);
	}

	public void setMaintainedServer(MaintainedServer ms) {
		_maintainedServer = ms;
	}

	public void addFile(KeyValue file) {
		if (_files == null)
			_files = new Vector<KeyValue>();
		_files.add(file);
	}

	public Vector<KeyValue> getActions() {
		return _actions;
	}

	public Vector<KeyValue> getLinks() {
		return _links;
	}

	public Vector<KeyValue> getUploads() {
		return _uploads;
	}

	public Vector<KeyValue> getFiles() {
		return _files;
	}

	public String getContextName() {
		return _config.getString(CONTEXT_NAME);
	}

	public void setActFile(String name) {
		_actFile = getConfigFile(name);
	}

	public ConfigFile getActFile() {
		return _actFile;
	}

	public void rereadFilesFromDisk() {
		for (ConfigFile f : _configFiles)
			f.rereadFromDisk();
	}

	public Vector<KeyValue> parseConfig(String id) {
		String actDescriptor = _config.getString(id);
		if (StringUtil.isFilled(actDescriptor)) {
			KeyValues kv = new KeyValues(actDescriptor,
					FILEDESCRIPTION_SEPARATOR, FILEDESCRIPTION_SEPARATOR_INTERN);
			return kv.getKeyValuesVector();
		}
		return null;
	}

	public ConfigFile getConfigFile(String name) {
		for (ConfigFile c : _configFiles) {
			if (c.getName().compareTo(name) == 0)
				return c;
		}
		return null;
	}

	public void init() {
		_config = new KeyValues(_configPath);
		_config.setPathBase(_pathBase);
		String preProPath = _config.getPathValue(PREPRO_PATH);
		if (StringUtil.isFilled(preProPath))
			setPreproFilter(preProPath, null);
		parseConfig();
		_maintainedServer.reInitializeServer();
	}

	public void executeCommand(String command) {
		_maintainedServer.executeCommand(command);
	}

	public String showInfo(String info) {
		return _maintainedServer.showInfo(info);
	}

	public void writeFileToDisk(String contents, String name) {
		ConfigFile c = getConfigFile(name);
		c.writeToDisk(contents);
	}

	public void parseConfig() {
		try {
			_actions = parseConfig(MAINTENANCE_ACTIONS);
		} catch (Exception e) {
			_logger.info("problem getting actions: " + e.getMessage());
		}
		try {
			_links = parseConfig(MAINTENANCE_LINKS);
		} catch (Exception e) {
			_logger.info("problem getting links: " + e.getMessage());
		}
		try {
			_uploads = parseConfig(MAINTENANCE_UPLOADS);
		} catch (Exception e) {
			_logger.info("problem getting upload files: " + e.getMessage());
		}
		try {
			parseFiles();
		} catch (Exception e) {
			_logger.info("problem getting files: " + e.getMessage());
		}
	}

	public void parseFiles() {
		String actDescriptor = _config.getString(MAINTENANCE_FILES);
		_configFiles = new Vector<ConfigFile>();
		_files = new Vector<KeyValue>();
		StringTokenizer st = new StringTokenizer(actDescriptor,
				FILEDESCRIPTION_SEPARATOR);
		try {
			while (st.hasMoreTokens()) {
				String fileS = st.nextToken();
				String[] p = fileS.split(FILEDESCRIPTION_SEPARATOR_INTERN);
				String key = p[0];
				String name = p[1];
				_files.add(new KeyValue(key, name));
				String writable = "";
				String reverse = "";
				String filter = "";
				try {
					writable = p[2];
				} catch (Exception e) {
				}
				try {
					reverse = p[3];
				} catch (Exception e) {
				}
				try {
					filter = p[4];
				} catch (Exception e) {
				}
				String filePapth = _config.getPathValue(CONFIG_DIR) + name;
				String absFp = new File(filePapth).getCanonicalPath();
				if (!absFp.startsWith(_pathBase)) {
					System.err.println("attempt to access file above context: "
							+ absFp + ", context:" + _pathBase);

				} else {
					ConfigFile configFile = new ConfigFile(filePapth, name);
					if (writable.compareTo(READ_ONLY) == 0) {
						configFile.setReadOnly(true);
					}
					if (reverse.compareTo(REVERSE_ORDER) == 0) {
						configFile.setReverse(true);
					}
					if (filter.compareTo(FILTER) == 0) {
						configFile.setFilterOutput(true);
					}
					configFile.setPreproFilter(_preproFilter);
					_configFiles.add(configFile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setPreproFilter(String rulesFile, String vocabFile) {
		this._preproFilter = new Preprocessor(rulesFile, vocabFile);
	}

}
