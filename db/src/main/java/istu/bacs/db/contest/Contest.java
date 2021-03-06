package istu.bacs.db.contest;

import lombok.*;
import lombok.experimental.Wither;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

@Data
@Entity
@Table(name = "contest", schema = "public")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "contestId")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Wither
    private Integer contestId;

    private String name;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    @OneToMany(cascade = ALL, mappedBy = "contest", fetch = EAGER)
    @OrderBy("problemIndex ASC")
    private List<ContestProblem> problems;

    public ContestProblem getProblem(String index) {
        for (ContestProblem problem : getProblems())
            if (Objects.equals(problem.getProblemIndex(), index))
                return problem;
        return null;
    }
}