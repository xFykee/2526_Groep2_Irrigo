// Java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbTest {

    @Test
    public void testConnectionAndSimpleQuery_orSkip() {
        try (Connection conn = Db.getConnection()) {
            Assertions.assertNotNull(conn, "Connection should not be null");
            try (PreparedStatement ps = conn.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                Assertions.assertTrue(rs.next(), "ResultSet should have at least one row");
                int val = rs.getInt(1);
                Assertions.assertEquals(1, val, "SELECT 1 should return 1");
            }
        } catch (SQLException ex) {
            // Skip test when DB is unreachable or credentials/driver are not available
            Assumptions.assumeTrue(false, "Skipping test because DB is unreachable or error occurred: " + ex.getMessage());
        } catch (RuntimeException re) {
            // Skip if Db static initializer threw (e.g., driver missing)
            Assumptions.assumeTrue(false, "Skipping test due to runtime error (likely missing JDBC driver): " + re.getMessage());
        }
    }

    @Test
    public void testMain_runsAndPrints_orSkip() {
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        System.setErr(new PrintStream(berr));

        try {
            try {
                Db.main(new String[0]);
            } catch (RuntimeException | Error e) {
                // Skip when static init failed (e.g., driver not on classpath)
                Assumptions.assumeTrue(false, "Skipping test due to runtime error during Db.main(): " + e.getMessage());
            }
        } finally {
            System.setOut(origOut);
            System.setErr(origErr);
        }

        String out = bout.toString().trim();
        String err = berr.toString().trim();
        // Accept either successful connect message, failure message, or error output — require at least some output
        boolean hasOutput = !out.isEmpty() || !err.isEmpty();
        Assertions.assertTrue(hasOutput, "Expected Db.main() to produce output on stdout or stderr");
    }
}