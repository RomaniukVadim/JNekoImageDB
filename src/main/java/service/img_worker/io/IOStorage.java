package service.img_worker.io;

import java.nio.file.Path;
import java.util.Objects;

public class IOStorage extends IOAbstract {
	public IOStorage() {
		super("data");
	}

	public byte[] readFromFs(Path p) {
		final String idLine = genIdLine(p);
		return read(idLine);
	}

	public byte[] readFromDb(long id) {
		final String idLine = getDbIdLine(id);
		return read(idLine);
	}

	public String writeFromFs(Path p, byte[] image) {
		if (Objects.isNull(p)) return null;
		if (Objects.isNull(image)) return null;

		final String idLine = genIdLine(p);
		return write(idLine, image);
	}

	public String writeFromDb(Long id, byte[] image) {
		if (Objects.isNull(id)) return null;
		if (Objects.isNull(image)) return null;

		final String idLine = getDbIdLine(id);
		return write(idLine, image);
	}

	private String genIdLine(Path p) {
		final StringBuilder sb = new StringBuilder();
		sb.append(p.toFile().getName())
				.append("-")
				.append(p.toFile().length())
				.append("-")
				.append(salt);
		return new String(sb);
	}

	private String getDbIdLine(long id) {
		final StringBuilder sb = new StringBuilder();
		sb.append(id).append("0000000000").append(salt);
		return  new String(sb);
	}
}
