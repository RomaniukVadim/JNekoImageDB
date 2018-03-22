package utils.workers.async_fs;

import fao.ImageFile;
import utils.Utils;
import utils.workers.async_img_resizer.ImageResizeUtils;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FSUtils {
    public static final int STORAGE_DEEP = 4;
    private static final Set<String> allowedExtentions = Utils.createSet(".jpg", ".jpeg", ".jpe", ".png");

    public static CopyOnWriteArrayList<ImageFile> list(Path dir) {
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

    public static String getStoragePath(File storageDirectory, String id) throws IOException {
        if (Objects.isNull(id)) throw new IOException("Id of the file cannot be null");
        if (id.length() <= (STORAGE_DEEP * 2)) throw new IOException("Id too short");

        final StringBuilder name = new StringBuilder();
        name.append(storageDirectory.getAbsolutePath()).append(File.separator);
        for (int i=0; i<STORAGE_DEEP; i++) name.append(id.charAt(i)).append(File.separator);

        final File dir = new File(name.toString()).getAbsoluteFile();
        if ((!dir.exists()) && (!dir.mkdirs())) throw new IOException("Cannot create a directories tree!");

        name.append(id.substring(STORAGE_DEEP));
        return name.toString();
    }
}
