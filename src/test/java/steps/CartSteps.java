package steps;

import api.ApiClient;
import context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import models.request.CartProductRequest;
import models.request.CartRequest;
import models.request.CartUpdateRequest;
import org.testng.asserts.SoftAssert;
import utils.ConfigReader;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CartSteps {

    private final ApiClient apiClient = new ApiClient();
    private final TestContext context = TestContext.current();

    @Given("I store the first cart id")
    public void storeFirstCartId() {
        context.response = apiClient.get("/carts");

        assertEquals(context.response.statusCode(), 200);
        context.cartId = context.response.jsonPath().getInt("carts[0].id");
        assertTrue(context.cartId > 0, "Cart ID should be greater than 0");
    }

    @Given("I store the first cart product")
    public void storeFirstCartProduct() {
        context.response = apiClient.get("/carts");

        assertEquals(context.response.statusCode(), 200);
        context.cartId = context.response.jsonPath().getInt("carts[0].id");
        context.productId = context.response.jsonPath().getInt("carts[0].products[0].id");
        context.originalQuantity = context.response.jsonPath().getInt("carts[0].products[0].quantity");
        assertTrue(context.cartId > 0, "Cart ID should be greater than 0");
        assertTrue(context.productId > 0, "Product ID should be greater than 0");
    }

    @When("I request all carts")
    public void requestAllCarts() {
        context.response = apiClient.get("/carts");
    }

    @When("I request cart with stored id")
    public void requestCartWithStoredId() {
        context.response = apiClient.get("/carts/" + context.cartId);
    }

    @When("I request cart with id {int}")
    public void requestCartWithId(int id) {
        context.response = apiClient.get("/carts/" + id);
    }

    @When("I add a cart with stored product quantity {int}")
    public void addCartWithStoredProduct(int quantity) {
        context.response = apiClient.post("/carts/add", cartRequest(
                Integer.parseInt(ConfigReader.get("userId")),
                context.productId,
                quantity
        ));
    }

    @When("I add a cart with invalid product id {int}")
    public void addCartWithInvalidProduct(int invalidProductId) {
        context.response = apiClient.post("/carts/add", cartRequest(
                Integer.parseInt(ConfigReader.get("userId")),
                invalidProductId,
                1
        ));
    }

    @When("I update the stored cart product quantity by one")
    public void updateStoredCartProductQuantity() {
        context.response = apiClient.put("/carts/" + context.cartId, new CartUpdateRequest(List.of(
                new CartProductRequest(context.productId, context.originalQuantity + 1)
        )));
    }

    @When("I delete cart with stored id")
    public void deleteStoredCart() {
        context.response = apiClient.delete("/carts/" + context.cartId);
    }

    @When("I delete cart with id {int}")
    public void deleteCartWithId(int id) {
        context.response = apiClient.delete("/carts/" + id);
    }

    private CartRequest cartRequest(int userId, int productId, int quantity) {
        return new CartRequest(userId, List.of(new CartProductRequest(productId, quantity)));
    }

    @Then("the carts list should contain carts")
    public void verifyCartsList() {
        List<?> carts = context.response.jsonPath().getList("carts");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(carts, "Cart list should not be null");
        softAssert.assertFalse(carts.isEmpty(), "Cart list should not be empty");
        softAssert.assertTrue(context.response.jsonPath().getInt("total") > 0, "Total carts should be greater than 0");
        softAssert.assertTrue(context.response.jsonPath().getInt("limit") > 0, "Limit should be greater than 0");
        softAssert.assertTrue(context.response.jsonPath().getInt("carts[0].id") > 0, "Cart ID should be greater than 0");
        softAssert.assertTrue(context.response.jsonPath().getInt("carts[0].userId") > 0, "User ID should be greater than 0");
        softAssert.assertNotNull(context.response.jsonPath().getList("carts[0].products"), "Products list should not be null");
        softAssert.assertEquals(context.response.jsonPath().getInt("skip"), 0, "Default skip should be 0");
        softAssert.assertAll();
    }

    @Then("the cart response should contain stored cart data")
    public void verifyStoredCartData() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(context.response.jsonPath().getInt("id"), context.cartId, "Cart ID should match the requested ID");
        softAssert.assertTrue(context.response.jsonPath().getInt("userId") > 0, "User ID should be greater than 0");
        softAssert.assertNotNull(context.response.jsonPath().getList("products"), "Products list should not be null");
        softAssert.assertAll();
    }

    @Then("the created cart should contain stored product quantity {int}")
    public void verifyCreatedCart(int quantity) {
        List<?> products = context.response.jsonPath().getList("products");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(products, "Products list should not be null");
        softAssert.assertFalse(products.isEmpty(), "Products list should not be empty");
        softAssert.assertEquals(context.response.jsonPath().getInt("userId"), Integer.parseInt(ConfigReader.get("userId")));
        softAssert.assertEquals(context.response.jsonPath().getInt("products[0].id"), context.productId);
        softAssert.assertEquals(context.response.jsonPath().getInt("products[0].quantity"), quantity);
        softAssert.assertTrue(context.response.jsonPath().getInt("totalProducts") > 0, "Total products should be greater than 0");
        softAssert.assertTrue(context.response.jsonPath().getInt("totalQuantity") > 0, "Total quantity should be greater than 0");
        softAssert.assertAll();
    }

    @Then("the created cart should be empty")
    public void verifyCreatedCartIsEmpty() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(context.response.jsonPath().getList("products").isEmpty(), "Products list should be empty");
        softAssert.assertEquals(context.response.jsonPath().getInt("totalProducts"), 0, "Total products should be 0");
        softAssert.assertEquals(context.response.jsonPath().getInt("totalQuantity"), 0, "Total quantity should be 0");
        softAssert.assertAll();
    }

    @Then("the updated cart should contain increased product quantity")
    public void verifyUpdatedCart() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(context.response.jsonPath().getInt("id"), context.cartId, "Cart ID should match the requested ID");
        softAssert.assertEquals(context.response.jsonPath().getInt("products[0].id"), context.productId);
        softAssert.assertEquals(context.response.jsonPath().getInt("products[0].quantity"), context.originalQuantity + 1);
        softAssert.assertTrue(context.response.jsonPath().getInt("totalQuantity") > 0, "Total quantity should be greater than 0");
        softAssert.assertAll();
    }

    @Then("the deleted cart response should confirm deletion")
    public void verifyDeletedCart() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(context.response.jsonPath().getInt("id"), context.cartId, "Deleted cart ID should match the requested ID");
        softAssert.assertTrue(context.response.jsonPath().getBoolean("isDeleted"), "Response should indicate that the cart was deleted");
        softAssert.assertAll();
    }
}
