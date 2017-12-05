package istu.bacs.web.dto;

import istu.bacs.model.Contest;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class ContestDto {

    private Integer contestId;
    private String contestName;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private List<ProblemDto> problems;

    private ContestDto(Contest contest) {
        contestId = contest.getContestId();
        contestName = contest.getContestName();
        startTime = contest.getStartTime();
        finishTime = contest.getFinishTime();
    }

    public String getContestUrl() {
        return "/contest/" + contestId;
    }

    public static ContestDto withoutProblems(Contest contest) {
        return new ContestDto(contest);
    }

    public static ContestDto withProblems(Contest contest) {
        ContestDto dto = new ContestDto(contest);
        dto.problems = ProblemDto.convert(contest.getProblems(), contest.getContestId());
        return dto;
    }
}