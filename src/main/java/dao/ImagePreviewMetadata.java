package dao;

import java.nio.file.Path;

import javafx.scene.image.Image;

public class ImagePreviewMetadata {
	private ImageDimension dimension;
	private int localIndex;
	private int globalIndex;
	private Path path;
	private Long dbId;
	private boolean selected;

	public ImageDimension getDimension() {
		return dimension;
	}

	public void setDimension(ImageDimension dimension) {
		this.dimension = dimension;
	}

	public int getLocalIndex() {
		return localIndex;
	}

	public void setLocalIndex(int localIndex) {
		this.localIndex = localIndex;
	}

	public int getGlobalIndex() {
		return globalIndex;
	}

	public void setGlobalIndex(int globalIndex) {
		this.globalIndex = globalIndex;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Long getDbId() {
		return dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
