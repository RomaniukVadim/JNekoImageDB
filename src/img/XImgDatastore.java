package img;

import datasources.DSImage;
import java.awt.Container;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javax.xml.bind.DatatypeConverter;
import jnekoimagesdb.Lang;
import org.iq80.leveldb.DB;

public class XImgDatastore {
    public static final int
            FILE_PART_SIZE_FOR_CHECKING_MD5 = 1024 * 32,
            MINIMUM_IMAGE_SIZE = 1024;
    
    private static final XImgImages
                imgConv = new XImgImages();
    
    private static XImgCrypto
            dsCrypto;
    
    private static String
            databaseName, storeBasePath;
    
    private static File
            storeRootDirectory;
    
    private static volatile boolean 
            isNotInit = true;
    
    public static void init(XImgCrypto c, String dbname) throws Exception {
        dsCrypto = c;
        databaseName = dbname;
        storeBasePath = "." + File.separator + databaseName + File.separator + "store";
        storeRootDirectory = new File(storeBasePath);
        isNotInit = false;
    }
    
    public static String getDBName() {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        return databaseName;
    }

    public static void removeFile(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        Files.delete(out); 
    }
    
    public static void saveFile(byte[] md5, Path savePath) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final byte[] out = getFile(md5);
        Files.write(savePath, out);
    }
    
    public static Image getImage(byte[] md5) throws IOException {
        final byte[] b = getFile(md5);
        final ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return new Image(bais);
    }
    
    public static byte[] getFile(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final String p = getPathString(md5);
        final Path out = FileSystems.getDefault().getPath(p);
        
        final byte[] nc = Files.readAllBytes(out);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Decrypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        return cc;
    }

    public static byte[] pushFile(Path file) throws Exception {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        final byte[] md5 = getFilePartMD5(file.toString());
        final String p = getPathString(md5);
        if (Files.exists(FileSystems.getDefault().getPath(p))) throw new IOException("ImgFSDatastore: file already exist;");
        
        final byte[] nc = Files.readAllBytes(file);
        if (nc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: file too small;");
        
        final byte[] cc = dsCrypto.Crypt(nc);
        if (cc.length < MINIMUM_IMAGE_SIZE) throw new IOException("ImgFSDatastore: strange crypt error;");
        
        final Path out = FileSystems.getDefault().getPath(p);
        Files.write(out, cc); 
        
        return md5;
    }
    
    public static String getPathString(byte[] md5) throws IOException {
        if (isNotInit) throw new Error(Lang.ImgFSDatastore_ERROR_01);
        
        if (md5.length != 16) throw new IOException("ImgFSDatastore: Input data is not correct;");
        
        if (!storeRootDirectory.mkdirs())
            if (!storeRootDirectory.exists()) throw new IOException("ImgFSDatastore: Cannot create root directory;");
        
        final String md5s = DatatypeConverter.printHexBinary(md5).toLowerCase();
        final String dir = storeBasePath + File.separator + md5s.substring(0, 2) + File.separator + md5s.substring(2, 4);
        final File tdir = new File(dir);
        if (!tdir.mkdirs())
            if (!tdir.exists()) throw new IOException("ImgFSDatastore: Cannot create end directory;");
                
        final String path = dir + File.separator + md5s.substring(4);
        return path;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public static byte[] getFilePartMD5(String path) throws IOException {
        try {
            final FileInputStream fis = new FileInputStream(path);
            FileChannel fc = fis.getChannel();
            fc.position(0);
            final ByteBuffer bb = ByteBuffer.allocate(FILE_PART_SIZE_FOR_CHECKING_MD5);
            int counter = fc.read(bb);
            fc.close();
            fis.close();
            if (counter > 0) {
                if (counter == FILE_PART_SIZE_FOR_CHECKING_MD5) 
                    return dsCrypto.MD5(bb.array(), dsCrypto.getSalt());
                else {
                    final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
                    bb_cutted.put(bb.array(), 0, counter);
                    return dsCrypto.MD5(bb_cutted.array(), dsCrypto.getSalt());
                }
            } else 
                throw new IOException("cannot calculate MD5 for file ["+path+"]");
        } catch (IOException ex) {
            throw new IOException("cannot calculate MD5 for file ["+path+"], " + ex.getMessage());
        }
    }
    
    public static void copyToExchangeFolderFromDB(Path folder, ArrayList<DSImage> img) throws IOException {
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) 
            throw new IOException("Preview default size is not set.");
        img.forEach(c -> {
            try {
                copyToExchangeFolderFromDB(folder, c);
            } catch (IOException ex) {
                Logger.getLogger(XImgDatastore.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    public static void copyToExchangeFolderFromDB(Path folder, DSImage img) throws IOException {
        if (Files.isDirectory(folder) && Files.isWritable(folder)) {
            final byte[] file = getFile(img.getMD5());
            final Path currFile = FileSystems.getDefault().getPath(folder.toString(), img.getImageFileName());
            Files.write(currFile, file); 
        } else 
            throw new IOException("Path not foung or not writable. ["+folder.toString()+"]");
    }
    
    public static Image createPreviewEntryFromExistDBFile(byte[] md5b, XImg.PreviewType type) throws IOException, InterruptedException {
        if (XImg.getPSizes().getPrimaryPreviewSize() == null) {
            throw new IOException("Preview default size is not set.");
        }
        
        final String filePath = getPathString(md5b);
        final Path path = FileSystems.getDefault().getPath(filePath);
        final byte[] fileCC = Files.readAllBytes(path);
        final byte[] decryptedCC = XImg.getCrypt().Decrypt(fileCC);
        
        final java.awt.Image image2 = Toolkit.getDefaultToolkit().createImage(decryptedCC);
        final MediaTracker mediaTracker = new MediaTracker(new Container()); 
        mediaTracker.addImage(image2, 0); 
        mediaTracker.waitForAll();
        
        final byte[] image = imgConv.getPreviewFS(image2);
        imgConv.setPreviewSize((int) XImg.getPSizes().getPrimaryPreviewSize().getWidth(), (int) XImg.getPSizes().getPrimaryPreviewSize().getHeight(), XImg.getPSizes().getPrimaryPreviewSize().isSquared());
        
        final byte previewCrypted[] = XImg.getCrypt().Crypt(image);
        final XImgPreviewGen.PreviewElement peDB = new XImgPreviewGen.PreviewElement();
        peDB.setMD5(md5b);
        peDB.setCryptedImageBytes(previewCrypted, XImg.getPSizes().getPrimaryPreviewSize().getPrevName());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(peDB);
        oos.flush();
        
        final byte[] crypted = XImg.getCrypt().Crypt(baos.toByteArray());
        if (crypted == null) throw new IOException("Crypt() return null;");
        
        final DB db = XImg.getDB(type);
        synchronized (db) {
            db.put(md5b, crypted);
        }
        
        oos.close();
        baos.close();
        
        return new Image(new ByteArrayInputStream(image));
    }
    
    public static XImgPreviewGen.PreviewElement readPreviewEntry(byte[] md5b) throws XImgPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
        try {
            return _readEntry(md5b, XImg.PreviewType.previews);
        } catch (XImgPreviewGen.RecordNotFoundException e) {
            return _readEntry(md5b, XImg.PreviewType.cache);
        }
    }
    
    public static XImgPreviewGen.PreviewElement readCacheEntry(byte[] md5b) throws XImgPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
        return _readEntry(md5b, XImg.PreviewType.cache);
    }
    
    private static XImgPreviewGen.PreviewElement _readEntry(byte[] md5b, XImg.PreviewType type) throws XImgPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
        if (XImg.getDB(type) == null) throw new IOException("database not opened;");
        
        final DB db = XImg.getDB(type);
        byte[] retnc;
        synchronized (db) {
            retnc = db.get(md5b);
            if (retnc == null ) throw new XImgPreviewGen.RecordNotFoundException("");
        }
        
        final byte[] ret = dsCrypto.Decrypt(retnc);
        if (ret == null ) throw new IOException("Decrypt() return null value;");
        
        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
        final ObjectInputStream oos = new ObjectInputStream(bais);
        final XImgPreviewGen.PreviewElement retVal = (XImgPreviewGen.PreviewElement) oos.readObject();
        if (retVal == null ) throw new IOException("readObject() return null value;");
        
        return retVal;
    }
}
