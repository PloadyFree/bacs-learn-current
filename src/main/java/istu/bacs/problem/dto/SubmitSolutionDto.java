package istu.bacs.problem.dto;

import istu.bacs.submission.Language;
import lombok.Data;

@Data
public class SubmitSolutionDto {
    private Language language;
    private String solution;
}