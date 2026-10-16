package com.itq.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.net.URI;

public final class ApplicationInitializer {

    private static boolean initialized = false;

    private ApplicationInitializer() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        String userDir = System.getProperty("user.dir");
        String catalinaBase = System.getProperty("catalina.base");
        String catalinaHome = System.getProperty("catalina.home");
        String workingDir = new File("").getAbsolutePath();
        String currentPath = new File(".").getAbsolutePath();

        String[] searchPaths = {
                userDir,
                catalinaBase,
                catalinaHome,
                workingDir,
                currentPath,
                userDir != null ? new File(userDir, "Backend").getAbsolutePath() : null,
                userDir != null ? new File(userDir, ".env").getParent() : null,
                catalinaBase != null ? new File(catalinaBase, "Backend").getAbsolutePath() : null,
                catalinaBase != null ? new File(catalinaBase, ".env").getParent() : null,
                findProjectRoot(),
        };

        Dotenv dotenv = null;
        for (String path : searchPaths) {
            if (path == null) continue;
            File envFile = new File(path, ".env");
            if (envFile.exists()) {
                dotenv = Dotenv.configure()
                        .directory(path)
                        .filename(".env")
                        .load();
                break;
            }
        }

        if (dotenv == null) {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        }

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        initialized = true;
    }

    private static String findProjectRoot() {
        try {
            File classDir = new File(ApplicationInitializer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File current = classDir;

            for (int i = 0; i < 10 && current != null; i++) {
                File pomFile = new File(current, "pom.xml");
                File envFile = new File(current, "Backend/.env");
                File envFile2 = new File(current, ".env");

                if (pomFile.exists() && envFile.exists()) {
                    return new File(current, "Backend").getAbsolutePath();
                }
                if (pomFile.exists() && envFile2.exists()) {
                    return current.getAbsolutePath();
                }
                current = current.getParentFile();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}