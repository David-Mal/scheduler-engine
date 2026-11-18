package scheduler.constraints;

import scheduler.domain.*;

import java.util.*;

public class ScheduleValidator {

    public ValidationReport validate(Schedule schedule, ScheduleProblem scheduleProblem,
                                     List<Constraint> constraints) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(scheduleProblem, "ScheduleProblem cannot be null");
        Objects.requireNonNull(constraints,  "Constraints cannot be null");

        checkReferences(schedule, scheduleProblem);

        List<Violation> violations = constraints.stream()
                .flatMap(c -> c.evaluate(schedule, scheduleProblem).stream())
                .toList();

        return new ValidationReport(violations);
    }

    private void checkReferences(Schedule schedule, ScheduleProblem scheduleProblem) {

        for (Assignment assignment : schedule.assignments()) {
            if (!scheduleProblem.containsSlot(assignment.slotId())) {
                throw new IllegalArgumentException(
                        "Assignment references an unknown slot: " + assignment.slotId());
            }
            if (!scheduleProblem.containsParticipant(assignment.participantId())) {
                throw new IllegalArgumentException(
                        "Assignment references an unknown participant: " + assignment.participantId());
            }
        }
    }
}
