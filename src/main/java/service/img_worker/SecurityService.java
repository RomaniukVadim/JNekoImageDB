package service.img_worker;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dao.AuthInfo;
import javafx.application.Platform;
import ui.dialog.PasswordDialog;
import ui.dialog.YesNoDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static service.RootService.DATASTORAGE_ROOT;

public class SecurityService {
    private static SecurityService securityService;

    private static final File storageDir = new File(DATASTORAGE_ROOT + "auth/").getAbsoluteFile();
    private static final Gson gson = new Gson();
    private static final SecureRandom rnd = new SecureRandom();

    private static final PasswordDialog passwordDialog = new PasswordDialog();

    private static byte[] authData = null;

    public static byte[] createAuthDataWithUIPassworRequest() {
        if (!Platform.isFxApplicationThread()) throw new IllegalThreadStateException("This method allowed only in UI thread");

        final byte[] authData;
        passwordDialog.showAndWait();
        if (passwordDialog.getAction() != PasswordDialog.ACTION_OK) {
            return null;
        } else {
            final String password = passwordDialog.getPassword();
            authData = SecurityService.getAuthData(password);
            if (Objects.isNull(authData)) {
                final YesNoDialog yesNoDialog = new YesNoDialog("Database, associated with this password do not exist. Would you create a new database with this password?", true);
                yesNoDialog.showAndWait();
                if (yesNoDialog.getAction() == YesNoDialog.ACTION_YES) {
                    final byte[] temporaryAuthData = SecurityService.createAuthData(password);
                    if (Objects.isNull(temporaryAuthData)) {
                        new YesNoDialog("Cannot write auth-file to disk. Check your permissions.", false).showAndWait();
                        return null;
                    } else {
                        setAuthData(authData);
                        return authData;
                    }
                } else {
                    return null;
                }
            } else {
                setAuthData(authData);
                return authData;
            }
        }
    }

    public static byte[] createAuthData(String password) {
        return securityService.createAuthDataByPassword(password);
    }

    public static byte[] getAuthData(String password) {
        return securityService.getAuthDataByPassword(password);
    }

    public static void init() {
        if (Objects.isNull(securityService)) securityService = new SecurityService();
    }

    protected SecurityService() {
        storageDir.mkdirs();
    }

    public static byte[] getAuthData() {
        return authData;
    }

    public static void setAuthData(byte[] authData) {
        SecurityService.authData = authData;
    }

    private byte[] readFileWOException(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private AuthInfo getAuthInfoFromJson(String json) {
        try {
            return gson.fromJson(json, AuthInfo.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    private boolean verifyAuthData(String password, AuthInfo obj) {
        try {
            final byte[] ecnryptedHash = SecurityCryptUtils.sha512(obj.getEncryptedMasterKey());
            if (!Arrays.equals(ecnryptedHash, obj.getEncryptedMasterKeyHash())) return false;
            //System.out.println(Hex.encodeHexString(obj.getEncryptedMasterKey()));

            final byte[] masterKey = SecurityCryptUtils.aes256Decrypt(obj.getEncryptedMasterKey(), password);
            if (Objects.isNull(masterKey)) return false;

            final byte[] denryptedHash = SecurityCryptUtils.sha512(masterKey);
            return Arrays.equals(denryptedHash, obj.getDecryptedMasterKeyHash());
        } catch (Exception e) {
            return false;
        }
    }

    protected byte[] createAuthDataByPassword(String password) {
        final byte[] masterKey = new byte[512];
        rnd.nextBytes(masterKey);
        final byte[] masterKeyHash = SecurityCryptUtils.sha512(masterKey);
        final byte[] encryptedMasterKey = SecurityCryptUtils.aes256Encrypt(masterKey, password);
        final byte[] encryptedMasterKeyHash = SecurityCryptUtils.sha512(encryptedMasterKey);

        final AuthInfo authInfo = new AuthInfo();
        authInfo.setEncryptedMasterKey(encryptedMasterKey);
        authInfo.setDecryptedMasterKeyHash(masterKeyHash);
        authInfo.setEncryptedMasterKeyHash(encryptedMasterKeyHash);

        final String json = gson.toJson(authInfo, AuthInfo.class);
        final Path savePath = new File(storageDir.getAbsolutePath() + File.separator + "auth" + System.currentTimeMillis() + ".json").toPath();
        try {
            Files.write(savePath, json.getBytes(), CREATE);
        } catch (IOException e) {
            System.err.println("Cannot write auth file");
            return null;
        }

        return masterKey;
    }

    protected byte[] getAuthDataByPassword(String password) {
        final File[] files = storageDir.listFiles();
        if (Objects.isNull(files)) return null;
        if (files.length <= 0) return null;

        final List<Path> jsonList = Arrays.asList(files).stream()
                .filter(e -> e.getName().endsWith(".json"))
                .map(e -> e.toPath())
                .collect(Collectors.toList());
        if (jsonList.isEmpty()) return null;

        return jsonList.stream()
                .map(path -> readFileWOException(path))
                .filter(data -> (data.length > 0))
                .map(data -> new String(data))
                .map(data -> getAuthInfoFromJson(data))
                .filter(obj -> Objects.nonNull(obj))
                .filter(obj -> verifyAuthData(password, obj))
                .findFirst()
                .map(obj -> obj.getEncryptedMasterKey())
                .map(arr -> SecurityCryptUtils.aes256Decrypt(arr, password))
                .orElse(null);
    }
}
