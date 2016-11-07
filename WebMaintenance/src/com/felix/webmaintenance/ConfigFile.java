package com.felix.webmaintenance;

import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import com.felix.util.FileUtil;
import com.felix.util.Preprocessor;

/**
 * A class to model the configuration file that shhould be editable.
 * 
 * @author burkhardt.felix
 * 
 */
public class ConfigFile {
	private String _path, _name;
	private Vector<String> _lines;
	private Vector<String> _linesReverse;
	private boolean _isReadonly = false, _reverse = false,
			_filterOutput = false;
	private Preprocessor _preproFilter = null;

	public ConfigFile(String path, String name) {
		try {
			_name = name;
			_path = path;
			if(FileUtil.existFile(path)) {
				_lines = FileUtil.getFileLines(path);				
			} else {
				_lines= new Vector<String>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	};

	/**
	 * Reread from file system.
	 */
	public void rereadFromDisk() {
		try {
			_lines = FileUtil.getFileLines(_path);
			_linesReverse = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * (Over) Write new contents to disk.
	 * 
	 * @param contents
	 *            The contents.
	 */
	public void writeToDisk(String contents) {
		try {
			_lines.removeAllElements();
			FileUtil.writeFileContent(_path, contents.replace("\r", ""));
			StringTokenizer st = new StringTokenizer(contents, "\n\r");
			while (st.hasMoreTokens()) {
				_lines.add(st.nextToken());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return _name;
	}

	public String getPath() {
		return _path;
	}

	public Vector<String> getLines() {
		Vector<String> retVec = _lines;
		if (_reverse) {
			if (_linesReverse == null) {
				_linesReverse = new Vector<String>();
				_linesReverse.addAll(_lines);
				Collections.reverse(_linesReverse);
			}
			retVec = _linesReverse;
		}
		if (_filterOutput) {
			retVec = _preproFilter.processVector(retVec);
		}
		return retVec;
	}

	public boolean getIsReadOnly() {
		return _isReadonly;
	}

	public void setReadOnly(boolean _isReadonly) {
		this._isReadonly = _isReadonly;
	}

	public boolean isReverse() {
		return _reverse;
	}

	public void setReverse(boolean reverse) {
		this._reverse = reverse;
	}

	public void setPreproFilter(Preprocessor preproFilter) {
		this._preproFilter = preproFilter;
	}

	public void setFilterOutput(boolean filterOutput) {
		this._filterOutput = filterOutput;
	}

}
