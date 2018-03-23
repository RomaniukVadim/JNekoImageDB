package utils.workers.async_fs;

import static utils.workers.async_dao.AsyncDaoTransactionType.INSERT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import dao.ImageId;
import service.img_worker.FsStorageIO;
import utils.Loggable;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.messages.MultithreadedSingletone;
import utils.workers.async_dao.AsyncDaoService;
import utils.workers.async_dao.AsyncDaoTransaction;

public class AsyncFsService extends MultithreadedSingletone<AsyncFsService.Task> implements Loggable {
	public static class Task {
		private final UUID uuid = UUID.randomUUID();
		private Path path;

		public UUID getUuid() {
			return uuid;
		}

		public Path getPath() {
			return path;
		}

		public void setPath(Path path) {
			this.path = path;
		}
	}

	private static AsyncFsService asyncFsService;
	private final FsStorageIO filesIO;

	public static void init() {
		if (asyncFsService == null) asyncFsService = new AsyncFsService();
	}

	public static void dispose() {
		if (asyncFsService != null) asyncFsService.disposeInstance();
	}

	public static byte[] read(String id) throws IOException {
		return asyncFsService.getFilesIO().read(id);
	}

	@Override
	public void processQueue(Task element) {
		if (Objects.isNull(element)) return;
		if (Objects.isNull(element.getPath())) return;
		try {
			final byte[] bytes = Files.readAllBytes(element.getPath());
			final String id = getFilesIO().write(bytes);
			final ImageId imageId = new ImageId(id);
			final AsyncDaoTransaction<ImageId> transaction = new AsyncDaoTransaction<>(INSERT, imageId);
			final Msg<AsyncDaoTransaction<ImageId>> message = new Msg<>(transaction, 0, AsyncDaoService.SERVICE_UUID);
			MessageQueue.send(message);
		}catch (IOException e) {
			E("ERROR: " + e.getMessage());
		}
	}

	protected AsyncFsService() {
		super();
		filesIO = new FsStorageIO();

		MessageQueue.subscribe(SERVICE_UUID, (Msg<Task> msg) -> {
			pushTask(msg.getPayload());
		});
	}

	@Override
	public void disposeInstance() {
		super.disposeInstance();
	}

	public FsStorageIO getFilesIO() {
		return filesIO;
	}
}
