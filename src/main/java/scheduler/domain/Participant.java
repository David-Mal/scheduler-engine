package scheduler.domain;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Participant(UUID id, String name,
                          Set<Role> allowedRoles, Map<Role, Integer> monthlyLimit) {

    public Participant {
        if (id == null || name == null || name.isBlank()
                || allowedRoles == null || monthlyLimit == null) {
            throw new IllegalArgumentException("id, name, allowedRoles and monthlyLimit are required");
        }
        if (allowedRoles.isEmpty()) {
            throw new IllegalArgumentException("A participant needs at least one allowed role");
        }
        if (monthlyLimit.values().stream().anyMatch(n -> n == null || n < 0)) {
            throw new IllegalArgumentException("Monthly limits must be 0 or more");
        }
        if (!allowedRoles.containsAll(monthlyLimit.keySet())) {
            throw new IllegalArgumentException("A limit is set for a role the participant cannot do");
        }
        allowedRoles = Set.copyOf(allowedRoles);
        monthlyLimit = Map.copyOf(monthlyLimit);
    }
}
