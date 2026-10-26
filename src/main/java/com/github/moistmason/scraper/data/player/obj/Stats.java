package com.github.moistmason.scraper.data.player.obj;

import com.google.gson.JsonObject;
import org.jsoup.nodes.Element;

import java.util.List;

import static com.github.moistmason.scraper.util.HtmlUtil.*;
import static com.github.moistmason.scraper.util.JsonUtil.EMPTY_INT;

/**
 * Different stat categories.
 * <p>
 * Only major category not included here are penalties. For some reason PFR lists them separately and by game.
 * Fumbles lost (fmbLost) are also not here, which is less weird. For both, I'm considering using the stats I have from
 * Pro Football Archive. For now, though, leave blank.
 * </p>
 */
public record Stats(
        List<General> generalStats,
        List<Passing> passingStats,
        List<RushingReceiving> rushingReceivingStats,
        List<DefenseFumble> defenseFumbleStats,
        List<KickPuntReturn> kickPuntReturnStats,
        List<Kicking> kickingStats,
        List<Punting> puntingStats
) {

    public record General(
            int season,
            String team,
            String pos,
            int gp,
            int gs,
            int av
    ) {

        /**
         *
         * @param element All the stat tables have these values, so pick the first on a player's page.
         * @return object.
         */
        public static General fromHtml(final Element element) {
            return null;
        }

        public JsonObject toJson() {
            return null;
        }
    }

    public interface StatCategory { }

    public record Passing(
            int season,
            String team,
            String pos,
            int qbW,
            int qbL,
            int qbT,
            int pssCmp,
            int pss,
            int pssYds,
            int pssTD,
            int pssLng,
            int pssSk,
            int pssSkYds
    ) implements StatCategory {

        public static Passing fromHtml(final Element element) {
            final String qbRec = getStringData(element, "qb_rec").orElse(null);
            return new Passing(
                    getIntegerData(element, "year_id").orElseThrow(),
                    getStringData(element, "team_name_abbrev").orElse(null),
                    getStringData(element, "pos").orElseThrow(),
                    getQbRecStat(qbRec, 0),
                    getQbRecStat(qbRec, 1),
                    getQbRecStat(qbRec, 2),
                    getIntegerData(element, "pass_cmp").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_att").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_yds").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_td").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_long").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_sacked").orElse(EMPTY_INT),
                    getIntegerData(element, "pass_sacked_yds").orElse(EMPTY_INT)
            );
        }

        // QB records are stored in a single cell value.
        private static int getQbRecStat(final String data, final int index) {
            if (data == null) {
                return EMPTY_INT;
            } else {
                final String[] split = data.split("-");
                final int[] array = new int[]{ Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) };
                return array[index];
            }
        }

        public JsonObject toJson() {
            return null;
        }
    }

    public record RushingReceiving(
            int season,
            String team,
            String pos,
            int rus,
            int rusYds,
            int rusTD,
            int rusLng,
            int tgt,
            int rec,
            int recYds,
            int recTD,
            int recLng
    ) implements StatCategory {

        public static RushingReceiving fromHtml(final Element element) {
            return new RushingReceiving(
                    getIntegerData(element, "year_id").orElseThrow(),
                    getStringData(element, "team_name_abbrev").orElse(null),
                    getStringData(element, "pos").orElseThrow(),
                    getIntegerData(element, "rush_att").orElse(EMPTY_INT),
                    getIntegerData(element, "rush_yds").orElse(EMPTY_INT),
                    getIntegerData(element, "rush_td").orElse(EMPTY_INT),
                    getIntegerData(element, "rush_long").orElse(EMPTY_INT),
                    getIntegerData(element, "targets").orElse(EMPTY_INT),
                    getIntegerData(element, "rec").orElse(EMPTY_INT),
                    getIntegerData(element, "rec_yds").orElse(EMPTY_INT),
                    getIntegerData(element, "rec_td").orElse(EMPTY_INT),
                    getIntegerData(element, "rec_long").orElse(EMPTY_INT)
            );
        }
    }

    public record DefenseFumble(
            int season,
            String team,
            String pos,
            int defInt,
            int defIntYds,
            int defIntLng,
            int defPssDef,
            int fmb,
            int defFmbFrc,
            int defFmbRec,
            int defFmbYds,
            int defFmbTD,
            double defSk,
            int defTckSolo,
            int defTckAst,
            int defTckLoss,
            int defSft
    ) implements StatCategory {

    }

    public record KickPuntReturn(
            int season,
            String team,
            String pos,
            int pr,
            int prYds,
            int prTD,
            int prLng,
            int kr,
            int krYds,
            int krTD,
            int krLng
    ) { }

    public record Kicking(
            int season,
            String team,
            String pos,
            int fg0,
            int fga0,
            int fg20,
            int fga20,
            int fg30,
            int fga30,
            int fg40,
            int fga40,
            int fg50,
            int fga50,
            int fgLng,
            int xp,
            int xpm,
            int ko,
            int koYds,
            int koTB,
            int ok,
            int okRec
    ) { }

    public record Punting(
            int season,
            String team,
            String pos,
            int pnt,
            int pntYds,
            int pntLng,
            int pntTB,
            int pntIn20,
            int pntBlk
    ) { }
}
