package scheduler.domain;

import scheduler.domain.Role;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record Slot(UUID id, LocalDateTime start, Map<Role, Integer> requiredRoles) {

    public Slot {
        if (start == null || requiredRoles == null) {
            throw new IllegalArgumentException("start and requiredRoles are required");
        }
        if (requiredRoles.isEmpty()) {
            throw new IllegalArgumentException("A slot needs at least one role");
        }
        if (requiredRoles.values().stream().anyMatch(n -> n == null || n < 1)) {
            throw new IllegalArgumentException("Each role needs at least 1 person");
        }
        requiredRoles = Map.copyOf(requiredRoles);
    }
}