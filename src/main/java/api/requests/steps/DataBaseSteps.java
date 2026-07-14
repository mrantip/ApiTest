package api.requests.steps;

import api.configs.Config;
import api.dao.AccountDao;
import api.dao.UserDao;
import api.database.Condition;
import api.database.DBRequest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBaseSteps {

    public static UserDao getUserByUsername(String username) {
        return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("customers")
                    .where(Condition.equalTo("username", username))
                    .extractAs(UserDao.class);
    }

    public static UserDao getUserById(Long id) {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("customers")
                    .where(Condition.equalTo("id", id))
                    .extractAs(UserDao.class);
    }

    public static UserDao getUserByRole(String role) {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("customers")
                    .where(Condition.equalTo("role", role))
                    .extractAs(UserDao.class);
    }

    public static AccountDao getAccountByAccountNumber(String accountNumber) {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("accounts")
                    .where(Condition.equalTo("account_number", accountNumber))
                    .extractAs(AccountDao.class);
    }

    public static AccountDao getAccountById(Long id) {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("accounts")
                    .where(Condition.equalTo("id", id))
                    .extractAs(AccountDao.class);
    }

    public static AccountDao getAccountByCustomerId(Long customerId) {
            return DBRequest.builder()
                    .requestType(DBRequest.RequestType.SELECT)
                    .table("customers")
                    .where(Condition.equalTo("customer_id", customerId))
                    .extractAs(AccountDao.class);
    }

    public static void updateAccountBalance(Long accountId, Double newBalance) {
            try (Connection connection = DriverManager.getConnection(
                    Config.getProperty("db.url"),
                    Config.getProperty("db.username"),
                    Config.getProperty("db.password"))) {

                String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setDouble(1, newBalance);
                    statement.setLong(2, accountId);
                    int rowsAffected = statement.executeUpdate();

                    if (rowsAffected == 0) {
                        throw new RuntimeException("No account found with ID: " + accountId);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update account balance", e);
            }
    }
}