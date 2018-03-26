package service.img_worker;

import java.nio.file.Path;
import java.util.List;

import fao.ImageFileDimension;
import javafx.scene.image.Image;

public interface LocalImageService {
	void loadPreviewFromFs(Path path,  ImageFileDimension imageFileDimension, LocalImageServiceCallback callback);
	void loadPreviewFromStorage(Long id, ImageFileDimension imageFileDimension, LocalImageServiceCallback callback);

	void getImageFilesList(Path directory, LocalImageServiceCallback callback);
}
