package steps;

import api.ApiClient;
import context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import models.request.CartProductRequest;
import models.request.CartRequest;
import org.testng.asserts.SoftAssert;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CartFlowSteps {

    private final ApiClient apiClient = new ApiClient();
    private final TestContext context = TestContext.current();

    @When("I create a cart with the selected product")
    public void createCartWithSelectedProduct() {
        context.response = apiClient.post("/carts/add", new CartRequest(
                context.userId,
                List.of(new CartProductRequest(context.productId, 1))
        ));
    }

    @Then("the cart should belong to the current user")
    public void verifyCartBelongsToCurrentUser() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(context.response.jsonPath().getInt("id") > 0, "Cart ID should be greater than 0");
        softAssert.assertEquals(context.response.jsonPath().getInt("userId"), context.userId);
        softAssert.assertAll();
    }

    @Then("the cart should contain the selected product")
    public void verifyCartContainsSelectedProduct() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(context.response.jsonPath().getInt("products[0].id"), context.productId);
        softAssert.assertFalse(context.response.jsonPath().getList("products").isEmpty(), "Products list should not be empty");
        softAssert.assertAll();
    }
}
