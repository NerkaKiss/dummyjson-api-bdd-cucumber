package steps;

import api.ApiClient;
import context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import models.request.LoginRequest;
import org.testng.asserts.SoftAssert;
import utils.ConfigReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AuthSteps {

    private final ApiClient apiClient = new ApiClient();
    private final TestContext context = TestContext.current();

    @Given("I am logged in")
    public void login() {
        context.response = apiClient.post("/auth/login", new LoginRequest(
                ConfigReader.get("username"),
                ConfigReader.get("password")
        ));

        assertEquals(context.response.statusCode(), 200);
        context.accessToken = context.response.jsonPath().getString("accessToken");
        context.userId = context.response.jsonPath().getInt("id");
        assertNotNull(context.accessToken, "Access token should not be null");
        assertTrue(context.userId > 0, "User ID should be greater than 0");
    }

    @When("I login with valid credentials")
    public void loginWithValidCredentials() {
        login();
    }

    @When("I login with invalid credentials")
    public void loginWithInvalidCredentials() {
        context.response = apiClient.post("/auth/login", new LoginRequest("wrongUser", "wrongPassword"));
    }

    @When("I request current user")
    public void requestCurrentUser() {
        context.response = apiClient.getWithBearerToken("/auth/me", context.accessToken);
    }

    @Then("the login response should contain user data and tokens")
    public void verifyLoginResponse() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(context.response.jsonPath().getString("username"), "Username should not be null");
        softAssert.assertNotNull(context.response.jsonPath().getString("accessToken"), "Access token should not be null");
        softAssert.assertNotNull(context.response.jsonPath().getString("refreshToken"), "Refresh token should not be null");
        softAssert.assertEquals(context.response.jsonPath().getString("username"), ConfigReader.get("username"));
        softAssert.assertAll();
    }

    @Then("the current user response should contain configured user")
    public void verifyCurrentUser() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(context.response.jsonPath().getInt("id") > 0, "User ID should be greater than 0");
        softAssert.assertNotNull(context.response.jsonPath().getString("username"), "Username should not be null");
        softAssert.assertNotNull(context.response.jsonPath().getString("email"), "Email should not be null");
        softAssert.assertEquals(context.response.jsonPath().getString("username"), ConfigReader.get("username"));
        softAssert.assertAll();
    }
}
