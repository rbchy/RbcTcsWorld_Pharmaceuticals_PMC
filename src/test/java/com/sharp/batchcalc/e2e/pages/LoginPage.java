package com.sharp.batchcalc.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private final By username = By.id("login-user");
    private final By password = By.id("login-pass");
    private final By signInButton = By.cssSelector("button[onclick='doLogin()']");
    private final By registerLink = By.cssSelector("#screen-login .auth-switch a");
    private final By langToggle = By.id("langToggle1");
    private final By screen = By.id("screen-login");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/");
        waitVisible(screen);
    }

    public boolean isDisplayed_() {
        return isDisplayed(screen) && driver.findElement(screen).getAttribute("class").contains("active");
    }

    public LoginPage typeUsername(String value) { type(username, value); return this; }
    public LoginPage typePassword(String value) { type(password, value); return this; }

    /** Submits the form. Caller decides whether to expect navigation afterward. */
    public void clickSignIn() { click(signInButton); }

    public void goToRegister() { click(registerLink); }

    public void toggleLanguage() { click(langToggle); }
}
