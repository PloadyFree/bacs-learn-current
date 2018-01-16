package istu.bacs.db.submission;

import istu.bacs.db.contest.Contest;
import istu.bacs.db.contest.ContestProblem;
import istu.bacs.db.problem.Problem;
import istu.bacs.db.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue
    private Integer submissionId;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "contest_problem_id")
    private ContestProblem contestProblem;

    private boolean pretestsOnly;
    private LocalDateTime created;
    @Enumerated(STRING)
    private Language language;
    private String solution;

    private String externalSubmissionId;

    @OneToOne(cascade = ALL, mappedBy = "submission")
    private SubmissionResult result;

    public Verdict getVerdict() {
        return result.getVerdict();
    }

    public Contest getContest() {
        return contestProblem.getContest();
    }

    public Problem getProblem() {
        return contestProblem.getProblem();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        Submission submission = (Submission) other;
        return Objects.equals(submissionId, submission.submissionId);
    }

    @Override
    public int hashCode() {
        return submissionId;
    }
}