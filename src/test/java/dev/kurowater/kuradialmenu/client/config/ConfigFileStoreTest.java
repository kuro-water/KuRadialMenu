package dev.kurowater.kuradialmenu.client.config;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** Filesystem regressions; requires only Gson and the production config classes. */
public final class ConfigFileStoreTest {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("kuradial-config-test-");
        try {
            Path file = dir.resolve("nested/kuradialmenu.json");
            ModConfig config = new ModConfig();
            config.slotActions.set(0, "日本語.mod.key");
            ConfigFileStore.write(file, config);
            check(ConfigFileStore.read(file).slotActions.get(0).equals("日本語.mod.key"), "UTF-8 roundtrip");
            config.slotCount = 12;
            ConfigFileStore.write(file, config);
            check(ConfigFileStore.read(file).slotActions.size() == 12, "replacement save validated");
            try (var files = Files.list(file.getParent())) {
                check(files.count() == 1, "temporary files cleaned after save");
            }

            Files.writeString(file, "{\"slotActions\":null,\"slotCount\":4}");
            check(ConfigFileStore.read(file).slotActions.size() == 4, "null list load repaired");
            for (String invalid : new String[]{"", "null", "[]", "{", "{} {}", "{unquoted:1}",
                    "{\"slotCount\":\"bad\"}", "{\"slotActions\":{}}", "{\"x\":NaN}"}) {
                Files.writeString(file, invalid);
                expectInvalid(file);
                check(Files.readString(file).equals(invalid), "invalid source remains untouched");
            }
            String damaged = "{\"slotCount\":";
            Files.writeString(file, damaged);
            Path backup = ConfigFileStore.backupInvalid(file);
            Path secondBackup = ConfigFileStore.backupInvalid(file);
            check(!backup.equals(secondBackup), "backup filenames unique");
            ConfigFileStore.write(file, new ModConfig());
            check(Files.readString(backup).equals(damaged), "recovery preserves exact damaged bytes");

            Files.writeString(file, "{\"unknown\":" + "[".repeat(40) + "0" + "]".repeat(40) + "}");
            expectInvalid(file);
            Files.writeString(file, " ".repeat(1024 * 1024 + 1));
            expectInvalid(file);

            Path directoryTarget = dir.resolve("not-a-file");
            Files.createDirectory(directoryTarget);
            Files.writeString(directoryTarget.resolve("keep"), "unchanged");
            try {
                ConfigFileStore.write(directoryTarget, new ModConfig());
                throw new AssertionError("Saving over a nonempty directory must fail");
            } catch (IOException expected) {
                check(Files.readString(directoryTarget.resolve("keep")).equals("unchanged"),
                        "failed replacement leaves original data intact");
            }
            try (var files = Files.list(dir)) {
                check(files.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")),
                        "temporary files cleaned after failure");
            }
            System.out.println("ConfigFileStoreTest passed");
        } finally {
            try (var files = Files.walk(dir)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void expectInvalid(Path file) throws IOException {
        try {
            ConfigFileStore.read(file);
            throw new AssertionError("Malformed config was accepted");
        } catch (JsonParseException expected) {
            // The caller can recover without replacing the source file.
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
