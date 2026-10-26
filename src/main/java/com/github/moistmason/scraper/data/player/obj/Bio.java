package com.github.moistmason.scraper.data.player.obj;

import com.github.moistmason.scraper.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;

import java.util.List;

public record Bio(
        String name,
        String pos,
        int height,
        int weight,
        int birthYear,
        int diedYear,
        String country,
        String college,
        Draft draft,
        List<Relative> relatives,
        List<JerseyNumber> jerseyNumbers
) implements Ser<Bio> {

    /**
     * @param elements Primarily from the top section of a player's PFR page. Also includes one of the stat table elements in case
     * a player was never drafted. See the Draft object for more info.
     * @return object.
     */
    @Override
    public Bio fromHtml(final List<Element> elements) {
        return null;
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("pos", pos);
        json.addProperty("height", height);
        json.addProperty("weight", weight);
        json.addProperty("birthYear", birthYear);

        if (diedYear > 0) {
            json.addProperty("diedYear", diedYear);
        }

        json.addProperty("country", country);
        json.addProperty("college", college);
        json.add("draft", draft.toJson());

        if (!jerseyNumbers.isEmpty()) {
            final JsonArray jerseyNumbersA = JsonUtil.toJsonArray(jerseyNumbers, JerseyNumber::toJson);
            json.add("jerseyNumbers", jerseyNumbersA);
        }
        return json;
    }

    /**
     *
     * @param year The draft year.
     * @param gameYear The draft year, minus one. <b>This should be used for a player's draft year in a roster, instead of the real-life year.</b>
     * Football GM groups the season and the draft for the following season in the same calendar year.
     * @param round Draft round.
     * @param pick Draft pick.
     */
    public record Draft(
            int year,
            int gameYear,
            int round,
            int pick
    ) implements Ser<Draft> {

        public static Draft undrafted(final int year) {
            return new Draft(year, year - 1, 0, 0);
        }

        /**
         * @param elements From the following:
         * <ul>
         *     <li><b>Drafted NFL players</b>: From the draft section on the player page. If a player has draft information for both
         *     the NFL and AFL (or some other league), only the NFL info is used. </li>
         *     <li><b>Drafted AFL-only players</b>: Same source as point 1, but If a player was only ever drafted by a non-NFL league,
         *     they will be marked undrafted.</li>
         *     <li><b>Undrafted players</b>: All undrafted players use their first played season as their draft year,
         *     so we use one of their stat tables and go from there.</li>
         * </ul>
         * @return object.
         */
        @Override
        public Draft fromHtml(final List<Element> elements) {
            return null;
        }

        @Override
        public JsonObject toJson() {
            return null;
        }
    }

    public record Relative(String slug, String type) { }

    public record JerseyNumber(
            int number,
            int from,
            int to,
            String team
    ) {

        public static JerseyNumber fromHtml(final Element parent) {
            int number;
            int from;
            int to;
            String team;

            final String href = parent.attr("href");
            final String[] split = href.split("\\?"); // split the HTML ref to separate the part listing team and number.
            final String[] teamNumberSplit = split[1].split("&"); // Split the team and number component of the href.
            team = teamNumberSplit[0].substring(teamNumberSplit[0].indexOf("=") + 1);
            number = Integer.parseInt(teamNumberSplit[1].substring(teamNumberSplit[1].indexOf("=") + 1));

            final String dataTip = parent.attr("data-tip");
            final String[] dataTipSplit = dataTip.split(" ");
            final String years = dataTipSplit[split.length - 1];

            // Dash indicates multi-year range.
            if (years.contains("-")) {
                from = Integer.parseInt(years.split("-")[0]);
                to = Integer.parseInt(years.split("-")[1]);
            } else { // No dash means it's just a single year
                from =  Integer.parseInt(years);
                to = number;
            }

            return new JerseyNumber(number, from, to, team);
        }

        public JsonObject toJson() {
            final JsonObject json = new JsonObject();
            json.addProperty("number", number);
            json.addProperty("from", from);
            json.addProperty("to", to);
            json.addProperty("team", team);
            return json;
        }
    }
}
