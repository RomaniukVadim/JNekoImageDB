package service.img_worker.results;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;
import utils.Loggable;

public class PreviewGenerationResult implements Loggable {
	private Path path;
	private Image image;
	private Exception exception;
	private String errorText;
	private Map<String, Object> meta;
	private Long id;

	public PreviewGenerationResult(Path path, Long id, String errorText, Exception e) {
		this.path = path;
		this.errorText = errorText;
		this.exception = e;
		this.id = id;
		W(errorText + "; error: " + e.getMessage());
	}

	public PreviewGenerationResult(Path path, Long id, String errorText) {
		this.path = path;
		this.errorText = errorText;
		this.id = id;
		W(errorText);
	}

	public PreviewGenerationResult(Path path, Image image) {
		this.path = path;
		this.image = image;
	}

	public PreviewGenerationResult(Long id, Image image) {
		this.id = id;
		this.image = image;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
