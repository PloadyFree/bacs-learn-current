package istu.bacs.web.externalapi.sybon;

import istu.bacs.db.problem.Problem;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static istu.bacs.web.externalapi.ExternalApiHelper.addResource;

@Component
public class SybonProblemConverter implements Converter<SybonProblem, Problem> {
    @Override
    public Problem convert(SybonProblem sybonProblem) {
        return Problem.builder()
                .problemId(addResource(sybonProblem.getId(), SybonApi.API_NAME))
                .problemName(sybonProblem.getName())
                .statementUrl(sybonProblem.getStatementUrl())
                .timeLimitMillis(sybonProblem.getResourceLimits().getTimeLimitMillis())
                .memoryLimitBytes(sybonProblem.getResourceLimits().getMemoryLimitBytes())
                .build();
    }
}