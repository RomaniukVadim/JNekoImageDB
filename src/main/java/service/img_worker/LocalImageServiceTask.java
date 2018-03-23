package service.img_worker;

import java.nio.file.Path;

import fao.ImageFileDimension;

public class LocalImageServiceTask {
	private Path path;
	private String databaseId;
	private ImageFileDimension imageFileDimension;
	private LocalImageServiceCallback callback;

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(String databaseId) {
		this.databaseId = databaseId;
	}

	public LocalImageServiceCallback getCallback() {
		return callback;
	}

	public void setCallback(LocalImageServiceCallback callback) {
		this.callback = callback;
	}

	public ImageFileDimension getImageFileDimension() {
		return imageFileDimension;
	}

	public void setImageFileDimension(ImageFileDimension imageFileDimension) {
		this.imageFileDimension = imageFileDimension;
	}
}
