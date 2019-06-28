package msgrsc.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import msgrsc.utils.Language;

/**
 * A directory in Axon where message resource files can be found. 
 */
public class MsgRscDir {

	private String path;
	
	private List<MsgRscFile> msgRscFiles;
	
	private List<MsgRscFile> infoFiles;

	public MsgRscDir() {
		msgRscFiles = new ArrayList<>();
		infoFiles = new ArrayList<>();
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public void addMsgRscFile(MsgRscFile msgRscFile) {
		msgRscFiles.add(msgRscFile);
	}

	public void addMsgRscFiles(Collection<MsgRscFile> files) {
		msgRscFiles.addAll(files);
	}
	
	public void addInfoFile(MsgRscFile infoFile) {
		infoFiles.add(infoFile);
	}
	
	public void addInfoFiles(Collection<MsgRscFile> files) {
		infoFiles.addAll(files);
	}
	
	public void removeFile(MsgRscFile file) {
		if (file.getType() == MsgRscFile.Type.MSG) {
			msgRscFiles.remove(file);
		} else {
			infoFiles.remove(file);
		}
	}
	
	public List<MsgRscFile> getMsgRscFiles() {
		return msgRscFiles;
	}	
	
	public List<MsgRscFile> getInfoFiles() {
		return infoFiles;
	}

	public List<MsgRscFile> getFiles(MsgRscFile.Type type) {
		if (type == MsgRscFile.Type.MSG)
			return msgRscFiles;
		else
			return infoFiles;
	}
	
	public MsgRscFile getFile(MsgRscFile.Type type, Language language) {
		List<MsgRscFile> files = type == MsgRscFile.Type.MSG ? msgRscFiles : infoFiles;
		for (MsgRscFile file : files) {
			if (file.getLanguage() == language) {
				return file;
			}
		}
		return null;
	}
	
	public void forEachFile(Consumer<MsgRscFile> action) {
		for (MsgRscFile file : msgRscFiles) {
			action.accept(file);
		}
		for (MsgRscFile file : infoFiles) {
			action.accept(file);
		}
	}
	
	/**
	 * Indicates whether this directory contains message resource files for all
	 * supported languages.
	 */
	public boolean isComplete() {
		return msgRscFiles.size() >= 6 && (infoFiles.size() == 0 || infoFiles.size() >= 6);
	}
		
	@Override
	public String toString() {
		String toString = "Directory " + path + " contains: ";
		for (MsgRscFile file : msgRscFiles) {
			toString += System.lineSeparator() + file.getFullPath();
		}
		for (MsgRscFile infoFile : infoFiles) {
			toString += System.lineSeparator() + infoFile.getFullPath();
		}
		return toString;
	}
}
