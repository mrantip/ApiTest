package api.fraud;

import api.base.BaseTest;
import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.requests.steps.usersteps.UserStepsDeposit;
import api.requests.steps.usersteps.UserStepsTransfer;
import api.specs.RequestSpecs;
import common.annotations.APIVersion;
import common.annotations.FraudCheckMock;
import common.extensions.FraudCheckWireMockExtension;
import common.extensions.TimingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static common.config.TestConstants.*;
import static common.factory.TransferResponseFactory.createApprovedResponse;
import static common.factory.TransferResponseFactory.createNoCheckStatusResponse;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    @Test
    @FraudCheckMock(
            status = FRAUD_STATUS_SUCCESS,
            decision = FRAUD_DECISION_APPROVED,
            riskScore = FRAUD_RISK_SCORE_LOW,
            reason = FRAUD_REASON_LOW_RISK,
            requiresManualReview = FRAUD_NO_MANUAL_REVIEW,
            additionalVerificationRequired = FRAUD_NO_VERIFICATION
    )
    @APIVersion("with_fraud_check")
    public void testTransferWithFraudCheck() {
        CreateUserRequest user1 = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));

        CreateAccountResponse account1 = userSteps.createAccount();

        double depositAmount = Math.random() * MAX_DEPOSIT + 0.1;
        userSteps.depositFraud(account1.getAccountNumber(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        UserStepsDeposit userSteps2 = new UserStepsDeposit(RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()));
        CreateAccountResponse account2 = userSteps2.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        TransferResponse transferResponse = userStepsTransfer.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = createApprovedResponse(
                transferAmount,
                account1.getId(),
                account2.getId());

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            // GET /fraud-check/{transactionId}
            checkStatus = FRAUD_CHECK_STATUS_NO_CHECK,
            note = FRAUD_CHECK_NOTE_NO_CHECK
    )
    @APIVersion("with_fraud_check")
    public void testTransferTransaction() {
        CreateUserRequest user1 = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));

        CreateAccountResponse account1 = userSteps.createAccount();

        double depositAmount = Math.random() * MAX_DEPOSIT + 0.1;
        userSteps.depositFraud(account1.getAccountNumber(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        UserStepsDeposit userSteps2 = new UserStepsDeposit(RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()));
        CreateAccountResponse account2 = userSteps2.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        TransferResponse transferResponse = userStepsTransfer.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );


        FraudCheckResponse statusResponse = userStepsTransfer.transferWithFraudStatus(
                String.valueOf(transferResponse.getTransactionId())
        );

        FraudCheckResponse expectedResponse = createNoCheckStatusResponse(String.valueOf(transferResponse.getTransactionId()));

        ModelAssertions.assertThatModels(expectedResponse, statusResponse).match();
    }
}