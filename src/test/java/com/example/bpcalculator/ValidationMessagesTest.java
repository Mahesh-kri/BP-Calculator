package com.example.bpcalculator;

import com.example.bpcalculator.model.BloodPressure;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ValidationMessagesTest {

    @Autowired
    private Validator validator;

    @Test
    void validationMessagesResolveFromMessageSource() {
        Set<String> messages = validator.validate(new BloodPressure()).stream()
                .map(v -> v.getMessage())
                .collect(Collectors.toSet());

        assertThat(messages).containsExactlyInAnyOrder("Systolic is required", "Diastolic is required");
    }
}
