package api.requests.steps.usersteps;

import api.models.CreateUserRequest;
import api.models.DepositFraudRequest;
import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import api.models.DepositRequest;
import api.models.DepositResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

public class UserStepsDeposit extends UserSteps {
    private static final double MAX_DEPOSIT_AMOUNT = 5000.0;

    public UserStepsDeposit(RequestSpecification authSpec) {
        super(authSpec);
    }

    public static UserStepsDeposit asUser(CreateUserRequest user) {
        return new UserStepsDeposit(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()));
    }

    public DepositResponse deposit(String accountNumber, double money) {
        return new ValidatedCrudRequester<DepositResponse>(authSpec, Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(buildDepositRequest(accountNumber, money));
    }

    public DepositResponse depositFraud(String accountNumber, double money) {
        return new ValidatedCrudRequester<DepositResponse>(authSpec, Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(buildDepositFraudRequest(accountNumber, money));
    }

    public void depositMaxMultipleTimes(String accountNumber, int times) {
        for (int i = 0; i < times; i++) {
            deposit(accountNumber, MAX_DEPOSIT_AMOUNT);
        }
    }

    public void depositInvalidAmount(String accountNumber, double money) {
        new CrudRequester(authSpec, Endpoint.DEPOSIT, ResponseSpecs.requestReturnsBadRequest())
                .post(buildDepositRequest(accountNumber, money));
    }

    public void depositToStrangerAccount(long strangerAccountId) {
        DepositRequest request = DepositRequest.builder()
                .id(strangerAccountId)
                .balance(100.0)
                .build();
        new CrudRequester(authSpec, Endpoint.DEPOSIT, ResponseSpecs.requestReturnsUnauthorized())
                .post(request);
    }

    private DepositRequest buildDepositRequest(String accountNumber, double money) {
        return DepositRequest.builder()
                .id(getAccountByNumber(accountNumber).getId())
                .balance(money)
                .build();
    }

    private DepositFraudRequest buildDepositFraudRequest(String accountNumber, double money) {
        return DepositFraudRequest.builder()
                .accountId(getAccountByNumber(accountNumber).getId())
                .amount(money)
                .description("Test deposit with fraud check")
                .build();
    }
}
