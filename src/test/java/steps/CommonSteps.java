package steps;

import context.TestContext;
import io.cucumber.java.en.Then;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CommonSteps {

    private final TestContext context = TestContext.current();

    @Then("the response status code should be {int}")
    public void verifyStatusCode(int statusCode) {
        context.response.then()
                .statusCode(statusCode);
    }

    @Then("the error message should contain {string}")
    public void verifyErrorMessageContains(String text) {
        String message = context.response.jsonPath().getString("message");
        assertNotNull(message, "Error message should not be null");
        assertTrue(message.toLowerCase().contains(text.toLowerCase()), "Error message should contain expected text");
    }
}
