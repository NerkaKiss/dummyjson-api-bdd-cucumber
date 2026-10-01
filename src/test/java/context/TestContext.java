package context;

import io.restassured.response.Response;

public class TestContext {

    private static final ThreadLocal<TestContext> CONTEXT = ThreadLocal.withInitial(TestContext::new);

    public Response response;
    public String accessToken;
    public int userId;
    public int productId;
    public int cartId;
    public int originalQuantity;

    public static TestContext current() {
        return CONTEXT.get();
    }

    public static void reset() {
        CONTEXT.remove();
    }
}
