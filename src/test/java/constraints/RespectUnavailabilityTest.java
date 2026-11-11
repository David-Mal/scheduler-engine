package constraints;

import org.junit.jupiter.api.Test;

import scheduler.constraints.RespectUnavailability;
import scheduler.constraints.Violation;
import scheduler.domain.*;
import scheduler.constraints.Severity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RespectUnavailabilityTest {

    private final RespectUnavailability constraint = new RespectUnavailability();

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER), Map.of());
    private final Participant ana = new Participant(UUID.randomUUID(), "Ana",
            Set.of(Role.PRAYER), Map.of());
    private final Slot sunday15 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0), Map.of(Role.PRAYER, 1));
    private final Slot sunday22 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 22, 10, 0), Map.of(Role.PRAYER, 1));

    private ScheduleProblem problemWith(Unavailability... unavailabilities) {
        return new ScheduleProblem(List.of(ion, ana), List.of(sunday15, sunday22),
                List.of(unavailabilities));
    }

    @Test
    void noViolationWhenNobodyIsUnavailable() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));

        assertThat(constraint.evaluate(schedule, problemWith())).isEmpty();
    }

    @Test
    void unavailabilityOfAnotherPersonDoesNotMatter() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        Unavailability anaAway = Unavailability.onDay(ana.id(), LocalDate.of(2026, 11, 15));

        assertThat(constraint.evaluate(schedule, problemWith(anaAway))).isEmpty();
    }

    @Test
    void noViolationWhenUnavailabilityEndsBeforeTheSlot() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        Unavailability ionAway = new Unavailability(ion.id(),
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 14));

        assertThat(constraint.evaluate(schedule, problemWith(ionAway))).isEmpty();
    }

    @Test
    void reportsOnlyTheAssignmentOnTheUnavailableDay() {
        Schedule schedule = new Schedule();
        schedule.add(new Assignment(sunday15.id(), Role.PRAYER, ion.id()));
        schedule.add(new Assignment(sunday22.id(), Role.PRAYER, ion.id()));
        Unavailability ionAway = Unavailability.onDay(ion.id(), LocalDate.of(2026, 11, 15));

        List<Violation> violations = constraint.evaluate(schedule, problemWith(ionAway));

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("RespectUnavailability");
        assertThat(v.slotId()).isEqualTo(sunday15.id());
        assertThat(v.participantId()).isEqualTo(ion.id());
    }
}