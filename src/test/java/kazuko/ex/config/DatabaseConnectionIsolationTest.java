package kazuko.ex.config;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionIsolationTest {

    @Test
    void closingOneDaoConnectionDoesNotInvalidateAnother() throws Exception {
        try (Connection first = DatabaseConnection.getConnection();
             Connection second = DatabaseConnection.getConnection()) {
            assertNotSame(first, second);

            first.close();

            assertFalse(second.isClosed());
            try (Statement statement = second.createStatement();
                 ResultSet result = statement.executeQuery("SELECT 1 FROM RDB$DATABASE")) {
                assertTrue(result.next());
            }
        }
    }
}
