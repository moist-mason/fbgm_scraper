package com.github.moistmason.scraper.download;

import com.github.moistmason.scraper.util.HtmlUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlayerHtmlDownloader {

    // TODO: Make it accept args in the Gradle CLI, somehow. Kind of annoying having to edit this file constantly.
    static void main() throws IOException, InterruptedException {
        final String letter = "b";
        final Path in = Path.of("data/pfr/player_urls/player_urls_" + letter + ".json");
        final JsonObject root = JsonParser.parseReader(Files.newBufferedReader(in)).getAsJsonObject();
        final JsonObject playerUrls = root.getAsJsonObject("playerUrls");

        parseChunk(letter, playerUrls, 1051, 1200);
    }

    public static void parseThreeChunks(final String letter, final int firstMin, final int secondMin, final int max) throws IOException, InterruptedException {
        final Path in = Path.of("data/pfr/player_urls/player_urls_" + letter + ".json");
        final JsonObject root = JsonParser.parseReader(Files.newBufferedReader(in)).getAsJsonObject();
        final JsonObject playerUrls = root.getAsJsonObject("playerUrls");

        parseChunk(letter, playerUrls, firstMin, secondMin - 1);
        parseChunk(letter, playerUrls, secondMin, max);
    }

    public static void parseChunk(final String letter, final JsonObject playerUrls, final int min, final int max) throws IOException, InterruptedException {

        // Random interval between session.
        final long sessionInterval = new Random().nextLong(20L);
        System.out.println("New session. Sleeping for " +  sessionInterval + "s");
        Thread.sleep(sessionInterval * 1000);

        final String sessionId = "session_" + letter + "_" + min + "_" + max;
        HtmlUtil.sessionRequest("sessions.create", sessionId);

        final Map<String, String> responseMap = new HashMap<>();

        // Get the bounds we want.
        final Map<String, JsonElement> boundedMap = boundedMap(playerUrls, min, max);

        for (Map.Entry<String, JsonElement> entry : boundedMap.entrySet()) {

            // Random interval between requests.
            final long requestInterval = new Random().nextLong(5L) * 1000;
            Thread.sleep(requestInterval);

            final long current = System.currentTimeMillis();

            final String slug = entry.getKey();
            final String url = entry.getValue().getAsJsonObject().get("url").getAsString();
            final int i = entry.getValue().getAsJsonObject().get("index").getAsInt();

            final Optional<String> response = HtmlUtil.getUrlResponse(url, sessionId);
            response.ifPresent(string -> responseMap.put(slug, string));

            final long elapsed = System.currentTimeMillis() - current;
            System.out.println(slug + " (" + i + " of " + max + ") took " + (elapsed / 1000) + "s");
        }

        HtmlUtil.sessionRequest("sessions.destroy", sessionId);
        write(responseMap);
    }

    public static Map<String, JsonElement> boundedMap(final JsonObject playerUrls, final int min, final int max) {
        final Map<String, JsonElement> allMap = playerUrls.asMap();
        final Map<String, JsonElement> boundedMap = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : allMap.entrySet()) {
            final String slug = entry.getKey();
            final JsonObject obj = entry.getValue().getAsJsonObject();
            final int index = obj.get("index").getAsInt();

            if (index >= min && index <= max) {
                boundedMap.put(slug, obj);
            }
        }

        return boundedMap;
    }

    public static void write(final Map<String, String> responseMap) throws IOException {
        for (Map.Entry<String, String> entry : responseMap.entrySet()) {
            final String slug = entry.getKey();
            final String response = entry.getValue();
            final Path path = Path.of("data/pfr/html/player/" + slug.substring(0, 1).toLowerCase() + "/" + slug + ".json");
            Files.createDirectories(path.getParent());

            try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(response);
            }
        }
    }
}
