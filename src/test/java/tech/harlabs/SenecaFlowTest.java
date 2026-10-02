package tech.harlabs;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
public class SenecaFlowTest {

    @Test
    public void testUnauthorizedRedirectsToLogin() {
        // Accessing /super/dashboard without session should redirect to /login
        given()
            .redirects().follow(false)
            .when().get("/super/dashboard")
            .then()
            .statusCode(303)
            .header("Location", containsString("/login"));

        // Accessing /owner/dashboard without session should redirect to /login
        given()
            .redirects().follow(false)
            .when().get("/owner/dashboard")
            .then()
            .statusCode(303)
            .header("Location", containsString("/login"));
    }

    @Test
    public void testSysadminLoginAndSuperDashboard() {
        // 1. Login as sysadmin
        var response = given()
            .redirects().follow(false)
            .contentType(ContentType.URLENC)
            .formParam("email", "sysadmin@seneca.local")
            .formParam("password", "Admin@Seneca2026!")
            .when().post("/login")
            .then()
            .statusCode(303)
            .header("Location", containsString("/super/dashboard"))
            .extract();

        String sessionCookie = response.cookie("seneca_session");
        Assertions.assertNotNull(sessionCookie);

        // 2. Access /super/dashboard with the session cookie
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/super/dashboard")
            .then()
            .statusCode(200)
            .body(containsString("Ringkasan Sistem Global"));

        // 3. Access /super/tenants
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/super/tenants")
            .then()
            .statusCode(200)
            .body(containsString("Manajemen Tenant"));

        // 4. Access /super/users
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/super/users")
            .then()
            .statusCode(200)
            .body(containsString("Manajemen Pengguna Global"));
    }

    @Test
    public void testOwnerLoginAndOwnerDashboard() {
        // 1. Login as owner
        var response = given()
            .redirects().follow(false)
            .contentType(ContentType.URLENC)
            .formParam("email", "owner@seneca.local")
            .formParam("password", "Admin@Seneca2026!")
            .when().post("/login")
            .then()
            .statusCode(303)
            .header("Location", containsString("/owner/dashboard"))
            .extract();

        String sessionCookie = response.cookie("seneca_session");
        Assertions.assertNotNull(sessionCookie);

        // 2. Access /owner/dashboard with owner cookie
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/owner/dashboard")
            .then()
            .statusCode(200)
            .body(containsString("Seneca Teknologi Nusantara"));

        // 3. Access /owner/members
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/owner/members")
            .then()
            .statusCode(200)
            .body(containsString("Daftar Pegawai & Struktur Akses"));

        // 4. Access /owner/roles
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/owner/roles")
            .then()
            .statusCode(200)
            .body(containsString("HR Administrator"));

        // 5. Access /owner/face-embeddings
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/owner/face-embeddings")
            .then()
            .statusCode(200)
            .body(containsString("Biometrik Wajah"));

        // 6. Security guard: Owner cannot access /super/dashboard (must return 403 Forbidden)
        given()
            .cookie("seneca_session", sessionCookie)
            .when().get("/super/dashboard")
            .then()
            .statusCode(403);
    }
}
