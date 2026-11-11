package scheduler.constraints;

import scheduler.domain.*;

import java.util.ArrayList;
import java.util.List;

public class ParticipantHasRole implements Constraint {

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        List<Violation> violations = new ArrayList<>();

        for (Assignment assignment : schedule.assignments()) {
            Participant participant = problem.participant(assignment.participantId());

            if (!participant.allowedRoles().contains(assignment.role())) {
                Slot slot = problem.slot(assignment.slotId());
                violations.add(Violation.hard(name(),
                        participant.name() + " is not allowed to do " + assignment.role()
                                + " (" + slot.start().toLocalDate() + ")",
                        slot.id(), participant.id()));
            }
        }
        return violations;
    }
}
