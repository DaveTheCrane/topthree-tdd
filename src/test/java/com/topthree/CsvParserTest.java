package com.topthree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CsvParser following TDD Red-Green-Refactor cycles.
 */
class CsvParserTest {
    private CsvParser csvParser;

    @BeforeEach
    void setUp() {
        // During RED phase, CsvParser implementation does not exist yet.
        // This will be implemented in the GREEN phase (3.2).
        // For now, we use a stub that throws UnsupportedOperationException.
        csvParser = new CsvParser() {
            @Override
            public Result<ScoreRecord, ParseError> parseLine(String csvLine) {
                throw new UnsupportedOperationException("CsvParser.parseLine not yet implemented");
            }

            @Override
            public Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines) {
                throw new UnsupportedOperationException("CsvParser.parseLines not yet implemented");
            }
        };
    }

    /**
     * RED PHASE TEST: Valid six-field CSV line parses to correct ScoreRecord
     * 
     * Input: "alice,Alice Wonder,pac-man,Pac-Man,10,75"
     * Expected: ScoreRecord with:
     *   - Player(alice, Alice Wonder)
     *   - GameEntry(pac-man, Pac-Man, 10, 75)
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
}
