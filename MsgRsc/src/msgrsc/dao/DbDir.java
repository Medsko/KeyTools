package msgrsc.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DbDir {

	private String fullPath;
	
	private List<DbFile> files;
	
	public DbDir() {
		files = new ArrayList<>();
	}
	
	public boolean isEmpty() {
		return files.size() == 0;
	}

	public void addFile(DbFile file) {
		files.add(file);
	}
	
	public void addFiles(Collection<DbFile> files) {
		this.files.addAll(files);
	}
	
	public List<DbFile> getFiles() {
		return files;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
}
