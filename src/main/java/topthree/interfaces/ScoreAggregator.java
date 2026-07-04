package topthree.interfaces;

import topthree.models.AggregationError;
import topthree.models.PlayerAggregate;
import topthree.models.Result;
import topthree.models.ScoreRecord;
import java.util.List;

public interface ScoreAggregator {
    Result<List<PlayerAggregate>, AggregationError> aggregate(List<ScoreRecord> records);
}