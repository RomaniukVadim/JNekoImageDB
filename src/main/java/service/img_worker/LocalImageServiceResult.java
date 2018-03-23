package service.img_worker;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;

public class LocalImageServiceResult {
	private Path path;
	private Image image;
	private Exception exception;
	private String errorText;
	private List<Path> files;
	private Map<String, Object> meta;

	public LocalImageServiceResult(Path path, String errorText) {
		this.path = path;
		this.errorText = errorText;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public List<Path> getFiles() {
		return files;
	}

	public void setFiles(List<Path> files) {
		this.files = files;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
}
