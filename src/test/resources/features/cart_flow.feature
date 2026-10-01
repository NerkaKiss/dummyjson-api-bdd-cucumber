Feature: Cart Flow API

  Scenario: Logged-in user creates a cart with an available product
    Given I am logged in
    And an available product is selected
    When I create a cart with the selected product
    Then the response status code should be 201
    And the cart should belong to the current user
    And the cart should contain the selected product
