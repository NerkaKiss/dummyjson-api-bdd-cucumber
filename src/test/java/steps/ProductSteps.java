package steps;

import api.ApiClient;
import context.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.asserts.SoftAssert;
import utils.ConfigReader;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ProductSteps {

    private final ApiClient apiClient = new ApiClient();
    private final TestContext context = TestContext.current();

    @Given("I store the first product id")
    public void storeFirstProductId() {
        context.response = apiClient.get("/products");

        assertEquals(context.response.statusCode(), 200);
        context.productId = context.response.jsonPath().getInt("products[0].id");
        assertTrue(context.productId > 0, "Product ID should be greater than 0");
    }

    @Given("an available product is selected")
    public void selectAvailableProduct() {
        storeFirstProductId();
    }

    @When("I request product with id {int}")
    public void requestProduct(int id) {
        context.response = apiClient.get("/products/" + id);
    }

    @When("I request product with stored id")
    public void requestProductWithStoredId() {
        requestProduct(context.productId);
    }

    @When("I request all products")
    public void requestAllProducts() {
        context.response = apiClient.get("/products");
    }

    @When("I search products with configured query")
    public void searchProductsWithConfiguredQuery() {
        context.response = apiClient.get("/products/search", Map.of("q", ConfigReader.get("product.search.query")));
    }

    @When("I search products with query {string}")
    public void searchProductsWithQuery(String query) {
        context.response = apiClient.get("/products/search", Map.of("q", query));
    }

    @Then("the product id should be {int}")
    public void verifyProductId(int id) {
        assertEquals(context.response.jsonPath().getInt("id"), id);
    }

    @Then("the product response should contain requested product details")
    public void verifyProductDetails() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(context.response.jsonPath().getInt("id"), context.productId);
        softAssert.assertNotNull(context.response.jsonPath().getString("title"), "Product title should not be null");
        softAssert.assertTrue(context.response.jsonPath().getDouble("price") > 0, "Product price should be greater than 0");
        softAssert.assertAll();
    }

    @Then("the products list should contain products")
    public void verifyProductsList() {
        List<?> products = context.response.jsonPath().getList("products");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(products, "Product list should not be null");
        softAssert.assertFalse(products.isEmpty(), "Product list should not be empty");
        softAssert.assertTrue(context.response.jsonPath().getInt("total") > 0, "Total products should be greater than 0");
        softAssert.assertTrue(context.response.jsonPath().getInt("limit") > 0, "Limit should be greater than 0");
        softAssert.assertEquals(context.response.jsonPath().getInt("skip"), 0, "Default skip should be 0");
        softAssert.assertAll();
    }

    @Then("at least one product title should contain configured query")
    public void verifyProductSearchResults() {
        List<String> titles = context.response.jsonPath().getList("products.title");
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(titles, "Search results should not be null");
        softAssert.assertFalse(titles.isEmpty(), "Search results should not be empty");

        String query = ConfigReader.get("product.search.query").toLowerCase();
        boolean hasMatch = titles.stream().anyMatch(title -> title.toLowerCase().contains(query));
        softAssert.assertTrue(hasMatch, "At least one product title should contain the search query");
        softAssert.assertAll();
    }

    @Then("the product search results should be empty")
    public void verifyEmptyProductSearchResults() {
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertTrue(context.response.jsonPath().getList("products").isEmpty(), "Search results should be empty");
        softAssert.assertEquals(context.response.jsonPath().getInt("total"), 0, "Total should be 0");
        softAssert.assertAll();
    }
}
