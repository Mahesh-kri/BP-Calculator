package com.example.bpcalculator.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class BloodPressureTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violationProperties(BloodPressure bp) {
        return VALIDATOR.validate(bp).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private BloodPressure bp(int systolic, int diastolic) {
        BloodPressure bp = new BloodPressure();
        bp.setSystolic(systolic);
        bp.setDiastolic(diastolic);
        return bp;
    }

    @Test
    void lowWhenSystolicBelow90() {
        assertThat(bp(85, 70).getCategory()).isEqualTo(BpCategory.LOW);
    }

    @Test
    void lowWhenDiastolicBelow60() {
        assertThat(bp(100, 55).getCategory()).isEqualTo(BpCategory.LOW);
    }

    @Test
    void idealWhenSystolic90To120AndDiastolic60To80() {
        assertThat(bp(90, 60).getCategory()).isEqualTo(BpCategory.IDEAL);
        assertThat(bp(100, 70).getCategory()).isEqualTo(BpCategory.IDEAL);
        assertThat(bp(120, 80).getCategory()).isEqualTo(BpCategory.IDEAL);
    }

    @Test
    void preHighWhenSystolic121To140OrDiastolic81To90() {
        assertThat(bp(130, 80).getCategory()).isEqualTo(BpCategory.PRE_HIGH);
        assertThat(bp(120, 85).getCategory()).isEqualTo(BpCategory.PRE_HIGH);
        assertThat(bp(140, 90).getCategory()).isEqualTo(BpCategory.PRE_HIGH);
    }

    @Test
    void highWhenSystolicAtLeast141OrDiastolicAtLeast91() {
        assertThat(bp(141, 80).getCategory()).isEqualTo(BpCategory.HIGH);
        assertThat(bp(120, 91).getCategory()).isEqualTo(BpCategory.HIGH);
        assertThat(bp(180, 100).getCategory()).isEqualTo(BpCategory.HIGH);
    }

    @Test
    void nullCategoryWhenValuesMissing() {
        BloodPressure bp = new BloodPressure();
        assertThat(bp.getCategory()).isNull();
    }

    @Test
    void requiredViolationsWhenNull() {
        assertThat(violationProperties(new BloodPressure()))
                .containsExactlyInAnyOrder("systolic", "diastolic");
    }

    @Test
    void rangeViolationsWhenOutOfBounds() {
        assertThat(violationProperties(bp(200, 30)))
                .containsExactlyInAnyOrder("systolic", "diastolic");
    }

    @Test
    void noViolationsWhenInRange() {
        assertThat(violationProperties(bp(120, 80))).isEmpty();
    }
}
