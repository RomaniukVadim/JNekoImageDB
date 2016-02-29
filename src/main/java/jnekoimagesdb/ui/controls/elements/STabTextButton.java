package jnekoimagesdb.ui.controls.elements;

import static jnekoimagesdb.ui.controls.elements.GUIElements.EVENT_CODE_CLICK;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import jnekoimagesdb.ui.GUITools;

public class STabTextButton extends Button {
    private volatile int xID = -1;

    public STabTextButton(String text, int id, int sizeX, int sizeY, GUIActionListener al) {
        super(text);
        init(id, sizeX, sizeY, al, "STabTextButton");
    }

    public STabTextButton(String text, int id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
        super(text);
        init(id, sizeX, sizeY, al, styleName);
    }

    private void init(int id, int sizeX, int sizeY, GUIActionListener al, String styleName) {
        xID = id;
        GUITools.setStyle(this, "GUIElements", styleName);
        GUITools.setFixedSize(this, sizeX, sizeY);
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked((c) -> {
            al.OnItemEvent(EVENT_CODE_CLICK, xID); 
        });
    }

    public int getID() {
        return xID;
    }
}