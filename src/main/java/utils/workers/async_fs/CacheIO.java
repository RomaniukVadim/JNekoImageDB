package utils.workers.async_fs;

import fao.ImageFile;
import utils.Loggable;
import utils.security.SecurityCryptUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;

public class CacheIO extends AbstractIO implements Loggable {
    protected CacheIO() {
        super("cache");
    }

    public byte[] read(AsyncCacheService.Task p) {
        if (Objects.isNull(p)) return null;
        if (Objects.isNull(p.getData())) return null;
        if (Objects.isNull(p.getImageFileDimension())) return null;

        try {
            String hashString = null;
            if (Objects.isNull(p.getImageDatabaseId())) {
                final byte[] hashOfEncryptedData = hash(getIdString(p).getBytes());
                hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);
            } else {
                hashString = getIDBCacheId(p);
            }

            final String storagePath = FSUtils.getStoragePath(this.storageDir, hashString);
            L("R storagePath = " + storagePath);

            final File file = new File(storagePath);
            if (!file.isFile()) throw new IOException("File not a regular!");
            if (!file.exists()) throw new IOException("File not exist!");
            if (!file.canRead()) throw new IOException("File cannot be read!");

            final Path path = file.toPath();
            final byte[] encryptedFile = Files.readAllBytes(path);

            return decrypt(encryptedFile);
        } catch (IOException e) {
            E("IOException " + e.getMessage());
        }
        return null;
    }

    public String write(AsyncCacheService.Task p) {
        if (Objects.isNull(p)) return null;
        if (Objects.isNull(p.getData())) return null;
        if (Objects.isNull(p.getImageFileDimension())) return null;

        try {
            String hashString = null;
            if (Objects.isNull(p.getImageDatabaseId())) {
                final byte[] hashOfEncryptedData = hash(getIdString(p).getBytes());
                hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);
            } else {
                hashString = getIDBCacheId(p);
            }

            final byte[] encrypted = crypt(p.getData());
            final String storagePath = FSUtils.getStoragePath(this.storageDir, hashString);
            final Path fileForSave = new File(storagePath).getAbsoluteFile().toPath();
            Files.write(fileForSave, encrypted, CREATE);
            return hashString;
        } catch (IOException e) {
            return null;
        }
    }

    public String getIDBCacheId(AsyncCacheService.Task p) {
        final String cacheIdText = p.getImageDatabaseId().getOid() + "-" + p.getImageFileDimension().getPreviewWidth() + "-" + p.getImageFileDimension().getPreviewHeight();
        return SecurityCryptUtils.toHex(hash(cacheIdText.getBytes()));
    }

    private String getIdString(AsyncCacheService.Task p) {
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
}
