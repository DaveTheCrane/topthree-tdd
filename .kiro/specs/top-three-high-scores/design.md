# Design Document: Top Three High Scores

## Overview

The system processes a weekly CSV export from a gaming platform to determine the top three players by weighted score. It is structured as a four-component pipeline following hexagonal architecture: each component is a pure function (or single-responsibility class) with no I/O side effects, making every step independently testable under strict TDD.

Processing order matches the data flow:

```
raw CSV strings
      │
      ▼
 CSV_Parser  ──► [Score_Record]
      │
      ▼
Score_Aggregator ──► [Player_Aggregate]
      │
      ▼
Leaderboard_Ranker ──► Ranked_Result
      │
      ▼
Pipeline (wires all three)
```

Components are built in edge-case-density order: CSV_Parser first (most edge cases), then Score_Aggregator, then Leaderboard_Ranker, then Pipeline.

---

## Architecture

The design follows hexagonal (ports-and-adapters) architecture. Each component exposes a single public interface (port) and contains all its logic internally (adapter). No component depends on another at compile time — they are wired together only in the Pipeline.

```
┌─────────────────────────────────────────────────────────┐
│                        Pipeline                         │
│                                                         │
│  ┌─────────────┐  ┌──────────────────┐  ┌───────────┐  │
│  │ CSV_Parser  │  │ Score_Aggregator │  │ Leaderboard│  │
│  │  (port)     │  │    (port)        │  │  _Ranker   │  │
│  └─────────────┘  └──────────────────┘  │  (port)   │  │
│                                         └───────────┘  │
└─────────────────────────────────────────────────────────┘
```

Each component returns a discriminated result type (`Either`-style) — either a success value or a descriptive error — so errors propagate without exceptions crossing component boundaries.

### Build Order Rationale

1. **CSV_Parser** — highest edge-case count (field count, type validation, range validation, whitespace, empty ids, round-trip). Interfaces may need adjustment once edge cases are fully explored.
2. **Score_Aggregator** — depends only on `Score_Record`; moderate edge cases (empty input, single record, multi-record, name conflict).
3. **Leaderboard_Ranker** — depends only on `Player_Aggregate`; tie logic is the main complexity.
4. **Pipeline** — wires the three above; edge cases are mostly delegated to components.

---

## Components and Interfaces

### CSV_Parser

Converts a list of raw CSV strings into a list of `Score_Record` objects.

```java
public interface CsvParser {
    /**
     * Parses a single CSV line into a Score_Record.
     * Returns ParseError if the line is malformed or values are out of range.
     */
    Result<ScoreRecord, ParseError> parseLine(String csvLine);

    /**
     * Parses an ordered list of CSV lines.
     * Returns the first ParseError encountered, or the full list of ScoreRecords.
     */
    Result<List<ScoreRecord>, ParseError> parseLines(List<String> csvLines);
}
```

Expected CSV column order (1-indexed):

| # | Field | Type | Constraints |
|---|-------|------|-------------|
| 1 | player_id | String | non-empty after trim |
| 2 | player_name | String | trimmed |
| 3 | game_id | String | non-empty after trim |
| 4 | game_name | String | trimmed |
| 5 | hours_played | int | parseable integer |
| 6 | normalised_score | int | integer, 1–100 inclusive |

Exactly six fields required. Whitespace around each field is trimmed before validation.

### Pretty_Printer

Formats a `Score_Record` back into canonical CSV. Lives alongside `CsvParser` in the parsing layer.

```java
public interface PrettyPrinter {
    /**
     * Formats a ScoreRecord as a six-field comma-separated string.
     * No leading/trailing whitespace around fields.
     */
    String print(ScoreRecord record);
}
```

### Score_Aggregator

Converts a list of `Score_Record` objects into one `Player_Aggregate` per distinct player id.

```java
public interface ScoreAggregator {
    /**
     * Aggregates score records by player id.
     * If the same player id appears with different display names, the last-seen name wins.
     * Never returns an error for name conflicts.
     */
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}
```

Weighted score per game entry: `hours_played × normalised_score`.  
Total score per player: sum of all weighted scores.

### Leaderboard_Ranker

Sorts `Player_Aggregate` objects and returns a `Ranked_Result` that surfaces tie ambiguity at boundary positions.

```java
public interface LeaderboardRanker {
    /**
     * Ranks players by Total_Score descending and returns a RankedResult.
     * definite_winners: players unambiguously in the top three.
     * tied_candidates: players sharing the boundary score that creates ambiguity.
     */
    RankedResult rank(List<PlayerAggregate> aggregates);
}
```

Tie logic summary:
- Sort all players descending by `totalScore`.
- Take up to three. If the player at position 3 (0-indexed: index 2) shares their score with any player at position 4+, those players are all `tied_candidates` and none of them are `definite_winners`.
- Players strictly above the boundary score are `definite_winners`.
- If all players share the same score, `definite_winners` is empty and all are `tied_candidates`.

