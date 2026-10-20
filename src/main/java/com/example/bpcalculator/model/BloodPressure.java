package com.example.bpcalculator.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BloodPressure {

    public static final int SYSTOLIC_MIN = 70;
    public static final int SYSTOLIC_MAX = 190;
    public static final int DIASTOLIC_MIN = 40;
    public static final int DIASTOLIC_MAX = 100;

    @NotNull(message = "{bp.systolic.required}")
    @Min(value = SYSTOLIC_MIN, message = "{bp.systolic.min}")
    @Max(value = SYSTOLIC_MAX, message = "{bp.systolic.max}")
    private Integer systolic;   // mmHG

    @NotNull(message = "{bp.diastolic.required}")
    @Min(value = DIASTOLIC_MIN, message = "{bp.diastolic.min}")
    @Max(value = DIASTOLIC_MAX, message = "{bp.diastolic.max}")
    private Integer diastolic;  // mmHG

    // calculate BP category using standard NHS thresholds
    public BpCategory getCategory() {
        if (systolic == null || diastolic == null) {
            return null;
        }
        if (systolic < 90 || diastolic < 60) {
            return BpCategory.LOW;
        }
        if (systolic >= 141 || diastolic >= 91) {
            return BpCategory.HIGH;
        }
        if (systolic >= 121 || diastolic >= 81) {
            return BpCategory.PRE_HIGH;
        }
        return BpCategory.IDEAL;
    }
}
