package com.sharp.batchcalc.e2e;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

import com.sharp.batchcalc.e2e.pages.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium/JUnit5 E2E tests for the browser UI — maps to
 * RbcTcsWorld_Test_Cases.xlsx, sheet "4-UI E2E Tests".
 *
 * NOT part of `mvn test` (excluded by pom.xml's surefire config, see the
 * comment there) because these need:
 *   1) A real Chrome browser + chromedriver (auto-managed by WebDriverManager)
 *   2) The app actually RUNNING first — start it yourself before running
 *      this class:  mvn spring-boot:run   (or ./run.sh)
 *
 * Run explicitly:
 *   mvn test -Dtest=com.sharp.batchcalc.e2e.CriticalJourneysE2ETest
 *
 * IMPORTANT — HONESTY NOTE: this class was written without access to a real
 * browser or the running application (sandboxed authoring environment has
 * neither). Every locator was cross-checked against the exact id/data-act
 * attributes the JavaScript generates in src/main/resources/static/index.html,
 * but the test bodies themselves have NOT been executed. Treat your first run
 * of this suite as the real first verification, and expect to fix minor
 * timing/locator issues — that is normal for a first E2E pass, not a sign
 * the whole approach is wrong.
 */
@Epic("E2E — Browser UI (Selenium)")
public class CriticalJourneysE2ETest {

    private static final String BASE_URL = System.getProperty("e2e.baseUrl", "http://localhost:8081");

    private WebDriver driver;

