package org.example;
import com.sun.org.apache.xpath.internal.operations.Equals;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.responseSpecification;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

public class MyTest {
    static Properties prop = new Properties();
    static Map<String, String> headers = new HashMap<>();
    final static String baseUrl = "http://httpbin.org/";
static ResponseSpecification responseSpecification;


@BeforeAll
     static void init(){
        responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(MyProperties.code)
                .expectStatusLine(MyProperties.status)
                .expectResponseTime(Matchers.lessThan(25000L))
                .expectHeader("Access-Control-Allow-Credentials", "true")
                .build();

    }
    @BeforeAll
    static void setUp() throws IOException {
        RestAssured.filters(new AllureRestAssured());
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        headers.put("Authorization", "Basic bWFzaGE6MjEyMQ==");
        FileInputStream  fis = new FileInputStream("src/test/resources/properties");
        prop.load(fis);
    }

    @Test
    void getIp() {

        RestAssured.get(baseUrl +"ip")
                .then()
                .spec(responseSpecification)
                .contentType(MyProperties.json);
    }

    @Test
    void getUserAgent() {
        given()
                .when()
                .request("GET", baseUrl+"user-agent")
                .then()
                .spec(responseSpecification)
                .contentType(MyProperties.json);
    }

    @Test
    void getImagesWepb() {
        given()
                .log().all()
                .when()
                .request("GET", baseUrl+"image/webp")
                .then()
                .spec(responseSpecification)
                .contentType("image/webp");
    }

    @Test
    void getImagesPng() {
        given()
                .log().uri()
                .when()
                .request("GET", baseUrl+"image/png")
                .then()
                .spec(responseSpecification)
                .contentType("image/png");
    }


    @Test
    void postResponseHeader() {
        given()
                .when()
                .post(baseUrl+"response-headers?freeform={freeform}", "Hello World!")
                .prettyPeek()
                .then()
                .spec(responseSpecification)
                .contentType(MyProperties.json)
                .body("freeform", equalTo ("Hello World!"));
    }

    @Test
    void getResponseHeader() {
        String result = given()
                .when()
                .get(baseUrl+"response-headers?freeform={freeform}", (String)prop.get("freeform"))
                .then()
                .spec(responseSpecification)
                .contentType(MyProperties.json)
                .extract()
                .response()
                .jsonPath()
                .getString("freeform");
        assertThat(result, equalTo("My name is Mariya"));
    }

    @Test
    void getUtf() {
        RestAssured.get(baseUrl+"encoding/utf8")
                .then()
                .spec(responseSpecification)
                .contentType("text/html; charset=utf-8");
    }
    @Test
    void getDecoded() {
        given()
                .when()
                .get(baseUrl+"base64/{Text_decode}", (String)prop.get("Text_decode"))
                .prettyPeek()
                .then()
                .spec(responseSpecification)
                .contentType("text/html; charset=utf-8");
    }
    @Test
    void getAuthBad() {
        given()
                .headers(headers)
                .when()
                .get(baseUrl+"basic-auth/{login}/{password_bad}", (String)prop.get("login"), (String)prop.get("password_bad"))
                .then()
                .statusCode(401);
    }
    @Test
    void getAuthGood() {
       UserDTO user = given()
                .headers(headers)
                .when()
                .get(baseUrl+"basic-auth/{login}/{password_good}", (String)prop.get("login"), (String)prop.get("password_good"))
                .prettyPeek()
                .then()
                .spec(responseSpecification)
                .contentType(MyProperties.json)
                .extract()
                .body()
                .as(UserDTO.class);
       assertThat(user.getUser(),equalTo("masha"));
       assertThat(user.getAuthenticated(), equalTo("true"));




    }





}








