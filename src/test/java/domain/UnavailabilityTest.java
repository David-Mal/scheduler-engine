package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.Role;
import scheduler.domain.Slot;
import scheduler.domain.Unavailability;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnavailabilityTest {

    private final UUID ion = UUID.randomUUID();

    private Slot slotOn(LocalDate day, int hour) {
        return new Slot(UUID.randomUUID(), day.atTime(hour, 0), Map.of(Role.PRAYER, 1));
    }

    @Test
    void coversSlotInsideInterval() {
        Unavailability u = new Unavailability(ion,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));

        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 15), 10))).isTrue();
    }

    @Test
    void coversBoundaryDays() {
        Unavailability u = new Unavailability(ion,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));

        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 10), 18))).isTrue();
        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 20), 10))).isTrue();
    }

    @Test
    void doesNotCoverOutsideInterval() {
        Unavailability u = new Unavailability(ion,
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 20));

        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 9), 18))).isFalse();
        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 21), 10))).isFalse();
    }

    @Test
    void singleDayCoversMorningAndEveningOnly() {
        Unavailability u = Unavailability.onDay(ion, LocalDate.of(2026, 11, 15));

        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 15), 10))).isTrue();
        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 15), 18))).isTrue();
        assertThat(u.covers(slotOn(LocalDate.of(2026, 11, 16), 10))).isFalse();
    }

    @Test
    void rejectsReversedInterval() {
        assertThatThrownBy(() -> new Unavailability(ion,
                LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}