package com.github.moistmason.scraper.data.player.obj;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Optional;

// Serializing interface
public interface Ser<T> {

    // Gets type T from the HTML element(s).
    T fromHtml(final List<Element> elements);

    // Gets the JSON tree of type T.
    JsonObject toJson();
}
