package domain;

import org.junit.jupiter.api.Test;
import scheduler.domain.Assignment;
import scheduler.domain.Role;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentTest {

    @Test
    void storesItsFields() {
        UUID slotId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        Assignment a = new Assignment(slotId, Role.SERMON, participantId);

        assertThat(a.slotId()).isEqualTo(slotId);
        assertThat(a.role()).isEqualTo(Role.SERMON);
        assertThat(a.participantId()).isEqualTo(participantId);
    }

    @Test
    void rejectsNullFields() {
        assertThatThrownBy(() -> new Assignment(null, Role.SERMON, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Assignment(UUID.randomUUID(), null, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Assignment(UUID.randomUUID(), Role.SERMON, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}