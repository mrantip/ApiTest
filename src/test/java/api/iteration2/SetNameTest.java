package api.iteration2;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.ChangeNameResponse;
import api.models.CreateUserRequest;
import api.models.CustomerModel;
import api.requests.steps.DataBaseSteps;
import common.annotations.APIVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsName;
import api.specs.RequestSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SetNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"Dasha Pupkina", "sAhA tUtKiN", "DDDDDDDDDDDDDDDDDDDDDDDDDD DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD"})
    public void setValidNameTest(String name) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        String initialName = userSteps.getProfileInfo().getName();

        ChangeNameResponse changeNameResponse = userSteps.updateName(name);
        CustomerModel customer = changeNameResponse.getCustomer();

        assertEquals(name, changeNameResponse.getCustomer().getName());
        assertNotEquals(initialName, changeNameResponse.getCustomer().getName());

        //BD
        UserDao userDao = DataBaseSteps.getUserById(changeNameResponse.getCustomer().getId());
        DaoAndModelAssertions.assertThat(customer, userDao).match();

    }

//    @APIVersion("with_validation_fix")
    @ParameterizedTest
    @ValueSource(strings = {"Tora_Dora", "c_DcZ3", "fatid", "Neo Naruto Junior", " ", "d", ""})
    public void setInvalidNameTest(String name) {
        CreateUserRequest userRequest = AdminSteps.createUser();
        UserStepsName userSteps = new UserStepsName(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));

        String initialName = userSteps.getProfileInfo().getName();

        userSteps.updateInvalidName(name);

        String finalName = userSteps.getProfileInfo().getName();

        assertEquals(initialName, finalName);

        //BD
        UserDao userDao = DataBaseSteps.getUserById(userSteps.getProfileInfo().getId());
        DaoAndModelAssertions.assertThat(userSteps.getProfileInfo(), userDao).match();
    }
}