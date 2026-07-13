package api.iteration2;

import api.base.BaseTest;
import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositResponse;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import api.specs.RequestSpecs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DepositMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {5000.0, 4999.99, 0.01})
    public void depositValidSumTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        DepositResponse depositResponse = userSteps.deposit(createdAccount.getAccountNumber(), deposit);

        assertNotEquals(createdAccount.getBalance(), depositResponse.getBalance());
        assertEquals(deposit, depositResponse.getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccount.getAccountNumber());

        DaoAndModelAssertions.assertThat(depositResponse, accountDao).match();
    }


    @ParameterizedTest
    @ValueSource(doubles = {5000.010, -0.01, 1.01124342})
    public void depositInvalidSumTest(double deposit) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        userSteps.depositInvalidAmount(createdAccount.getAccountNumber(), deposit);

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());

        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccount.getAccountNumber());

        DaoAndModelAssertions.assertThat(createdAccount, accountDao).match();
    }

//    @APIVersion("with_validation_fix")
    @Test
    public void depositToStrangerAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccount = userSteps.createAccount();

        CreateUserRequest userRequestStranger = AdminSteps.createUser();
        UserStepsDeposit userStepsStranger = new UserStepsDeposit(RequestSpecs.authAsUser(userRequestStranger.getUsername(), userRequestStranger.getPassword()));

        CreateAccountResponse createdAccountStranger = userStepsStranger.createAccount();

        userSteps.depositToStrangerAccount(createdAccountStranger.getId());

        AccountModel foundAccount = userSteps.getAccountByNumber(createdAccount.getAccountNumber());
        AccountModel foundAccountStranger = userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber());

        assertThat(foundAccount.getBalance()).isEqualTo(createdAccount.getBalance());
        assertThat(foundAccountStranger.getBalance()).isEqualTo(createdAccountStranger.getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccount.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountStranger.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
        DaoAndModelAssertions.assertThat(createdAccountStranger, accountDaoStranger).match();
    }
}