Feature: Blood pressure category calculation
  As a patient
  I want to know my blood pressure category
  So that I understand what my reading means

  The lower limit of each range is inclusive, so a reading of
  140/90 is classified as high blood pressure.

  Scenario Outline: A reading is categorised using inclusive lower limits
    Given a systolic reading of <systolic> and a diastolic reading of <diastolic>
    When I calculate my blood pressure category
    Then the category is <category>

    Examples:
      | systolic | diastolic | category |
      | 85       | 70        | LOW      |
      | 100      | 55        | LOW      |
      | 90       | 60        | IDEAL    |
      | 120      | 80        | IDEAL    |
      | 121      | 80        | PRE_HIGH |
      | 120      | 81        | PRE_HIGH |
      | 139      | 89        | PRE_HIGH |
      | 140      | 90        | HIGH     |
      | 140      | 80        | HIGH     |
      | 120      | 90        | HIGH     |
      | 180      | 100       | HIGH     |
