package constraints;

import org.junit.jupiter.api.Test;
import scheduler.constraints.NoDoubleRoleInSlot;
import scheduler.constraints.Severity;
import scheduler.constraints.Violation;
import scheduler.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoDoubleRoleInSlotTest {

    private final NoDoubleRoleInSlot constraint = new NoDoubleRoleInSlot();

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant ana = new Participant(UUID.randomUUID(), "Ana",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Slot morning = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0), Map.of(Role.PRAYER, 1, Role.SERMON, 1));
    private final Slot evening = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 18, 0), Map.of(Role.PRAYER, 1, Role.SERMON, 1));
    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(ion, ana), List.of(morning, evening), List.of());

    @Test
    void noViolationWhenEachPersonHasOneRoleInTheSlot() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(morning.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(morning.id(), Role.SERMON, ana.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void samePersonWithDifferentRolesInDifferentSlotsIsAllowed() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(morning.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(evening.id(), Role.SERMON, ion.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void reportsPersonWithTwoRolesInTheSameSlot() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(morning.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(morning.id(), Role.SERMON, ion.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("NoDoubleRoleInSlot");
        assertThat(v.slotId()).isEqualTo(morning.id());
        assertThat(v.participantId()).isEqualTo(ion.id());
    }
}