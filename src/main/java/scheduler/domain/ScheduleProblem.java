package scheduler.domain;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScheduleProblem {

    private final Map<UUID, Participant> participants;
    private final Map<UUID, Slot> slots;
    private final List<Unavailability> unavailabilities;

    public ScheduleProblem(Collection<Participant> participants,
                           Collection<Slot> slots, 
                           Collection<Unavailability> unavailabilities) {
        this.participants = participants.stream().collect(Collectors.toUnmodifiableMap(Participant::id, Function.identity()));
        this.slots = slots.stream().collect(Collectors.toUnmodifiableMap(Slot::id, Function.identity()));
        this.unavailabilities = List.copyOf(unavailabilities);
    }
    public Participant participant(UUID id) {
        Participant p = participants.get(id);
        if (p == null) {
            throw new IllegalArgumentException("Unknown participant: " + id);
        }
        return p;
    }

    public Slot slot(UUID id) {
        Slot s = slots.get(id);
        if (s == null) {
            throw new IllegalArgumentException("Unknown slot: " + id);
        }
        return s;
    }

    public List<Participant> participants() {
        return participants.values().stream()
                .sorted(Comparator.comparing(Participant::name).thenComparing(Participant::id))
                .toList();
    }

    public List<Slot> slots() {
        return slots.values().stream()
                .sorted(Comparator.comparing(Slot::start).thenComparing(Slot::id))
                .toList();
    }

    public List<Unavailability> unavailabilities() {
        return unavailabilities;
    }

    public boolean containsSlot(UUID id) {
        return slots.containsKey(id);
    }

    public boolean containsParticipant(UUID id) {
        return participants.containsKey(id);
    }
}
