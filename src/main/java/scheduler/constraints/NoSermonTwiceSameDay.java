package scheduler.constraints;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import scheduler.domain.*;

public class NoSermonTwiceSameDay implements Constraint {

    private record ParticipantDay(UUID participantId, LocalDate day) {}

    @Override
    public List<Violation> evaluate(Schedule schedule, ScheduleProblem problem) {
        Map<ParticipantDay, List<Assignment>> sermonsByPersonAndDay = schedule.assignments().stream()
                .filter(a -> a.role() == Role.SERMON)
                .collect(Collectors.groupingBy(
                        a -> new ParticipantDay(a.participantId(),
                                problem.slot(a.slotId()).start().toLocalDate()),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<Violation> violations = new ArrayList<>();
        for (Map.Entry<ParticipantDay, List<Assignment>> entry : sermonsByPersonAndDay.entrySet()) {
            List<Assignment> sermons = entry.getValue();

            if (sermons.size() > 1) {
                Participant person = problem.participant(entry.getKey().participantId());
                violations.add(Violation.hard(name(),
                        person.name() + " preaches " + sermons.size()
                                + " times on " + entry.getKey().day(),
                        sermons.get(1).slotId(), person.id()));
            }
        }
        return violations;
    }
}