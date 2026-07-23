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
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();

        // Проверяем, что WireMock доступен
        String fraudUrl = System.getProperty("FRAUD_DETECTION_SERVICE_URL");
        System.out.println("✅ FRAUD_DETECTION_SERVICE_URL: " + fraudUrl);

        // Проверяем переменные окружения
        String envUrl = System.getenv("FRAUD_DETECTION_SERVICE_URL");
        System.out.println("✅ FRAUD_DETECTION_SERVICE_URL (Env): " + envUrl);

        if (fraudUrl == null || fraudUrl.isEmpty()) {
            System.err.println("❌ FRAUD_DETECTION_SERVICE_URL is not set!");
        }

        // Проверяем доступность WireMock
        if (fraudUrl != null && !fraudUrl.isEmpty()) {
            try {
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection)
                        new java.net.URL(fraudUrl + "/__admin/health").openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                System.out.println("✅ WireMock health check: " + responseCode);
            } catch (Exception e) {
                System.err.println("❌ WireMock not accessible: " + e.getMessage());
            }
        }
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    @APIVersion("with_fraud_check")
    public void testTransferWithFraudCheck() {
        CreateUserRequest user1 = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));

        CreateAccountResponse account1 = userSteps.createAccount();

        double depositAmount = Math.random() * 4999.9 + 0.1;
        DepositResponse depositResponse = userSteps.depositFraud(account1.getAccountNumber(), depositAmount);

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

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(transferAmount)
                .senderAccountId(account1.getId())
                .receiverAccountId(account2.getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            // GET /fraud-check/{transactionId}
            checkStatus = "NO_FRAUD_CHECK_REQUIRED",
            note = "This transaction does not require fraud checking."
    )
    @APIVersion("with_fraud_check")
    public void testTransferTransaction() {
        CreateUserRequest user1 = AdminSteps.createUser();
        UserStepsDeposit userSteps = new UserStepsDeposit(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));
        UserStepsTransfer userStepsTransfer = new UserStepsTransfer(RequestSpecs.authAsUser(user1.getUsername(), user1.getPassword()));

        CreateAccountResponse account1 = userSteps.createAccount();

        double depositAmount = Math.random() * 4999.9 + 0.1;
        DepositResponse depositResponse = userSteps.depositFraud(account1.getAccountNumber(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        UserStepsDeposit userSteps2 = new UserStepsDeposit(RequestSpecs.authAsUser(user2.getUsername(), user2.getPassword()));
        CreateAccountResponse account2 = userSteps2.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        TransferResponse transferResponse = userStepsTransfer.transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                transferAmount
        );

        String id = transferResponse.getTransactionId().toString();
        userStepsTransfer.transferWithFraudStatus(id);

        FraudCheckResponse statusResponse = userStepsTransfer.transferWithFraudStatus(
                String.valueOf(transferResponse.getTransactionId())
        );

        softly.assertThat(statusResponse.getTransactionId())
                .as("Transaction ID должен совпадать")
                .isEqualTo(String.valueOf(transferResponse.getTransactionId()));
        softly.assertThat(statusResponse.getStatus())
                .as("Статус должен быть NO_FRAUD_CHECK_REQUIRED")
                .isEqualTo("NO_FRAUD_CHECK_REQUIRED");
        softly.assertThat(statusResponse.getNote())
                .as("Note должен быть корректным")
                .isEqualTo("This transaction does not require fraud checking.");

    }
}