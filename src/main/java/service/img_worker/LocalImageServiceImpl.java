package service.img_worker;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import dao.ImageDimension;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import service.img_worker.io.IOAbstract;
import service.img_worker.io.IOCache;
import service.img_worker.io.IOStorage;
import service.img_worker.proto.Callback;
import service.img_worker.results.DirectoryListResult;
import service.img_worker.results.PreviewGenerationResult;
import utils.Loggable;
import utils.messages.MultithreadedSingletone;

public class LocalImageServiceImpl extends MultithreadedSingletone<LocalImageServiceTask> implements LocalImageService, Loggable {
	private static LocalImageServiceImpl localImageService;

	private final IOCache fsCacheIO;
	private final IOStorage fsStorageIO;
	private final Hibernate hibernate;

	public static void dispose() {
		if (localImageService != null) localImageService.disposeInstance();
	}

	public static LocalImageService getInstance() {
		if (localImageService == null) localImageService = new LocalImageServiceImpl();
		return localImageService;
	}

	@Override
	public void disposeInstance() {
		super.disposeInstance();
		hibernate.dispose();
	}

	private LocalImageServiceImpl() {
		fsCacheIO = new IOCache();
		fsStorageIO = new IOStorage();
		hibernate = Hibernate.getInstance();
	}

	@Override
	public LocalImageServiceTask<PreviewGenerationResult> loadPreviewFromFs(Path path, ImageDimension imageDimension, Callback<PreviewGenerationResult> callback) {
		final LocalImageServiceTask<PreviewGenerationResult> task = new LocalImageServiceTask<>();
		task.setCallback(callback);
		task.setPath(path);
		task.setImageDimension(imageDimension);
		task.setTaskType(LocalImageServiceTask.Type.CREATE_PREVIEW);
		super.pushTask(task);
		return task;
	}

	@Override
	public LocalImageServiceTask<PreviewGenerationResult> loadPreviewFromStorage(Long id, ImageDimension imageDimension, Callback<PreviewGenerationResult> callback) {
		final LocalImageServiceTask<PreviewGenerationResult> task = new LocalImageServiceTask<>();
		task.setCallback(callback);
		task.setDatabaseId(id);
		task.setImageDimension(imageDimension);
		task.setTaskType(LocalImageServiceTask.Type.CREATE_PREVIEW);
		super.pushTask(task);
		return task;
	}

	@Override
	public void getImageFilesList(Path directory, Callback<DirectoryListResult> callback) {
		final LocalImageServiceTask<DirectoryListResult> task = new LocalImageServiceTask<>();
		task.setCallback(callback);
		task.setTaskType(LocalImageServiceTask.Type.DIRECTORY_LIST);
		super.pushTask(task);
	}

	@Override
	public void processQueue(LocalImageServiceTask element) {
		if (element == null) return;
		if (element.getTaskType() == null) return;
		if (element.getCallback() == null) return;

		switch (element.getTaskType()) {
		case CREATE_PREVIEW:
			createPreview(element);
			break;
		case UPLOAD_TO_DATABASE:

			break;
		case DOWNLOAD_FROM_DATABASE:

			break;
		case DIRECTORY_LIST:
			getDirectoryList(element);
			break;
		}
	}

	private void getDirectoryList(LocalImageServiceTask<DirectoryListResult> element) {
		if (element.getPath() == null) {
			E("Path can't be a null");
			return;
		}

		final CopyOnWriteArrayList<Path> list = IOAbstract.list(element.getPath());
		final DirectoryListResult result = new DirectoryListResult();
		result.setFiles(list);
		element.getCallback().onEvent(result);
	}

	private void createPreview(LocalImageServiceTask<PreviewGenerationResult> element) {
		if (element.getCallback() == null) {
			E("Callback can't be a null");
			return;
		}
		if ((element.getPath() == null) && (element.getDatabaseId() == null)) {
			W("Path and id is null; Can't convert this image;");
			return;
		}
		if ((element.getPath() != null) && (element.getDatabaseId() != null)) {
			W("Path and id is not null; Can't convert this image;");
			return;
		}

		final ImageDimension dim = element.getImageDimension();

		if (element.getPath() != null) {
			final byte[] img = fsCacheIO.readFromFs(element.getPath(), dim);
			if (img == null) {
				final long w = Math.round(dim.getPreviewWidth());
				final long h = Math.round(dim.getPreviewHeight());

				if ((w < 1) || (h < 1)) {
					element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(), element.getDatabaseId(),"Dimensions are incorrect"));
					return;
				}

				final BufferedImage bi = Optional.ofNullable(element.getPath().toAbsolutePath().toFile())
						.map(file -> ImageResizeUtils.resizeImage(file, w, h, true)).orElse(null);
				if (Objects.isNull(bi)) {
					element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(), element.getDatabaseId(),
							"Can't create an image. File is broken or not exist."));
					return;
				}

				final Image resultImage = SwingFXUtils.toFXImage(bi, null);
				element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(), resultImage));

				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					ImageIO.write(bi, "jpg", baos);
					final byte retVal[] = baos.toByteArray();
					fsCacheIO.writeFromFs(element.getPath(), dim, retVal);
				} catch (IOException e) {
					element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(),
							element.getDatabaseId(), "Cannot save image as jpeg", e));
				}
			} else {
				final ByteArrayInputStream in = new ByteArrayInputStream(img);
				final Image resultImage = new Image(in);
				element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(), resultImage));
			}
		} else {
			final Long id = element.getDatabaseId();
			final byte[] img = fsCacheIO.readFromDb(id, dim);
			if (img == null) {
				final byte[] file = fsStorageIO.readFromDb(id);
				if (file == null) {
					element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(),
							element.getDatabaseId(), "Can't read file from database; Strange error;"));
					return;
				}
				try {
					final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(file));
					final BufferedImage bi = ImageResizeUtils.resizeImage(bufferedImageOriginal,
							Math.round(dim.getPreviewWidth()), Math.round(dim.getPreviewHeight()), true);
					final Image resultImage = SwingFXUtils.toFXImage(bi, null);
					element.getCallback().onEvent(new PreviewGenerationResult(id, resultImage));
				} catch (IOException e) {
					element.getCallback().onEvent(new PreviewGenerationResult(element.getPath(), element.getDatabaseId(), "Can't read image", e));
				}
			} else {
				final ByteArrayInputStream in = new ByteArrayInputStream(img);
				final Image resultImage = new Image(in);
				element.getCallback().onEvent(new PreviewGenerationResult(id, resultImage));
			}
		}
	}
}
