package com.github.moistmason.scraper.util;

import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class HtmlUtil {
    public static final String[] LETTERS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
    public static final String PFR_URL = "https://www.pro-football-reference.com/";
    public static final String PFR_PLAYER_INDEX_HEADER = PFR_URL + "players/";

    // Requests for creating and destroying browser sessions.
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
            System.out.print(""); // Need to put something here, but I don't want anything from this flooding the console.
        }
    }

    // Checks what sessions are active. Not used very much, but kept in case I need it.
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

    // Requests the URL, returns the JSON response.
    public static Optional<String> getUrlResponse(final String url, final String id) throws IOException, InterruptedException {

        // Requests are 30 seconds until timing out.
        // This seems aggressive, but requests that go beyond 30s will usually error out regardless of the set timeout length, so why wait?
        final String jsonPayload =
                """
                {
                    "cmd": "request.get",
                    "url": "${url}",
                    "session": "${id}",
                    "maxTimeout": 30000
                }
                """;
        return getResponse(jsonPayload.replace("${url}", url)
                .replace("${id}", id)
        );
    }

    // Gets the response from the input payload.
    public static Optional<String> getResponse(final String payload) throws IOException, InterruptedException {
        try (final HttpClient client = HttpClient.newHttpClient()) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8191/v1"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Returns empty if an error 500 occurred, meaning that FlareSolverr failed to detect and/or solve the challenge.
            return response.statusCode() != 500 ? Optional.of(response.body()) : Optional.empty();
        }
    }

    // Gets the actual HTML Document data from the response JSON, using JSoup.
    public static Document getDocument(final JsonObject root) {
        final JsonObject solution = root.get("solution").getAsJsonObject();
        final String response = solution.get("response").getAsString();
        return Jsoup.parse(response);
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
        final Optional<Element> tbody = getElementByTag(parent, "tbody");
        return tbody.isPresent() ? HtmlUtil.getAllElementsByTag(tbody.get(), "tr") : Optional.empty();
    }

    public static Optional<String> getStringData(final Element tableRow, final String attributeName) {
        final Optional<Element> cell = tableRow.getElementsByAttributeValue("data_stat", attributeName).stream().findFirst();
        return cell.map(Element::text);
    }

    public static Optional<Integer> getIntegerData(final Element tableRow, final String attributeName) {
        return getStringData(tableRow, attributeName).map(Integer::parseInt);
    }

    public static Optional<Double> getDoubleData(final Element tableRow, final String attributeName) {
        return getStringData(tableRow, attributeName).map(Double::parseDouble);
    }
}
