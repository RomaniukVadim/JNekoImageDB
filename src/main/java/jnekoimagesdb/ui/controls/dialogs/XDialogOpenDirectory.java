package jnekoimagesdb.ui.controls.dialogs;

import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Pagination;
import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.SettingsUtil;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.NPPagedFileList;
import jnekoimagesdb.ui.controls.PagedFileListActionListener;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import static jnekoimagesdb.ui.controls.dialogs.XImageUpload.BTN_GOROOT;
import static jnekoimagesdb.ui.controls.dialogs.XImageUpload.BTN_LEVELUP;
import static jnekoimagesdb.ui.controls.dialogs.XImageUpload.IMG64_GOROOT;
import static jnekoimagesdb.ui.controls.dialogs.XImageUpload.IMG64_LEVELUP;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SElementPair;
import jnekoimagesdb.ui.controls.elements.STabTextButton;


public class XDialogOpenDirectory extends XDialogWindow {
    public static enum XDialogODBoxResult {
        dOpen, dUnknown
    }
    
    private XDialogODBoxResult
            dResult = XDialogODBoxResult.dUnknown;

    private final Pagination
            pag = new Pagination(); 
    
    private final PagedFileListActionListener
            pflal = new PagedFileListActionListener() {
                @Override
                public void onThreadStart(long tid) { }

                @Override
                public void onThreadPause(long tid, boolean pause, long counter, Path px) { }

                @Override
                public void onThreadProgress(long tid, long counter) { }

                @Override
                public void onDisable(boolean dis) { }

                @Override
                public void onPageCountChange(int pageCount) {
                    pag.setCurrentPageIndex(0);
                    pag.setPageCount((pageCount == 0) ? 1 : pageCount);
                }

                @Override
                public void onPageChange(int page) {
                    pag.setCurrentPageIndex(page);
                }
            };
    
    private final NPPagedFileList 
            filesList = new NPPagedFileList(pflal);
    
    private final ToolsPanelTop
            panelTop = new ToolsPanelTop(c -> {
                switch (c) {
                    case BTN_LEVELUP:
                        filesList.levelUp();
                        break;
                    case BTN_GOROOT:
                        filesList.navigateRoot();
                        break;
                }
            });
    
    private final SEVBox
            mContainer = new SEVBox();
    
    public XDialogOpenDirectory() {
        super();
        panelTop.addButton(IMG64_LEVELUP, BTN_LEVELUP);
        panelTop.addButton(IMG64_GOROOT, BTN_GOROOT); 
        
        pag.setCurrentPageIndex(0);
        pag.setPageCount(1); 
        pag.setMaxPageIndicatorCount(150);
        pag.currentPageIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (oldValue.intValue() == newValue.intValue()) return;
            filesList.pageSet(newValue.intValue());
        });
        
        mContainer.getChildren().addAll(
                filesList,
                pag,
                GUITools.getHSeparator(32),
                new SElementPair(
                        new STabTextButton("Отмена", 0 , 120, 32, (x, y) -> {
                            dResult = XDialogODBoxResult.dUnknown;
                            this.hide();
                        }), 
                        4, 32, 32,
                        GUITools.getSeparator(),
                        new STabTextButton("Открыть", 0 , 120, 32, (x, y) -> {
                            if (filesList.getPath() != null) {
                                dResult = XDialogODBoxResult.dOpen;
                                this.hide();
                            } else 
                                XImg.msgbox("Ничего не выбрано!"); 
                        })
                ).setAlign(Pos.CENTER_RIGHT)
        );

        this.create(panelTop, null, mContainer, COLOR_BLACK, 1000, 600);
    }
    
    public void showDialog() {
        final Path p = SettingsUtil.getPath("currentNPPath");
        filesList.navigateTo(p.toAbsolutePath().toString());
        dResult = XDialogODBoxResult.dUnknown;
        this.showModal();
    }
    
    public Path getSelected() {
        return filesList.getPath();
    }
    
    public XDialogODBoxResult getResult() {
        return dResult;
    }
    
    public void init() {
        filesList.init(true);
    }
    
    public void dispose() {
        filesList.dispose();
    }
}