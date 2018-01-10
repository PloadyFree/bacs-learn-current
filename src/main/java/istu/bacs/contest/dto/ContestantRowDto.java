package istu.bacs.contest.dto;

import istu.bacs.standings.Standings;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Data
public class ContestantRowDto {

    private UserDto contestant;
    private List<Standings.SolvingResult> results;
    private int solvedCount;
    private int penalty;
    private int place;

    public ContestantRowDto(Standings.ContestantRow row) {
        contestant = new UserDto(row.getContestant());
        results = Arrays.stream(row.getProgresses())
                .map(Standings.ProblemProgress::getResult)
                .collect(toList());
        solvedCount = row.getSolvedCount();
        penalty = row.getPenalty();
        place = row.getPlace();
    }
}