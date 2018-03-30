package service;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import dao.AppSettings;
import service.img_worker.LocalImageServiceImpl;
import ui.dialog.ImportImagesDialog;
import utils.messages.MessageQueue;

public class RootService {
    public static final String DATASTORAGE_ROOT = "storage" + File.separator;

    private static final Gson gson = new Gson();
    private static final Path appConfigPath = new File(DATASTORAGE_ROOT + "config.json").toPath();

    private static ImportImagesDialog importImagesDialog;
    private static AppSettings appSettings;

    public static void dispose() {
        if (Objects.nonNull(importImagesDialog)) importImagesDialog.dispose();

        LocalImageServiceImpl.dispose();
        MessageQueue.dispose();
    }

    public static AppSettings getAppSettings() {
        if (Objects.isNull(appSettings)) throw new IllegalStateException("AppSettings need init before use");
        return appSettings;
    }

    public static void setAppSettings(AppSettings appSettings) {
        RootService.appSettings = appSettings;
    }

    public static void showImportDialog() {
        if (Objects.nonNull(importImagesDialog)) importImagesDialog.showAndWait();
    }

    public static void saveConfig() {
        try {
            Files.write(appConfigPath, gson.toJson(RootService.getAppSettings(), AppSettings.class).getBytes(), CREATE, TRUNCATE_EXISTING);
        } catch (IOException e1) {
            System.err.println("System settings not be wrtitten. " + e1.getMessage());
        }
    }

    public static boolean loadConfig() {
        try {
            setAppSettings(Optional.ofNullable(Files.readAllBytes(appConfigPath))
                    .map(bytes -> new String(bytes))
                    .map(str -> gson.fromJson(str, AppSettings.class))
                    .orElse(new AppSettings()));
            return true;
        } catch (IOException | JsonSyntaxException e) {
            RootService.setAppSettings(new AppSettings());
            try {
                Files.write(appConfigPath, gson.toJson(RootService.getAppSettings(), AppSettings.class).getBytes(), CREATE);
                return true;
            } catch (IOException e1) {
                return false;
            }
        }
    }
}
