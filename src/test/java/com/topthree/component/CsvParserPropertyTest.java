package com.topthree.component;

import com.topthree.model.*;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.*;

public class CsvParserPropertyTest {
    private final CsvParser parser = new CsvParserImpl();
    private final PrettyPrinter printer = new PrettyPrinterImpl();

    // Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
    @Property(tries = 100)
    void roundTripParsePrintParse(@ForAll("validScoreRecords") ScoreRecord record) {
        String csv = printer.print(record);
        Result<ScoreRecord, ParseError> result = parser.parseLine(csv);

        assertThat(result).isInstanceOf(Result.Ok.class);
        Result.Ok<ScoreRecord, ParseError> ok = (Result.Ok<ScoreRecord, ParseError>) result;
        assertThat(ok.value()).isEqualTo(record);
    }

    @Provide
    Arbitrary<ScoreRecord> validScoreRecords() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.integers().between(1, 100),
            Arbitraries.integers().between(1, 100)
        ).as((playerId, playerName, gameId, gameName, hours, score) -> {
            Player player = new Player(playerId, playerName);
            GameEntry gameEntry = new GameEntry(gameId, gameName, hours, score);
            return new ScoreRecord(player, gameEntry);
        });
    }
}
