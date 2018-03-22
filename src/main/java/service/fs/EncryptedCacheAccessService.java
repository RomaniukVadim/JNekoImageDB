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

public class EncryptedCacheAccessService  {





/*    public void invalidateCache() {
        final File f = new File(DATASTORAGE_ROOT + CACHE).getAbsoluteFile();
        Arrays.asList(f.listFiles()).forEach(file -> FileUtils.deleteQuietly(file));
    }*/

}
