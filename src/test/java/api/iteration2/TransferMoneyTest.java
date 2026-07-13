package api.iteration2;

import api.base.BaseTest;
import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountModel;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferResponse;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import api.requests.steps.usersteps.UserStepsTransfer;
import api.specs.RequestSpecs;

import static org.assertj.core.api.Assertions.assertThat;


public class TransferMoneyTest extends BaseTest {

    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToOwnAccountTest(double transferSum) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double currentBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double currentBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        TransferResponse transferResponse = userStepsTransfer.transfer(createdAccountFirst.getId(), createdAccountSecond.getId(), transferSum);

        assertThat(transferSum).isEqualTo(transferResponse.getAmount());
        assertThat(currentBalanceFirst).isNotEqualTo(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance());
        assertThat(currentBalanceSecond).isNotEqualTo(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance());

        //BD
        AccountModel foundAccountFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber());
        AccountModel foundAccountSecond = userSteps.getAccountByNumber(createdAccountSecond .getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountFirst.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountSecond, accountDaoStranger).match();
    }


    @ParameterizedTest
    @ValueSource(doubles = {10000, 9999.99, 0.01, 500})
    public void transferValidSumToStrangersAccountTest(double transferSum) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double currentBalance = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();

        CreateUserRequest userRequestStranger = AdminSteps.createUser();
        UserSteps userStepsStranger = new UserSteps(RequestSpecs.authAsUser(userRequestStranger.getUsername(), userRequestStranger.getPassword()));

        CreateAccountResponse createdAccountStranger = userStepsStranger.createAccount();
        double currentBalanceStranger = userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber()).getBalance();

        TransferResponse transferResponse = userStepsTransfer.transfer(createdAccountFirst.getId(), createdAccountStranger.getId(), transferSum);

        assertThat(transferSum).isEqualTo(transferResponse.getAmount());
        assertThat(currentBalance).isNotEqualTo(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance());
        assertThat(currentBalanceStranger).isNotEqualTo(userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber()).getBalance());

        //BD
        AccountModel foundAccountFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber());
        AccountModel foundAccountStranger= userStepsStranger.getAccountByNumber(createdAccountStranger .getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountFirst.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountStranger.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountStranger, accountDaoStranger).match();

    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.01, 10000.01})
    public void transferInvalidSumToOwnAccountTest(double transferSum) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double currentBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double currentBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        userStepsTransfer.transferInvalidAmount(createdAccountFirst.getId(), createdAccountSecond.getId(), transferSum);

        assertThat(currentBalanceFirst).isEqualTo(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance());
        assertThat(currentBalanceSecond).isEqualTo(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance());

        //BD
        AccountModel foundAccountFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber());
        AccountModel foundAccountSecond = userSteps.getAccountByNumber(createdAccountSecond .getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountFirst.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountSecond, accountDaoStranger).match();
    }

    @Test
    public void transferInvalidSumToStrangersAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 3);

        double currentBalance = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();

        CreateUserRequest userRequestStranger = AdminSteps.createUser();
        UserSteps userStepsStranger = new UserSteps(RequestSpecs.authAsUser(userRequestStranger.getUsername(), userRequestStranger.getPassword()));

        CreateAccountResponse createdAccountStranger = userStepsStranger.createAccount();
        double currentBalanceStranger = userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber()).getBalance();

        userStepsTransfer.transferInvalidAmount(createdAccountFirst.getId(), createdAccountStranger.getId(), 0);

        assertThat(currentBalance).isEqualTo(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance());
        assertThat(currentBalanceStranger).isEqualTo(userStepsStranger.getAccountByNumber(createdAccountStranger.getAccountNumber()).getBalance());

        //BD
        AccountModel foundAccountFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber());
        AccountModel foundAccountStranger= userStepsStranger.getAccountByNumber(createdAccountStranger .getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountFirst.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountStranger.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountStranger, accountDaoStranger).match();
    }

    @ParameterizedTest
    @ValueSource(doubles = {5000.01, 10000})
    public void transferInsufficientSumTest(double transferSum) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        CreateAccountResponse createdAccountFirst = userSteps.createAccount();
        CreateAccountResponse createdAccountSecond = userSteps.createAccount();
        userSteps.depositMaxMultipleTimes(createdAccountFirst.getAccountNumber(), 1);

        double currentBalanceFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance();
        double currentBalanceSecond = userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance();

        userStepsTransfer.transferInvalidAmount(createdAccountFirst.getId(), createdAccountSecond.getId(), transferSum);

        assertThat(currentBalanceFirst).isEqualTo(userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber()).getBalance());
        assertThat(currentBalanceSecond).isEqualTo(userSteps.getAccountByNumber(createdAccountSecond.getAccountNumber()).getBalance());

        //BD
        AccountModel foundAccountFirst = userSteps.getAccountByNumber(createdAccountFirst.getAccountNumber());
        AccountModel foundAccountSecond = userSteps.getAccountByNumber(createdAccountSecond .getAccountNumber());

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createdAccountFirst.getAccountNumber());
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(createdAccountSecond.getAccountNumber());

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountSecond, accountDaoStranger).match();
    }
}