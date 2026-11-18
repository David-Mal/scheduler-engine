package scheduler.constraints;

import org.junit.jupiter.api.Test;
import scheduler.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleValidatorTest {

    private final ScheduleValidator validator = new ScheduleValidator();

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER), Map.of());
    private final Slot sunday = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 1, 10, 0), Map.of(Role.PRAYER, 1));
    private final ScheduleProblem problem =
            new ScheduleProblem(List.of(ion), List.of(sunday), List.of());
    private final Schedule schedule = new Schedule(List.of(
            new Assignment(sunday.id(), Role.PRAYER, ion.id())));

    private static Constraint returning(Violation... violations) {
        return (s, p) -> List.of(violations);
    }

    @Test
    void reportRejectsNullViolations() {
        assertThatThrownBy(() -> new ValidationReport(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noConstraintsMeansValidWithZeroScore() {
        ValidationReport report = validator.validate(schedule, problem, List.of());

        assertThat(report.isValid()).isTrue();
        assertThat(report.score()).isZero();
    }

    @Test
    void hardViolationMakesPlanInvalid() {
        Constraint hard = returning(Violation.hard("X", "broken", sunday.id(), ion.id()));

        ValidationReport report = validator.validate(schedule, problem, List.of(hard));

        assertThat(report.isValid()).isFalse();
        assertThat(report.score()).isZero();
    }

    @Test
    void softViolationsAddUpToScoreAndPlanStaysValid() {
        Constraint soft = returning(
                Violation.soft("Y", 10, "a", null, ion.id()),
                Violation.soft("Y", 5, "b", null, ion.id()));

        ValidationReport report = validator.validate(schedule, problem, List.of(soft));

        assertThat(report.isValid()).isTrue();
        assertThat(report.score()).isEqualTo(15);
    }

    @Test
    void collectsViolationsFromAllConstraints() {
        Constraint first = returning(Violation.hard("A", "m", null, null));
        Constraint second = returning(
                Violation.soft("B", 1, "m", null, null),
                Violation.soft("B", 2, "m", null, null));

        ValidationReport report = validator.validate(schedule, problem, List.of(first, second));

        assertThat(report.violations()).hasSize(3);
    }

    @Test
    void unknownSlotThrowsBeforeAnyConstraintRuns() {
        Schedule bad = new Schedule(List.of(
                new Assignment(UUID.randomUUID(), Role.PRAYER, ion.id())));
        boolean[] called = {false};
        Constraint spy = (s, p) -> {
            called[0] = true;
            return List.of();
        };

        assertThatThrownBy(() -> validator.validate(bad, problem, List.of(spy)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown slot");
        assertThat(called[0]).isFalse();
    }

    @Test
    void unknownParticipantThrows() {
        Schedule bad = new Schedule(List.of(
                new Assignment(sunday.id(), Role.PRAYER, UUID.randomUUID())));

        assertThatThrownBy(() -> validator.validate(bad, problem, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown participant");
    }

    @Test
    void worksWithRealConstraints() {
        ValidationReport report = validator.validate(new Schedule(), problem,
                List.of(new SlotFullyCovered()));

        assertThat(report.isValid()).isFalse();
        assertThat(report.violations()).hasSize(1);
    }

    @Test
    void rejectsNullArguments() {
        assertThatThrownBy(() -> validator.validate(null, problem, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validator.validate(schedule, null, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> validator.validate(schedule, problem, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void reportViolationsCannotBeModified() {
        ValidationReport report = validator.validate(schedule, problem,
                List.of(returning(Violation.hard("A", "m", null, null))));

        assertThatThrownBy(() -> report.violations().add(Violation.hard("B", "m", null, null)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reportIsNotAffectedByLaterChangesToTheSourceList() {
        List<Violation> source = new ArrayList<>();
        source.add(Violation.hard("A", "m", null, null));

        ValidationReport report = new ValidationReport(source);
        source.add(Violation.hard("B", "m", null, null));

        assertThat(report.violations()).hasSize(1);
    }

    // ---- small hand-made plans checked with the real constraints ----

    private final Participant realIon = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant realAna = new Participant(UUID.randomUUID(), "Ana",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());
    private final Participant choir = new Participant(UUID.randomUUID(), "Choir",
            Set.of(Role.SINGING), Map.of());
    private final Participant boys = new Participant(UUID.randomUUID(), "Boys' group",
            Set.of(Role.SINGING), Map.of());

    private final Slot nov1 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 1, 10, 0),
            Map.of(Role.PRAYER, 1, Role.SERMON, 1, Role.SINGING, 1));
    private final Slot nov8 = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 8, 10, 0),
            Map.of(Role.PRAYER, 1, Role.SERMON, 1, Role.SINGING, 1));

    private final ScheduleProblem realProblem = new ScheduleProblem(
            List.of(realIon, realAna, choir, boys), List.of(nov1, nov8), List.of());

    private final List<Constraint> allConstraints = List.of(
            new NoDoubleRoleInSlot(),
            new RespectUnavailability(),
            new ParticipantHasRole(),
            new SlotFullyCovered(),
            new MonthlyLimit(),
            new NoSermonTwiceSameDay(),
            new RoleRotation(10),
            new FairRoleDistribution(Role.PRAYER, 5),
            new FairRoleDistribution(Role.SERMON, 5),
            new FairRoleDistribution(Role.SINGING, 5));

    private static Assignment assign(Slot slot, Role role, Participant participant) {
        return new Assignment(slot.id(), role, participant.id());
    }

    // everyone alternates roles and groups: no hard and no soft violation
    private List<Assignment> validAssignments() {
        return List.of(
                assign(nov1, Role.PRAYER, realIon), assign(nov1, Role.SERMON, realAna),
                assign(nov1, Role.SINGING, choir),
                assign(nov8, Role.PRAYER, realAna), assign(nov8, Role.SERMON, realIon),
                assign(nov8, Role.SINGING, boys));
    }

    // Ion always prays and Ana always preaches: valid, but with soft violations
    private List<Assignment> repetitiveAssignments() {
        return List.of(
                assign(nov1, Role.PRAYER, realIon), assign(nov1, Role.SERMON, realAna),
                assign(nov1, Role.SINGING, choir),
                assign(nov8, Role.PRAYER, realIon), assign(nov8, Role.SERMON, realAna),
                assign(nov8, Role.SINGING, boys));
    }

    @Test
    void validPlanHasNoViolationsWithAllRealConstraints() {
        ValidationReport report = validator.validate(
                new Schedule(validAssignments()), realProblem, allConstraints);

        assertThat(report.violations()).isEmpty();
        assertThat(report.isValid()).isTrue();
        assertThat(report.score()).isZero();
    }

    @Test
    void missingSingerIsOneRealHardViolation() {
        List<Assignment> assignments = new ArrayList<>(validAssignments());
        assignments.remove(assign(nov8, Role.SINGING, boys));

        ValidationReport report = validator.validate(
                new Schedule(assignments), realProblem, allConstraints);

        assertThat(report.isValid()).isFalse();
        assertThat(report.violations()).hasSize(1);
        Violation v = report.violations().get(0);
        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.constraintName()).isEqualTo("SlotFullyCovered");
        assertThat(v.slotId()).isEqualTo(nov8.id());
        assertThat(report.score()).isZero();
    }

    @Test
    void repetitivePlanIsValidButHasRealSoftViolations() {
        ValidationReport report = validator.validate(
                new Schedule(repetitiveAssignments()), realProblem, allConstraints);

        // rotation: Ion prayer + Ana sermon on consecutive Sundays = 2 x 10
        // fairness: prayer (Ion 2, Ana 0) and sermon (Ana 2, Ion 0) = 2 x 5
        assertThat(report.isValid()).isTrue();
        assertThat(report.violations()).hasSize(4)
                .allMatch(v -> v.severity() == Severity.SOFT);
        assertThat(report.score()).isEqualTo(30);
    }

    @Test
    void hardAndSoftViolationsAreBothReported() {
        List<Assignment> assignments = new ArrayList<>(repetitiveAssignments());
        assignments.remove(assign(nov8, Role.SINGING, boys));

        ValidationReport report = validator.validate(
                new Schedule(assignments), realProblem, allConstraints);

        assertThat(report.isValid()).isFalse();
        assertThat(report.violations()).hasSize(5);
        assertThat(report.violations().stream()
                .filter(v -> v.severity() == Severity.HARD)).hasSize(1);
        assertThat(report.score()).isEqualTo(30);
    }
}