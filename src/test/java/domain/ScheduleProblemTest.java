package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.Participant;
import scheduler.domain.Role;
import scheduler.domain.ScheduleProblem;
import scheduler.domain.Slot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleProblemTest {

    private final Participant ion = new Participant(UUID.randomUUID(), "Ion",
            Set.of(Role.PRAYER), Map.of());
    private final Slot evening = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 18, 0), Map.of(Role.PRAYER, 1));
    private final Slot morning = new Slot(UUID.randomUUID(),
            LocalDateTime.of(2026, 11, 15, 10, 0), Map.of(Role.PRAYER, 1));

    @Test
    void looksUpByIdAndSortsSlots() {
        ScheduleProblem problem = new ScheduleProblem(List.of(ion), List.of(evening, morning), List.of());

        assertThat(problem.participant(ion.id())).isEqualTo(ion);
        assertThat(problem.slot(morning.id())).isEqualTo(morning);
        assertThat(problem.slots()).containsExactly(morning, evening);
    }

    @Test
    void unknownIdIsRejected() {
        ScheduleProblem problem = new ScheduleProblem(List.of(ion), List.of(morning), List.of());

        assertThatThrownBy(() -> problem.slot(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicateIdsAreRejected() {
        assertThatThrownBy(() -> new ScheduleProblem(List.of(ion, ion), List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}