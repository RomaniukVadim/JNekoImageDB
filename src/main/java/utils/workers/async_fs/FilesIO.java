package utils.workers.async_fs;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import fao.ImageFile;
import utils.Utils;
import utils.security.SecurityCryptUtils;
import utils.security.SecurityService;
import utils.workers.image_resizer.ImageResizeUtils;

public class FilesIO {
	public static final int STORAGE_DEEP = 4;
	private final byte[] masterKey;
	private final byte[] iv;
	private final String storageName;
	private final File storageDir;

	private static final Set<String> allowedExtentions = Utils.createSet(".jpg", ".jpeg", ".jpe", ".png");

	protected FilesIO(String imgSubdirectory) {
		final byte[] sha512 = SecurityCryptUtils.sha512(SecurityService.getAuthData());
		this.masterKey = Arrays.copyOfRange(sha512, 0, 32);
		this.iv = Arrays.copyOfRange(sha512, 32, 48);

		this.storageName = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha512(sha512), 48, 64));
		this.storageDir = new File(DATASTORAGE_ROOT + "data"  + File.separator + imgSubdirectory + File.separator + this.storageName).getAbsoluteFile();
		this.storageDir.mkdirs();


	}

	public CopyOnWriteArrayList<ImageFile> list(Path dir) {
		final Set<Path> imagesList = Arrays.asList(dir.toAbsolutePath().toFile().listFiles()).parallelStream()
				.filter(file -> file.isFile())
				.filter(file -> allowedExtentions.stream().filter(name -> file.getName().toLowerCase().endsWith(name)).count() > 0)
				.map(file -> file.toPath())
				.collect(Collectors.toSet());
		final CopyOnWriteArrayList<ImageFile> retval = new CopyOnWriteArrayList<>();
		if (Objects.nonNull(imagesList)) {
			imagesList.forEach(file -> {
				try {
					final Dimension dimension = ImageResizeUtils.getImageDimension(file.toFile().getAbsoluteFile());
					final ImageFile imageFile = new ImageFile(file);
					imageFile.setRealSize(dimension.getWidth(), dimension.getHeight());
					retval.add(imageFile);
				} catch (IOException e) {
					System.err.println("File \"" + file.toFile().getName() + "\" not an image.");
				}
			});

			Collections.sort(retval, Comparator.comparing(a -> a.getImagePath().toFile().getName()));
		}
		return retval;
	}

	public byte[] read(String id) throws IOException {
		final String storagePath = getStoragePath(id);

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

		final String storagePath = getStoragePath(hashString);
		final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();

		Files.write(fileForSave, encrypted, CREATE);

		return hashString;
	}

	private String getStoragePath(String id) throws IOException {
		if (Objects.isNull(id)) throw new IOException("Id of the file cannot be null");
		if (id.length() <= (STORAGE_DEEP * 2)) throw new IOException("Id too short");

		final StringBuilder name = new StringBuilder();
		name.append(getStorageDirectory().getAbsolutePath()).append(File.separator);
		for (int i=0; i<STORAGE_DEEP; i++) name.append(id.charAt(i)).append(File.separator);

		final File dir = new File(name.toString()).getAbsoluteFile();
		if ((!dir.exists()) && (!dir.mkdirs())) throw new IOException("Cannot create a directories tree!");

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

	private File getStorageDirectory() {
		return this.storageDir;
	}
}
