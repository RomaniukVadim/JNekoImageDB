package albums;

import dataaccess.DBWrapper;
import dialogs.DialogWindow;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import jnekoimagesdb.GUITools;

public class AlbumSelectDialog {
    private interface ASDNewElementActionListener {
        void OnNew(long parent, String title);
    }
    
    private class ASDNewElement extends HBox{
        private final Button
                addBtn = new Button("", new ImageView(new Image(new File("./icons/plus32.png").toURI().toString())));
        
        private final TextField
                newItemName = new TextField();
        
        private final Label       
                newTitle = new Label("Добавить альбом");
        
        private final ASDNewElementActionListener
                elAL;
        
        private final long
                parent_id;
        
        public ASDNewElement(ASDNewElementActionListener al, long pid) {
            super();
            this.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
            this.getStyleClass().add("ASDElementHBox");
            
            elAL = al;
            parent_id = pid;
            
            GUITools.setFixedSize(newTitle, 196, 32);
            newTitle.setAlignment(Pos.CENTER_LEFT);
            newTitle.getStyleClass().add("newTitle");
            
            GUITools.setFixedSize(addBtn, 32, 32);
            addBtn.getStyleClass().add("ImgButtonG");
            addBtn.setOnMouseClicked((MouseEvent event) -> {
                if (newItemName.getText().trim().length() > 0) elAL.OnNew(parent_id, newItemName.getText().trim());
            });
            
            GUITools.setMaxSize(newItemName, 9999, 32);
            newItemName.getStyleClass().add("newItemName");
            
            this.getChildren().addAll(newTitle, newItemName, addBtn);
        }
    }
    
    private long 
            albumID = 0;
    
    private ArrayList<Long> iidsElements = null;
    
    private final ASDNewElementActionListener
            newAL = (long parent, String title) -> {
                DBWrapper.addNewAlbumGroup(title, albumID);
                refresh();
    };
    
    private final ASDElementActionListener
            elAL = new ASDElementActionListener() {
                @Override
                public void OnCheck(Long id, AlbumsListElement e) {
                    selectedElements.add(e);
                }

                @Override
                public void OnUncheck(Long id, AlbumsListElement e) {
                    selectedElements.remove(e);
                }

                @Override
                public void OnItemClick(Long id, AlbumsListElement e) {
                    if (id > 0) 
                        genAlbList(id);
                    else {
                        long parent_el = DBWrapper.getParentAlbum(e.parent);
                        genAlbList(parent_el);
                    }
                        
                }

                @Override
                public void OnSave(Long id, AlbumsListElement e, String t) {
                    DBWrapper.saveAlbumsCategoryChanges(t, 0, id);
                }
            };
    
    private final ArrayList<AlbumsListElement>
            selectedElements = new ArrayList<>();

    private final Button 
            yesImg = new Button("", new ImageView(new Image(new File("./icons/d_yes.png").toURI().toString()))), 
            noImg  = new Button("", new ImageView(new Image(new File("./icons/d_no.png").toURI().toString())));
    
    private final TextArea 
            messageStr = new TextArea("");
        
    private final DialogWindow 
            dw = new DialogWindow(700, 800);
    
    private final ScrollPane
            sp = new ScrollPane();
    
    private final HBox 
            panel = new HBox();
    
    private final VBox
            mainContainer = new VBox();
    
    public AlbumSelectDialog() {
        GUITools.setMaxSize(panel, 9999, 64);
        panel.setMinSize(128, 64);
        panel.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        panel.getStyleClass().add("topPanel");
        
        messageStr.setText("Выберите альбомы");
        messageStr.setEditable(false);
        messageStr.setWrapText(true);
        
        GUITools.setFixedSize(yesImg, 64, 64);
        GUITools.setFixedSize(noImg, 64, 64);
        GUITools.setMaxSize(messageStr, 9999, 64);
        
        setStyle(yesImg, "DYesButton");
        setStyle(noImg, "DNoButton");
        setStyle(messageStr, "DMessageStr");
        
        panel.getChildren().addAll(messageStr, noImg, yesImg);
        dw.getToolbox().getChildren().add(panel);
        
        sp.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        sp.getStyleClass().add("ImageList");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);
        
        GUITools.setMaxSize(sp, 9999, 9999);
        
        mainContainer.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        mainContainer.getStyleClass().add("mainContainer");
        GUITools.setMaxSize(mainContainer, 9999, 9999);
        
        sp.setContent(mainContainer);
        dw.getMainContainer().getChildren().add(sp);
        
        yesImg.setOnMouseClicked((MouseEvent event) -> {
            if (iidsElements == null) return;
            if (iidsElements.size() <= 0) return;
            if (selectedElements.size() <= 0) return; // Весьма спорный момент. Ибо удаление из альбомов надо будет реализовывать отдельно. Однако позволяет безболезненно делать пересечения.
            
            final ArrayList<Long> tmp1 = new ArrayList<>();
            for (AlbumsListElement el1 : selectedElements) {
                tmp1.add(el1.ID);
            }
            
            DBWrapper.setImageGroupsIDs(iidsElements, tmp1);
            dw.hide();
        });
        
        noImg.setOnMouseClicked((MouseEvent event) -> {
            dw.hide();
        });
        
        genAlbList(0);
    }
    
    private void genAlbList(long aid) {
        mainContainer.getChildren().clear();
        ArrayList<AlbumsCategory> alac = DBWrapper.getAlbumsGroupsID(aid);
        if (alac == null) return;
        
        albumID = aid;
        if (albumID > 0) {
            final AlbumsListElement el_root = new AlbumsListElement(-1L, albumID, "...", elAL);
            mainContainer.getChildren().add(el_root);
        }
        
        alac.stream().map((a) -> new AlbumsListElement(a.ID, a.parent, a.name, elAL)).forEach((el) -> {
            mainContainer.getChildren().add(el);
        });
        
        if (albumID > 0) {
            final ASDNewElement ne = new ASDNewElement(newAL, albumID);
            mainContainer.getChildren().add(ne);
        }
    }
    
    private void refresh() {
        genAlbList(albumID);
    }
    
    public int Show(ArrayList<Long> iids) {
        iidsElements = iids;
        dw.showModal();
        return 1;
    }

    private void setStyle(Region n, String styleID) {
        n.getStylesheets().add(getClass().getResource("panel.css").toExternalForm());
        n.getStyleClass().add(styleID);
    }
}
