package utils.workers.async_fs;

import java.nio.file.Path;
import java.util.UUID;

public class AsyncFsTask {
	private final UUID uuid = UUID.randomUUID();
	private AsyncFsFileType type;
	private Path path;
	private byte[] fileContent;

	public UUID getUuid() {
		return uuid;
	}

	public AsyncFsFileType getType() {
		return type;
	}

	public void setType(AsyncFsFileType type) {
		this.type = type;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
}
