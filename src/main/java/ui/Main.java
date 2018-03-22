package ui;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import service.RootService;
import ui.activity.AbstractActivity;
import ui.activity.AllImagesActivity;
import ui.dialog.YesNoDialog;
import ui.menu.Menu;
import ui.menu.MenuGroup;
import ui.menu.MenuItem;
import ui.simplepanel.Panel;
import utils.messages.MessageQueue;
import utils.messages.Msg;
import utils.security.SecurityService;
import utils.workers.async_dao.AsyncDaoService;
import utils.workers.async_fs.AsyncCacheService;
import utils.workers.async_fs.AsyncFsService;
import utils.workers.async_img_resizer.ImageResizeService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Main extends Application {
    public static final UUID TOP_PANEL_UUID = UUID.randomUUID();
    public static final int TOP_PANEL_ACTION_ADD_NODES = 1;
    public static final int TOP_PANEL_ACTION_REMOVE_NODES = 2;

    private static final Map<String, AbstractActivity> activities = new HashMap<>();

    private final Image logoImage = new Image("/style/images/logo_inv.png");
    private final ImageView imgLogoNode = new ImageView(logoImage);

    private final VBox windowBox = new VBox();
    private final HBox rootContainerBox = new HBox();
    private final VBox rootMenuBox = new VBox();
    private final VBox optionsBox = new VBox();
    private final VBox rootPane = new VBox();

    private HBox subPanelMain = new HBox();
    private Panel panel = new Panel("panel_main_1", imgLogoNode, subPanelMain);

    private void dispose() {
        System.exit(0);
    }

    private void initServices() {
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());

        if (!RootService.loadConfig()) {
            new YesNoDialog("Cannot write application settings.", false).showAndWait();
            dispose();
        }

        MessageQueue.init();
        SecurityService.init();
        ImageResizeService.init();
        if (SecurityService.createAuthDataWithUIPassworRequest() == null) dispose();
        AsyncDaoService.init();
        AsyncFsService.init();
        AsyncCacheService.init();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initServices();

        final Scene scene = new Scene(windowBox, RootService.getAppSettings().getMainWindowWidth(), RootService.getAppSettings().getMainWindowHeight());
        scene.heightProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setMainWindowHeight(n.doubleValue());
            RootService.saveConfig();
        });
        scene.widthProperty().addListener((e, o, n) -> {
            RootService.getAppSettings().setMainWindowWidth(n.doubleValue());
            RootService.saveConfig();
        });

        primaryStage.getIcons().add(new Image("/style/icons/icon32.png"));
        primaryStage.getIcons().add(new Image("/style/icons/icon64.png"));
        primaryStage.getIcons().add(new Image("/style/icons/icon128.png"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> {
            //RootService.dispose();
        });

        windowBox.getStylesheets().add(getClass().getResource("/style/css/main.css").toExternalForm());

        final Menu mn = new Menu(
                new MenuGroup(
                        "Images", "menu_group_container_red",
                        new MenuItem("All local images", (c) -> {
                            showActivity("AllImages");
                        }),
                        /*new MenuItem("Albums", (c) -> {

                        }),*/
                        new MenuItem("Tags", (c) -> {

                        })
                ),
                new MenuGroup(
                        "Service", "menu_group_container_green",
                        new MenuItem("Settings", (c) -> {

                        }),
                        new MenuItem("Logs", (c) -> {

                        })
                )
        );

        addActivity("AllImages", new AllImagesActivity());

        rootMenuBox.getStyleClass().addAll("null_pane", "menu_270px_width", "max_height");
        optionsBox.getStyleClass().addAll("null_pane", "max_width", "height_32px");
        rootPane.getStyleClass().addAll("null_pane", "max_width", "max_height");
        windowBox.getStyleClass().addAll("null_pane", "max_width", "max_height");
        subPanelMain.getStyleClass().addAll("null_pane", "max_width", "max_height");

        rootMenuBox.getChildren().addAll(mn);
        rootContainerBox.getChildren().addAll(rootMenuBox, rootPane);
        windowBox.getChildren().addAll(panel, rootContainerBox);

        MessageQueue.subscribe(TOP_PANEL_UUID, (Msg<Set<Node>> msg) -> {
            switch (msg.getMessageCategory()) {
            case TOP_PANEL_ACTION_ADD_NODES:
                subPanelMain.getChildren().clear();
                subPanelMain.getChildren().addAll(msg.getPayload());
                break;
            }
        });
    }

    public void addActivity(String name, AbstractActivity activity) {
        if (activities.containsValue(activity)) return;
        activities.put(name, activity);
    }

    public void showActivity(String name) {
        rootPane.getChildren().clear();
        final AbstractActivity n = activities.get(name);
        if (Objects.nonNull(n)) {
            rootPane.getChildren().add(n);
            n.onShow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
