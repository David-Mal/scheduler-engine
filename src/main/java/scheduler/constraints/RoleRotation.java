package scheduler.constraints;

import scheduler.domain.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class RoleRotation implements Constraint {

    private static final Set<Role> ROTATING_ROLES = Set.of(Role.PRAYER, Role.SERMON);

    private record ParticipantRole(UUID participantId, Role role) {
    }

    private final int weight;

    public RoleRotation(int weight) {
        this.weight = weight;
    }

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        Map<ParticipantRole, List<Assignment>> grouped = schedule.assignments().stream()
                .filter(a -> ROTATING_ROLES.contains(a.role()))
                .collect(Collectors.groupingBy(
                        a -> new ParticipantRole(a.participantId(), a.role()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Violation> violations = new ArrayList<>();
        for (Map.Entry<ParticipantRole, List<Assignment>> entry : grouped.entrySet()) {
            List<Slot> slots = entry.getValue().stream()
                    .map(a -> problem.slot(a.slotId()))
                    .sorted(Comparator.comparing(Slot::start))
                    .toList();

            for (int i = 1; i < slots.size(); i++) {
                Slot previous = slots.get(i - 1);
                Slot current = slots.get(i);

                if (weeksBetween(previous, current) <= 1) {
                    Participant participant = problem.participant(entry.getKey().participantId());
                    violations.add(Violation.soft(name(), weight,
                            participant.name() + " has " + entry.getKey().role() + " on "
                                    + current.start().toLocalDate() + " after "
                                    + previous.start().toLocalDate(),
                            current.id(), participant.id()));
                }
            }
        }
        return violations;
    }

    private static long weeksBetween(Slot earlier, Slot later) {
        LocalDate firstMonday = earlier.start().toLocalDate().with(DayOfWeek.MONDAY);
        LocalDate secondMonday = later.start().toLocalDate().with(DayOfWeek.MONDAY);
        return ChronoUnit.WEEKS.between(firstMonday, secondMonday);
    }
}
