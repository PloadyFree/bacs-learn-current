package istu.bacs.sybon;

import com.fasterxml.jackson.databind.ObjectMapper;
import istu.bacs.db.contest.Contest;
import istu.bacs.db.contest.ContestProblem;
import istu.bacs.db.problem.Problem;
import istu.bacs.db.submission.Submission;
import istu.bacs.db.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES;
import static istu.bacs.db.submission.Language.Python3;
import static istu.bacs.db.submission.SubmissionResult.withVerdict;
import static istu.bacs.db.submission.Verdict.OK;
import static istu.bacs.db.submission.Verdict.PENDING;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@ExtendWith(SpringExtension.class)
@TestInstance(PER_CLASS)
class SybonApiTests {

    private SybonApi sybonApi;

    @BeforeAll
    void initApi() {
        ObjectMapper om = new ObjectMapper().configure(ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplateBuilder.messageConverters(new MappingJackson2HttpMessageConverter(om));

        sybonApi = new SybonApi(new SybonConfigurationProperties(), restTemplateBuilder);
    }

    @Test
    void fetchProblems() {
        assertThat(sybonApi.getAllProblems(), is(not(empty())));
    }

    @Test
    void submitSolution() {
        User author = User.builder()
                .userId(245)
                .username("admin")
                .password("password")
                .build();

        Problem problem = sybonApi.getAllProblems().get(0);
        ContestProblem contestProblem = ContestProblem.builder()
                .contestProblemId(0)
                .problem(problem)
                .problemIndex("A")
                .build();

        Contest contest = Contest.builder()
                .contestId(123)
                .contestName("Test contest")
                .startTime(LocalDateTime.now())
                .finishTime(LocalDateTime.now().plusHours(5))
                .problems(singletonList(contestProblem))
                .build();
        contestProblem.setContest(contest);

        Submission submission = Submission.builder()
                .submissionId(5)
                .author(author)
                .contestProblem(contestProblem)
                .pretestsOnly(false)
                .created(contest.getStartTime().plusMinutes(7))
                .language(Python3)
                .solution("print(sum(map(int, input().split())))")
                .build();
        submission.setResult(withVerdict(submission, PENDING));

        sybonApi.submit(submission);
        assertThat(submission.getExternalSubmissionId(), is(not(isEmptyOrNullString())));

        while (submission.getVerdict() == PENDING)
            sybonApi.updateSubmissionDetails(singletonList(submission));
        assertThat(submission.getVerdict(), is(OK));
    }
}