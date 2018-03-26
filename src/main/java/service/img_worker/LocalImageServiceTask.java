package service.img_worker;

import java.nio.file.Path;

import fao.ImageFileDimension;

public class LocalImageServiceTask {
	public static enum Type {
		CREATE_PREVIEW, UPLOAD_TO_DATABASE, DOWNLOAD_FROM_DATABASE
	}

	private Path path;
	private Long databaseId;
	private ImageFileDimension imageFileDimension;
	private LocalImageServiceCallback callback;
	private Type taskType;

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(Long databaseId) {
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

	public Type getTaskType() {
		return taskType;
	}

	public void setTaskType(Type taskType) {
		this.taskType = taskType;
	}
}
