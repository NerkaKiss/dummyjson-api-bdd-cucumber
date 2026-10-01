Feature: Products API

  Scenario: Get all products
    When I request all products
    Then the response status code should be 200
    And the products list should contain products

  Scenario: Get product by valid ID
    Given I store the first product id
    When I request product with id 1
    Then the response status code should be 200
    And the product id should be 1

  Scenario: Get stored product by valid ID
    Given I store the first product id
    When I request product with stored id
    Then the response status code should be 200
    And the product response should contain requested product details

  Scenario: Get product by invalid ID
    When I request product with id 999999
    Then the response status code should be 404
    And the error message should contain "not found"

  Scenario: Search products with existing query
    When I search products with configured query
    Then the response status code should be 200
    And at least one product title should contain configured query

  Scenario: Search products with non-existing query
    When I search products with query "nonExistingProductQWERTY"
    Then the response status code should be 200
    And the product search results should be empty
