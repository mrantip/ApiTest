package api.iteration1;

import api.base.BaseTest;
import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserSteps;
import api.specs.RequestSpecs;

import static org.assertj.core.api.Assertions.assertThat;


public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserSteps userSteps = new UserSteps(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());

        assertThat(foundAccount.getId()).isEqualTo(createdAccount.getId());
        assertThat(foundAccount.getAccountNumber()).isEqualTo(createdAccount.getAccountNumber());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccount.getAccountNumber());

        DaoAndModelAssertions.assertThat(createdAccount, accountDao).match();
    }
}