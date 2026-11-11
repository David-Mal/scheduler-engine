package constraints;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import scheduler.constraints.Severity;
import scheduler.constraints.SlotFullyCovered;
import scheduler.constraints.Violation;
import scheduler.domain.*;

import static org.assertj.core.api.Assertions.assertThat;

class SlotFullyCoveredTest {

    private final SlotFullyCovered constraint = new SlotFullyCovered();

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant paul = new Participant(UUID.randomUUID(), "Paul",
            Set.of(Role.PRAYER), Map.of());
    private final Participant dan = new Participant(UUID.randomUUID(), "Dan",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Slot sunday15 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0), Map.of(Role.PRAYER, 2, Role.SERMON, 1));
    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(ion, paul, dan), List.of(sunday15), List.of());

    @Test
    void noViolationWhenEveryRoleIsFullyCovered() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, paul.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, dan.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void reportsMissingPerson() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, dan.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("SlotFullyCovered");
        assertThat(v.slotId()).isEqualTo(sunday15.id());
        assertThat(v.participantId()).isNull();
        assertThat(v.message()).contains("PRAYER", "needs 2", "has 1");
    }

    @Test
    void reportsTooManyPeople() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, paul.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, dan.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, dan.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).contains("PRAYER", "needs 2", "has 3");
    }

    @Test
    void reportsRoleThatTheSlotDoesNotRequire() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, paul.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, dan.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SINGING, ion.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).message()).contains("SINGING", "needs 0", "has 1");
    }

    @Test
    void emptyScheduleReportsEveryRequiredRole() {
        List<Violation> violations = constraint.evaluate(new Schedule(), problem);

        assertThat(violations).hasSize(2);
    }
}