package com.example.bpcalculator.model;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BpCategory {

    LOW("Low Blood Pressure",
            "Your reading is lower than the ideal range. Stay hydrated and rise slowly.",
            "low", false,
            List.of("Drink plenty of water and stay hydrated",
                    "Stand up slowly to avoid dizziness",
                    "Eat regular meals and snacks")),
    IDEAL("Ideal Blood Pressure",
            "Your blood pressure is in the healthy range. Keep up the good work.",
            "ideal", false,
            List.of("Stay active on most days",
                    "Eat a balanced, low-salt diet",
                    "Limit alcohol and keep to a healthy weight")),
    PRE_HIGH("Pre-High Blood Pressure",
            "Slightly above the ideal range. Healthy lifestyle changes can help.",
            "prehigh", false,
            List.of("Cut back on salt and salty foods",
                    "Exercise regularly",
                    "Maintain a healthy weight")),
    HIGH("High Blood Pressure",
            "Your reading is above the recommended range and should be checked by a professional.",
            "high", true,
            List.of("Reduce salt and alcohol",
                    "Take any prescribed medication as directed",
                    "Check your blood pressure at home and keep a record"));

    private final String displayName;
    private final String description;
    private final String cssClass;
    private final boolean consultDoctor;
    private final List<String> tips;
}
