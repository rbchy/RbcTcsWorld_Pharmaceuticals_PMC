package com.sharp.batchcalc.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Page object for the Hierarchy tool panel — representative of all six
 * stateless calculator tabs, which share the same action-row button
 * structure (data-act="add|edit|update|del|display|search|back").
 */
public class HierarchyToolPage extends BasePage {
    private final By panel = By.id("panel-hierarchy");
    private final By cartonsOrdered = By.id("h-cartonsOrdered");
    private final By workOrderNoField = By.id("cfg-hierarchy-workOrderNo");
    private final By resultArea = By.id("result-hierarchy");

    private final By addBtn = By.cssSelector("#actions-hierarchy button[data-act='add']");
    private final By editBtn = By.cssSelector("#actions-hierarchy button[data-act='edit']");
    private final By updateBtn = By.cssSelector("#actions-hierarchy button[data-act='update']");
    private final By deleteBtn = By.cssSelector("#actions-hierarchy button[data-act='del']");
    private final By displayBtn = By.cssSelector("#actions-hierarchy button[data-act='display']");
    private final By searchBtn = By.cssSelector("#actions-hierarchy button[data-act='search']");
    private final By backBtn = By.cssSelector("#actions-hierarchy button[data-act='back']");

    public HierarchyToolPage(WebDriver driver) {
        super(driver);
    }

    public boolean isDisplayed_() {
        return isDisplayed(panel) && driver.findElement(panel).getAttribute("class").contains("active");
    }

    public void setCartonsOrdered(String value) { type(cartonsOrdered, value); }
    public String cartonsOrderedValue() { return driver.findElement(cartonsOrdered).getAttribute("value"); }
    public String workOrderNoValue() { return driver.findElement(workOrderNoField).getAttribute("value"); }

    public void clickAdd() { click(addBtn); }
    public void clickEdit() { click(editBtn); }
    public void clickUpdate() { click(updateBtn); }
    public void clickDelete() { click(deleteBtn); }
    public void clickDisplay() { click(displayBtn); }
    public void clickSearch() { click(searchBtn); }
    public void clickBack() { click(backBtn); }

    public String resultText() { return textOf(resultArea); }

    /** Waits until the result area contains the given substring (e.g. after Add completes). */
    public void waitForResultContaining(String substring) {
        wait.until(d -> resultText().contains(substring));
    }
}
