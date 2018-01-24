package istu.bacs.externalapi.sybon;

import istu.bacs.db.problem.Problem;
import istu.bacs.db.submission.Submission;
import istu.bacs.db.submission.SubmissionResult;
import istu.bacs.externalapi.ExternalApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static istu.bacs.db.submission.Verdict.PENDING;
import static istu.bacs.externalapi.sybon.SybonContinueCondition.Always;
import static java.lang.Integer.parseInt;
import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Slf4j
public class SybonApi implements ExternalApi {

    static final String API_NAME = "SYBON";

    private static final SybonSubmitResultConverter submitResultConverter = new SybonSubmitResultConverter();
    private static final SybonLanguageConverter languageConverter = new SybonLanguageConverter();
    private static final SybonProblemConverter problemConverter = new SybonProblemConverter();

    private final SybonApiEndpointConfiguration config;
    private final RestTemplate restTemplate;

    public SybonApi(SybonApiEndpointConfiguration config, RestTemplateBuilder restTemplateBuilder) {
        this.config = config;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public List<Problem> getAllProblems() {
        String url = buildUrl(config.getCollectionUrl(), singletonMap("id", 1));

        try {
            List<SybonProblem> problems = restTemplate.getForObject(url, SybonProblemCollection.class).getProblems();
            return problems.stream()
                    .map(problemConverter::convert)
                    .collect(toList());
        } catch (Exception e) {
            log.warn("Unable to get problems", e);
            return emptyList();
        }
    }

    @Override
    public void submit(Submission submission) {
        submit(singletonList(submission));
    }

    @Override
    public void submit(List<Submission> submissions) {
        List<SybonSubmit> sybonSubmits = submissions.stream()
                .map(this::createSubmit)
                .collect(toList());

        String url = buildUrl(config.getSubmitAllUrl(), emptyMap());

        try {
            int[] submissionIds = restTemplate.postForObject(url, sybonSubmits, int[].class);
            for (int i = 0; i < submissions.size(); i++) {
                Submission submission = submissions.get(i);
                submission.getResult().setVerdict(PENDING);
                submission.setExternalSubmissionId(submissionIds[i]);
            }
        } catch (Exception e) {
            log.warn("Unable to submit submissions", e);
        }
    }

    private SybonSubmit createSubmit(Submission submission) {
        SybonSubmit submit = new SybonSubmit();

        submit.setCompilerId(languageConverter.convert(submission.getLanguage()));
        submit.setSolution(Base64.getEncoder().encodeToString(submission.getSolution().getBytes()));
        submit.setSolutionFileType("Text");
        submit.setProblemId(parseInt(submission.getProblem().getRawProblemName()));
        submit.setPretestsOnly(submission.isPretestsOnly());
        submit.setContinueCondition(Always);

        return submit;
    }

    @Override
    public void checkSubmissionResult(Submission submission) {
        checkSubmissionResult(singletonList(submission));
    }

    @Override
    public void checkSubmissionResult(List<Submission> submissions) {
        String ids = submissions.stream()
                .map(sub -> sub.getExternalSubmissionId() + "")
                .collect(joining(","));

        String url = buildUrl(config.getResultsUrl(), emptyMap(), singletonMap("ids", ids));

        try {
            SybonSubmitResult[] sybonResults = restTemplate.getForObject(url, SybonSubmitResult[].class);
            for (int i = 0; i < submissions.size(); i++) {
                SubmissionResult result = submitResultConverter.convert(sybonResults[i]);
                updateSubmissionResult(submissions.get(i).getResult(), result);
            }
        } catch (Exception e) {
            log.warn("Unable to check submission results for submissions {}", ids, e);
        }
    }

    private void updateSubmissionResult(SubmissionResult myRes, SubmissionResult newRes) {
        myRes.setBuildInfo(newRes.getBuildInfo());
        myRes.setVerdict(newRes.getVerdict());
        myRes.setTestsPassed(newRes.getTestsPassed());
        myRes.setTimeUsedMillis(newRes.getTimeUsedMillis());
        myRes.setMemoryUsedBytes(newRes.getMemoryUsedBytes());
    }

    private String buildUrl(String url, Map<String, ?> urlParams, Map<String, ?> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        queryParams.forEach(builder::queryParam);
        builder.queryParam("api_key", config.getApiKey());

        return builder.buildAndExpand(urlParams).toString();
    }

    private String buildUrl(String url, Map<String, ?> urlParams) {
        return buildUrl(url, urlParams, emptyMap());
    }

    @Override
    public String getApiName() {
        return API_NAME;
    }
}