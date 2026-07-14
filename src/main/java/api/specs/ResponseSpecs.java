package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

import static org.hamcrest.Matchers.*;

public class ResponseSpecs {

    public static final String TRANSFER_SUCCESS_MESSAGE = "Transfer successful";
    public static final String TRANSFER_INVALID_MESSAGE = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String UPDATE_PROFILE_SUCCESS_MESSAGE = "Profile updated successfully";

    private ResponseSpecs() {}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification profileUpdatedSuccess() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("message", Matchers.equalTo(UPDATE_PROFILE_SUCCESS_MESSAGE))
                .build();
    }

    public static ResponseSpecification transferSuccessful() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectBody("message", Matchers.equalTo(TRANSFER_SUCCESS_MESSAGE))
                .build();
    }

    public static ResponseSpecification transferUnsuccessful() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody("message", Matchers.equalTo(TRANSFER_INVALID_MESSAGE))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, List<String> errorValues) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, hasItem(isIn(errorValues)))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static ResponseSpecification requestReturnsUnauthorized() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }

    public static ResponseSpecification getDefaultResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
    }
}