package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

    private final Slot sunday = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0), Map.of(Role.PRAYER, 2));
    private final Slot thursday = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 19, 18, 0), Map.of(Role.PRAYER, 2));
    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER), Map.of());
    private final Participant ana = new Participant(UUID.randomUUID(), "Ana",
            Set.of(Role.PRAYER), Map.of());

    @Test
    void findsAssignmentsBySlot() {
        Schedule schedule = new Schedule();
        Assignment a1 = new Assignment(sunday.id(), Role.PRAYER, ion.id());
        Assignment a2 = new Assignment(sunday.id(), Role.PRAYER, ana.id());
        Assignment a3 = new Assignment(thursday.id(), Role.PRAYER, ion.id());
        schedule.add(a1);
        schedule.add(a2);
        schedule.add(a3);

        assertThat(schedule.getAssignmentsFor(sunday)).containsExactlyInAnyOrder(a1, a2);
    }

    @Test
    void findsAssignmentsByParticipant() {
        Schedule schedule = new Schedule();
        Assignment a1 = new Assignment(sunday.id(), Role.PRAYER, ion.id());
        Assignment a2 = new Assignment(sunday.id(), Role.PRAYER, ana.id());
        Assignment a3 = new Assignment(thursday.id(), Role.PRAYER, ion.id());
        schedule.add(a1);
        schedule.add(a2);
        schedule.add(a3);

        assertThat(schedule.getAssignmentsFor(ion)).containsExactlyInAnyOrder(a1, a3);
    }

    @Test
    void removesAssignment() {
        Schedule schedule = new Schedule();
        Assignment a1 = new Assignment(sunday.id(), Role.PRAYER, ion.id());
        schedule.add(a1);

        assertThat(schedule.remove(a1)).isTrue();
        assertThat(schedule.assignments()).isEmpty();
        assertThat(schedule.remove(a1)).isFalse();
    }

    @Test
    void rejectsDuplicateAssignment() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday.id(), Role.PRAYER, ion.id()));

        assertThatThrownBy(() ->
                schedule.add(new Assignment(sunday.id(), Role.PRAYER, ion.id())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void copyIsIndependent() {
        Schedule original = new Schedule();
        original.add(new Assignment(sunday.id(), Role.PRAYER, ion.id()));

        Schedule copy = original.copy();
        copy.add(new Assignment(sunday.id(), Role.PRAYER, ana.id()));

        assertThat(original.assignments()).hasSize(1);
        assertThat(copy.assignments()).hasSize(2);
    }
}