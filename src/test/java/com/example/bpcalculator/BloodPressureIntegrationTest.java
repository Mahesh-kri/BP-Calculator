package com.example.bpcalculator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class BloodPressureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexRendersCalculatorForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("bp"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Check your blood pressure category")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"systolic\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"diastolic\"")));
    }

    @Test
    void postValidReadingRendersResultWithCategoryAndDoctorCta() throws Exception {
        mockMvc.perform(post("/").param("systolic", "145").param("diastolic", "95"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("hasErrors", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("High Blood Pressure")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Speak to a doctor")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("What can help")));
    }

    @Test
    void postIdealReadingRendersResultWithoutDoctorCta() throws Exception {
        mockMvc.perform(post("/").param("systolic", "110").param("diastolic", "70"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("hasErrors", false))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ideal Blood Pressure")));
    }

    @Test
    void postInvalidReadingReRendersFormWithErrors() throws Exception {
        mockMvc.perform(post("/").param("systolic", "200").param("diastolic", "30"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("hasErrors", true))
                .andExpect(model().attributeHasFieldErrors("bp", "systolic", "diastolic"));
    }

    @Test
    void unknownPathIsNotHandled() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
