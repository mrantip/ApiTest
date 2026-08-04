package common.factory;

import api.models.FraudCheckResponse;
import api.models.TransferResponse;

import static common.config.TestConstants.*;

public class TransferResponseFactory {

    public static TransferResponse createApprovedResponse(
            double amount,
            Long senderAccountId,
            Long receiverAccountId) {

        return TransferResponse.builder()
                .status(TRANSFER_STATUS_APPROVED)
                .message(TRANSFER_MESSAGE_APPROVED)
                .amount(amount)
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .fraudRiskScore(FRAUD_RISK_SCORE_LOW)
                .fraudReason(FRAUD_REASON_LOW_RISK)
                .requiresManualReview(TRANSFER_NO_MANUAL_REVIEW)
                .requiresVerification(TRANSFER_NO_VERIFICATION)
                .build();
    }

    public static FraudCheckResponse createNoCheckStatusResponse(String transactionId) {
        return FraudCheckResponse.builder()
                .transactionId(transactionId)
                .status(FRAUD_CHECK_STATUS_NO_CHECK)
                .note(FRAUD_CHECK_NOTE_NO_CHECK)
                .build();
    }
}