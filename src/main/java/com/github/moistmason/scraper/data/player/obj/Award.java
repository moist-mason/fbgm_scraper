package com.github.moistmason.scraper.data.player.obj;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;

import java.util.List;

/**
 * Award.
 *
 * @param season Season.
 * @param type Used for hardcoded awards: All-Star and Champion.
 * @param name Full name of regular award.
 * @param shortName Full
 * @param rank For individual awards, indicates the voting placement of the player. For team awards, indicates the team # (First or Second).
 */
public record Award(
        int season,
        String type,
        String name,
        String shortName,
        int rank
) {

    /**
     * @param elements From one of the stat tables.
     * @return object.
     */
    public static Award fromHtml(final List<Element> elements) {
        return null;
    }

    public JsonObject toJson() {
        return null;
    }
}
