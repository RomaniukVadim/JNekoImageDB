package ui.imagelist;

import java.nio.file.Path;

public interface BaseImageListItemSelectionListener {
    void onSelect(Path imageFile, int index, boolean selected);
    void OnRightClick(Path imageFile, int index);
}
