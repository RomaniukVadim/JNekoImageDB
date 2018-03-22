package utils.workers.async_fs;

import utils.security.SecurityCryptUtils;
import utils.security.SecurityService;

import java.io.File;
import java.util.Arrays;

import static service.RootService.DATASTORAGE_ROOT;

public abstract class AbstractIO {
    final byte[] masterKey;
    final byte[] iv;
    final String storageName;
    final File storageDir;

    AbstractIO(String folderName) {
        final byte[] sha512 = SecurityCryptUtils.sha512(SecurityService.getAuthData());
        this.masterKey = Arrays.copyOfRange(sha512, 0, 32);
        this.iv = Arrays.copyOfRange(sha512, 32, 48);

        this.storageName = SecurityCryptUtils.toHex(Arrays.copyOfRange(SecurityCryptUtils.sha512(sha512), 48, 64));
        this.storageDir = new File(DATASTORAGE_ROOT + folderName + File.separator + this.storageName).getAbsoluteFile();
        this.storageDir.mkdirs();
    }

    byte[] crypt(byte[] plainBlob) {
        return SecurityCryptUtils.aes256Encrypt(plainBlob, masterKey, iv);
    }

    byte[] decrypt(byte[] cryptedBlob) {
        return SecurityCryptUtils.aes256Decrypt(cryptedBlob, masterKey, iv);
    }

    byte[] hash(byte[] data) {
        return SecurityCryptUtils.sha256(data);
    }
}
