package common.config;

import lombok.Getter;

@Getter
public class TestConstants {
    public static final String FRAUD_STATUS_SUCCESS = "SUCCESS";
    public static final String FRAUD_DECISION_APPROVED = "APPROVED";
    public static final String FRAUD_DECISION_MANUAL_REVIEW = "MANUAL_REVIEW";
    public static final double FRAUD_RISK_SCORE_LOW = 0.2;
    public static final String FRAUD_REASON_LOW_RISK = "Low risk transaction";
    public static final boolean FRAUD_NO_MANUAL_REVIEW = false;
    public static final boolean FRAUD_MANUAL_REVIEW_REQUIRED = true;
    public static final boolean FRAUD_NO_VERIFICATION = false;
    public static final boolean FRAUD_VERIFICATION_REQUIRED = true;

    public static final String FRAUD_CHECK_STATUS_NO_CHECK = "NO_FRAUD_CHECK_REQUIRED";
    public static final String FRAUD_CHECK_NOTE_NO_CHECK = "This transaction does not require fraud checking.";

    public static final String TRANSFER_STATUS_APPROVED = "APPROVED";
    public static final String TRANSFER_MESSAGE_APPROVED = "Transfer approved and processed immediately";
    public static final boolean TRANSFER_NO_MANUAL_REVIEW = false;
    public static final boolean TRANSFER_MANUAL_REVIEW = true;
    public static final boolean TRANSFER_NO_VERIFICATION = false;
    public static final boolean TRANSFER_VERIFICATION = true;

    public static final double MIN_DEPOSIT = 0.1;
    public static final double MAX_DEPOSIT = 4999.9;
}