Feature: Carts API

  Scenario: Get all carts
    When I request all carts
    Then the response status code should be 200
    And the carts list should contain carts

  Scenario: Get cart by valid ID
    Given I store the first cart id
    When I request cart with stored id
    Then the response status code should be 200
    And the cart response should contain stored cart data

  Scenario: Get cart by invalid ID
    When I request cart with id 99999
    Then the response status code should be 404
    And the error message should contain "not found"

  Scenario: Add cart with valid product
    Given I store the first product id
    When I add a cart with stored product quantity 2
    Then the response status code should be 201
    And the created cart should contain stored product quantity 2

  Scenario: Add cart with invalid product
    When I add a cart with invalid product id 999999
    Then the response status code should be 201
    And the created cart should be empty

  Scenario: Update cart by ID
    Given I store the first cart product
    When I update the stored cart product quantity by one
    Then the response status code should be 200
    And the updated cart should contain increased product quantity

  Scenario: Delete cart by valid ID
    Given I store the first cart id
    When I delete cart with stored id
    Then the response status code should be 200
    And the deleted cart response should confirm deletion

  Scenario: Delete cart by invalid ID
    When I delete cart with id 9999
    Then the response status code should be 404
    And the error message should contain "not found"
