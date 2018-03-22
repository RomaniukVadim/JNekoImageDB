package ui.activity;

import static java.util.Arrays.asList;
import static ui.Main.TOP_PANEL_ACTION_ADD_NODES;
import static ui.Main.TOP_PANEL_UUID;

import java.util.LinkedHashSet;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import jiconfont.icons.GoogleMaterialDesignIcons;
import service.RootService;
import ui.imagelist.DBImageList;
import ui.simplepanel.Panel;
import ui.simplepanel.PanelButton;
import utils.Utils;
import utils.messages.MessageQueue;
import utils.messages.MessageReceiver;
import utils.messages.Msg;

public class AllImagesActivity extends AbstractActivity {
    private final DBImageList dbImageList = new DBImageList();
    private final Panel panel = new Panel("panel_bottom_1",
            Panel.getSpacer(),
            dbImageList.getPaginator(),
			Panel.getFixedSpacer()
    );

    private final PanelButton panelButton = new PanelButton("Upload...", GoogleMaterialDesignIcons.CLOUD_UPLOAD, e -> RootService.showImportDialog());
	private final LinkedHashSet<Node> buttons = new LinkedHashSet<>();

	@Override
    public void onShow() {
        dbImageList.refresh();
		MessageQueue.send(new Msg<Set<Node>>(buttons, TOP_PANEL_ACTION_ADD_NODES, null, TOP_PANEL_UUID, msg -> {}));
    }

    public AllImagesActivity() {
        dbImageList.generateView(5, 5);
        dbImageList.regenerateCache();
        addAll(dbImageList, panel);
		buttons.addAll(asList(Panel.getSpacer(), panelButton));
    }

}
