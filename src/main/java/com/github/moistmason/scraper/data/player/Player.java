package com.github.moistmason.scraper.data.player;

import com.github.moistmason.scraper.data.player.obj.Award;
import com.github.moistmason.scraper.data.player.obj.Bio;
import com.github.moistmason.scraper.data.player.obj.Stats;
import com.github.moistmason.scraper.util.HtmlUtil;
import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static com.github.moistmason.scraper.util.JsonUtil.EMPTY_INT;

/**
 * Represents the root player object, represented in the final JSON data.
 *
 * @param awards The player's awards.
 * @param bio The player biographical info.
 * @param stats The player's regular season stats.
 * @param playoffStats The player's playoff stats.
 */
public record Player(
        List<Award> awards,
        Bio bio,
        Stats stats,
        Stats playoffStats
) {

    public static List<Stats> getStats(final Element parent, final boolean post) {
        /*
        return new Stats(
                getStatCategory(parent, "passing", post, Stats.Passing::fromHtml),
                getStatCategory(parent, "rushing_and_receiving", post, Stats.RushingReceiving::fromHtml),
        )
         */
        return null;
    }

    /**
     * Retrieves general information about a player.
     * To find games played and AV for a specific season, we parse the first-listed stat table.
     * To find the jersey number, we have to look at the section in the header.
     * @param parent The parent.
     * @return
     */
    public static List<Stats.General> getGeneralInfo(final Element parent) {
        return null;
    }

    /**
     * Gets the jersey number for the provided season.
     *
     * @param parent
     * @param season
     * @param team
     * @return
     */
    public static int getJerseyNumberForSeason(final Element parent, final int season, final String team) {
        int number;

        for (Element element : parent.children()) {

            final String href = element.attr("href");
            final String[] split = href.split("\\?"); // split the HTML ref to separate the part listing team and number.
            final String[] teamNumberSplit = split[1].split("&"); // Split the team and number component of the href.
            final String hrefTeam = teamNumberSplit[0].substring(teamNumberSplit[0].indexOf("=") + 1);

            if (hrefTeam.equals(team.toLowerCase(Locale.ROOT))) {
                number = Integer.parseInt(teamNumberSplit[1].substring(teamNumberSplit[1].indexOf("=") + 1));

                // Get the hover tooltip that shows team and years. Split into spaces, and get the last value of the split (the year(s)).
                final String dataTip = element.attr("data-tip");
                final String[] dataTipSplit = dataTip.split(" ");
                final String years = split[split.length - 1];

                // Dash indicates multi-year range. Return the jersey number if the provided season fits within that range.
                if (years.contains("-")) {
                    final int min = Integer.parseInt(years.split("-")[0]);
                    final int max = Integer.parseInt(years.split("-")[1]);

                    if (season <= min && season >= max) {
                        return number;
                    }
                } else { // No dash means it's just a single year, so just check if the year matches the provided season.
                    if (season == Integer.parseInt(years)) {
                        return number;
                    }
                }
            }
        }

        return EMPTY_INT; // Not sure what circumstances this would ever be returned.
    }

    /**
     * Gets the stat table data for the provided stat type.
     *
     * @param <C> The stat category (passing, rushing/receiving, defense, etc.)
     * @param parent The "content" section of the HTML page.
     * @param id The category ID.
     * @param post {@code } true if this is the postseason table.
     * @param htmlFunc Function that converts HTML into an object in the {@link Stats.StatCategory} hierarchy.
     *
     * @return List of stat entries.
     */
    public static <C extends Stats.StatCategory> List<C> getStatCategory(
            final Element parent,
            final String id,
            final boolean post,
            final Function<Element, C> htmlFunc
    ) {
        final List<C> data = new ArrayList<>();
        final String postSuffix = post ? "_post" : "";
        final Optional<Element> all = HtmlUtil.getElement(parent, "all_" + id);

        if (all.isPresent()) {
            final Optional<Element> switcher = HtmlUtil.getElement(parent, "switcher_" + id);

            if (switcher.isPresent()) {
                final String divId = "div_" + id + postSuffix;
                final Optional<Element> div = HtmlUtil.getElement(all.get(), "div_" + divId);

                if (div.isPresent()) {
                    final String tableId = id + postSuffix;
                    final Optional<Element> table = HtmlUtil.getElement(div.get(), tableId);

                    if (table.isPresent()) {
                        final Optional<Elements> rows = HtmlUtil.getTableRows(table.get());

                        if (rows.isPresent()) {
                            for (final Element row : rows.get()) {
                                C line = htmlFunc.apply(row);
                                data.add(line);
                            }
                        }
                    }
                }
            }
        }

        return data;
    }

    public static List<Stats.Passing> getPassing(final Element parent, final boolean playoffs) {
        return getStatCategory(parent, "passing", playoffs, Stats.Passing::fromHtml);
    }

    public JsonObject toJson() {
        return null;
    }
}
