package com.github.moistmason.scraper.data.player.obj;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Serializing interface
public interface Ser<T> {

    // Gets type T from the HTML element(s).
    T fromHtml(final List<Element> elements);

    default T fromHtml(final Element element) {
        return fromHtml(Collections.singletonList(element));
    }

    // Gets the JSON tree of type T.
    JsonObject toJson();
}
