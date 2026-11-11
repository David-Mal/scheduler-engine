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

class NoSermonTwiceSameDayTest {

    private final NoSermonTwiceSameDay constraint = new NoSermonTwiceSameDay();

    private final Participant ion = person("Ion");
    private final Participant vasile = person("Vasile");

    private final Slot sundayMorning = slot(LocalDateTime.of(2026, 11, 1, 10, 0));
    private final Slot sundayEvening = slot(LocalDateTime.of(2026, 11, 1, 18, 0));
    private final Slot thursday = slot(LocalDateTime.of(2026, 11, 5, 18, 0));

    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(ion, vasile), List.of(sundayMorning, sundayEvening, thursday), List.of());

    private static Participant person(String name) {
        return new Participant(UUID.randomUUID(), name,
                Set.of(Role.SERMON, Role.PRAYER), Map.of());
    }

    private static Slot slot(LocalDateTime start) {
        return new Slot(UUID.randomUUID(), start, Map.of(Role.SERMON, 1));
    }

    private static Assignment assign(Slot slot, Role role, Participant p) {
        return new Assignment(slot.id(), role, p.id());
    }

    @Test
    void reportsSamePersonPreachingTwiceOnSameDay() {
        Schedule schedule = new Schedule(List.of(
                assign(sundayMorning, Role.SERMON, ion),
                assign(sundayEvening, Role.SERMON, ion)));

        List<Violation> violations = constraint.evaluate(schedule, problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("NoSermonTwiceSameDay");
        assertThat(v.participantId()).isEqualTo(ion.id());
        assertThat(v.slotId()).isEqualTo(sundayEvening.id());
    }

    @Test
    void noViolationOnDifferentDays() {
        Schedule schedule = new Schedule(List.of(
                assign(sundayMorning, Role.SERMON, ion),
                assign(thursday, Role.SERMON, ion)));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void noViolationForDifferentPeopleOnSameDay() {
        Schedule schedule = new Schedule(List.of(
                assign(sundayMorning, Role.SERMON, ion),
                assign(sundayEvening, Role.SERMON, vasile)));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }

    @Test
    void otherRolesAreIgnored() {
        Schedule schedule = new Schedule(List.of(
                assign(sundayMorning, Role.PRAYER, ion),
                assign(sundayEvening, Role.PRAYER, ion)));

        assertThat(constraint.evaluate(schedule, problem)).isEmpty();
    }
}
