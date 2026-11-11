package scheduler.constraints;

import scheduler.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FairSingingDistribution implements Constraint {

    private final int weight;

    public FairSingingDistribution(int weight) {
        this.weight = weight;
    }

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        List<Participant> singers = problem.participants().stream()
                .filter(p -> p.allowedRoles().contains(Role.SINGING))
                .toList();
        if (singers.isEmpty()) {
            return List.of();
        }

        Map<UUID, Long> timesSung = schedule.assignments().stream()
                .filter(a-> a.role() == Role.SINGING)
                .collect(Collectors.groupingBy(Assignment::participantId, Collectors.counting()));

        long min = singers.stream()
                .mapToLong(p -> timesSung.getOrDefault(p.id(), 0L))
                .min()
                .getAsLong();

        List<Violation> violations = new ArrayList<>();
        for (Participant singer : singers) {
            long times = timesSung.getOrDefault(singer.id(), 0L);
            long excess = times - min - 1;

            if (excess > 0) {
                violations.add(Violation.soft(name(), (int) (weight * excess),
                        singer.name() + " sings " + times
                                + " times, while the least used singer sings " + min,
                        null, singer.id()));
            }
        }
        return violations;
    }
}
