package service.fs;

import fao.ImageFile;

import org.apache.commons.io.FileUtils;
import utils.security.SecurityCryptUtils;
import utils.Loggable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

public class EncryptedCacheAccessService extends AbstractFileAccessService implements Loggable {
    public static final String CACHE = "cache";
    private final byte[] masterKey;
    private final byte[] iv;
    private final String storageName;
    private final File storageDir;

    public EncryptedCacheAccessService(byte[] authData) {
        if (Objects.isNull(authData)) throw new IllegalArgumentException("authData cannot be null");

        final byte[] sha512 = SecurityCryptUtils.sha512(authData);
        this.masterKey = Arrays.copyOfRange(sha512, 16, 48);
        this.iv = Arrays.copyOfRange(sha512, 48, 64);
        this.storageName = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha512(sha512), 0, 16));
        this.storageDir = new File(DATASTORAGE_ROOT + CACHE + File.separator + this.storageName).getAbsoluteFile();
        this.storageDir.mkdirs();
    }

    private String getIdString(ImageFile p) {
        final StringBuilder sb = new StringBuilder();
        sb.append(p.getImagePath().toFile().getName())
                .append("-")
                .append(p.getImagePath().toFile().length())
                .append("-")
                .append(p.getImageFileDimension().getPreviewWidth())
                .append("-")
                .append(p.getImageFileDimension().getPreviewHeight());
        return sb.toString();
    }

    public byte[] readCacheElement(ImageFile p) {
        try {
            String id = null;
            switch (p.getType()) {
                case LOCAL_FS:
                    final byte[] hashOfEncryptedData = hash(getIdString(p).getBytes());
                    id = SecurityCryptUtils.toHex(hashOfEncryptedData);
                    break;
                case INTERNAL_DATABASE:
                    id = getIDBCacheId(p);
                    break;
            }

            final String storagePath = getStoragePath(id);
            L("R storagePath = " + storagePath);

            final File file = new File(storagePath);
            if (!file.isFile()) throw new IOException("File not a regular!");
            if (!file.exists()) throw new IOException("File not exist!");
            if (!file.canRead()) throw new IOException("File cannot be read!");

            final Path path = file.toPath();
            final byte[] encryptedFile = Files.readAllBytes(path);

            return decrypt(encryptedFile);
        } catch (IOException e) {
            //E("IOException " + e.getMessage());
        }
        return null;
    }

    public String writeCacheElement(ImageFile p, byte[] data) {
        try {
            String hashString = null;
            switch (p.getType()) {
                case LOCAL_FS:
                    if (Objects.isNull(data)) throw new IOException("Cannot write a null files");
                    final byte[] hashOfEncryptedData = hash(getIdString(p).getBytes());
                    hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);
                    break;
                case INTERNAL_DATABASE:
                    hashString = getIDBCacheId(p);
                    break;
            }

            final byte[] encrypted = crypt(data);
            final String storagePath = getStoragePath(hashString);
            L("W storagePath = " + storagePath);

            final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();

            Files.write(fileForSave, encrypted, CREATE);

            return hashString;
        } catch (IOException e) {
            return null;
        }
    }

    public String getIDBCacheId(ImageFile p) {
        final String cacheIdText = p.getType().name() + "-" + p.getImageDatabaseId().getOid() + "-" +
                p.getImageFileDimension().getPreviewWidth() + "-" + p.getImageFileDimension().getPreviewHeight();
        return SecurityCryptUtils.toHex(hash(cacheIdText.getBytes()));
    }

    public void invalidateCache() {
        final File f = new File(DATASTORAGE_ROOT + CACHE).getAbsoluteFile();
        Arrays.asList(f.listFiles()).forEach(file -> FileUtils.deleteQuietly(file));
    }

    @Override
    byte[] crypt(byte[] plainBlob) {
        return SecurityCryptUtils.aes256Encrypt(plainBlob, masterKey, iv);
    }

    @Override
    byte[] decrypt(byte[] cryptedBlob) {
        return SecurityCryptUtils.aes256Decrypt(cryptedBlob, masterKey, iv);
    }

    @Override
    byte[] hash(byte[] data) {
        return SecurityCryptUtils.sha256(data);
    }

    @Override
    File getStorageDirectory() {
        return this.storageDir;
    }

    @Override
    int getStorageDeep() {
        return 4;
    }
}
