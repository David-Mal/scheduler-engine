package scheduler.constraints;

import scheduler.domain.*;

import java.util.*;
import java.util.stream.Collectors;


public class NoDoubleRoleInSlot implements Constraint {


    private record SlotParticipant(UUID slotId, UUID participantId) {
    }

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        Map<SlotParticipant, List<Assignment>> grouped = schedule.assignments().stream()
                .collect(Collectors.groupingBy(
                        a -> new SlotParticipant(a.slotId(), a.participantId()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Violation> violations = new ArrayList<>();
        for (Map.Entry<SlotParticipant, List<Assignment>> entry : grouped.entrySet()) {
            List<Assignment> assignments = entry.getValue();
            if (assignments.size() > 1) {
                Participant participant = problem.participant(entry.getKey().participantId());
                Slot slot = problem.slot(entry.getKey().slotId());
                String roles = assignments.stream()
                        .map(a -> a.role().name())
                        .sorted()
                        .collect(Collectors.joining(", "));
                 violations.add(Violation.hard(name(),
                 participant.name() + " has multiple roles (" + roles + ") in slot " + slot.start(),
                         slot.id(), participant.id()));
            }
        }
        return violations;
    }
}