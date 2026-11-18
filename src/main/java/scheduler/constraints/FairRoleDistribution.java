package scheduler.constraints;

import scheduler.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Soft constraint: the assignments of one role should be spread evenly
 * over the participants allowed to do it. A difference of one is free;
 * every extra assignment above that costs {@code weight}.
 */
public class FairRoleDistribution implements Constraint {

    private final Role role;
    private final int weight;

    public FairRoleDistribution(Role role, int weight) {
        if (role == null) {
            throw new IllegalArgumentException("role is required");
        }
        this.role = role;
        this.weight = weight;
    }

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        List<Participant> eligible = problem.participants().stream()
                .filter(p -> p.allowedRoles().contains(role))
                .toList();
        if (eligible.isEmpty()) {
            return List.of();
        }

        Map<UUID, Long> timesAssigned = schedule.assignments().stream()
                .filter(a -> a.role() == role)
                .collect(Collectors.groupingBy(Assignment::participantId, Collectors.counting()));

        long min = eligible.stream()
                .mapToLong(p -> timesAssigned.getOrDefault(p.id(), 0L))
                .min()
                .getAsLong();

        List<Violation> violations = new ArrayList<>();
        for (Participant participant : eligible) {
            long times = timesAssigned.getOrDefault(participant.id(), 0L);
            long excess = times - min - 1;

            if (excess > 0) {
                violations.add(Violation.soft(name(), (int) (weight * excess),
                        participant.name() + " has " + role + " " + times
                                + " times, while the least used participant has it " + min,
                        null, participant.id()));
            }
        }
        return violations;
    }
}
