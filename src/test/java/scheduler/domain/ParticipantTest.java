package scheduler.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParticipantTest {
    @Test
    public void storeParticipant() {
        UUID id = UUID.randomUUID();
        Participant user = new Participant(
            id,
            "Ion Banu",
            Set.of(Role.PRAYER, Role.SERMON),
            Map.of(Role.SERMON, 2));
        assertThat(user.id()).isEqualTo(id);
        assertThat(user.name()).isEqualTo("Ion Banu");
        assertThat(user.allowedRoles()).containsExactlyInAnyOrder(Role.PRAYER, Role.SERMON);
        assertThat(user.monthlyLimit()).containsEntry(Role.SERMON, 2);
    }

    @Test
    void groupCanHaveOnlySinging() {
        Participant boys = new Participant(
                UUID.randomUUID(),
                "Boys' group",
                Set.of(Role.SINGING),
                Map.of());

        assertThat(boys.allowedRoles()).containsOnly(Role.SINGING);
        assertThat(boys.monthlyLimit()).isEmpty();
    }

    @Test
    void rejectsMissingFields() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new Participant(null, "Ion", Set.of(Role.PRAYER), Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Participant(id, " ", Set.of(Role.PRAYER), Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Participant(id, "Ion", null, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Participant(id, "Ion", Set.of(Role.PRAYER), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsParticipantWithoutRoles() {
        assertThatThrownBy(() -> new Participant(UUID.randomUUID(), "Ion", Set.of(), Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeLimit() {
        assertThatThrownBy(() -> new Participant(UUID.randomUUID(), "Ion",
                Set.of(Role.SERMON), Map.of(Role.SERMON, -1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLimitForRoleThatIsNotAllowed() {
        assertThatThrownBy(() -> new Participant(UUID.randomUUID(), "Ion",
                Set.of(Role.PRAYER), Map.of(Role.SERMON, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void copiesItsCollections() {
        Set<Role> roles = new HashSet<>(Set.of(Role.PRAYER));
        Map<Role, Integer> limits = new HashMap<>(Map.of(Role.PRAYER, 2));
        Participant ion = new Participant(UUID.randomUUID(), "Ion", roles, limits);

        roles.add(Role.SERMON);
        limits.put(Role.PRAYER, 99);

        assertThat(ion.allowedRoles()).containsOnly(Role.PRAYER);
        assertThat(ion.monthlyLimit()).containsEntry(Role.PRAYER, 2);
    }
}
