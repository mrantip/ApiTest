package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $%s to account %s!"),
    INVALID_AMOUNT_DEPOSIT("❌ Please enter a valid amount."),
    DEPOSIT_LESS_OR_EQUAL_5000("❌ Please deposit less or equal to 5000$."),
    NO_ACCOUNT_DEPOSIT("❌ Please select an account."),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $%s to account %s!"),
    INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    INVALID_TRANSFER_EXCEED_10000("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    NO_DATA_FOR_TRANSFER("❌ Please fill all fields and confirm."),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_INVALID("❌ Please enter a valid name."),
    NAME_INCORRECT("Name must contain two words with letters only");


    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}