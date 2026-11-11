package constraints;

import org.junit.jupiter.api.Test;
import scheduler.constraints.ParticipantHasRole;
import scheduler.constraints.Severity;
import scheduler.constraints.Violation;
import scheduler.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantHasRoleTest {

    private final ParticipantHasRole constraint = new ParticipantHasRole();

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant paul = new Participant(UUID.randomUUID(), "Paul",
            Set.of(Role.PRAYER), Map.of());
    private final Participant girlsGroup = new Participant(UUID.randomUUID(), "Girls' group",
            Set.of(Role.SINGING), Map.of());
    private final Slot sunday15 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0),
            Map.of(Role.PRAYER, 1, Role.SERMON, 1, Role.SINGING, 1));
    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(ion, paul, girlsGroup), List.of(sunday15), List.of());

    @Test
    void noViolationWhenEveryoneDoesAnAllowedRole() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, paul.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SINGING, girlsGroup.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void reportsGroupAssignedToSermon() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, girlsGroup.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("ParticipantHasRole");
        assertThat(v.slotId()).isEqualTo(sunday15.id());
        assertThat(v.participantId()).isEqualTo(girlsGroup.id());
    }

    @Test
    void reportsOnlyTheWrongAssignments() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, paul.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, paul.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).participantId()).isEqualTo(paul.id());
    }
}