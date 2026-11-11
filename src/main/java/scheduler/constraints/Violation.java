package scheduler.constraints;

import java.util.UUID;

public record Violation(Severity severity, int weight, String constraintName,
                        String message, UUID slotId, UUID participantId) {

    public Violation {
        if (severity == null || constraintName == null || message == null) {
            throw new IllegalArgumentException("severity, weight, constraintName and message are required");
        }
        if (weight < 0) {
            throw new IllegalArgumentException("weight must not be negative");
        }
    }

    public static Violation hard(String constraintName, String message, UUID slotId, UUID participantId) {
        return new Violation(Severity.HARD, 0, constraintName, message, slotId, participantId);
    }

    public static Violation soft(String constraintName, int weight, String message, UUID slotId, UUID participantId) {
        return new Violation(Severity.SOFT, weight, constraintName, message, slotId, participantId);
    }

}
