package scheduler.constraints;

import scheduler.domain.*;

import java.util.ArrayList;
import java.util.List;

public class RespectUnavailability implements Constraint{

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {

        List<Violation> violations = new ArrayList<>();

        for (Assignment assignment : schedule.assignments()) {
            Slot slot = problem.slot(assignment.slotId());
            boolean unavailable = problem.unavailabilities().stream()
                    .anyMatch(u -> u.participantId().equals(assignment.participantId()) && u.covers(slot));

            if (unavailable) {
                Participant participant = problem.participant(assignment.participantId());
                violations.add(Violation.hard(name(),
                        participant.name() + " is unavailable for slot " + slot.start().toLocalDate()
                          + " (" + assignment.role() + ")",
                                slot.id(), participant.id()));
            }
        }
        return violations;
    }
}
