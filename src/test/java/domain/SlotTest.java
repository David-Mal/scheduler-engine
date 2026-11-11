package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.Role;
import scheduler.domain.Slot;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotTest {

    @Test
    void storesItsFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2026, 11, 8, 10, 0);

        Slot slot = new Slot(id, start,
                Map.of(Role.PRAYER, 2, Role.SERMON, 2, Role.SINGING, 1));

        assertThat(slot.id()).isEqualTo(id);
        assertThat(slot.start()).isEqualTo(start);
        assertThat(slot.requiredRoles())
                .containsEntry(Role.PRAYER, 2)
                .containsEntry(Role.SERMON, 2)
                .containsEntry(Role.SINGING, 1);
    }

    @Test
    void rolesAreCopiedDefensively() {
        Map<Role, Integer> roles = new HashMap<>(Map.of(Role.PRAYER, 1));
        Slot slot = new Slot(UUID.randomUUID(),
                LocalDateTime.of(2026, 11, 8, 18, 0), roles);

        roles.put(Role.SERMON, 1);

        assertThat(slot.requiredRoles()).containsOnlyKeys(Role.PRAYER);
    }

    @Test
    void rejectsEmptyRoles() {
        assertThatThrownBy(() -> new Slot(UUID.randomUUID(),
                LocalDateTime.of(2026, 11, 8, 10, 0), Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsZeroPeopleForARole() {
        assertThatThrownBy(() -> new Slot(UUID.randomUUID(),
                LocalDateTime.of(2026, 11, 8, 10, 0), Map.of(Role.PRAYER, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}