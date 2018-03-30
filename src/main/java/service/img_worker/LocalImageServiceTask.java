package service.img_worker;

import java.nio.file.Path;

import dao.ImageDimension;
import service.img_worker.proto.Callback;

public class LocalImageServiceTask<T> {
	public enum Type {
		CREATE_PREVIEW, UPLOAD_TO_DATABASE, DOWNLOAD_FROM_DATABASE, DIRECTORY_LIST
	}

	private Path path;
	private Long databaseId;
	private ImageDimension imageDimension;
	private Callback<T> callback;
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

	public Callback<T> getCallback() {
		return callback;
	}

	public void setCallback(Callback<T> callback) {
		this.callback = callback;
	}

	public ImageDimension getImageDimension() {
		return imageDimension;
	}

	public void setImageDimension(ImageDimension imageDimension) {
		this.imageDimension = imageDimension;
	}

	public Type getTaskType() {
		return taskType;
	}

	public void setTaskType(Type taskType) {
		this.taskType = taskType;
	}
}
