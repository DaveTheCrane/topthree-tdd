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
- **Ranked_Result**: The output of the Leaderboard_Ranker, containing two fields: `definite_winners` (a list of Player_Aggregate objects unambiguously in the Top_Three) and `tied_candidates` (a list of Player_Aggregate objects sharing the boundary score that creates tie ambiguity). When no tie exists at a boundary position, `tied_candidates` is empty.
- **Boundary_Position**: The rank position (1st, 2nd, or 3rd) at which a tie causes ambiguity about which players belong in the Top_Three.
- **CSV_Parser**: The component that converts a list of raw CSV strings into a list of Score_Record objects.
- **Score_Aggregator**: The component that converts a list of Score_Record objects into a list of Player_Aggregate objects.
- **Leaderboard_Ranker**: The component that sorts Player_Aggregate objects and returns a Ranked_Result surfacing any tie ambiguity at a boundary position.
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
6. WHEN two Score_Record objects share the same player id but carry different player display names, THEN THE Score_Aggregator SHALL use the display name from the last such record in the input order (i.e. the most recent occurrence wins).
7. WHEN the input list is empty, THE Score_Aggregator SHALL return an empty list of Player_Aggregate objects.
8. WHILE processing a list of Score_Record objects, THE Score_Aggregator SHALL preserve the player display name in the resulting Player_Aggregate, using the last-seen name for that player id.

---

### Requirement 3: Rank Players and Return Top Three

**User Story:** As a rewards coordinator, I want to retrieve the top three players by Total_Score with any tie ambiguity clearly surfaced, so that I can decide how to award weekly prizes without the ranker imposing an arbitrary tiebreaker.

#### Acceptance Criteria

1. WHEN a list of Player_Aggregate objects is provided, THE Leaderboard_Ranker SHALL return a Ranked_Result containing a `definite_winners` list and a `tied_candidates` list.
2. WHEN no tie exists at any Boundary_Position, THE Leaderboard_Ranker SHALL place exactly the top three players (by Total_Score descending) in `definite_winners` and SHALL leave `tied_candidates` empty.
3. WHEN two or more players share the same Total_Score at a Boundary_Position, THE Leaderboard_Ranker SHALL place all players with a Total_Score strictly above the boundary score in `definite_winners` and SHALL place all players sharing the boundary score in `tied_candidates`.
4. WHEN fewer than three Player_Aggregate objects are provided and no tie exists, THE Leaderboard_Ranker SHALL place all of them in `definite_winners` in descending Total_Score order and SHALL leave `tied_candidates` empty.
5. WHEN the input list is empty, THE Leaderboard_Ranker SHALL return a Ranked_Result with both `definite_winners` and `tied_candidates` empty.
6. WHEN all players in the input share the same Total_Score, THE Leaderboard_Ranker SHALL leave `definite_winners` empty and SHALL place all players in `tied_candidates`.

---

### Requirement 4: End-to-End Pipeline

**User Story:** As an operator, I want to run the full pipeline from a list of raw CSV strings to the Top_Three result, so that I can obtain winners without manually chaining components.

#### Acceptance Criteria

1. WHEN a list of raw CSV strings is provided to the pipeline, THE Pipeline SHALL parse, aggregate, and rank the data and return the Top_Three.
2. WHEN any CSV line in the input list is invalid, THEN THE Pipeline SHALL return a descriptive error identifying the offending line.
3. WHEN the input list contains duplicate player-id/game-id combinations, THEN THE Pipeline SHALL return a descriptive error identifying the duplicate.
4. WHEN the input list is empty, THE Pipeline SHALL return an empty Top_Three list.
