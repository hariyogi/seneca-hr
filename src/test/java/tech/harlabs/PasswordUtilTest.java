package tech.harlabs;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.harlabs.web.security.PasswordUtil;

@QuarkusTest
public class PasswordUtilTest {

    @Test
    public void testHashAndVerify() {
        String raw = "Admin@Seneca2026!";
        String hash = PasswordUtil.hash(raw);
        System.out.println("HASH_OUTPUT: " + hash);
        Assertions.assertTrue(PasswordUtil.verify(raw, hash));
        Assertions.assertFalse(PasswordUtil.verify("wrong", hash));
    }
}
