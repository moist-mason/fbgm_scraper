package com.github.moistmason.scraper.util;

import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
    public static final String[] LETTERS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
    public static final String PFR_URL = "https://www.pro-football-reference.com/";
    public static final String PFR_PLAYER_INDEX_HEADER = PFR_URL + "players/";

    public static void sessionRequest(final String cmd, final String id) throws IOException, InterruptedException {
        final String payload = """
                {
                    "cmd": "${cmd}",
                    "session": "${id}"
                }
                """;
        final Optional<String> response = HtmlUtil.getResponse(
                payload.replace("${cmd}", cmd)
                        .replace("${id}", id)
        );

        if (response.isPresent()) {
            System.out.println(cmd + " " + id);
        }
    }

    public static void sessionCheck() throws IOException, InterruptedException {
        final String payload = """
                {
                    "cmd": "sessions.list"
                }
                """;
        final Optional<String> response = HtmlUtil.getResponse(
                payload
        );

        response.ifPresent(System.out::println);
    }

    public static Optional<String> getUrlResponse(final String url, final String id) throws IOException, InterruptedException {
        final String jsonPayload =
                """
                {
                    "cmd": "request.get",
                    "url": "${url}",
                    "session": "${id}",
                    "maxTimeout": 180000
                }
                """;
        return getResponse(jsonPayload.replace("${url}", url)
                .replace("${id}", id)
        );
    }

    public static Optional<String> getResponse(final String payload) throws IOException, InterruptedException {
        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8191/v1"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.ofNullable(response.body());
        }
    }

    public static HttpRequest getRequest(final String payload) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8191/v1"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
    }

    public static Document getHtml(final JsonObject root) {
        final JsonObject solution = root.get("solution").getAsJsonObject();
        final String response = solution.get("response").getAsString();
        return Jsoup.parse(response);
    }

    public static Document getDocument(final String url) throws IOException, InterruptedException {
        final String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
        final String jsonPayload =
                """
                {
                    "cmd": "request.get",
                    "url": "${url}",
                    "maxTimeout": 60000
                }
                """;
        // Random interval between requests
        final long randomInterval = new Random().nextLong(5L) * 1000;
        Thread.sleep(randomInterval);

        Thread.sleep(randomInterval);

        return Jsoup.connect(url)
                .userAgent(agent)
                .header("Content-Type", "application/json")
                .get();
    }

    // From text file, for testing
    public static Document getDocument(final Path path, final String base) throws IOException {
        final List<String> lines = Files.readAllLines(path);
        final String html = String.join("\n", lines);
        return Jsoup.parse(html);
    }

    public static void download(final String url, final Path output) throws IOException, InterruptedException {
        final Document document = getDocument(url);
        final String html = document.html();

        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write(html);
            writer.flush();
        }
    }

    public static Optional<Element> getElement(final Element parent, final String id) {
        final Element element = parent.getElementById(id);
        return Optional.ofNullable(element);
    }

    public static Optional<Element> getElementByClass(final Element parent, final String _class) {
        final Elements elements = parent.getElementsByClass(_class);
        return elements.isEmpty() ? Optional.empty() : Optional.ofNullable(elements.first());
    }

    public static Optional<Element> getElementByHeader(final Element parent, final String _class, final String header) {
        final Optional<Elements> elements = getAllElementsByClass(parent, _class);

        if (elements.isPresent()) {
            for (Element e : elements.get()) {
                final String raw = e.html();

                if (raw.contains("<th style=\"text-align:left\">" + header + "</th>")) {
                    return Optional.of(e);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<Elements> getAllElementsByClass(final Element parent, final String _class) {
        final Elements elements = parent.getElementsByClass(_class);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements);
    }

    public static Optional<Element> getElementByTag(final Element parent, final String tag) {
        final Elements elements = parent.getElementsByTag(tag);
        return elements.isEmpty() ? Optional.empty() : Optional.ofNullable(elements.first());
    }

    public static Optional<Element> getElementByTag(final Element parent, final String tag, final int id) {
        final Elements elements = parent.getElementsByTag(tag);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(id));
    }

    public static Optional<Elements> getAllElementsByTag(final Element parent, final String tag) {
        final Elements elements = parent.getElementsByTag(tag);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements);
    }

    public static Optional<Elements> getTableRows(final Element parent) {
        final Optional<Element> tableOpt = getElementByTag(parent, "table");

        if (tableOpt.isPresent()) {
            final Optional<Element> tbody = getElementByTag(tableOpt.get(), "tbody");

            if (tbody.isPresent()) {
                return HtmlUtil.getAllElementsByTag(tbody.get(), "tr");
            }
        }

        return Optional.empty();
    }

    public static String getDataFromKey(final String text, final String keyRegex) {
        return getDataFromKey(text, keyRegex, 1);
    }

    public static String getDataFromKey(final String text, final String keyRegex, final int beginIndex) {

        // Find the expected substring of data using regex.
        String data;
        final Matcher matcher = Pattern.compile(keyRegex).matcher(text);

        if (matcher.find()) {
            data = matcher.group(0);

            // Split the matched data so we separate the key (split[0]) from the data value that we want (split[beginIndex]).
            final String[] split = data.split(" ");
            return split[beginIndex];
        }

        return "";
    }
}
