package ui.iteration2;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
import common.context.UserStepsWithAccountAndDeposit;
import com.codeborne.selenide.*;
import api.models.AccountModel;
import common.annotations.UserSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import common.storage.SessionStorage;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoneyTest extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"5000.0", "4999.99", "0.01"})
    @UserSession(withAccount = true)
    public void depositValidSumSendKeysTest(String deposit) {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(accountNumber)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.format(deposit, accountNumber));

        AccountModel foundAccount = user.getAccountByNumber(accountNumber);
        double expectedBalance = Double.parseDouble(deposit);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertThat(foundAccount.getBalance()).isNotEqualTo(user.getAccount().getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountNumber);

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
    }

    @Test
    @UserSession(withAccount = true)
    public void depositValidSumArrowsTest() {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        String actualAmount = "3";

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(accountNumber)
                .enterAmountArrowUp(3)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_DEPOSITED.format(actualAmount, accountNumber));

        AccountModel foundAccount = user.getAccountByNumber(accountNumber);
        double expectedBalance = Double.parseDouble(actualAmount);
        assertThat(foundAccount.getBalance()).isEqualTo(expectedBalance);
        assertThat(foundAccount.getBalance()).isNotEqualTo(user.getAccount().getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountNumber);

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"5000.01", "10000", "1000000"})
    @UserSession(withAccount = true)
    public void depositBiggerValidSumTest(String deposit) {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(accountNumber)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_LESS_OR_EQUAL_5000.getMessage());

        AccountModel foundAccount = user.getAccountByNumber(accountNumber);
        assertThat(foundAccount.getBalance()).isEqualTo(user.getAccount().getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountNumber);

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "1.01124342"})
    @UserSession(withAccount = true)
    public void depositInvalidSumTest(String deposit) {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .chooseAnAccount(accountNumber)
                .enterAmount(deposit)
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.INVALID_AMOUNT_DEPOSIT.getMessage());

        AccountModel foundAccount = user.getAccountByNumber(accountNumber);
        assertThat(foundAccount.getBalance()).isEqualTo(user.getAccount().getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountNumber);

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
    }

    @Test
    @UserSession(withAccount = true)
    public void depositNotChoosingAccountAndSumTest() {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        new UserDashboard().open()
                .depositMoney()
                .depositClick()
                .andExpect()
                .checkAlertMessageAndAccept(BankAlert.NO_ACCOUNT_DEPOSIT.getMessage());

        AccountModel foundAccount = user.getAccountByNumber(accountNumber);
        assertThat(foundAccount.getBalance()).isEqualTo(user.getAccount().getBalance());

        //BD
        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountNumber);

        DaoAndModelAssertions.assertThat(foundAccount, accountDao).match();
    }
}