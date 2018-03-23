package service.img_worker;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import fao.ImageFile;
import fao.ImageFileDimension;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import utils.messages.MultithreadedSingletone;
import utils.workers.async_img_resizer.ImageResizeUtils;

public class LocalImageServiceImpl extends MultithreadedSingletone<LocalImageServiceTask> implements LocalImageService {
	private static LocalImageServiceImpl localImageService;

	private final FsCacheIO fsCacheIO;
	private final FsStorageIO fsStorageIO;
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
		fsCacheIO = new FsCacheIO();
		fsStorageIO = new FsStorageIO();
		hibernate = Hibernate.getInstance();
	}


	@Override
	public void loadImageFromFs(Path path, ImageFileDimension imageFileDimension, LocalImageServiceCallback callback) {





	}

	@Override
	public void loadImageFromStorage(Long id, ImageFileDimension imageFileDimension, LocalImageServiceCallback callback) {

	}

	@Override
	public void getImageFilesList(Path directory, LocalImageServiceCallback callback) {

	}

	@Override
	public boolean isImageOnLocaleStorage(Long id) {
		return false;
	}

	@Override
	public boolean isImageOnLocaleStorage(String id) {
		return false;
	}

	@Override
	public void processQueue(LocalImageServiceTask element) {
		if (element == null) return;
		if (element.getCallback() == null) return;
		if ((element.getPath() == null) && (element.getDatabaseId() == null)) return;
		if ((element.getPath() != null) && (element.getDatabaseId() != null)) return;

		if (element.getPath() != null) {
			//fsCacheIO.


			final ImageFileDimension dim = element.getImageFileDimension();
			final long w = Math.round(dim.getPreviewWidth());
			final long h = Math.round(dim.getPreviewHeight());

			if ((w < 1) || (h < 1)) {
				element.getCallback().onEvent(new LocalImageServiceResult(element.getPath(), "Dimensions are incorrect"));
				return;
			}

			final BufferedImage bi = Optional.ofNullable(element.getPath().toAbsolutePath().toFile())
					.map(file -> ImageResizeUtils.resizeImage(file, w, h, true)).orElse(null);
			if (Objects.isNull(bi)) {
				element.getCallback().onEvent(new LocalImageServiceResult(element.getPath(), "Can't create an image. File is broken or not exist."));
				return;
			}

			final Image image = SwingFXUtils.toFXImage(bi, null);



		} else {



		}

	}
}
