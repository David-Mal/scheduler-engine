package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.Participant;
import scheduler.domain.Role;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}
