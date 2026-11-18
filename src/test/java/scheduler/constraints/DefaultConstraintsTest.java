package scheduler.constraints;

import org.junit.jupiter.api.Test;
import scheduler.domain.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultConstraintsTest {

    private final ScheduleValidator validator = new ScheduleValidator();

    @Test
    void hasTenConstraints() {
        assertThat(DefaultConstraints.standard()).hasSize(10);
    }

    @Test
    void validPlanPassesWithZeroScore() {
        Participant ion = new Participant(UUID.randomUUID(), "Ion", Set.of(Role.PRAYER), Map.of());
        Participant vasile = new Participant(UUID.randomUUID(), "Vasile", Set.of(Role.SERMON), Map.of());
        Slot sunday = new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, 1, 10, 0),
                Map.of(Role.PRAYER, 1, Role.SERMON, 1));
        ScheduleProblem problem = new ScheduleProblem(List.of(ion, vasile), List.of(sunday), List.of());
        Schedule schedule = new Schedule(List.of(
                new Assignment(sunday.id(), Role.PRAYER, ion.id()),
                new Assignment(sunday.id(), Role.SERMON, vasile.id())));

        ValidationReport report = validator.validate(schedule, problem, DefaultConstraints.standard());

        assertThat(report.isValid()).isTrue();
        assertThat(report.score()).isZero();
    }

    @Test
    void planBreakingEveryHardRuleIsReportedByAllSixHardConstraints() {
        Participant ion = new Participant(UUID.randomUUID(), "Ion",
                Set.of(Role.PRAYER, Role.SERMON), Map.of(Role.SERMON, 1));
        Participant maria = new Participant(UUID.randomUUID(), "Maria", Set.of(Role.SINGING), Map.of());

        Slot morning = new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, 1, 10, 0),
                Map.of(Role.PRAYER, 1, Role.SERMON, 1));
        Slot evening = new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, 1, 18, 0),
                Map.of(Role.PRAYER, 1, Role.SERMON, 1));
        Slot thursday = new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, 5, 18, 0),
                Map.of(Role.PRAYER, 1));

        ScheduleProblem problem = new ScheduleProblem(
                List.of(ion, maria),
                List.of(morning, evening, thursday),
                List.of(Unavailability.onDay(ion.id(), LocalDate.of(2026, 11, 1))));

        Schedule schedule = new Schedule(List.of(
                new Assignment(morning.id(), Role.PRAYER, ion.id()),   // double role + unavailable
                new Assignment(morning.id(), Role.SERMON, ion.id()),
                new Assignment(evening.id(), Role.SERMON, ion.id()),   // 2 sermons same day, over limit
                new Assignment(evening.id(), Role.PRAYER, maria.id()))); // Maria cannot pray; Thursday empty

        ValidationReport report = validator.validate(schedule, problem, DefaultConstraints.standard());

        Set<String> violated = report.violations().stream()
                .map(Violation::constraintName)
                .collect(Collectors.toSet());

        assertThat(report.isValid()).isFalse();
        assertThat(violated).contains(
                "NoDoubleRoleInSlot", "RespectUnavailability", "ParticipantHasRole",
                "SlotFullyCovered", "MonthlyLimit", "NoSermonTwiceSameDay");
    }
}