### Pipeline

Wires all three components end-to-end.

```java
public interface TopThreePipeline {
    /**
     * Runs the full pipeline: parse → aggregate → rank.
     * Returns PipelineError on any invalid CSV line, duplicate player/game pair,
     * or aggregation error. Returns RankedResult on success.
     */
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}
```

---

## Data Models

```java
// ── Primitives ────────────────────────────────────────────────

record Player(String playerId, String playerName) {}

record GameEntry(String gameId, String gameName, int hoursPlayed, int normalisedScore) {}

// ── CSV_Parser output ─────────────────────────────────────────

record ScoreRecord(Player player, GameEntry gameEntry) {}

// ── Score_Aggregator output ───────────────────────────────────

record PlayerAggregate(Player player, int totalScore) {}

// ── Leaderboard_Ranker output ─────────────────────────────────

record RankedResult(
    List<PlayerAggregate> definiteWinners,   // unambiguously in top three
    List<PlayerAggregate> tiedCandidates     // share boundary score; empty when no tie
) {}

// ── Result / Error types ──────────────────────────────────────

sealed interface Result<V, E> permits Result.Ok, Result.Err {
    record Ok<V, E>(V value)  implements Result<V, E> {}
    record Err<V, E>(E error) implements Result<V, E> {}
}

record ParseError(String message, String offendingLine) {}

record AggregationError(String message, String playerId) {}

record PipelineError(String message, String context) {}
```

### Weighted Score Calculation

```
WeightedScore(gameEntry) = gameEntry.hoursPlayed × gameEntry.normalisedScore

TotalScore(player) = Σ WeightedScore(gameEntry)  for all gameEntry rows belonging to player
```

### Ranked_Result Boundary Logic

Given players sorted descending by `totalScore`:

