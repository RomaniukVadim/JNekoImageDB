package utils.workers.image_resizer;

import dao.ImageId;
import fao.ImageFile;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import service.RootService;
import utils.Loggable;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.messages.MultithreadedSingletone;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.imageio.ImageIO;

public class ImageResizeService extends MultithreadedSingletone<ImageResizeTask> implements Loggable {
    private static ImageResizeService imageResizeService;

    public static void init() {
        if (Objects.isNull(imageResizeService)) imageResizeService = new ImageResizeService();
    }

    public static void dispose() {
        if (Objects.nonNull(imageResizeService)) imageResizeService.disposeInstance();
    }

    private ImageResizeService() {
        super();
        MessageQueue.subscribe(SERVICE_UUID, (Msg<ImageResizeTask> msg) -> pushTask(msg.getPayload()));
    }

    @Override
    public void processQueue(ImageResizeTask element) {
        try {
            final Image img = Optional.ofNullable(RootService.getCacheService().readCacheElement(element.getImageFile()))
                    .map(b -> new Image(new ByteArrayInputStream(b)))
                    .orElse(null);
            if (Objects.nonNull(img)) {
                Platform.runLater(() -> element.getTaskCallback().onImageResized(img, element.getImageFile().getLocalIndex()));
            } else {
                switch (element.getImageFile().getType()) {
                case LOCAL_FS:
                    processLocalFsImage(element);
                    break;
                case INTERNAL_DATABASE:
                    processInternalDBImage(element);
                    break;
                case HTTP:

                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getClass().getSimpleName() + " || " + e.getMessage());
        }
    }

    private void processInternalDBImage(ImageResizeTask task) throws IOException {
        final ImageId imageId = task.getImageFile().getImageDatabaseId();
        if (Objects.isNull(imageId)) {
            Platform.runLater(() -> task.getTaskCallback().onError(null, task.getImageFile().getLocalIndex()));
            return;
        }

        final byte[] file = RootService.getFileService().readDBFile(imageId.getImgId());
        final BufferedImage bufferedImageOriginal = ImageIO.read(new ByteArrayInputStream(file));
        if (Objects.isNull(bufferedImageOriginal)){
            Platform.runLater(() -> task.getTaskCallback().onError(null, task.getImageFile().getLocalIndex()));
            return;
        }

        final BufferedImage bi = ImageResizeUtils.resizeImage(bufferedImageOriginal,
                Math.round(task.getImageFile().getImageFileDimension().getPreviewWidth()),
                Math.round(task.getImageFile().getImageFileDimension().getPreviewHeight()), true);

        final Image image = SwingFXUtils.toFXImage(bi, null);
        Platform.runLater(() -> task.getTaskCallback().onImageResized(image, task.getImageFile().getLocalIndex()));

        writePreviewToCache(bi, task.getImageFile());
    }

    private void processLocalFsImage(ImageResizeTask task) throws IOException {
        final ImageFile imageFile = task.getImageFile();
        final BufferedImage bi = Optional.ofNullable(imageFile.getImagePath().toAbsolutePath().toFile())
                .map(file -> ImageResizeUtils.resizeImage(file,
                        Math.round(imageFile.getImageFileDimension().getPreviewWidth()),
                        Math.round(imageFile.getImageFileDimension().getPreviewHeight()), true)).orElse(null);
        if (Objects.isNull(bi)) {
            Platform.runLater(() -> task.getTaskCallback().onError(imageFile.getImagePath().toAbsolutePath(), imageFile.getLocalIndex()));
            return;
        }

        final Image image = SwingFXUtils.toFXImage(bi, null);
        Platform.runLater(() -> task.getTaskCallback().onImageResized(image, task.getImageFile().getLocalIndex()));

        writePreviewToCache(bi, task.getImageFile());
    }

    private void writePreviewToCache(BufferedImage bi, ImageFile imageFile) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpg", baos);
        final byte retVal[] = baos.toByteArray();
        RootService.getCacheService().writeCacheElement(imageFile, retVal);
    }
}
