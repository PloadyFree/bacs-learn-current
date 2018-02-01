package istu.bacs.web.rest.submission;

import istu.bacs.db.user.User;
import istu.bacs.web.model.Submission;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class SubmissionHandler {

    private final SubmissionService submissionService;

    public SubmissionHandler(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Bean
    public RouterFunction<ServerResponse> submissionsRouter() {
        return route(GET("/submissions"), this::getAllSubmissions)
                .andRoute(GET("/submissions/{submissionId}"), this::getSubmission);
    }

    private Mono<ServerResponse> getAllSubmissions(@SuppressWarnings("unused") ServerRequest request) {
        return Mono.justOrEmpty(request.queryParam("my"))
                .zipWith(request.principal())
                .map(t -> (User) ((UsernamePasswordAuthenticationToken) t.getT2()).getPrincipal())
                .flatMapMany(u -> submissionService.findAllByAuthor(Mono.just(u)))
                .transform(Submission::fromDb)
                .collectList()
                .map(Flux::fromIterable)
                .flatMap(submissions -> ok().body(submissions, Submission.class));
    }

    private Mono<ServerResponse> getSubmission(ServerRequest request) {
        int submissionId = Integer.parseInt(request.pathVariable("submissionId"));

        return Mono.just(submissionId)
                .transform(submissionService::findById)
                .transform(Submission::fromDb)
                .transform(submission -> ok().body(submission, Submission.class))
                .switchIfEmpty(badRequest().syncBody("Unable to find submission " + submissionId));
    }
}