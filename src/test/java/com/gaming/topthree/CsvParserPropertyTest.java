package com.gaming.topthree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link CsvParserImpl} (Properties 1-7).
 */
class CsvParserPropertyTest {

    private final CsvParser parser = new CsvParserImpl();
    private final PrettyPrinter printer = new PrettyPrinterImpl();

    // Fields free of commas and surrounding whitespace, non-blank.
    private Arbitrary<String> safeToken() {
        return Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(8);
    }

    @Provide
    Arbitrary<ScoreRecord> validScoreRecords() {
        Arbitrary<String> playerId = safeToken();
        Arbitrary<String> playerName = safeToken();
        Arbitrary<String> gameId = safeToken();
        Arbitrary<String> gameName = safeToken();
        Arbitrary<Integer> hours = Arbitraries.integers().between(0, 1000);
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);
        return Combinators.combine(playerId, playerName, gameId, gameName, hours, score)
                .as((pid, pname, gid, gname, h, s) ->
                        new ScoreRecord(new Player(pid, pname), new GameEntry(gid, gname, h, s)));
    }

    private String toCsv(ScoreRecord r) {
        return String.join(",",
                r.player().playerId(),
                r.player().playerName(),
                r.gameEntry().gameId(),
                r.gameEntry().gameName(),
                String.valueOf(r.gameEntry().hoursPlayed()),
                String.valueOf(r.gameEntry().normalisedScore()));
    }

    // Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
    @Property(tries = 1000)
    void validLineParsesToCorrectFields(@ForAll @From("validScoreRecords") ScoreRecord record) {
        Result<ScoreRecord, ParseError> result = parser.parseLine(toCsv(record));

        assertThat(result).isEqualTo(Result.ok(record));
    }

    // Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
    @Property(tries = 1000)
    void outOfRangeScoreReturnsError(
            @ForAll @From("validScoreRecords") ScoreRecord record,
            @ForAll("outOfRangeScores") int badScore) {
        String line = String.join(",",
                record.player().playerId(),
                record.player().playerName(),
                record.gameEntry().gameId(),
                record.gameEntry().gameName(),
                String.valueOf(record.gameEntry().hoursPlayed()),
                String.valueOf(badScore));

        assertThat(parser.parseLine(line)).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<Integer> outOfRangeScores() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(Integer.MIN_VALUE, 0),
                Arbitraries.integers().between(101, Integer.MAX_VALUE));
    }

    // Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
    @Property(tries = 1000)
    void nonIntegerHoursReturnsError(
            @ForAll @From("validScoreRecords") ScoreRecord record,
            @ForAll("nonIntegerTokens") String badHours) {
        String line = String.join(",",
                record.player().playerId(),
                record.player().playerName(),
                record.gameEntry().gameId(),
                record.gameEntry().gameName(),
                badHours,
                String.valueOf(record.gameEntry().normalisedScore()));

        assertThat(parser.parseLine(line)).isInstanceOf(Result.Err.class);
    }

    // Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns an error
    @Property(tries = 1000)
    void nonIntegerScoreReturnsError(
            @ForAll @From("validScoreRecords") ScoreRecord record,
            @ForAll("nonIntegerTokens") String badScore) {
        String line = String.join(",",
                record.player().playerId(),
                record.player().playerName(),
                record.gameEntry().gameId(),
                record.gameEntry().gameName(),
                String.valueOf(record.gameEntry().hoursPlayed()),
                badScore);

        assertThat(parser.parseLine(line)).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<String> nonIntegerTokens() {
        // Non-empty strings of letters that never parse as integers.
        return Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(6);
    }

    // Feature: top-three-high-scores, Property 5: Wrong field count returns an error
    @Property(tries = 1000)
    void wrongFieldCountReturnsError(
            @ForAll @From("validScoreRecords") ScoreRecord record,
            @ForAll @IntRange(min = 0, max = 10) int fieldCount) {
        if (fieldCount == 6) {
            return; // only exercise counts that are not exactly six
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldCount; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("x");
        }

        assertThat(parser.parseLine(sb.toString())).isInstanceOf(Result.Err.class);
    }

    // Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
    @Property(tries = 1000)
    void whitespaceTrimmingPreservesValues(@ForAll @From("validScoreRecords") ScoreRecord record) {
        String padded = String.join(",",
                "  " + record.player().playerId() + " ",
                " " + record.player().playerName() + "  ",
                "   " + record.gameEntry().gameId() + " ",
                " " + record.gameEntry().gameName() + "   ",
                "  " + record.gameEntry().hoursPlayed() + " ",
                " " + record.gameEntry().normalisedScore() + "  ");

        assertThat(parser.parseLine(padded)).isEqualTo(Result.ok(record));
    }

    // Feature: top-three-high-scores, Property 7: Parse -> print -> parse round trip
    @Property(tries = 1000)
    void roundTripProperty(@ForAll @From("validScoreRecords") ScoreRecord record) {
        String csv = printer.print(record);
        Result<ScoreRecord, ParseError> result = parser.parseLine(csv);

        assertThat(result).isEqualTo(Result.ok(record));
    }
}
