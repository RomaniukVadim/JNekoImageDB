package service.img_worker.io;

import java.nio.file.Path;
import java.util.Objects;

import fao.ImageFileDimension;

public class IOCache extends IOAbstract {
	public IOCache() {
		super("cache");
	}

	public byte[] readFromFs(Path p, ImageFileDimension d) {
		final String idLine = genIdLine(p, d);
		return read(idLine);
	}

	public byte[] readFromDb(long id, ImageFileDimension d) {
		final String idLine = getDbIdLine(id, d);
		return read(idLine);
	}

	public String writeFromFs(Path p, ImageFileDimension d, byte[] image) {
		if (Objects.isNull(p)) return null;
		if (Objects.isNull(d)) return null;

		final String idLine = genIdLine(p, d);
		return write(idLine, image);
	}

	public String writeFromDb(Long id, ImageFileDimension d, byte[] image) {
		if (Objects.isNull(id)) return null;
		if (Objects.isNull(d)) return null;

		final String idLine = getDbIdLine(id, d);
		return write(idLine, image);
	}

	private String genIdLine(Path p, ImageFileDimension d) {
		final StringBuilder sb = new StringBuilder();
		sb.append(p.toFile().getName())
				.append("-")
				.append(p.toFile().length())
				.append("-")
				.append(d.getPreviewWidth())
				.append("-")
				.append(d.getPreviewHeight())
				.append("-")
				.append(salt);
		return new String(sb);
	}

	private String getDbIdLine(long id, ImageFileDimension d) {
		final StringBuilder sb = new StringBuilder();
		sb.append(id)
				.append("-")
				.append(d.getPreviewWidth())
				.append("-")
				.append(d.getPreviewHeight())
				.append("-")
				.append(salt);
		return  new String(sb);
	}
}