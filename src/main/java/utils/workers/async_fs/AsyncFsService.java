package utils.workers.async_fs;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import fao.ImageFile;
import utils.Loggable;
import utils.Utils;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.messages.MultithreadedSingletone;
import utils.security.SecurityCryptUtils;
import utils.security.SecurityService;
import utils.workers.async_dao.AsyncDaoTransaction;
import utils.workers.image_resizer.ImageResizeUtils;

public class AsyncFsService extends MultithreadedSingletone<AsyncFsTask> implements Loggable {
	private final FilesIO cacheIO;
	private final FilesIO filesIO;

	private void savePreview(AsyncFsTask element) {
		if (Objects.isNull(element.getFileContent())) return;

	}

	private void saveImage(AsyncFsTask element) {
		if (Objects.isNull(element.getFileContent())) return;

	}

	@Override
	public void processQueue(AsyncFsTask element) {
		if (Objects.isNull(element.getPath())) return;
		try {
			switch (element.getType()) {
			case ORIGINAL:

				break;
			case PREVIEW:
				savePreview(element);
				break;
			}
			final byte[] bytes = Files.readAllBytes(element.getPath());


		if (element.getPath() != null) {


				if (bytes != null) writeDBFile(bytes);

		}
		}catch (IOException e) {
			E("ERROR: " + e.getMessage());
		}
	}

	protected AsyncFsService() {
		super();

		cacheIO = new FilesIO("cache");
		filesIO = new FilesIO("files");

		MessageQueue.subscribe(SERVICE_UUID, (Msg<AsyncFsTask> msg) -> {
			pushTask(msg.getPayload());
		});
	}

	@Override
	public void disposeInstance() {
		super.disposeInstance();
	}
}
