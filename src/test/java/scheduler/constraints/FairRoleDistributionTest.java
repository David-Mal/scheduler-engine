package scheduler.constraints;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import scheduler.domain.*;

import static org.assertj.core.api.Assertions.assertThat;

class FairRoleDistributionTest {

    private final FairRoleDistribution constraint = new FairRoleDistribution(Role.SINGING, 10);

    private final Participant choir = singer("Choir");
    private final Participant boys = singer("Boys' group");
    private final Participant girls = singer("Girls' group");
    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER, Role.SERMON), Map.of());

    private final List<Slot> slots = List.of(slot(1), slot(8), slot(15), slot(22), slot(29));

    private final ScheduleProblem problem = new ScheduleProblem(
            List.of(choir, boys, girls, ion), slots, List.of());

    private static Participant singer(String name) {
        return new Participant(UUID.randomUUID(), name, Set.of(Role.SINGING), Map.of());
    }

    private static Slot slot(int day) {
        return new Slot(UUID.randomUUID(), LocalDateTime.of(2026, 11, day, 10, 0),
                Map.of(Role.SINGING, 1));
    }

    // the i-th participant sings in the i-th slot
    private Schedule singing(Participant... singers) {
        Schedule schedule = new Schedule();
        for (int i = 0; i < singers.length; i++) {
            schedule.add(new Assignment(slots.get(i).id(), Role.SINGING, singers[i].id()));
        }
        return schedule;
    }

    @Test
    void noViolationWhenPerfectlyBalanced() {
        assertThat(constraint.evaluate(singing(choir, boys, girls), problem)).isEmpty();
    }

    @Test
    void noViolationWhenDifferenceIsOne() {
        // choir 2, boys 1, girls 1
        assertThat(constraint.evaluate(singing(choir, boys, girls, choir), problem)).isEmpty();
    }

    @Test
    void reportsOverusedSinger() {
        // boys 4, choir 1, girls 0
        List<Violation> violations =
                constraint.evaluate(singing(boys, boys, boys, boys, choir), problem);

        assertThat(violations).hasSize(1);
        Violation v = violations.get(0);
        assertThat(v.severity()).isEqualTo(Severity.SOFT);
        assertThat(v.constraintName()).isEqualTo("FairRoleDistribution");
        assertThat(v.weight()).isEqualTo(30);
        assertThat(v.participantId()).isEqualTo(boys.id());
        assertThat(v.slotId()).isNull();
    }

    @Test
    void singerWhoNeverSingsCountsAsZero() {
        // choir 2, boys 0, girls 0
        List<Violation> violations = constraint.evaluate(singing(choir, choir), problem);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).participantId()).isEqualTo(choir.id());
        assertThat(violations.get(0).weight()).isEqualTo(10);
    }

    @Test
    void noSingersMeansNoViolations() {
        ScheduleProblem withoutSingers = new ScheduleProblem(List.of(ion), slots, List.of());

        assertThat(constraint.evaluate(new Schedule(), withoutSingers)).isEmpty();
    }

    @Test
    void worksForAnyRoleAndIgnoresTheOtherRoles() {
        Participant dan = new Participant(UUID.randomUUID(), "Dan",
                Set.of(Role.PRAYER, Role.SERMON), Map.of());
        ScheduleProblem withTwoSpeakers = new ScheduleProblem(
                List.of(choir, ion, dan), slots, List.of());

        // Ion prays 3 times and Dan never; singing must not influence the result
        Schedule schedule = new Schedule();
        for (int i = 0; i < 3; i++) {
            schedule.add(new Assignment(slots.get(i).id(), Role.PRAYER, ion.id()));
            schedule.add(new Assignment(slots.get(i).id(), Role.SINGING, choir.id()));
        }

        List<Violation> violations =
                new FairRoleDistribution(Role.PRAYER, 5).evaluate(schedule, withTwoSpeakers);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).participantId()).isEqualTo(ion.id());
        assertThat(violations.get(0).weight()).isEqualTo(10);
        assertThat(violations.get(0).message()).contains("Ion", "PRAYER");
    }
}