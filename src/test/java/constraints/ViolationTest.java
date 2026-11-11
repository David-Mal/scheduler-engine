package constraints;

import org.junit.jupiter.api.Test;
import scheduler.constraints.Severity;
import scheduler.constraints.Violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViolationTest {

    @Test
    void hardViolationHasZeroWeight() {
        Violation v = Violation.hard("RespectUnavailability", "Unavailable", null, null);

        assertThat(v.severity()).isEqualTo(Severity.HARD);
        assertThat(v.weight()).isZero();
    }

    @Test
    void softViolationKeepsItsWeight() {
        Violation v = Violation.soft("Rotation", 5, "Repeated role", null, null);

        assertThat(v.severity()).isEqualTo(Severity.SOFT);
        assertThat(v.weight()).isEqualTo(5);
    }

    @Test
    void rejectsNegativeWeight() {
        assertThatThrownBy(() -> Violation.soft("Rotation", -1, "msg", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}