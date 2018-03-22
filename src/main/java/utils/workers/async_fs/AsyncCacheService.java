package utils.workers.async_fs;

import dao.ImageId;
import dao.ImgPreviewId;
import fao.ImageFile;
import fao.ImageFileDimension;
import utils.Loggable;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.messages.MultithreadedSingletone;
import utils.workers.async_dao.AsyncDaoService;
import utils.workers.async_dao.AsyncDaoTransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import static utils.workers.async_dao.AsyncDaoTransactionType.INSERT;

public class AsyncCacheService extends MultithreadedSingletone<AsyncCacheService.Task> implements Loggable {
	public static class Task {
		private final UUID uuid = UUID.randomUUID();
		private final ImageFileDimension imageFileDimension = new ImageFileDimension();
		private Path imagePath;
		private ImageId imageDatabaseId;
		private byte[] data;

		public UUID getUuid() {
			return uuid;
		}

		public void setPreviewSize(double previewWidth, double previewHeight) {
			getImageFileDimension().setPreviewWidth(previewWidth);
			getImageFileDimension().setPreviewHeight(previewHeight);
		}

		public ImageFileDimension getImageFileDimension() {
			return imageFileDimension;
		}

		public Path getImagePath() {
			return imagePath;
		}

		public void setImagePath(Path imagePath) {
			this.imagePath = imagePath;
		}

		public ImageId getImageDatabaseId() {
			return imageDatabaseId;
		}

		public void setImageDatabaseId(ImageId imageDatabaseId) {
			this.imageDatabaseId = imageDatabaseId;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	}

	private static AsyncCacheService asyncFsService;
	private final CacheIO cacheIO;

	public static void init() {
		if (asyncFsService == null) asyncFsService = new AsyncCacheService();
	}

	public static void dispose() {
		if (asyncFsService != null) asyncFsService.disposeInstance();
	}

	public static byte[] readCache(Task element) throws IOException {
		return asyncFsService.getCacheIO().read(element);
	}

	@Override
	public void processQueue(Task element) {
		if (Objects.isNull(element)) return;

		final String id = getCacheIO().write(element);
		if (id == null) return;

		final ImgPreviewId imgPreview  = new ImgPreviewId(id);
		final AsyncDaoTransaction<ImgPreviewId> transaction = new AsyncDaoTransaction<>(INSERT, imgPreview);
		final Msg<AsyncDaoTransaction<ImgPreviewId>> message = new Msg<>(transaction, 0, AsyncDaoService.SERVICE_UUID);
		MessageQueue.send(message);
	}

	protected AsyncCacheService() {
		super();
		cacheIO = new CacheIO();

		MessageQueue.subscribe(SERVICE_UUID, (Msg<Task> msg) -> {
			pushTask(msg.getPayload());
		});
	}

	@Override
	public void disposeInstance() {
		super.disposeInstance();
	}

	public CacheIO getCacheIO() {
		return cacheIO;
	}
}
