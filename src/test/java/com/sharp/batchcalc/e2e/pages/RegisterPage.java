package com.sharp.batchcalc.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage extends BasePage {
    private final By name = By.id("reg-name");
    private final By username = By.id("reg-user");
    private final By email = By.id("reg-email");
    private final By password = By.id("reg-pass");
    private final By password2 = By.id("reg-pass2");
    private final By registerButton = By.cssSelector("button[onclick='doRegister()']");
    private final By screen = By.id("screen-register");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed_() {
        return isDisplayed(screen) && driver.findElement(screen).getAttribute("class").contains("active");
    }

    public RegisterPage fillName(String v) { type(name, v); return this; }
    public RegisterPage fillUsername(String v) { type(username, v); return this; }
    public RegisterPage fillEmail(String v) { type(email, v); return this; }
    public RegisterPage fillPassword(String v) { type(password, v); return this; }
    public RegisterPage fillPassword2(String v) { type(password2, v); return this; }

    public void clickRegister() { click(registerButton); }
}
