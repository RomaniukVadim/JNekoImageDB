package service.img_worker;

import fao.ImageFileDimension;
import utils.Loggable;
import utils.workers.async_fs.AsyncCacheService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE;

public class FsCacheIO extends FsAbstractIO implements Loggable {
    protected FsCacheIO() {
        super("cache");
    }

    private String genIdLine(Path p, ImageFileDimension d) {
        final StringBuilder sb = new StringBuilder();
        sb.append(p.toFile().getName())
                .append("-")
                .append(p.toFile().length())
                .append("-")
                .append(d.getPreviewWidth())
                .append("-")
                .append(d.getPreviewHeight());
        return new String(sb);
    }

    private String getDbIdLine(long id, ImageFileDimension d) {
        final StringBuilder sb = new StringBuilder();
        sb.append(id)
                .append("-")
                .append(d.getPreviewWidth())
                .append("-")
                .append(d.getPreviewHeight());
        return  new String(sb);
    }

    private byte[] readFile(String idLine) throws IOException {
        final byte[] hashOfEncryptedData = hash(idLine.getBytes());
        final String hashString = SecurityCryptUtils.toHex(hashOfEncryptedData);

        final String storagePath = FsUtils.getStoragePath(this.storageDir, hashString);
        L("R storagePath = " + storagePath);

        final File file = new File(storagePath);
        if (!file.isFile()) throw new IOException("File not a regular!");
        if (!file.exists()) throw new IOException("File not exist!");
        if (!file.canRead()) throw new IOException("File cannot be read!");

        final Path path = file.toPath();
        final byte[] encryptedFile = Files.readAllBytes(path);

        return decrypt(encryptedFile);
    }

    public byte[] readFromFs(Path p, ImageFileDimension d) throws IOException {
        final String idLine = genIdLine(p, d);
        return readFile(idLine);
    }

    public byte[] readFromDb(long id, ImageFileDimension d) throws IOException {
        final String idLine = getDbIdLine(id, d);
        return readFile(idLine);
    }



/*
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
            final String storagePath = FsUtils.getStoragePath(this.storageDir, hashString);
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
    }*/
}
