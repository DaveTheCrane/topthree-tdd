# Requirements Document

## Introduction

An online gaming site runs weekly reward campaigns for its top three players. Because the main databases are under heavy load, scores are computed from an offline CSV file produced by a batch job. The system must parse that CSV data, aggregate each player's scores across all games they played that week, rank the players, and return the top three.

The solution must follow hexagonal architecture so that each processing step is independently testable, and must be built using strict TDD (Red-Green-Refactor).

---

## Glossary

- **CSV_Record**: A single line of raw CSV text representing one player/game combination.
- **Score_Record**: A parsed, structured representation of one CSV_Record, containing a Player and a Game_Entry.
- **Player**: A participant identified by a unique player id and an online display name.
- **Game_Entry**: A single game played by a Player in the current week, identified by a game id and display name, with hours played and a normalised score (integer 1–100).
- **Player_Aggregate**: The combined result for one Player across all Game_Entry rows: total weighted score used for ranking.
- **Weighted_Score**: The score contribution of one Game_Entry, calculated as `hours × normalised_score`.
- **Total_Score**: The sum of all Weighted_Score values for a Player across all their Game_Entry rows in the week.
- **Leaderboard**: The ordered list of Player_Aggregate records, highest Total_Score first.
- **Top_Three**: The first three entries of the Leaderboard (fewer if fewer than three players exist).
- **CSV_Parser**: The component that converts a list of raw CSV strings into a list of Score_Record objects.
- **Score_Aggregator**: The component that converts a list of Score_Record objects into a list of Player_Aggregate objects.
- **Leaderboard_Ranker**: The component that sorts Player_Aggregate objects and returns the Top_Three.
- **Pretty_Printer**: The component that formats a list of Player_Aggregate objects back into a canonical CSV string.

---

## Requirements

### Requirement 1: Parse CSV Records

**User Story:** As a batch-job consumer, I want to parse raw CSV lines into structured Score_Record objects, so that downstream components can work with typed data instead of raw strings.

#### Acceptance Criteria

1. WHEN a valid CSV line is provided, THE CSV_Parser SHALL produce a Score_Record containing the player id, player display name, game id, game display name, hours played (integer), and normalised score (integer).
2. WHEN a CSV line contains a normalised score outside the range 1–100, THEN THE CSV_Parser SHALL return a descriptive error for that line.
3. WHEN a CSV line contains a non-integer value in the hours-played field, THEN THE CSV_Parser SHALL return a descriptive error for that line.
4. WHEN a CSV line contains a non-integer value in the normalised-score field, THEN THE CSV_Parser SHALL return a descriptive error for that line.
5. WHEN a CSV line contains fewer than six comma-separated fields, THEN THE CSV_Parser SHALL return a descriptive error for that line.
6. WHEN a CSV line contains more than six comma-separated fields, THEN THE CSV_Parser SHALL return a descriptive error for that line.
7. WHEN a CSV line contains an empty player id field, THEN THE CSV_Parser SHALL return a descriptive error for that line.
8. WHEN a CSV line contains an empty game id field, THEN THE CSV_Parser SHALL return a descriptive error for that line.
9. WHEN a list of valid CSV lines is provided, THE CSV_Parser SHALL produce one Score_Record per line in the same order.
10. WHEN a CSV line contains leading or trailing whitespace around any field, THE CSV_Parser SHALL trim that whitespace before producing the Score_Record.
11. THE Pretty_Printer SHALL format a Score_Record back into a CSV line using the same six-field comma-separated format.
12. FOR ALL valid Score_Record objects, parsing then printing then parsing SHALL produce an equivalent Score_Record (round-trip property).

---

### Requirement 2: Aggregate Player Scores

**User Story:** As a score calculator, I want to aggregate all Game_Entry rows for each Player into a single Player_Aggregate, so that each player has one comparable Total_Score.

#### Acceptance Criteria

1. WHEN a list of Score_Record objects is provided, THE Score_Aggregator SHALL produce one Player_Aggregate per distinct player id.
2. THE Score_Aggregator SHALL compute the Weighted_Score for each Game_Entry as `hours_played × normalised_score`.
3. THE Score_Aggregator SHALL compute the Total_Score for each Player_Aggregate as the sum of all Weighted_Score values for that player.
4. WHEN a player appears in only one Score_Record, THE Score_Aggregator SHALL produce a Player_Aggregate whose Total_Score equals the Weighted_Score of that single Game_Entry.
5. WHEN a player appears in multiple Score_Record objects, THE Score_Aggregator SHALL sum all their Weighted_Score values into one Total_Score.
6. WHEN two Score_Record objects share the same player id but carry different player display names, THEN THE Score_Aggregator SHALL return a descriptive error indicating inconsistent player data.
7. WHEN the input list is empty, THE Score_Aggregator SHALL return an empty list of Player_Aggregate objects.
8. WHILE processing a list of Score_Record objects, THE Score_Aggregator SHALL preserve the player display name in the resulting Player_Aggregate.

---

### Requirement 3: Rank Players and Return Top Three

**User Story:** As a rewards coordinator, I want to retrieve the top three players by Total_Score, so that I can award weekly prizes to the correct winners.

#### Acceptance Criteria

1. WHEN a list of Player_Aggregate objects is provided, THE Leaderboard_Ranker SHALL return the Top_Three players ordered by Total_Score descending.
2. WHEN two players have the same Total_Score, THE Leaderboard_Ranker SHALL order those players by player id ascending (lexicographic) as a stable tiebreaker.
3. WHEN fewer than three Player_Aggregate objects are provided, THE Leaderboard_Ranker SHALL return all of them in descending Total_Score order.
4. WHEN the input list is empty, THE Leaderboard_Ranker SHALL return an empty list.
5. WHEN more than three Player_Aggregate objects are provided, THE Leaderboard_Ranker SHALL return exactly three Player_Aggregate objects.

---

### Requirement 4: End-to-End Pipeline

**User Story:** As an operator, I want to run the full pipeline from a list of raw CSV strings to the Top_Three result, so that I can obtain winners without manually chaining components.

#### Acceptance Criteria

1. WHEN a list of raw CSV strings is provided to the pipeline, THE Pipeline SHALL parse, aggregate, and rank the data and return the Top_Three.
2. WHEN any CSV line in the input list is invalid, THEN THE Pipeline SHALL return a descriptive error identifying the offending line.
3. WHEN the input list contains duplicate player-id/game-id combinations, THEN THE Pipeline SHALL return a descriptive error identifying the duplicate.
4. WHEN the input list is empty, THE Pipeline SHALL return an empty Top_Three list.
