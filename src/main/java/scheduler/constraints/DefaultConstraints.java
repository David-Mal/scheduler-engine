package scheduler.constraints;

import scheduler.domain.Role;

import java.util.List;

public final class DefaultConstraints {

    public static final int ROTATION_WEIGHT = 10;
    public static final int FAIRNESS_WEIGHT = 5;

    private DefaultConstraints() {} // prevents the usage of new DefaultConstraints()

    public static List<Constraint> standard() {
        return List.of(
                // hard
                new NoDoubleRoleInSlot(),
                new RespectUnavailability(),
                new ParticipantHasRole(),
                new SlotFullyCovered(),
                new MonthlyLimit(),
                new NoSermonTwiceSameDay(),
                // soft
                new RoleRotation(ROTATION_WEIGHT),
                new FairRoleDistribution(Role.PRAYER, FAIRNESS_WEIGHT),
                new FairRoleDistribution(Role.SERMON, FAIRNESS_WEIGHT),
                new FairRoleDistribution(Role.SINGING, FAIRNESS_WEIGHT));
    }
}
