package service.img_worker.io;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.bouncycastle.util.encoders.Hex;

import service.img_worker.SecurityCryptUtils;
import service.img_worker.SecurityService;
import utils.Loggable;
import utils.Utils;

public abstract class IOAbstract implements Loggable {
	private static final Set<String> allowedExtentions = Utils.createSet(".jpg", ".jpeg", ".jpe", ".png");
	private static final int STORAGE_DEEP = 4;

	private final byte[] masterKey;
	private final byte[] iv;
	private final String storageName;

	protected final File storageDir;
	protected final String salt;

	public IOAbstract(String folderName) {
		final byte[] sha512 = SecurityCryptUtils.sha512(SecurityService.getAuthData());
		this.masterKey = Arrays.copyOfRange(sha512, 0, 32);
		this.iv = Arrays.copyOfRange(sha512, 32, 48);
		this.salt = Hex.toHexString(sha512);
		this.storageName = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha512(sha512), 48, 64));
		this.storageDir = new File(DATASTORAGE_ROOT + folderName + File.separator + this.storageName).getAbsoluteFile();
		this.storageDir.mkdirs();
	}

	private String getStoragePath(File storageDirectory, String id) {
		if (Objects.isNull(id)) {
			W("Id of the file cannot be null");
			return null;
		}

		if (id.length() <= (STORAGE_DEEP * 2)) {
			W("Id too short");
			return null;
		}

		final StringBuilder name = new StringBuilder();
		name.append(storageDirectory.getAbsolutePath()).append(File.separator);
		for (int i=0; i<STORAGE_DEEP; i++) name.append(id.charAt(i)).append(File.separator);

		final File dir = new File(name.toString()).getAbsoluteFile();
		if ((!dir.exists()) && (!dir.mkdirs())) {
			W("Cannot create a directories tree!");
			return null;
		}

		name.append(id.substring(STORAGE_DEEP));
		return name.toString();
	}

	private byte[] crypt(byte[] plainBlob) {
		return SecurityCryptUtils.aes256Encrypt(plainBlob, masterKey, iv);
	}

	private byte[] decrypt(byte[] cryptedBlob) {
		return SecurityCryptUtils.aes256Decrypt(cryptedBlob, masterKey, iv);
	}

	private byte[] hash(byte[] data) {
		return SecurityCryptUtils.sha256(data);
	}

	protected byte[] read(String idLine) {
		final byte[] hashOfEncryptedData = hash(idLine.getBytes());
		final String hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);

		final String storagePath = getStoragePath(this.storageDir, hashString);
		if (storagePath == null) return null;

		final File file = new File(storagePath);
		if ((!file.isFile()) || (!file.exists()) || (!file.canRead())) {
			W("Bad or unreadable file: " + file.getAbsolutePath());
			return null;
		}

		final Path path = file.toPath();
		try {
			final byte[] encryptedFile = Files.readAllBytes(path);
			return decrypt(encryptedFile);
		} catch (IOException e) {
			W("Cannot read file: " + file.getAbsolutePath());
			return null;
		}
	}

	protected String write(String idLine, byte[] image) {
		final byte[] hashOfEncryptedData = hash(idLine.getBytes());
		final String hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);

		final byte[] encrypted = crypt(image);
		final String storagePath = getStoragePath(this.storageDir, hashString);
		if (storagePath == null) {
			W("Cannot generate path for hash " + hashString);
			return null;
		}
		final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();
		try {
			Files.write(fileForSave, encrypted, CREATE);
		} catch (IOException e) {
			W("Cannot write file: " + fileForSave.toFile().getAbsolutePath());
			return null;
		}

		return hashString;
	}

	public static CopyOnWriteArrayList<Path> list(Path dir) {
		final List<File> files = Optional.ofNullable(dir)
				.map(Path::toAbsolutePath)
				.map(Path::toFile)
				.map(File::listFiles)
				.map(Arrays::asList)
				.orElse(Collections.EMPTY_LIST);
		final List<Path> imagesList = files.parallelStream()
				.filter(File::isFile)
				.filter(file -> allowedExtentions.stream().filter(name -> file.getName().toLowerCase().endsWith(name)).count() > 0)
				.map(File::toPath)
				.collect(Collectors.toList());

		final CopyOnWriteArrayList<Path> retval = new CopyOnWriteArrayList<>(imagesList);
		retval.sort(Comparator.comparing(a -> a.toFile().getName()));
		return retval;
	}
}

