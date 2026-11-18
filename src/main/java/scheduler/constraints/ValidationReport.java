package scheduler.constraints;


import java.util.List;

public record ValidationReport(List<Violation> violations) {

    public ValidationReport {
        if (violations == null) {
            throw new IllegalArgumentException("violations cannot be null");
        }
        violations = List.copyOf(violations);
    }

    public boolean isValid() {
        return violations.stream().noneMatch(v -> v.severity() == Severity.HARD);
    }

    // total penalty of the soft violations, best case: 0
    public int score() {
        return violations.stream()
                .filter(v -> v.severity() == Severity.SOFT)
                .mapToInt(Violation::weight)
                .sum();
    }
}
