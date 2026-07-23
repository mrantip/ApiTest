package api.requests.steps.usersteps;

import api.models.FraudCheckResponse;
import io.restassured.specification.RequestSpecification;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.requests.skeleton.Endpoint;
import api.requests.skeleton.requesters.CrudRequester;
import api.requests.skeleton.requesters.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

public class UserStepsTransfer extends UserSteps {
    public UserStepsTransfer(RequestSpecification authSpec) {
        super(authSpec);
    }

    public TransferResponse transfer(long senderAccountId, long receiverAccountId, double money) {
        return new ValidatedCrudRequester<TransferResponse>(authSpec, Endpoint.TRANSFER,
                ResponseSpecs.transferSuccessful())
                .post(buildTransferRequest(senderAccountId, receiverAccountId, money));
    }

    public void transferInvalidAmount(long senderAccountId, long receiverAccountId, double money) {
        new CrudRequester(authSpec, Endpoint.TRANSFER, ResponseSpecs.requestReturnsBadRequest())
                .post(buildTransferRequest(senderAccountId, receiverAccountId, money));
    }

    private TransferRequest buildTransferRequest(long senderAccountId, long receiverAccountId, double money) {
        return TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(money)
                .build();
    }

    public TransferResponse transferWithFraudCheck(Long senderAccountId, Long receiverAccountId, double amount) {
        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .description("Test transfer with fraud check")
                .build();

        return new ValidatedCrudRequester<TransferResponse>(
                authSpec,
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                ResponseSpecs.requestReturnsOK()).post(transferRequest);
    }

    public FraudCheckResponse transferWithFraudStatus(String transactionId) {
        String url = Endpoint.FRAUD_CHECK_STATUS.getUrl().replace("{transactionId}", transactionId);

        System.out.println("Final URL: " + url);

        return new ValidatedCrudRequester<FraudCheckResponse>(
                authSpec,
                Endpoint.FRAUD_CHECK_STATUS,
                ResponseSpecs.requestReturnsOK())
                .getByPath(url);
    }
}