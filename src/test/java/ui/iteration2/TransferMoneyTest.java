package ui.iteration2;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountModel;
import api.requests.steps.DataBaseSteps;
import common.context.UserStepsWithAccountAndDeposit;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoneyTest extends BaseUiTest {

    @ParameterizedTest
    @ValueSource(strings = {"10000", "9999.99", "0.01", "500"})
    @UserSession(value = 2,  withAccountForAll = true, withDeposit = true)
    public void transferValidSumToAccountTest(String transfer) {
        UserStepsWithAccountAndDeposit user1 = SessionStorage.getContext(1);
        UserStepsWithAccountAndDeposit user2 = SessionStorage.getContext(2);

        String accountFirst = user1.getAccountNumber();
        String accountSecond = user2.getAccountNumber();

        double initialBalanceFirst = user1.getAccount().getBalance();;
        double initialBalanceSecond = user2.getAccount().getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .chooseAnAccount(accountFirst)
                .enterRecipientName("Gosha")
                .enterRecipientAccountNumber(accountSecond)
                .enterAmount(transfer)
                .confirmDetailsAreCorrect()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.SUCCESSFULLY_TRANSFERRED.format(transfer, accountSecond));

        assertThat(user1.getAccountByNumber(accountFirst).getBalance()).isNotEqualTo(initialBalanceFirst);
        assertThat(user2.getAccountByNumber(accountSecond).getBalance()).isNotEqualTo(initialBalanceSecond);

        //BD
        AccountModel foundAccountFirst = user1.getAccountByNumber(accountFirst);
        AccountModel foundAccountSecond = user2.getAccountByNumber(accountSecond);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountFirst);
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(accountSecond);

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountSecond, accountDaoStranger).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"10000.01", "0", "-0.01"})
    @UserSession(value = 2,  withAccountForAll = true, withDeposit = true)
    public void transferInvalidSumToAccountTest(String transfer) {
        UserStepsWithAccountAndDeposit user1 = SessionStorage.getContext(1);
        UserStepsWithAccountAndDeposit user2 = SessionStorage.getContext(2);

        String accountFirst = user1.getAccountNumber();
        String accountSecond = user2.getAccountNumber();

        double initialBalanceFirst = user1.getAccountByNumber(accountFirst).getBalance();
        double initialBalanceSecond = user2.getAccountByNumber(accountSecond).getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .chooseAnAccount(accountFirst)
                .enterRecipientName("Gosha")
                .enterRecipientAccountNumber(accountSecond)
                .enterAmount(transfer)
                .confirmDetailsAreCorrect()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.INVALID_TRANSFER.format(transfer, accountSecond));

        assertThat(user1.getAccountByNumber(accountFirst).getBalance()).isEqualTo(initialBalanceFirst);
        assertThat(user2.getAccountByNumber(accountSecond).getBalance()).isEqualTo(initialBalanceSecond);

        //BD
        AccountModel foundAccountFirst = user1.getAccountByNumber(accountFirst);
        AccountModel foundAccountStranger= user2.getAccountByNumber(accountSecond);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountFirst);
        AccountDao accountDaoStranger = DataBaseSteps.getAccountByAccountNumber(accountSecond);

        DaoAndModelAssertions.assertThat(foundAccountFirst, accountDao).match();
        DaoAndModelAssertions.assertThat(foundAccountStranger, accountDaoStranger).match();
    }

    @Test
    @UserSession(withAccount = true)
    public void transferNotChoosingTransferDataTest() {
        UserStepsWithAccountAndDeposit user = SessionStorage.getContext();
        String accountNumber = user.getAccountNumber();

        double initialBalanceFirst = user.getAccountByNumber(accountNumber).getBalance();

        new UserDashboard().open()
                .makeATransfer()
                .sendTransferClick()
                .checkAlertMessageAndAccept(BankAlert.NO_DATA_FOR_TRANSFER.getMessage());

        assertThat(user.getAccountByNumber(accountNumber).getBalance()).isEqualTo(initialBalanceFirst);
    }
}