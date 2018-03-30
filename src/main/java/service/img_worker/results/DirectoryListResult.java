package service.img_worker.results;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryListResult {
	private CopyOnWriteArrayList<Path> files;
	private Map<String, Object> meta;
	private Exception exception;
	private String errorText;

	public CopyOnWriteArrayList<Path> getFiles() {
		return files;
	}

	public void setFiles(CopyOnWriteArrayList<Path> files) {
		this.files = files;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
}
