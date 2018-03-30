package service.img_worker;

import java.nio.file.Path;

import dao.ImageDimension;
import service.img_worker.proto.Callback;
import service.img_worker.results.DirectoryListResult;
import service.img_worker.results.PreviewGenerationResult;

public interface LocalImageService {
	LocalImageServiceTask<PreviewGenerationResult> loadPreviewFromFs(Path path,  ImageDimension imageDimension, Callback<PreviewGenerationResult> callback);
	LocalImageServiceTask<PreviewGenerationResult> loadPreviewFromStorage(Long id, ImageDimension imageDimension, Callback<PreviewGenerationResult> callback);

	void cancelTask(LocalImageServiceTask task);

	void getImageFilesList(Path directory, Callback<DirectoryListResult> callback);
}
