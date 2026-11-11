package scheduler.domain;
import java.util.UUID;

public record Assignment(UUID slotId, Role role, UUID participantId) {

    public Assignment {
        if (slotId == null || role == null || participantId == null) {
            throw new IllegalArgumentException("slotId, role and participantId are required");
        }
    }
}
