package api.requests.skeleton;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import api.models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),
    TRANSFER(
            "/accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit",
            DepositRequest.class,
            DepositResponse.class
    ),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            ReceiveAllUserAccountResponse.class
    ),
    UPDATE_NAME(
            "/customer/profile",
            ChangeNameRequest.class,
            ChangeNameResponse.class
    ),
    PROFILE_INFO(
            "/customer/profile",
            BaseModel.class,
            ProfileInfoResponse.class
    ),
    TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check",
            TransferRequest.class,
            TransferResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/accounts/fraud-check/{transactionId}",
            BaseModel.class,
            FraudCheckResponse.class
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}