package com.gaming.topthree;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link TopThreePipelineImpl} (Properties 12-14).
 */
class PipelinePropertyTest {

    private final CsvParser parser = new CsvParserImpl();
    private final ScoreAggregator aggregator = new ScoreAggregatorImpl();
    private final LeaderboardRanker ranker = new LeaderboardRankerImpl();
    private final TopThreePipeline pipeline = new TopThreePipelineImpl(parser, aggregator, ranker);

    /** Fields of one CSV line, minus the game id (assigned by position to stay unique). */
    record LineSpec(int playerIdx, String name, int hours, int score) {}

    private static String toLine(LineSpec spec, String gameId) {
        return String.join(",",
                "p" + spec.playerIdx(),
                spec.name(),
                gameId,
                gameId + "-name",
                String.valueOf(spec.hours()),
                String.valueOf(spec.score()));
    }

    @Provide
    Arbitrary<List<LineSpec>> lineSpecs() {
        Arbitrary<Integer> playerIdx = Arbitraries.integers().between(0, 3);
        Arbitrary<String> name = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(6);
        Arbitrary<Integer> hours = Arbitraries.integers().between(0, 100);
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);
        Arbitrary<LineSpec> spec = Combinators.combine(playerIdx, name, hours, score).as(LineSpec::new);
        return spec.list().ofMinSize(0).ofMaxSize(15);
    }

    // Assigns a unique game id per line so (playerId, gameId) pairs never collide.
    private static List<String> toUniqueLines(List<LineSpec> specs) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < specs.size(); i++) {
            lines.add(toLine(specs.get(i), "g" + i));
        }
        return lines;
    }

    // Feature: top-three-high-scores, Property 12: Pipeline composition correctness
    @Property(tries = 1000)
    @SuppressWarnings("unchecked")
    void pipelineEqualsManualChaining(@ForAll("lineSpecs") List<LineSpec> specs) {
        List<String> lines = toUniqueLines(specs);

        Result<RankedResult, PipelineError> pipelineResult = pipeline.run(lines);

        // Manually chain the three components.
        Result<List<ScoreRecord>, ParseError> parsed = parser.parseLines(lines);
        List<ScoreRecord> records = ((Result.Ok<List<ScoreRecord>, ParseError>) parsed).value();
        List<PlayerAggregate> aggregates =
                ((Result.Ok<List<PlayerAggregate>, AggregationError>) aggregator.aggregate(records)).value();
        RankedResult expected = ranker.rank(aggregates);

        assertThat(pipelineResult).isEqualTo(Result.ok(expected));
    }

    // Feature: top-three-high-scores, Property 13: Pipeline propagates CSV parse errors
    @Property(tries = 1000)
    void pipelinePropagatesParseErrors(
            @ForAll("lineSpecs") List<LineSpec> specs,
            @ForAll("insertionPoints") int insertionFraction) {
        List<String> lines = new ArrayList<>(toUniqueLines(specs));
        // An invalid line: three fields is never exactly six.
        String invalidLine = "only,three,fields";
        int index = lines.isEmpty() ? 0 : insertionFraction % (lines.size() + 1);
        lines.add(index, invalidLine);

        assertThat(pipeline.run(lines)).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<Integer> insertionPoints() {
        return Arbitraries.integers().between(0, 1000);
    }

    // Feature: top-three-high-scores, Property 14: Pipeline rejects duplicate player-id/game-id pairs
    @Property(tries = 1000)
    void pipelineRejectsDuplicatePairs(@ForAll("nonEmptyLineSpecs") List<LineSpec> specs) {
        List<String> lines = new ArrayList<>(toUniqueLines(specs));
        // Duplicate the first line's (playerId, gameId) pair with an otherwise-valid line.
        LineSpec first = specs.get(0);
        lines.add(toLine(new LineSpec(first.playerIdx(), first.name(), 7, 99), "g0"));

        assertThat(pipeline.run(lines)).isInstanceOf(Result.Err.class);
    }

    @Provide
    Arbitrary<List<LineSpec>> nonEmptyLineSpecs() {
        Arbitrary<Integer> playerIdx = Arbitraries.integers().between(0, 3);
        Arbitrary<String> name = Arbitraries.strings().withCharRange('a', 'z').ofMinLength(1).ofMaxLength(6);
        Arbitrary<Integer> hours = Arbitraries.integers().between(0, 100);
        Arbitrary<Integer> score = Arbitraries.integers().between(1, 100);
        Arbitrary<LineSpec> spec = Combinators.combine(playerIdx, name, hours, score).as(LineSpec::new);
        return spec.list().ofMinSize(1).ofMaxSize(15);
    }
}
