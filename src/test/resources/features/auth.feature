@regression @auth
Feature: Authentication API

  @smoke @positive
  Scenario: Login with valid credentials
    When I login with valid credentials
    Then the response status code should be 200
    And the login response should contain user data and tokens

  @negative
  Scenario: Login with invalid credentials
    When I login with invalid credentials
    Then the response status code should be 400
    And the error message should contain "Invalid credentials"

  @positive
  Scenario: Get current user with valid token
    Given I am logged in
    When I request current user
    Then the response status code should be 200
    And the current user response should contain configured user
