package api.requests.steps.usersteps;

import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.ProfileInfoResponse;
import api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    protected String username;
    protected String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
        this.authSpec = RequestSpecs.authAsUser(username, password);
    }

    final RequestSpecification authSpec;

    public UserSteps(RequestSpecification authSpec) {
        this.authSpec = authSpec;
    }

    public CreateAccountResponse createAccount() {

        return new CrudRequester(authSpec, Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(CreateAccountResponse.class);
    }

    public List<AccountModel> getAllUserAccounts() {
        return new ValidatedCrudRequester<AccountModel>(
                authSpec,
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(AccountModel[].class);
    }

    public  List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);
    }

    public AccountModel getAccountByNumber(String accountNumber) {
        return getAllUserAccounts().stream()
                .filter(account -> account.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account " + accountNumber + " not found"));
    }

    public ProfileInfoResponse getProfileInfo() {
        return new ValidatedCrudRequester<ProfileInfoResponse>(authSpec, Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK())
                .get();
    }
}