package com.example.bpcalculator.bdd;

import com.example.bpcalculator.model.BloodPressure;
import com.example.bpcalculator.model.BpCategory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class BloodPressureSteps {

    private BloodPressure bloodPressure;
    private BpCategory category;

    @Given("a systolic reading of {int} and a diastolic reading of {int}")
    public void aReading(int systolic, int diastolic) {
        bloodPressure = new BloodPressure();
        bloodPressure.setSystolic(systolic);
        bloodPressure.setDiastolic(diastolic);
    }

    @When("I calculate my blood pressure category")
    public void iCalculateMyCategory() {
        category = bloodPressure.getCategory();
    }

    @Then("the category is {word}")
    public void theCategoryIs(String expected) {
        assertThat(category).isEqualTo(BpCategory.valueOf(expected));
    }
}
