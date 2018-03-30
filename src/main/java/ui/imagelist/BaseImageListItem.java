package ui.imagelist;

import dao.ImagePreviewMetadata;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import service.img_worker.LocalImageService;
import service.img_worker.LocalImageServiceImpl;
import service.img_worker.LocalImageServiceTask;
import service.img_worker.proto.Callback;
import service.img_worker.results.PreviewGenerationResult;

import java.util.Objects;
import java.util.UUID;

public class BaseImageListItem extends Canvas {
    //public final UUID THIS_UUID = UUID.randomUUID();

    //private static final Image badImage = new Image("/dummy/badimage.png");
    private static final Color notSelectedColor = Color.color(0.8,0.8,0.8);
    private static final Color selectedColor = Color.color(0.3,0.8,0.3);
    private static final Color grayColor = Color.color(0.3,0.3,0.3);
    private static final javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(
            BaseImageListItem.class.getResource("/style/fonts/QUILLC.TTF").toExternalForm(),56);
    private static final Image selectedIcon = IconFontFX.buildImage(GoogleMaterialDesignIcons.DONE, 64, selectedColor, selectedColor);

    private Image image;
    private ImagePreviewMetadata imageMeta;
    private final LocalImageService localImageService;
    private LocalImageServiceTask<PreviewGenerationResult> currentTask;

    private final Callback<PreviewGenerationResult> callback = r -> {
        final Image img = r.getImage();
        if (img != null) {
            image = img;
            Platform.runLater(this::drawImage);
        }
    };

    public BaseImageListItem(EventHandler<? super MouseEvent> value) {
        localImageService = LocalImageServiceImpl.getInstance();
        getImageMeta().setSelected(false);
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                getImageMeta().setSelected(!getImageMeta().isSelected());
                drawImage();
            }
            value.handle(e);
        });
    }

    private void drawImage() {
        if (Objects.isNull(image)) {
            setNullImage();
            return;
        }

        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
        context.drawImage(image, 0, 0);
        context.setLineWidth(getImageMeta().isSelected() ? 9.0 : 1.5);
        context.setStroke(getImageMeta().isSelected() ? selectedColor : notSelectedColor);
        context.strokeRect(0, 0, getWidth(), getHeight());
        if (getImageMeta().isSelected()) context.drawImage(selectedIcon, getWidth() - 74, 10);
    }

    public void setNullImage() {
        image = null;
        final GraphicsContext context = getGraphicsContext2D();
        context.clearRect(0, 0, getWidth(), getHeight());
    }

    public void createImage(ImagePreviewMetadata meta) {
        imageMeta = meta;

        if (currentTask != null) {
            localImageService.cancelTask(currentTask);
            currentTask = null;
        }

        if (meta == null) {
            setNullImage();
            return;
        }

        if (meta.getDimension() == null) {

        }

        if ((meta.getDbId() != null) && (meta.getPath() == null) && (meta.getDimension() != null)) {
            currentTask = localImageService.loadPreviewFromStorage(meta.getDbId(), meta.getDimension(), callback);
        } else if ((meta.getDbId() == null) && (meta.getPath() != null) && (meta.getDimension() != null)) {
            currentTask = localImageService.loadPreviewFromFs(meta.getPath(), meta.getDimension(), callback);
        } else {
            setNullImage();
        }
    }

    public ImagePreviewMetadata getImageMeta() {
        return imageMeta;
    }
}