    @BeforeAll
    static void setupDriverManager() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void openBrowser() {
        ChromeOptions options = new ChromeOptions();
        // Comment out headless while debugging locators for the first time —
        // seeing the real browser makes early failures much faster to diagnose.
        options.addArguments("--headless=new", "--window-size=1400,1000");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) driver.quit();
    }

    // ---------------------------------------------------------------
    // TC-UI-001 / TC-UI-002 — Login
    // ---------------------------------------------------------------
    @Feature("Login")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    void tc_ui_001_login_anyCredentials_reachesDashboard() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.typeUsername("qa.tester").typePassword("anything");
        login.clickSignIn();

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.isDisplayed_(), "Dashboard should be shown after login");
        assertTrue(dashboard.welcomeText().contains("qa.tester"));
    }

    @Feature("Login")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_ui_002_login_emptyUsername_blockedWithAlert() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.typeUsername("").typePassword("x");
        login.clickSignIn();

        // doLogin() calls a blocking window.alert() when username is empty —
        // must be accepted before Selenium can interact with the page again.
        var alert = driver.switchTo().alert();
        String alertText = alert.getText();
        alert.accept();

        assertFalse(alertText.isBlank(), "An alert should have been shown");
        assertTrue(login.isDisplayed_(), "Should remain on Login screen, not proceed to Dashboard");
    }

    // ---------------------------------------------------------------
    // TC-UI-004 / TC-UI-005 — Register
    // ---------------------------------------------------------------
    @Feature("Register")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_ui_004_register_validData_reachesDashboard() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.goToRegister();

        RegisterPage register = new RegisterPage(driver);
        assertTrue(register.isDisplayed_());
        register.fillName("QA Tester").fillUsername("qa.new").fillEmail("qa@example.com")
                .fillPassword("Passw0rd!").fillPassword2("Passw0rd!");
        register.clickRegister();

        DashboardPage dashboard = new DashboardPage(driver);
        assertTrue(dashboard.isDisplayed_());
    }

    @Feature("Register")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_ui_005_register_passwordMismatch_blockedWithAlert() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.goToRegister();

        RegisterPage register = new RegisterPage(driver);
        register.fillUsername("qa.mismatch").fillPassword("aaa").fillPassword2("bbb");
        register.clickRegister();

        var alert = driver.switchTo().alert();
        alert.accept();

        assertTrue(register.isDisplayed_(), "Should remain on Register screen when passwords don't match");
    }

    // ---------------------------------------------------------------
    // TC-UI-007 / TC-UI-008 — Dashboard title, watermark, tile navigation
    // ---------------------------------------------------------------
    @Feature("Dashboard")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_ui_007_dashboard_titleAndTilesRendered() {
        DashboardPage dashboard = loginAndReachDashboard();

        assertTrue(dashboard.titleText().toLowerCase().contains("pharmaceutical")
                || dashboard.titleText().contains("ফার্মাসিউটিক্যাল"),
                "Dashboard title should show the product name in the active language");
        assertEquals(7, dashboard.tileList().size(), "All 7 tool tiles should render");
    }

    @Feature("Dashboard")
    @Severity(SeverityLevel.CRITICAL)
    @Test
    void tc_ui_008_dashboard_clickHierarchyTile_opensHierarchyPanel() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openToolByName("Hierarchy");

        HierarchyToolPage hierarchy = new HierarchyToolPage(driver);
        assertTrue(hierarchy.isDisplayed_(), "Hierarchy panel should be active after clicking its tile");
    }

    // ---------------------------------------------------------------
    // TC-UI-012 / 013 / 014 / 015 — Hierarchy tab action buttons
    // ---------------------------------------------------------------
    @Feature("Hierarchy Tab — Action Buttons")
    @Severity(SeverityLevel.BLOCKER)
    @Test
    void tc_ui_012_hierarchy_add_showsCorrectResult() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openToolByName("Hierarchy");
        HierarchyToolPage hierarchy = new HierarchyToolPage(driver);

        hierarchy.clickAdd();
        hierarchy.waitForResultContaining("1,748");

        assertTrue(hierarchy.resultText().contains("1,748"), "Result should show 1,748 shippers for the default 62,916-carton example");
    }

    @Feature("Hierarchy Tab — Action Buttons")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_ui_013_hierarchy_edit_focusesFirstField() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openToolByName("Hierarchy");
        HierarchyToolPage hierarchy = new HierarchyToolPage(driver);

        hierarchy.clickEdit();
        String activeId = driver.switchTo().activeElement().getAttribute("id");
        assertEquals("cfg-hierarchy-workOrderNo", activeId, "Edit should move focus to the first form field");
    }

    @Feature("Hierarchy Tab — Action Buttons")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_ui_014_hierarchy_delete_resetsFieldsToDefault() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openToolByName("Hierarchy");
        HierarchyToolPage hierarchy = new HierarchyToolPage(driver);

        hierarchy.setCartonsOrdered("999");
        hierarchy.clickDelete();

        assertEquals("62916", hierarchy.cartonsOrderedValue(), "Delete/reset should restore the original seeded value");
    }

    @Feature("Hierarchy Tab — Action Buttons")
    @Severity(SeverityLevel.MINOR)
    @Test
    void tc_ui_015_hierarchy_display_beforeAdd_showsPlaceholder() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openToolByName("Hierarchy");
        HierarchyToolPage hierarchy = new HierarchyToolPage(driver);

        hierarchy.clickDisplay();
        assertTrue(hierarchy.resultText().toLowerCase().contains("no result yet")
                || hierarchy.resultText().contains("এখনো কোনো ফলাফল নেই"),
                "Display before any Add should show the 'no result yet' placeholder, not an error");
    }

    // ---------------------------------------------------------------
    // TC-UI-011 — language toggle
    // ---------------------------------------------------------------
    @Feature("Language Toggle")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_ui_011_languageToggle_persistsAcrossReload() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.toggleLanguage();
        String langAfterToggle = driver.findElement(org.openqa.selenium.By.tagName("body")).getAttribute("data-lang");

        driver.navigate().refresh();
        String langAfterReload = driver.findElement(org.openqa.selenium.By.tagName("body")).getAttribute("data-lang");

        assertEquals(langAfterToggle, langAfterReload, "Language choice should persist across a page reload (localStorage)");
    }

    // ---------------------------------------------------------------
    // TC-UI-029 — Help Center language switch + RTL check for Arabic
    // ---------------------------------------------------------------
    @Feature("Help Center")
    @Severity(SeverityLevel.NORMAL)
    @Test
    void tc_ui_029_helpCenter_arabicRendersRightToLeft() {
        DashboardPage dashboard = loginAndReachDashboard();
        dashboard.openHelpCenter();

        HelpCenterPage help = new HelpCenterPage(driver);
        assertTrue(help.isOpen());
        help.selectLanguage("ar");

        assertEquals("rtl", help.bodyDir(), "Arabic Help Center content should render right-to-left");
    }

    // ---------------------------------------------------------------
    // Shared setup
    // ---------------------------------------------------------------
    private DashboardPage loginAndReachDashboard() {
        LoginPage login = new LoginPage(driver);
        login.open(BASE_URL);
        login.typeUsername("qa.tester").typePassword("anything");
        login.clickSignIn();
        return new DashboardPage(driver);
    }
}
