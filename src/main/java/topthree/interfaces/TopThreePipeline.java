package topthree.interfaces;

import topthree.models.PipelineError;
import topthree.models.RankedResult;
import topthree.models.Result;
import java.util.List;

public interface TopThreePipeline {
    Result<RankedResult, PipelineError> run(List<String> csvLines);
}