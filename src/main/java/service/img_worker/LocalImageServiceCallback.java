package service.img_worker;

import service.img_worker.results.PreviewGenerationResult;

public interface LocalImageServiceCallback {
	void onEvent(PreviewGenerationResult result);
}
