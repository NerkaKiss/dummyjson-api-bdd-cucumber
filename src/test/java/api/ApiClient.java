package api;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiClient {

    public Response get(String path) {
        return given()
                .when()
                .get(path);
    }

    public Response get(String path, Map<String, ?> queryParams) {
        return given()
                .queryParams(queryParams)
                .when()
                .get(path);
    }

    public Response getWithBearerToken(String path, String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(path);
    }

    public Response post(String path, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }

    public Response put(String path, Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put(path);
    }

    public Response delete(String path) {
        return given()
                .when()
                .delete(path);
    }
}
