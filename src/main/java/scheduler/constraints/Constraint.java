package scheduler.constraints;

import scheduler.domain.Schedule;
import scheduler.domain.ScheduleProblem;

import java.util.List;

public interface Constraint {
    List<Violation> evaluate(Schedule schedule, ScheduleProblem problem);

    default String name() {
        return this.getClass().getSimpleName();
    }

}
