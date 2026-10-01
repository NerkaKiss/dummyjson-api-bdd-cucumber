# DummyJSON API - Java Cucumber BDD Automation Test Suite

BDD API regression test automation for [DummyJSON](https://dummyjson.com). Built as a QA Automation portfolio project with Java, Cucumber, Gherkin, REST Assured, TestNG, Maven, and GitHub Actions.

![Java](https://img.shields.io/badge/Java-21+-007396?logo=openjdk&logoColor=white)
![Cucumber](https://img.shields.io/badge/Cucumber-BDD-23d96c?logo=cucumber&logoColor=white)
![REST Assured](https://img.shields.io/badge/REST_Assured-API_Automation-4caf50)
![TestNG](https://img.shields.io/badge/TestNG-Test_Framework-f2c811)
![Maven](https://img.shields.io/badge/Maven-Build_Tool-c71a36?logo=apachemaven&logoColor=white)
[![API regression tests](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml/badge.svg)](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml)
[![Cucumber Report](https://img.shields.io/badge/Cucumber-View_Report-23d96c?logo=cucumber&logoColor=white)](https://nerkakiss.github.io/dummyjson-api-bdd-cucumber/)

> DummyJSON is a public demo API. Cart creation, updates, and deletion are simulated and do not persist server-side changes. The suite validates returned responses and depends on the availability of the external API.

---

## About This Project

This project reimplements the regression coverage of the [classic REST Assured/TestNG project](https://github.com/NerkaKiss/qa-dummyjson-restassured) using Cucumber and BDD principles.

The two repositories demonstrate different approaches to the same API coverage: Java test methods in the original project and domain-readable Gherkin scenarios in this project.

It covers:

- Valid and invalid authentication
- Current-user retrieval with a valid access token
- Product listing and product details
- Missing product validation
- Product search with matching and empty results
- Cart listing, retrieval, creation, updates, and deletion
- Invalid cart and product IDs
- A logged-in user creating a cart with an available product

Key design choices include:

- Gherkin scenarios expressed in domain language
- Step definitions organized by authentication, products, carts, and cart flow
- A single reusable HTTP helper rather than a large API framework
- Shared scenario state through `TestContext`
- Cucumber hooks for scenario setup
- Typed request bodies using Java records
- JSONPath response validation
- Fail-fast precondition checks and step-local soft assertions
- Smoke, regression, positive, negative, and domain tags
- Cucumber HTML reporting and CI report artifacts

---

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| BDD framework | Cucumber 7.20.1 |
| Scenario language | Gherkin |
| API automation | REST Assured 6.0.1 |
| Test framework | TestNG 7.12.0 |
| Request serialization | Jackson Databind 2.22.1 |
| Build tool | Maven |
| Test execution | Maven Surefire 3.5.2 |
| Reporting | Cucumber HTML + TestNG/Surefire reports |
| CI/CD | GitHub Actions |

---

## Testing Strategy

- **Smoke suite** - four core checks: valid login, product listing, cart listing, and the authenticated cart creation flow.
- **Regression suite** - all 17 scenarios, including the smoke scenarios.
- **Positive and negative coverage** - tags preserve the original TestNG project's grouping.
- **Independent scenarios** - scenarios prepare the data they need instead of depending on previous test execution.
- **Dynamic selection** - valid product and cart scenarios retrieve existing IDs from the API.
- **Scenario state** - login tokens, selected IDs, quantities, and the latest response are shared between step classes within one scenario.
- **Precondition validation** - setup steps fail immediately when required authentication or selected data is unavailable.
- **Response validation** - multi-field `Then` methods collect assertion failures using `SoftAssert` and call `assertAll()` before the step finishes.
- **Simulated mutations** - cart tests assert the response to each operation rather than expecting persisted changes.
- **Sequential execution** - parallel scenario execution and automatic retries are not configured.

### Example BDD Scenario

```gherkin
Scenario: Logged-in user creates a cart with an available product
  Given I am logged in
  And an available product is selected
  When I create a cart with the selected product
  Then the response status code should be 201
  And the cart should belong to the current user
  And the cart should contain the selected product
```

The feature describes the behavior being tested. Endpoints, HTTP request construction, serialization, and JSONPath expressions stay in Java code.

---

## Project Structure

```text
.
├── .github/workflows/
│   └── api-tests.yml                   # Regression CI, artifacts, and Pages deployment
├── src/test/java/
│   ├── api/
│   │   └── ApiClient.java              # GET, POST, PUT, DELETE, query parameters, bearer token
│   ├── context/
│   │   └── TestContext.java            # Shared scenario response, token, IDs, and quantity
│   ├── hooks/
│   │   └── Hooks.java                  # Context reset and REST Assured setup
│   ├── models/request/
│   │   ├── LoginRequest.java
│   │   ├── CartProductRequest.java
│   │   ├── CartRequest.java
│   │   └── CartUpdateRequest.java
│   ├── runners/
│   │   └── TestRunner.java             # Cucumber/TestNG integration and reporting
│   ├── steps/
│   │   ├── AuthSteps.java
│   │   ├── ProductSteps.java
│   │   ├── CartSteps.java
│   │   ├── CartFlowSteps.java
│   │   └── CommonSteps.java            # Shared status and error-message checks
│   └── utils/
│       └── ConfigReader.java           # Classpath properties reader
├── src/test/resources/
│   ├── config.properties              # API settings and public demo credentials
│   └── features/
│       ├── auth.feature
│       ├── products.feature
│       ├── carts.feature
│       └── cart_flow.feature
├── pom.xml
└── README.md
```

---

## Architecture

### Gherkin + Domain Steps + API Client + Scenario Context

Feature files describe API behavior. Step definitions implement the actions and assertions, while `ApiClient` centralizes HTTP execution through REST Assured.

```text
Maven Surefire
  └── TestRunner (Cucumber + TestNG)
        ├── feature files
        ├── Hooks
        │     ├── TestContext reset
        │     └── REST Assured configuration
        └── step definitions
              ├── TestContext
              ├── request records
              └── ApiClient
                    └── REST Assured → DummyJSON
```

### Scenario Lifecycle

```text
Before each scenario
  → reset TestContext
  → configure base URL and REST Assured validation logging
  → Given: prepare required state
  → When: perform the API action
  → Then: validate the latest response
  → write execution results to the reports
```

`TestRunner` uses `glue = {"steps", "hooks"}` so Cucumber discovers both the step definitions and the setup hook.

### Shared Scenario State

`TestContext` stores the latest `Response`, access token, user ID, product ID, cart ID, and original product quantity. This lets an authentication step, product-selection step, and cart step share data without combining all definitions into one class.

The context uses `ThreadLocal` storage and is reset before each scenario. Each new API response replaces `context.response`; values needed later, such as the selected product ID, are stored separately.

### Request Models and Assertions

- Request models are Java records serialized by Jackson.
- Response checks use REST Assured JSONPath without a response DTO hierarchy.
- Setup checks use hard assertions so invalid preconditions stop the scenario.
- Multi-field response checks use a new `SoftAssert` per step.
- `assertAll()` reports collected failures at the end of that step; subsequent Cucumber steps are skipped if it fails.
- `CommonSteps` contains shared status-code and error-message checks.

---

## Test Coverage

| Feature | File | Tags | Coverage |
| --- | --- | --- | --- |
| Authentication | `auth.feature` | `auth`, `positive`, `negative` | Valid login, invalid credentials, current user with a valid token |
| Products | `products.feature` | `products`, `positive`, `negative` | Product list, selected product details, missing product |
| Search | `products.feature` | `search`, `positive`, `negative` | Matching products and empty results for a non-existing query |
| Carts | `carts.feature` | `cart`, `positive`, `negative` | List, valid/invalid retrieval, valid/invalid-product creation, quantity update, valid/invalid deletion |
| Cart flow | `cart_flow.feature` | `cart`, `flow`, `positive` | Login, product selection, cart creation, ownership and selected-product checks |

### Current Suite Size

| Suite | Scenarios |
| --- | ---: |
| Full / Regression | 17 |
| Smoke | 4 |
| Positive | 11 |
| Negative | 6 |

---

## Test Tags

| Tag | Scenarios | Purpose |
| --- | ---: | --- |
| `@regression` | 17 | Complete regression coverage, including smoke |
| `@smoke` | 4 | Core happy-path checks |
| `@positive` | 11 | Valid input and successful operations |
| `@negative` | 6 | Invalid input, missing resources, and non-existing search queries |
| `@auth` | 3 | Authentication |
| `@products` | 3 | Product listing and lookup |
| `@search` | 2 | Product search |
| `@cart` | 9 | Eight cart scenarios plus the cart flow |
| `@flow` | 1 | Cross-feature cart creation flow |

Feature-level tags are inherited by all scenarios in that feature. Domain tags and positive/negative tags match the original project's TestNG groups.

A negative scenario can expect HTTP 200 or 201. For example, a non-existing search query returns an empty list, and creating a cart with an invalid product returns an empty cart. The tag describes the tested input or behavior, not only the HTTP status.

---

## Running Locally

### Prerequisites

- Java 21+
- Maven available on `PATH`
- Internet access to DummyJSON and Maven dependencies

### Quick Start

```bash
# Clone the repository
git clone https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber.git
cd dummyjson-api-bdd-cucumber

# Run the full suite
mvn clean test
```

### Configuration

Default settings are stored in `src/test/resources/config.properties`:

```properties
base.url=https://dummyjson.com
product.search.query=kitchen
userId=1
# Public DummyJSON demo credentials
username=emilys
password=emilyspass
```

The authentication credentials are **public demo credentials provided in the [DummyJSON documentation](https://dummyjson.com/docs/auth)**. They are intentionally included so the project runs without a private account, `.env` file, or GitHub Secrets configuration.

`ConfigReader` loads these settings from the classpath. Environment-variable overrides are not implemented.

### Run Test Tags

Quoted Maven property arguments work in both shell and PowerShell:

```bash
# Full regression suite
mvn clean test "-Dcucumber.filter.tags=@regression"

# Smoke suite
mvn clean test "-Dcucumber.filter.tags=@smoke"

# Positive scenarios
mvn clean test "-Dcucumber.filter.tags=@positive"

# Negative scenarios
mvn clean test "-Dcucumber.filter.tags=@negative"

# Authentication scenarios
mvn clean test "-Dcucumber.filter.tags=@auth"

# Positive cart scenarios, including the cart flow
mvn clean test "-Dcucumber.filter.tags=@cart and @positive"

# Cart scenarios without the flow
mvn clean test "-Dcucumber.filter.tags=@cart and not @flow"
```

Filtering uses Cucumber tag expressions rather than TestNG's `-Dgroups` option. Maven Surefire discovers `TestRunner`, which runs the matching scenarios.

---

## CI/CD

GitHub Actions workflow:

```text
.github/workflows/api-tests.yml
```

### Triggers

| Trigger | Suite | Behavior |
| --- | --- | --- |
| Push to `master` | Regression | Runs all 17 scenarios |
| Pull request targeting `master` | Regression | Validates the proposed changes |
| Manual dispatch | Regression | Runs the same regression suite on demand |

### CI Environment

The workflow:

- Checks out the repository.
- Sets up Temurin Java 21 on an Ubuntu runner.
- Caches Maven dependencies.
- Runs `mvn -B clean test "-Dcucumber.filter.tags=@regression"`.
- Uploads available reports after execution, including failed test runs.
- Publishes generated regression reports from `master` to GitHub Pages.

The job has a 10-minute timeout and uses the public demo configuration committed to the repository.

### CI Artifacts

The `api-test-reports` artifact contains:

```text
target/cucumber-report.html
target/surefire-reports/
```

Open a run in [GitHub Actions](https://github.com/NerkaKiss/dummyjson-api-bdd-cucumber/actions/workflows/api-tests.yml), download the artifact, extract it, and open the HTML report in a browser.

### GitHub Pages Setup

In the repository settings, select:

```text
Settings → Pages → Build and deployment → Source: GitHub Actions
```

The deployment job uses the `github-pages` environment with `pages: write` and `id-token: write` permissions. Pages deployments are serialized so concurrent jobs do not deploy at the same time.

---

## Cucumber Reporting

### Public Report

**View the latest published regression report:**

https://nerkakiss.github.io/dummyjson-api-bdd-cucumber/

Publishing follows these rules:

- Pushes to `master` and manual runs from `master` publish a generated report.
- Successful and failed regression runs can both publish their reports.
- Pull request runs upload diagnostic artifacts without replacing the public report.
- If no non-empty HTML report is generated, the previous public report is preserved.
- Cancelled runs do not deploy a report.
- Failed tests remain failed in GitHub Actions even when report deployment succeeds.

The HTML report is copied to `index.html` in a separate Pages artifact. Surefire reports remain downloadable in `api-test-reports`; they are not included in the public site.

### Local HTML Report

The runner generates the report automatically during test execution:

```text
target/cucumber-report.html
```

The report shows feature/scenario results, executed steps, and failure details. Open it directly in a browser after running the tests.

Each run replaces the report with results for the selected scenarios. A smoke run therefore produces a four-scenario report, while a regression run produces a 17-scenario report.

### TestNG / Surefire Reports

Additional execution results are written to:

```text
target/surefire-reports/
```

Reports are available locally and as GitHub Actions artifacts. Generated files under `target/` are excluded from version control.

---

## Failure Diagnostics

On a failed scenario, the project provides:

- Failed-step details in Cucumber console output and the HTML report
- Assertion messages and stack traces in TestNG/Surefire reports
- Multiple assertion failures from a response-validation step through `SoftAssert.assertAll()`
- REST Assured request/response logging when a REST Assured response validation fails, such as a status-code mismatch

REST Assured validation logging does not automatically cover separate TestNG assertions against JSONPath values. A failed soft assertion is reported through Cucumber and TestNG, but does not automatically attach the full response body.

Automatic retries are not configured, so the original scenario failure remains visible.

---

## Known Limitations

- Execution depends on the availability and current demo data of the public DummyJSON API.
- Cart creation, updates, and deletion are simulated; persistence is not tested.
- The flow checks login, product selection, and cart ownership, but does not prove that cart creation requires bearer-token authorization.
- Request models provide typed request bodies; response checks still rely on JSONPath field names and types.
- Parallel execution is not enabled by default. The thread-local context separates stored state, but REST Assured configuration is shared statically.
- The public report reflects the latest deployed run from `master`; reports for individual runs remain available as CI artifacts.

---

## Future Improvements

- Attach request/response evidence for failed JSONPath assertions.
- Add manual CI selection for smoke and regression suites.
- Extend focused API edge-case coverage where additional behavior is documented by DummyJSON.
