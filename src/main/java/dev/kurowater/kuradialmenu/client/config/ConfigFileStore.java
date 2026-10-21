package dev.kurowater.kuradialmenu.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** File operations isolated from Minecraft so recovery can be tested independently. */
public final class ConfigFileStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_BYTES = 1024 * 1024;
    private static final int MAX_DEPTH = 32;

    private ConfigFileStore() {
    }

    public static ModConfig read(Path path) throws IOException {
        byte[] bytes;
        try (InputStream input = Files.newInputStream(path)) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        }
        if (bytes.length > MAX_BYTES) {
            throw new JsonParseException("Config exceeds the 1 MiB size limit");
        }
        String json = new String(bytes, StandardCharsets.UTF_8);
        // Older Gson versions read leniently by default. Check syntax explicitly
        // before deserialization so truncated files and trailing content are rejected.
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                throw new JsonParseException("Config must be a JSON object");
            }
            consumeValue(reader, 0);
            if (reader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonParseException("Unexpected content after config object");
            }
        } catch (IOException | IllegalStateException e) {
            throw new JsonParseException("Invalid config JSON", e);
        }
        ModConfig result = GSON.fromJson(json, ModConfig.class);
        result.validate();
        return result;
    }

    private static void consumeValue(JsonReader reader, int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new JsonParseException("Config is nested too deeply");
        }
        switch (reader.peek()) {
            case BEGIN_OBJECT -> {
                reader.beginObject();
                while (reader.hasNext()) {
                    reader.nextName();
                    consumeValue(reader, depth + 1);
                }
                reader.endObject();
            }
            case BEGIN_ARRAY -> {
                reader.beginArray();
                while (reader.hasNext()) {
                    consumeValue(reader, depth + 1);
                }
                reader.endArray();
            }
            case STRING, NUMBER -> reader.nextString();
            case BOOLEAN -> reader.nextBoolean();
            case NULL -> reader.nextNull();
            default -> throw new JsonParseException("Expected a JSON value");
        }
    }

    /** Copy a damaged file to a unique sibling before an explicit recovery save. */
    public static Path backupInvalid(Path path) throws IOException {
        Path absolute = path.toAbsolutePath();
        Path backup = Files.createTempFile(absolute.getParent(),
                absolute.getFileName() + ".invalid-", ".json");
        try {
            Files.copy(absolute, backup, StandardCopyOption.REPLACE_EXISTING);
            return backup;
        } catch (IOException e) {
            Files.deleteIfExists(backup);
            throw e;
        }
    }

    /** Write and flush a sibling temp file before replacing the previous config. */
    public static void write(Path path, ModConfig config) throws IOException {
        config.validate();
        Path absolute = path.toAbsolutePath();
        Files.createDirectories(absolute.getParent());
        Path temporary = Files.createTempFile(absolute.getParent(),
                absolute.getFileName() + ".", ".tmp");
        try {
            byte[] bytes = (GSON.toJson(config) + "\n").getBytes(StandardCharsets.UTF_8);
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, absolute, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // Never truncate the existing file, even on file systems without atomic moves.
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
