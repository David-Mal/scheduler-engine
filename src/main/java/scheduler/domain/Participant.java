package scheduler.domain;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record Participant (UUID id, String name,
                           Set<Role> allowedRoles, Map<Role, Integer> monthlyLimit) {}

