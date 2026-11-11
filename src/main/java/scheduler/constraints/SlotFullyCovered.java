package scheduler.constraints;

import scheduler.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlotFullyCovered implements Constraint {

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {

        List<Violation> violations = new ArrayList<>();

        for (Slot slot : problem.slots()) {
            Map<Role, Long> assignedByRole = schedule.getAssignmentsFor(slot).stream()
                    .collect(Collectors.groupingBy(Assignment::role, Collectors.counting()));

            for (Role role : Role.values()) {
                int required = slot.requiredRoles().getOrDefault(role, 0);
                long assigned =  assignedByRole.getOrDefault(role, 0L);

                if (assigned != required) {
                    violations.add(Violation.hard(name(),
                            slot.start() + " " + role + ": needs " + required
                                            + ", has " + assigned,
                                    slot.id(), null));
                }
            }

        }
        return violations;
    }
}
