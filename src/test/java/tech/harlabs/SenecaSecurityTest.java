package tech.harlabs;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@QuarkusTest
public class SenecaSecurityTest {

    @Test
    public void testSecurityAndNoCacheHeadersOnProtectedRoutes() {
        // 1. Login as sysadmin to obtain valid session
        var loginResp = given()
            .redirects().follow(false)
            .contentType(ContentType.URLENC)
            .formParam("email", "sysadmin@seneca.local")
            .formParam("password", "Admin@Seneca2026!")
            .when().post("/login")
            .then()
            .statusCode(303)
            .extract();

        String sessionCookie = loginResp.cookie("seneca_session");
        Assertions.assertNotNull(sessionCookie);

        // 2. Request protected /super/dashboard and verify strict no-cache & security headers
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/super/dashboard")
            .then()
            .statusCode(200)
            .header("Cache-Control", containsString("no-store"))
            .header("Cache-Control", containsString("no-cache"))
            .header("Cache-Control", containsString("must-revalidate"))
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("Referrer-Policy", "strict-origin-when-cross-origin");

        // 3. Request /owner/dashboard without session (unauthorized redirect) and verify headers
        given()
            .redirects().follow(false)
            .when().get("/owner/dashboard")
            .then()
            .statusCode(303)
            .header("Cache-Control", containsString("no-store"))
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY");
    }

    @Test
    public void testLogoutClearsSessionAndSetsNoCacheHeaders() {
        // Test GET /logout
        given()
            .redirects().follow(false)
            .when().get("/logout")
            .then()
            .statusCode(303)
            .header("Location", containsString("/login"))
            .header("Cache-Control", containsString("no-store"))
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .cookie("seneca_session", "");

        // Test POST /logout
        given()
            .redirects().follow(false)
            .when().post("/logout")
            .then()
            .statusCode(303)
            .header("Location", containsString("/login"))
            .header("Cache-Control", containsString("no-store"))
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            .cookie("seneca_session", "");
    }

    @Test
    public void testReplayAfterLogoutIsBlocked() {
        // 1. Login
        var loginResp = given()
            .redirects().follow(false)
            .contentType(ContentType.URLENC)
            .formParam("email", "sysadmin@seneca.local")
            .formParam("password", "Admin@Seneca2026!")
            .when().post("/login")
            .then()
            .statusCode(303)
            .extract();

        String sessionCookie = loginResp.cookie("seneca_session");

        // 2. Perform Logout
        var logoutResp = given()
            .redirects().follow(false)
            .cookie("seneca_session", sessionCookie)
            .when().get("/logout")
            .then()
            .statusCode(303)
            .extract();

        String clearedCookie = logoutResp.cookie("seneca_session");

        // 3. Attempt to access protected page with cleared cookie (simulating back button request)
        given()
            .redirects().follow(false)
            .cookie("seneca_session", clearedCookie)
            .when().get("/super/dashboard")
            .then()
            .statusCode(303)
            .header("Location", containsString("/login"));
    }

    @Test
    public void testStaticAssetsDoNotHaveNoStoreHeader() {
        given()
            .when().get("/static/logo.svg")
            .then()
            .statusCode(200)
            .header("Cache-Control", not(containsString("no-store")));
    }
}
