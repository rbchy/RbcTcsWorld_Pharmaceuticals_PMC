package com.sharp.batchcalc.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DashboardPage extends BasePage {
    private final By screen = By.id("screen-dashboard");
    private final By title = By.cssSelector(".dash-title");
    private final By tiles = By.cssSelector(".tile");
    private final By logoutButton = By.cssSelector("button[onclick='doLogout()']");
    private final By helpCenterButton = By.cssSelector("button[onclick=\"openHelpCenter('glossary')\"]");
    private final By welcomeMsg = By.id("welcomeMsg");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed_() {
        return isDisplayed(screen) && driver.findElement(screen).getAttribute("class").contains("active");
    }

    public String titleText() { return textOf(title); }

    public List<WebElement> tileList() { return driver.findElements(tiles); }

    /** Opens a tool panel by its visible English tile title text, e.g. "Hierarchy". */
    public void openToolByName(String toolNameEn) {
        for (WebElement tile : tileList()) {
            WebElement enSpan = tile.findElement(By.cssSelector(".tile-title .en"));
            if (enSpan.getText().trim().equalsIgnoreCase(toolNameEn)) {
                tile.click();
                return;
            }
        }
        throw new IllegalArgumentException("No dashboard tile found for tool: " + toolNameEn);
    }

    public void clickLogout() { click(logoutButton); }
    public void openHelpCenter() { click(helpCenterButton); }
    public String welcomeText() { return textOf(welcomeMsg); }
}
