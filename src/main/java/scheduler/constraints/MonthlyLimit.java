package scheduler.constraints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import scheduler.domain.*;

public class MonthlyLimit implements Constraint {

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        List<Violation> violations = new ArrayList<>();

        for (Participant participant : problem.participants()) {
            Map<Role, Long> counts = schedule.getAssignmentsFor(participant).stream()
                    .collect(Collectors.groupingBy(Assignment::role, Collectors.counting()));

            for (Role role : Role.values()) {
                Integer limit = participant.monthlyLimit().get(role);
                long count = counts.getOrDefault(role, 0L);

                if (limit != null && count > limit) {
                    violations.add(Violation.hard(name(),
                            participant.name() + " has " + count + " " + role
                                    + " assignments, the limit is " + limit,
                            null, participant.id()));
                }
            }
        }
        return violations;
    }
}