```
positions 0..N-1 (0-indexed)

if N <= 3 and no tie:
    definiteWinners = all players
    tiedCandidates  = []

if N > 3:
    boundaryScore = score at position 2
    if score at position 3 == boundaryScore:
        // tie at boundary
        definiteWinners = players with score > boundaryScore
        tiedCandidates  = players with score == boundaryScore
    else:
        definiteWinners = players at positions 0..2
        tiedCandidates  = []

if all players share the same score:
    definiteWinners = []
    tiedCandidates  = all players
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

---

### Property 1: Valid CSV line parses to correct fields

*For any* well-formed CSV line with exactly six fields, a non-empty player id, a non-empty game id, an integer hours-played value, and a normalised score in [1, 100], parsing that line shall produce a `ScoreRecord` whose fields exactly match the (trimmed) field values in the input.

**Validates: Requirements 1.1**

---

### Property 2: Out-of-range normalised score returns an error

*For any* CSV line that is otherwise valid but whose normalised-score field contains an integer outside the range [1, 100] (e.g. 0, negative values, 101, or `Integer.MAX_VALUE`), the parser shall return a `ParseError` and shall not produce a `ScoreRecord`.

**Validates: Requirements 1.2**

---

### Property 3: Non-integer hours-played returns an error

*For any* CSV line whose hours-played field contains a string that cannot be parsed as an integer (e.g. `"abc"`, `"1.5"`, `""`), the parser shall return a `ParseError`.

**Validates: Requirements 1.3**

---

### Property 4: Non-integer normalised-score field returns an error

*For any* CSV line whose normalised-score field contains a string that cannot be parsed as an integer, the parser shall return a `ParseError`.

**Validates: Requirements 1.4**

---

### Property 5: Wrong field count returns an error

*For any* CSV line whose comma-separated field count is not exactly six (i.e. fewer than six or more than six), the parser shall return a `ParseError`.

**Validates: Requirements 1.5, 1.6**

---

### Property 6: Whitespace trimming preserves field values

*For any* valid CSV line, adding arbitrary leading and/or trailing whitespace around any subset of fields shall produce the same `ScoreRecord` as the version without that whitespace.

**Validates: Requirements 1.10**

---

### Property 7: Parse → print → parse round trip

*For any* valid `ScoreRecord`, printing it to a CSV string and then parsing that string shall produce a `ScoreRecord` equal to the original.

**Validates: Requirements 1.11, 1.12**

---

### Property 8: Aggregation correctness — count, total score, and name preservation

*For any* non-empty list of `ScoreRecord` objects (with consistent player names per id), the aggregator shall produce exactly one `PlayerAggregate` per distinct player id, each with a `totalScore` equal to the sum of `(hoursPlayed × normalisedScore)` across all that player's game entries, and with the player's display name preserved unchanged.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.8**

---

### Property 9: Last-seen display name wins on name conflict

*For any* list of `ScoreRecord` objects that contains two or more records sharing the same player id but carrying different player display names, the aggregator shall produce a `PlayerAggregate` for that player whose `playerName` equals the display name from the last such record in the input list.

**Validates: Requirements 2.6, 2.8**

---

### Property 10: No-tie ranking places top players in definite_winners

*For any* list of `PlayerAggregate` objects where no two players share the same `totalScore` at a boundary position, the ranker shall place exactly `min(3, N)` players in `definiteWinners` (in descending `totalScore` order) and shall leave `tiedCandidates` empty.

**Validates: Requirements 3.2, 3.4**

---

### Property 11: Tie-at-boundary produces correct partition

*For any* list of `PlayerAggregate` objects where two or more players share the `totalScore` at the boundary position (including the degenerate case where all players share the same score), the ranker shall place all players with a `totalScore` strictly above the boundary score in `definiteWinners` and shall place all players sharing the boundary score in `tiedCandidates`, with `definiteWinners` empty when the boundary score equals the highest score.

**Validates: Requirements 3.3, 3.6**

---

### Property 12: Pipeline composition correctness

*For any* list of valid CSV strings, the pipeline result shall equal the result obtained by manually chaining `CsvParser.parseLines` → `ScoreAggregator.aggregate` → `LeaderboardRanker.rank` in sequence.

**Validates: Requirements 4.1**

---

### Property 13: Pipeline propagates CSV parse errors

*For any* list of CSV strings that contains at least one invalid line, the pipeline shall return a `PipelineError` (and shall not return a `RankedResult`).

**Validates: Requirements 4.2**

---

### Property 14: Pipeline rejects duplicate player-id/game-id pairs

*For any* list of CSV strings that contains two or more lines with the same `(playerId, gameId)` combination, the pipeline shall return a `PipelineError` identifying the duplicate.

**Validates: Requirements 4.3**

---

## Error Handling

Each component uses a `Result<V, E>` sealed type (no exceptions crossing component boundaries):

| Component | Error type | Key error conditions |
|-----------|-----------|----------------------|
| `CsvParser` | `ParseError(message, offendingLine)` | wrong field count, non-integer fields, score out of range, empty id fields |
| `ScoreAggregator` | `AggregationError(message, playerId)` | (reserved for future error conditions; name conflicts are resolved by last-write-wins) |
| `Pipeline` | `PipelineError(message, context)` | any `ParseError`, any `AggregationError`, duplicate `(playerId, gameId)` |

Error messages must be descriptive enough for a consumer to identify the offending data without inspecting raw input themselves. The `offendingLine` / `playerId` / `context` fields carry the relevant identifier.

`Leaderboard_Ranker` does not return errors — it always produces a `RankedResult` (possibly with empty lists).

---

## Testing Strategy

### Dual Testing Approach

Both unit tests and property-based tests are required. They are complementary:

- **Unit tests** cover specific examples, integration points, and the named edge cases (empty input, single record, all-same-score, etc.).
- **Property tests** verify universal invariants across hundreds of randomly generated inputs, catching cases that hand-crafted examples miss.

### Property-Based Testing Library

Use **[jqwik](https://jqwik.net/)** (Java property-based testing library, integrates with JUnit 5). Each property test must run a minimum of **100 tries** (jqwik default is 1000; do not lower it below 100).

### Unit Test Focus

- Empty input → empty output (Requirements 2.7, 3.5, 4.4)
- Single valid record end-to-end
- Exactly three players, no tie
- Exactly three players, all tied
- Four players, tie at position 3
- Invalid CSV line in a mixed list
- Duplicate `(playerId, gameId)` in pipeline input
- Player id present but game id empty (and vice versa)

### Property Test Configuration

Each property test must carry a comment tag in the format:

```
// Feature: top-three-high-scores, Property <N>: <property_text>
```

Example:

```java
// Feature: top-three-high-scores, Property 7: Parse → print → parse round trip
@Property(tries = 1000)
void roundTripProperty(@ForAll("validScoreRecords") ScoreRecord record) {
    var csv    = prettyPrinter.print(record);
    var result = csvParser.parseLine(csv);
    assertThat(result).isEqualTo(Result.ok(record));
}
```

### Test Class Structure (one class per component)

```
CsvParserTest          → Properties 1–7  + unit edge cases
ScoreAggregatorTest    → Properties 8–9  + unit edge cases
LeaderboardRankerTest  → Properties 10–11 + unit edge cases
PipelineTest           → Properties 12–14 + unit edge cases
```

### TDD Cycle per Component

Following the Red-Green-Refactor rules:

1. Write one failing test describing a single behaviour in plain English.
2. Write the minimum production code to make it pass.
3. Refactor only when duplication exists.
4. Repeat until all acceptance criteria for the component are covered.

Components are implemented in edge-case-density order: `CsvParser` → `ScoreAggregator` → `LeaderboardRanker` → `Pipeline`. Each component is committed to git individually after its test suite is green.
