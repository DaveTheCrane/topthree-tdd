package com.topthree;

import org.junit.jupiter.api.Test;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests and property-based tests for CsvParser following TDD Red-Green-Refactor cycles.
 */
class CsvParserTest {
    private static final CsvParser csvParser = new CsvParserImpl();
    private static final PrettyPrinter prettyPrinter = new PrettyPrinterImpl();

    // ─────────────────────────────────────────────────────
    // UNIT TESTS (RED/GREEN/REFACTOR PHASES)
    // ─────────────────────────────────────────────────────

    /**
     * Test 3.1 & 3.2: Valid six-field CSV line parses to correct ScoreRecord
     */
    @Test
    void testValidSixFieldLineParses() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10,75";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        ScoreRecord expected = new ScoreRecord(
            new Player("alice", "Alice Wonder"),
            new GameEntry("pac-man", "Pac-Man", 10, 75)
        );
        
        assertThat(result).isEqualTo(new Result.Ok<ScoreRecord, ParseError>(expected));
    }

    /**
     * Test 3.3 & 3.4: Fewer than six fields returns ParseError
     */
    @Test
    void testFewerThanSixFieldsReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
        if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
            assertThat(err.error().message()).contains("6");
        }
    }

    /**
     * Test 3.5 & 3.6: More than six fields returns ParseError
     */
    @Test
    void testMoreThanSixFieldsReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10,75,extra";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
        if (result instanceof Result.Err<ScoreRecord, ParseError> err) {
            assertThat(err.error().message()).contains("6");
        }
    }

    /**
     * Test 3.7 & 3.8: Non-integer hours-played returns ParseError
     */
    @Test
    void testNonIntegerHoursPlayedReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,abc,75";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.9 & 3.10: Non-integer normalised-score returns ParseError
     */
    @Test
    void testNonIntegerNormalisedScoreReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10,1.5";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.11 & 3.12: Normalised score of 0 returns ParseError
     */
    @Test
    void testNormalisedScoreZeroReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10,0";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.13 & 3.14: Normalised score of 101 returns ParseError
     */
    @Test
    void testNormalisedScore101ReturnsError() {
        String csvLine = "alice,Alice Wonder,pac-man,Pac-Man,10,101";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.15 & 3.16: Empty player id returns ParseError
     */
    @Test
    void testEmptyPlayerIdReturnsError() {
        String csvLine = ",Alice Wonder,pac-man,Pac-Man,10,75";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.17 & 3.18: Empty game id returns ParseError
     */
    @Test
    void testEmptyGameIdReturnsError() {
        String csvLine = "alice,Alice Wonder,,Pac-Man,10,75";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.19 & 3.20: Fields with leading/trailing whitespace parse to trimmed values
     */
    @Test
    void testWhitespaceTrimmingPreservesValues() {
        String csvLine = "  alice  ,  Alice Wonder  ,  pac-man  ,  Pac-Man  ,  10  ,  75  ";
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        ScoreRecord expected = new ScoreRecord(
            new Player("alice", "Alice Wonder"),
            new GameEntry("pac-man", "Pac-Man", 10, 75)
        );
        
        assertThat(result).isEqualTo(new Result.Ok<ScoreRecord, ParseError>(expected));
    }

    /**
     * Test 3.21 & 3.22: parseLines returns all ScoreRecords in order for a valid list
     */
    @Test
    void testParseLinesPreservesOrderOfValidRecords() {
        List<String> csvLines = List.of(
            "alice,Alice Wonder,pac-man,Pac-Man,10,75",
            "bob,Bob Builder,snake,Snake,5,50"
        );
        
        Result<List<ScoreRecord>, ParseError> result = csvParser.parseLines(csvLines);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<List<ScoreRecord>, ParseError> ok) {
            assertThat(ok.value()).hasSize(2);
            assertThat(ok.value().get(0).player().playerId()).isEqualTo("alice");
            assertThat(ok.value().get(1).player().playerId()).isEqualTo("bob");
        }
    }

    /**
     * Test 3.23 & 3.24: parseLines short-circuits on first error
     */
    @Test
    void testParseLinesShortCircuitsOnFirstError() {
        List<String> csvLines = List.of(
            "alice,Alice Wonder,pac-man,Pac-Man,10,75",
            "invalid,line,with,only,four",
            "bob,Bob Builder,snake,Snake,5,50"
        );
        
        Result<List<ScoreRecord>, ParseError> result = csvParser.parseLines(csvLines);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Test 3.25 & 3.26: PrettyPrinter formats a ScoreRecord as six-field CSV
     */
    @Test
    void testPrettyPrinterFormatsScoreRecordAsCsv() {
        ScoreRecord record = new ScoreRecord(
            new Player("alice", "Alice Wonder"),
            new GameEntry("pac-man", "Pac-Man", 10, 75)
        );
        
        String csv = prettyPrinter.print(record);
        
        assertThat(csv).isEqualTo("alice,Alice Wonder,pac-man,Pac-Man,10,75");
    }

    // ─────────────────────────────────────────────────────
    // PROPERTY-BASED TESTS (jqwik) - simplified
    // ─────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────
    // PROPERTY TESTS (jqwik @ 100 tries each)
    // ─────────────────────────────────────────────────────

    /**
     * Feature: top-three-high-scores, Property 1: Valid CSV line parses to correct fields
     * Validates: Requirements 1.1
     */
    @Property(tries = 100)
    void property1_validCsvLineParses(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @IntRange(min = 1, max = 100) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int score
    ) {
        String csvLine = String.format("%s,%s,%s,%s,%d,%d", 
            playerId, playerName, gameId, gameName, hoursPlayed, score);
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Ok.class);
        if (result instanceof Result.Ok<ScoreRecord, ParseError> ok) {
            ScoreRecord record = ok.value();
            assertThat(record.player().playerId()).isEqualTo(playerId);
            assertThat(record.gameEntry().hoursPlayed()).isEqualTo(hoursPlayed);
            assertThat(record.gameEntry().normalisedScore()).isEqualTo(score);
        }
    }

    /**
     * Feature: top-three-high-scores, Property 2: Out-of-range normalised score returns an error
     * Validates: Requirements 1.2
     */
    @Property(tries = 100)
    void property2_outOfRangeScoreReturnsError(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @IntRange(min = 1, max = 100) int hoursPlayed,
            @ForAll int invalidScore
    ) {
        Assume.that(invalidScore < 1 || invalidScore > 100);
        
        String csvLine = String.format("%s,%s,%s,%s,%d,%d", 
            playerId, playerName, gameId, gameName, hoursPlayed, invalidScore);
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Feature: top-three-high-scores, Property 3: Non-integer hours-played returns an error
     * Validates: Requirements 1.3
     */
    @Property(tries = 100)
    void property3_nonIntegerHoursReturnsError(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @StringLength(min = 2, max = 5) String invalidHours,
            @ForAll @IntRange(min = 1, max = 100) int score
    ) {
        // Create an invalid hours string by replacing digits with letters
        String badHours = invalidHours.replaceAll("\\d", "x");
        Assume.that(!badHours.matches("^-?\\d+$") && !badHours.isEmpty());
        
        String csvLine = String.format("%s,%s,%s,%s,%s,%d", 
            playerId, playerName, gameId, gameName, badHours, score);
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Feature: top-three-high-scores, Property 4: Non-integer normalised-score field returns an error
     * Validates: Requirements 1.4
     */
    @Property(tries = 100)
    void property4_nonIntegerScoreFieldReturnsError(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @IntRange(min = 1, max = 100) int hoursPlayed,
            @ForAll @StringLength(min = 2, max = 5) String invalidScore
    ) {
        // Avoid pure digit strings - add alpha chars to make invalid
        String badScore = invalidScore.replaceAll("\\d", "x");
        Assume.that(!badScore.matches("^-?\\d+$") && !badScore.isEmpty());
        
        String csvLine = String.format("%s,%s,%s,%s,%d,%s", 
            playerId, playerName, gameId, gameName, hoursPlayed, badScore);
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csvLine);
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Feature: top-three-high-scores, Property 5: Wrong field count returns an error
     * Validates: Requirements 1.5, 1.6
     */
    @Property(tries = 100)
    void property5_wrongFieldCountReturnsError(
            @ForAll @IntRange(min = 0, max = 10) int fieldCount
    ) {
        Assume.that(fieldCount != 6);
        
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < fieldCount; i++) {
            if (i > 0) csv.append(",");
            csv.append("f").append(i);
        }
        
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csv.toString());
        
        assertThat(result).isInstanceOf(Result.Err.class);
    }

    /**
     * Feature: top-three-high-scores, Property 6: Whitespace trimming preserves field values
     * Validates: Requirements 1.10
     */
    @Property(tries = 100)
    void property6_whitespaceTrimming(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @IntRange(min = 1, max = 100) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int score
    ) {
        // Parse the trimmed version
        String csvTrimmed = String.format("%s,%s,%s,%s,%d,%d", 
            playerId, playerName, gameId, gameName, hoursPlayed, score);
        Result<ScoreRecord, ParseError> resultTrimmed = csvParser.parseLine(csvTrimmed);
        
        // Parse the padded version (add spaces around fields)
        String csvPadded = String.format("  %s  ,  %s  ,  %s  ,  %s  ,  %d  ,  %d  ", 
            playerId, playerName, gameId, gameName, hoursPlayed, score);
        Result<ScoreRecord, ParseError> resultPadded = csvParser.parseLine(csvPadded);
        
        assertThat(resultTrimmed).isEqualTo(resultPadded);
    }

    /**
     * Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
     * Validates: Requirements 1.11, 1.12
     */
    @Property(tries = 100)
    void property7_roundTripParsePrintParse(
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String playerId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String playerName,
            @ForAll @AlphaChars @StringLength(min = 1, max = 5) String gameId,
            @ForAll @CharRange(from = 'A', to = 'z') @StringLength(min = 1, max = 10) String gameName,
            @ForAll @IntRange(min = 1, max = 100) int hoursPlayed,
            @ForAll @IntRange(min = 1, max = 100) int score
    ) {
        ScoreRecord original = new ScoreRecord(
            new Player(playerId, playerName),
            new GameEntry(gameId, gameName, hoursPlayed, score)
        );
        
        String csv = prettyPrinter.print(original);
        Result<ScoreRecord, ParseError> result = csvParser.parseLine(csv);
        
        assertThat(result).isEqualTo(new Result.Ok<ScoreRecord, ParseError>(original));
    }
}
