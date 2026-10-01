# DummyJSON API BDD Regression Tests

[![API regression tests](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml/badge.svg)](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml)

A BDD API regression automation project using **Cucumber, Gherkin, REST Assured and TestNG**, targeting the [DummyJSON API](https://dummyjson.com/docs).

This project reimplements the regression coverage of the [REST Assured/TestNG project](https://github.com/NerkaKiss/qa-dummyjson-restassured) using BDD principles. The two repositories demonstrate different ways to express the same API regression coverage: classic Java tests and readable Gherkin scenarios.

## What this project demonstrates

- Domain-oriented Gherkin scenarios backed by reusable Java step definitions.
- Positive and negative API checks for authentication, products, search and carts.
- A cross-feature flow: log in, select an available product and create a cart.
- Shared scenario data, Cucumber hooks and typed request bodies using Java records.
- Fail-fast setup checks and TestNG soft assertions for multi-field response validation.
- Smoke/regression filtering, HTML reporting and automated CI execution.

## Technology stack

Java 21 · Maven · Cucumber · REST Assured · TestNG · Jackson · GitHub Actions

## Test coverage

| Area | Scenarios | Checks |
| --- | ---: | --- |
| Authentication | 3 | Valid/invalid login, current user with a valid token |
| Products and search | 5 | Product list, existing/missing product, matching/empty search |
| Carts | 8 | Cart list, existing/missing cart, creation, invalid product, update, deletion |
| Cart flow | 1 | Logged-in user creates a cart containing a selected product |
| **Total** | **17** | **All scenarios are tagged `@regression`** |

The four `@smoke` scenarios cover valid login, product listing, cart listing and the cart creation flow.

## Example BDD scenario

```gherkin
Scenario: Logged-in user creates a cart with an available product
  Given I am logged in
  And an available product is selected
  When I create a cart with the selected product
  Then the response status code should be 201
  And the cart should belong to the current user
  And the cart should contain the selected product
```

HTTP calls and JSON paths stay in Java code, allowing the feature file to describe behavior rather than request implementation details.

## Architecture

```text
Features → Steps → ApiClient → REST Assured → DummyJSON
              ↕
          TestContext

Hooks · ConfigReader · Request models · Runner
```

```text
src/test/java/
  api/                 Shared HTTP request helper
  context/             Response, token and selected IDs shared between steps
  hooks/               Setup before each scenario
  models/request/      Login and cart request records
  runners/             Cucumber/TestNG runner
  steps/               Auth, product, cart, flow and common step definitions
  utils/               Properties configuration reader
src/test/resources/
  config.properties    API URL, search query, user ID and demo credentials
  features/            Gherkin specifications
```

`Hooks` resets the thread-local context before each scenario and configures REST Assured. `TestContext` shares data between step classes during that scenario. Request records are serialized to JSON by Jackson; response checks use REST Assured JSONPath. Each multi-field validation creates its own `SoftAssert` and calls `assertAll()` at the end of the step.

## Run locally

### Requirements

- JDK 21 or newer (CI uses Java 21).
- Maven installed and available on `PATH`.
- Internet access to `https://dummyjson.com` and Maven dependencies.

Clone the repository and run the commands from its root:

```shell
git clone https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber.git
cd dummyjson-api-bdd-cucumber
mvn clean test
```

Run only regression or smoke scenarios (quoted arguments also work in PowerShell):

```shell
mvn test "-Dcucumber.filter.tags=@regression"
mvn test "-Dcucumber.filter.tags=@smoke"
```

Expected scenario counts: **17 regression**, **4 smoke**.

### Scenario tags

Tags preserve the grouping used in the original TestNG project:

| Tag | Scenarios | Purpose |
| --- | ---: | --- |
| `@regression` | 17 | Complete regression coverage |
| `@smoke` | 4 | Core functionality checks |
| `@positive` | 11 | Valid input and successful operations |
| `@negative` | 6 | Invalid input, missing resources and empty search results |
| `@auth` | 3 | Authentication |
| `@products` | 3 | Product listing and lookup |
| `@search` | 2 | Product search |
| `@cart` | 9 | Eight cart scenarios plus the cart flow |
| `@flow` | 1 | Cross-feature cart creation flow |

Feature-level tags are inherited by all scenarios in that feature. Product search scenarios use `@search`, while listing/lookup scenarios use `@products`, matching the original grouping. Negative scenarios can still expect HTTP 200 or 201: the tag describes the input or behavior under test, not just the status code.

```shell
mvn test "-Dcucumber.filter.tags=@positive"
mvn test "-Dcucumber.filter.tags=@negative"
mvn test "-Dcucumber.filter.tags=@cart and @positive"
```

### Configuration

Settings are loaded from `src/test/resources/config.properties`:

```properties
base.url=https://dummyjson.com
product.search.query=kitchen
userId=1

# Public DummyJSON demo credentials
username=emilys
password=emilyspass
```

The authentication credentials used in this project are **public demo credentials provided in the [DummyJSON documentation](https://dummyjson.com/docs/auth)**. No local `.env` file or GitHub Secrets setup is required to run this demo project.

## Reports

After a test run:

- Open `target/cucumber-report.html` in a browser for the Cucumber scenario/step report.
- Inspect `target/surefire-reports/` for TestNG/Maven results.

Each run overwrites the HTML report with the results of the selected scenarios. Generated reports are excluded from Git via `target/`.

## Continuous integration

[GitHub Actions](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml) runs regression scenarios on pushes to `master`, pull requests targeting `master`, and manual runs.

The workflow uses Java 21, caches Maven dependencies and runs:

```shell
mvn -B clean test "-Dcucumber.filter.tags=@regression"
```

Reports are uploaded even when tests fail. Open a workflow run and download the **`api-test-reports`** artifact to view the HTML and Surefire reports.

## DummyJSON behavior

DummyJSON simulates cart creation, updates and deletion; these operations do not persist server-side changes. Tests verify the returned responses rather than assuming subsequent requests will see those mutations. Valid product/cart tests select existing data from the API instead of relying on fixed IDs.

Because tests call a public API, execution depends on its availability and current demo data.
