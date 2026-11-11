package constraints;

import org.junit.jupiter.api.Test;
import scheduler.constraints.RoleRotation;
import scheduler.domain.*;
import scheduler.constraints.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRotationTest {

    private final RoleRotation constraint = new RoleRotation(10);

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant girlsGroup = new Participant(UUID.randomUUID(), "Girls' group",
            Set.of(Role.SINGING), Map.of());

    private final Slot sunday8 = slot(8, 10);
    private final Slot thursday12 = slot(12, 18);
    private final Slot sunday15 = slot(15, 10);
    private final Slot sunday22 = slot(22, 10);

    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(ion, girlsGroup),
            List.of(sunday8, thursday12, sunday15, sunday22),
            List.of());

    private static Slot slot(int day, int hour) {
        return new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, day, hour, 0),
                Map.of(Role.PRAYER, 1, Role.SERMON, 1, Role.SINGING, 1));
    }

    @Test
    void noViolationWhenRepeatIsTwoWeeksApart() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.SERMON, ion.id()));
        schedule.add(new Assignment(sunday22.id(), Role.SERMON, ion.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void differentRolesDoNotCount() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SERMON, ion.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void singingIsIgnored() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.SINGING, girlsGroup.id()));
        schedule.add(new Assignment(sunday15.id(), Role.SINGING, girlsGroup.id()));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void reportsSameRoleOnConsecutiveSundays() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.SOFT);
        assertThat(v.weight()).isEqualTo(10);
        assertThat(v.constraintName()).isEqualTo("RoleRotation");
        assertThat(v.slotId()).isEqualTo(sunday15.id());
        assertThat(v.participantId()).isEqualTo(ion.id());
    }

    @Test
    void reportsRepeatOnTheThursdayAfterASunday() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.SERMON, ion.id()));
        schedule.add(new Assignment(thursday12.id(), Role.SERMON, ion.id()));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).slotId()).isEqualTo(thursday12.id());
    }

    @Test
    void chainOfThreeRepeatsGivesTwoViolations() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday8.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday22.id(), Role.PRAYER, ion.id()));

        assertThat(constraint.evaluate(schedule, problem)).hasSize(2);
    }
}