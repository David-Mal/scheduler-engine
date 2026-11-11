package scheduler.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Schedule {
    private final List<Assignment> assignments = new ArrayList<>();

    public Schedule() {
    }

    public Schedule(Collection<Assignment> initial) {
        initial.forEach(this::add);
    }

    public void add(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("assignment is required");
        }
        if (assignments.contains(assignment)) {
            throw new IllegalArgumentException("assignment already exists");
        }
        assignments.add(assignment);
    }

    public boolean remove(Assignment assignment) {
        return assignments.remove(assignment);
    }

    public List<Assignment> getAssignmentsFor(Slot slot) {
        return assignments.stream()
                .filter(a -> a.slotId().equals(slot.id()))
                .toList();
    }

    public List<Assignment> getAssignmentsFor(Participant participant) {
        return assignments.stream()
                .filter(a -> a.participantId().equals(participant.id()))
                .toList();
    }

    public Schedule copy() {
        return new Schedule(assignments);
    }

    public List<Assignment> assignments() {
        return List.copyOf(assignments);
    }
}