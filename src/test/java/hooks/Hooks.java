package hooks;

import context.TestContext;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import utils.ConfigReader;

public class Hooks {

    @Before
    public void setUp() {
        TestContext.reset();
        RestAssured.baseURI = ConfigReader.get("base.url");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
