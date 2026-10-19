package com.github.moistmason.scraper.download;

import com.github.moistmason.scraper.util.HtmlUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.json.Json;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PlayerIndexParser {
    static void main() throws IOException {
        format();
    }

    // Downloads each index page's HTML source.
    public static void download() throws IOException {
        for (final String letter : HtmlUtil.LETTERS) {
            final Path output = Path.of("data/pfr/index/player_index_" + letter + ".json");
            Files.createDirectories(output.getParent());
            final String url = HtmlUtil.PFR_PLAYER_INDEX_HEADER + letter.toUpperCase(Locale.ROOT) + "/";

            try (final BufferedWriter writer = Files.newBufferedWriter(output)) {
                System.out.println(url);
                final Optional<String> html = HtmlUtil.getUrlResponse(url, "");

                if (html.isPresent()) {
                    writer.write(html.get());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Retrieves each index's collection of player URLs and formats them into a text file.
    public static void format() throws IOException {
        for (String letter : HtmlUtil.LETTERS) {
            final Path input = Path.of("data/pfr/index/player_index_" + letter + ".json");
            final Path output = Path.of("data/pfr/player_urls/player_urls_" + letter + ".json");
            final JsonObject json = JsonParser.parseReader(Files.newBufferedReader(input)).getAsJsonObject();
            final Document response = HtmlUtil.getHtml(json);
            final List<Entry> entries = getEntries(response);
            writeJson(output, entries);
        }
    }

    public static void writeJson(final Path file, final List<Entry> entries) throws IOException {
        System.out.println(file.toString());
        Files.createDirectories(file.getParent());
        final JsonObject root = new JsonObject();
        final JsonObject playerUrls = new JsonObject();

        for (Entry entry : entries) {
            final JsonObject entryObj = new JsonObject();
            entryObj.addProperty("index", entry.i);
            entryObj.addProperty("url", entry.url);
            playerUrls.add(entry.slug, entryObj);
        }

        root.add("playerUrls", playerUrls);

        try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(root.toString());
        }
    }

    public static List<Entry> getEntries(final Document response) {
        final Optional<Element> body = HtmlUtil.getElementByTag(response, "body");
        final List<Entry> entries = new ArrayList<>();

        if (body.isPresent()) {
            final Optional<Element> wrap = HtmlUtil.getElement(body.get(), "wrap");

            if (wrap.isPresent()) {
                final Optional<Element> content =  HtmlUtil.getElement(wrap.get(), "content");

                if (content.isPresent()) {
                    final Optional<Element> allPlayers = HtmlUtil.getElement(content.get(), "all_players");

                    if (allPlayers.isPresent()) {
                        final Optional<Element> divPlayers = HtmlUtil.getElement(allPlayers.get(), "div_players");

                        if (divPlayers.isPresent()) {
                            final Optional<Elements> paragraphs = HtmlUtil.getAllElementsByTag(divPlayers.get(), "p");

                            if (paragraphs.isPresent()) {
                                for (int i = 0; i <  paragraphs.get().size(); i++) {
                                    final Element p = paragraphs.get().get(i);
                                    Entry entry = getEntry(p, i + 1);
                                    entries.add(entry);
                                }
                            }
                        }
                    }
                }
            }
        }

        return entries;
    }

    public static Entry getEntry(final Element p, final int i) {
        String slug = "";
        String ref = "";

        // Bold text. Used for indicating active players. Pass through this first if it's present.
        final Optional<Element> bold = HtmlUtil.getElementByTag(p, "b");

        if (bold.isPresent()) {
            final Optional<Element> a = HtmlUtil.getElementByTag(bold.get(), "a");

            if (a.isPresent()) {
                ref = a.get().attr("href");
                slug = ref.substring(ref.lastIndexOf('/') + 1, ref.indexOf(".htm"));
            }
        } else {
            final Optional<Element> a = HtmlUtil.getElementByTag(p, "a");

            if (a.isPresent()) {
                ref = a.get().attr("href");
                slug = ref.substring(ref.lastIndexOf('/') + 1, ref.indexOf(".htm"));
            }
        }

        return new Entry(slug, HtmlUtil.PFR_URL + ref, i);
    }

    // Holder for the slug (url ID) and the url.
    public record Entry(String slug, String url, int i) { }
}
