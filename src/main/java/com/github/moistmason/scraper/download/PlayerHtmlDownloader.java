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

/**
 * Main downloading class for the player pages on PFR.
 */
public class PlayerHtmlDownloader {

    // TODO: Make it accept args in the Gradle CLI, somehow. Kind of annoying having to edit this file constantly.
    static void main() throws IOException, InterruptedException {
        final String letter = "b";
        parseChunks(letter, 1501, 1800);
    }

    public static void parseChunks(final String letter, final int min, final int max) throws IOException, InterruptedException {
        final Path in = Path.of("data/pfr/player_urls/player_urls_" + letter + ".json");
        final JsonObject root = JsonParser.parseReader(Files.newBufferedReader(in)).getAsJsonObject();
        final JsonObject playerUrls = root.getAsJsonObject("playerUrls");
        final Map<String, JsonElement> boundedMap = boundedMap(playerUrls, min, max); // Get the bounds we want.

        /*
         * The chunk is separated into pairs that each get their own session.
         * This seems to alleviate the long solving times, as longer sessions lag the system, even though they still do happen.
         * Hopefully, if/when FlareSolverr gets updated, I can go back to longer sessions.
         */
        for (int i = min; i <= max; i++) {
            int bound = i + 1;
            parseChunk("b", boundedMap, i, bound);
            i = bound;
        }
    }

    public static void parseChunk(final String letter, final Map<String, JsonElement> boundedMap, final int min, final int max) throws IOException, InterruptedException {
        final String sessionId = "session_" + letter + "_" + min + "_" + max;
        HtmlUtil.sessionRequest("sessions.create", sessionId);

        final Map<String, String> responseMap = new HashMap<>();

        // Get the bounds we want.

        for (Map.Entry<String, JsonElement> entry : boundedMap.entrySet()) {

            // Random interval between requests. 3 to 5 seconds.
            final long requestInterval = new Random().nextLong(3, 5) * 1000;
            Thread.sleep(requestInterval);

            // Number of attempts for solving this challenge.
            int attempts = 1;

            final long current = System.currentTimeMillis();

            final String slug = entry.getKey();
            final String url = entry.getValue().getAsJsonObject().get("url").getAsString();
            final int i = entry.getValue().getAsJsonObject().get("index").getAsInt();

            Optional<String> response = HtmlUtil.getUrlResponse(url, sessionId);

            /*
             * The response is empty if a timeout (Error 500) occurred.
             * When this happens, the current session is destroyed and gets recreated. Then we try again until it's solved.
             */
            while (response.isEmpty()) {
                attempts++; // New attempt.
                HtmlUtil.sessionRequest("sessions.destroy", sessionId);
                HtmlUtil.sessionRequest("sessions.create", sessionId);
                response = HtmlUtil.getUrlResponse(url, sessionId);
            }

            response.ifPresent(string -> responseMap.put(slug, string));

            final long elapsed = System.currentTimeMillis() - current;
            System.out.println(slug + " (#" + i + ") took " + (elapsed / 1000) + "s on " + attempts + " attempts");
        }

        HtmlUtil.sessionRequest("sessions.destroy", sessionId);
        write(responseMap);

        // Take a break after the session and before the next chunk.
        final long sessionInterval = new Random().nextLong(10);
        Thread.sleep(sessionInterval * 1000);
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
