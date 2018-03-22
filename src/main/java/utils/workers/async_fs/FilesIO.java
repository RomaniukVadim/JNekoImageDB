package utils.workers.async_fs;

import utils.Loggable;
import utils.security.SecurityCryptUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;

public class FilesIO extends AbstractIO implements Loggable {
	protected FilesIO() {
		super("data");
	}

	public byte[] read(String id) throws IOException {
		final String storagePath = FSUtils.getStoragePath(this.storageDir, id);

		final File file = new File(storagePath);
		if (!file.isFile()) throw new IOException("File not a regular!");
		if (!file.exists()) throw new IOException("File not exist!");
		if (!file.canRead()) throw new IOException("File cannot be read!");

		final Path path = file.toPath();
		final byte[] encryptedFile = Files.readAllBytes(path);
		final byte[] rawFile = decrypt(encryptedFile);

		final byte[] hashOfEncryptedData = hash(rawFile);
		final String hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);
		if (!hashString.contentEquals(id.toLowerCase())) throw new IOException("File broken");

		return rawFile;
	}

	public String write(byte[] data) throws IOException {
		if (Objects.isNull(data)) throw new IOException("Cannot write a null files");

		final byte[] hashOfEncryptedData = hash(data);
		final byte[] encrypted = crypt(data);
		final String hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);

		final String storagePath = FSUtils.getStoragePath(this.storageDir, hashString);
		final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();

		Files.write(fileForSave, encrypted, CREATE);

		return hashString;
	}
}
