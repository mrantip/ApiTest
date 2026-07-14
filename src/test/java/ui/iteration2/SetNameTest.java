package ui.iteration2;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.usersteps.UserSteps;
import common.annotations.UserSession;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;
import common.storage.SessionStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest extends BaseUiTest {
    private static final String DEFAULT_USERNAME = "noname";

    @ParameterizedTest
    @ValueSource(strings = {"Dasha Pupkina", "pAhA tUtKiN", "DDDDDDDDDDDDDDDDDDDDDDDDDD DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD"})
    @UserSession
    public void setValidNameTest(String newName) {
        UserSteps userSteps = SessionStorage.getSteps();
        Long accountNumber = userSteps.getProfileInfo().getId();

        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals(DEFAULT_USERNAME, userDashboard.getWelcomeNameText());
        assertEquals(DEFAULT_USERNAME, userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick()
                .enterNewName(newName)
                .saveChangesButtonClick()
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage())
                .goHome();

        assertEquals(newName, userDashboard.getWelcomeNameText());
//        assertEquals(newName, userDashboard.getNameChangeButtonText());
        assertEquals(newName, userSteps.getProfileInfo().getName());

        //BD
        UserDao userDao = DataBaseSteps.getUserById(accountNumber);
        DaoAndModelAssertions.assertThat(userSteps.getProfileInfo(), userDao).match();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Tora_Dora", "c_DcZ3", "fatid", "Neo Naruto Junior", "d"})
    @UserSession
    public void setIncorrectNameTest(String newName) {
        UserSteps userSteps = SessionStorage.getSteps();
        Long accountNumber = userSteps.getProfileInfo().getId();

        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals(DEFAULT_USERNAME, userDashboard.getWelcomeNameText());
        assertEquals(DEFAULT_USERNAME, userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick()
                .enterNewName(newName)
                .saveChangesButtonClick()
                .checkAlertMessageAndAccept(BankAlert.NAME_INCORRECT.getMessage())
                .goHome();

        assertNotEquals(newName, userDashboard.getWelcomeNameText());
        assertNotEquals(newName, userDashboard.getNameChangeButtonText());
        assertNotEquals(newName, userSteps.getProfileInfo().getName());

        //BD
        UserDao userDao = DataBaseSteps.getUserById(accountNumber);
        DaoAndModelAssertions.assertThat(userSteps.getProfileInfo(), userDao).match();
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", ""})
    @UserSession
    public void setInvalidNameTest(String newName) {
        UserSteps userSteps = SessionStorage.getSteps();
        Long accountNumber = userSteps.getProfileInfo().getId();

        UserDashboard userDashboard = new UserDashboard().open();

        assertEquals(DEFAULT_USERNAME, userDashboard.getWelcomeNameText());
        assertEquals(DEFAULT_USERNAME, userDashboard.getNameChangeButtonText());

        userDashboard.changeNameClick()
                .enterNewName(newName)
                .saveChangesButtonClick()
                .checkAlertMessageAndAccept(BankAlert.NAME_INVALID.getMessage())
                .goHome();

        assertNotEquals(newName, userDashboard.getWelcomeNameText());
        assertNotEquals(newName, userDashboard.getNameChangeButtonText());
        assertNotEquals(newName, userSteps.getProfileInfo().getName());

        //BD
        UserDao userDao = DataBaseSteps.getUserById(accountNumber);
        DaoAndModelAssertions.assertThat(userSteps.getProfileInfo(), userDao).match();
    }
}