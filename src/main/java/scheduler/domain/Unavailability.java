package scheduler.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record Unavailability(UUID participantId, LocalDate from, LocalDate to) {

    public Unavailability {
        if (participantId == null || from == null || to == null) {
            throw new IllegalArgumentException("participantId, from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from date must be before or equal to to date");
        }
    }

    public static Unavailability onDay(UUID participantId, LocalDate date) {
        return new Unavailability(participantId, date, date);
    }

    public boolean covers(Slot slot) {
        LocalDate day = slot.start().toLocalDate();
        return !day.isBefore(from) && !day.isAfter(to);
    }

}
