package istu.bacs.standings;

import istu.bacs.submission.SubmissionService;
import istu.bacs.util.PlatformUnitInitializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StandingsInitializer implements PlatformUnitInitializer {

    private final StandingsService standingsService;
    private final SubmissionService submissionService;

    public StandingsInitializer(StandingsService standingsService, SubmissionService submissionService) {
        this.standingsService = standingsService;
        this.submissionService = submissionService;
    }

    @Override
    @Transactional
    public void init() {
        standingsService.update(submissionService.findAll());
        submissionService.subscribeOnSolutionTested(standingsService::update);
    }
}