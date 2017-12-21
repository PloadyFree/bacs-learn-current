package istu.bacs.service.impl;

import istu.bacs.domain.Contest;
import istu.bacs.domain.Submission;
import istu.bacs.domain.User;
import istu.bacs.externalapi.ExternalApiAggregator;
import istu.bacs.repository.SubmissionRepository;
import istu.bacs.service.SubmissionService;
import org.springframework.stereotype.Service;

import java.util.List;

import static istu.bacs.domain.Verdict.PENDING;
import static java.util.Collections.singletonList;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final ExternalApiAggregator externalApi;

    public SubmissionServiceImpl(SubmissionRepository submissionRepository, ExternalApiAggregator externalApi) {
        this.submissionRepository = submissionRepository;
        this.externalApi = externalApi;
    }

    @Override
    public Submission findById(int submissionId) {
        return submissionRepository.findById(submissionId)
                .map(submission -> {
                    externalApi.updateSubmissionDetails(singletonList(submission));
                    return submission;
                })
                .orElse(null);
    }

    @Override
    public List<Submission> findAll() {
        List<Submission> submissions = submissionRepository.findAll();
        prepareSubmissions(submissions);
        return submissions;
    }

    @Override
    public List<Submission> findAllByContest(Contest contest) {
        List<Submission> submissions = submissionRepository.findAllByContest(contest);
        prepareSubmissions(submissions);
        return submissions;
    }

    @Override
    public List<Submission> findAllByContestAndAuthor(Contest contest, User author) {
        List<Submission> submissions = submissionRepository.findAllByContestAndAuthor(contest, author);
        prepareSubmissions(submissions);
        return submissions;
    }

    private void prepareSubmissions(List<Submission> submissions) {
        for (Submission submission : submissions)
            if (submission.getVerdict() == PENDING)
                externalApi.updateSubmissionDetails(submissions);
    }

    @Override
    public void submit(Submission submission) {
        externalApi.submit(submission);
        submissionRepository.save(submission);
    }

    @Override
    public void submitAll(List<Submission> submissions) {
        externalApi.submitAll(submissions);
        submissionRepository.saveAll(submissions);
    }
}