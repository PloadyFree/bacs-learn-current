package istu.bacs.web.contest;

import istu.bacs.db.contest.Contest;
import istu.bacs.db.contest.ContestProblem;
import istu.bacs.db.contest.ContestRepository;
import istu.bacs.web.model.WebModelUtils;
import istu.bacs.web.model.contest.builder.EditContest;
import istu.bacs.web.model.contest.builder.EditContestProblem;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ContestServiceImpl implements ContestService {

    private final ContestRepository contestRepository;

    public ContestServiceImpl(ContestRepository contestRepository) {
        this.contestRepository = contestRepository;
    }

    @Override
    @Transactional
    public Contest findById(int contestId) {
        Contest contest = contestRepository.findById(contestId).orElse(null);
        contest.getProblems().size();
        return contest;
    }

    @Override
    public List<Contest> findAll(Pageable pageable) {
        return contestRepository.findAll(pageable).getContent();
    }

    @Override
    public int createContest(EditContest contest) {
        Contest c = Contest.builder()
                .name(contest.getName())
                .startTime(WebModelUtils.parseDateTime(contest.getStartTime()))
                .finishTime(WebModelUtils.parseDateTime(contest.getFinishTime()))
                .build();

        joinProblems(c, contest.getProblems());
        contestRepository.save(c);

        return c.getContestId();
    }

    @Override
    public void editContest(EditContest contest, int contestId) {
        if (!contestRepository.findById(contestId).isPresent())
            throw new IllegalArgumentException("Contest with id " + contestId + " doesn't exist");

        Contest c = Contest.builder()
                .contestId(contestId)
                .name(contest.getName())
                .startTime(WebModelUtils.parseDateTime(contest.getStartTime()))
                .finishTime(WebModelUtils.parseDateTime(contest.getFinishTime()))
                .build();

        joinProblems(c, contest.getProblems());
        contestRepository.save(c);
    }

    private void joinProblems(Contest contest, List<EditContestProblem> problems) {
        List<ContestProblem> contestProblems = problems.stream()
                .map(p -> new ContestProblem().withId(contest.getContestId(), p.getProblemIndex()))
                .collect(toList());
        contest.setProblems(contestProblems);
    }
}