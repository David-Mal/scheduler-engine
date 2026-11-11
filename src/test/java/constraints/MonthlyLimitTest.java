package constraints;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import scheduler.constraints.*;
import scheduler.domain.*;


import static org.assertj.core.api.Assertions.assertThat;

class MonthlyLimitTest {

    private final MonthlyLimit constraint = new MonthlyLimit();

    private final List<Slot> slots = List.of(slot(1), slot(8), slot(15));

    private static Slot slot(int day) {
        return new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, day, 10, 0),
                Map.of(Role.PRAYER, 1, Role.SERMON, 1));
    }

    private Participant person(Map<Role, Integer> limits) {
        return new Participant(UUID.randomUUID(), "Ion",
                Set.of(Role.PRAYER, Role.SERMON), limits);
    }

    private ScheduleProblem problemWith(Participant p) {
        return new ScheduleProblem(List.of(p), slots, List.of());
    }

    private Assignment assign(int slotIndex, Role role, Participant p) {
        return new Assignment(slots.get(slotIndex).id(), role, p.id());
    }

    @Test
    void noViolationWhenWithinLimit() {
        Participant ion = person(Map.of(Role.PRAYER, 2));
        Schedule schedule = new Schedule(List.of(
                assign(0, Role.PRAYER, ion), assign(1, Role.PRAYER, ion)));

        assertThat(constraint.evaluate(schedule, problemWith(ion))).isEmpty();
    }

    @Test
    void reportsViolationWhenLimitExceeded() {
        Participant ion = person(Map.of(Role.PRAYER, 2));
        Schedule schedule = new Schedule(List.of(
                assign(0, Role.PRAYER, ion), assign(1, Role.PRAYER, ion),
                assign(2, Role.PRAYER, ion)));

        List<Violation> violations = constraint.evaluate(schedule, problemWith(ion));

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("MonthlyLimit");
        assertThat(v.participantId()).isEqualTo(ion.id());
        assertThat(v.slotId()).isNull();
    }

    @Test
    void noLimitMeansNoViolation() {
        Participant ion = person(Map.of());
        Schedule schedule = new Schedule(List.of(
                assign(0, Role.PRAYER, ion), assign(1, Role.PRAYER, ion),
                assign(2, Role.PRAYER, ion)));

        assertThat(constraint.evaluate(schedule, problemWith(ion))).isEmpty();
    }

    @Test
    void limitAppliesOnlyToItsOwnRole() {
        Participant ion = person(Map.of(Role.PRAYER, 1));
        Schedule schedule = new Schedule(List.of(
                assign(0, Role.PRAYER, ion), assign(1, Role.SERMON, ion),
                assign(2, Role.SERMON, ion)));

        assertThat(constraint.evaluate(schedule, problemWith(ion))).isEmpty();
    }
}