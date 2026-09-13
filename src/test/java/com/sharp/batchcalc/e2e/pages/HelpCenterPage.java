package com.sharp.batchcalc.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HelpCenterPage extends BasePage {
    private final By modal = By.id("helpCenterModal");
    private final By body = By.id("hcBody");

    public HelpCenterPage(WebDriver driver) {
        super(driver);
    }

    public boolean isOpen() {
        return driver.findElement(modal).getAttribute("class").contains("open");
    }

    public void selectLanguage(String langCode) {
        click(By.cssSelector("#hcLangTabs button[data-lang='" + langCode + "']"));
    }

    public void selectTopic(String topicId) {
        click(By.cssSelector("#hcTopicNav button[data-topic='" + topicId + "']"));
    }

    public String bodyText() { return textOf(body); }

    public String bodyDir() {
        // Arabic content is wrapped in elements with dir="rtl"
        return driver.findElement(By.cssSelector("#hcBody h3")).getAttribute("dir");
    }

    public void close() { click(By.cssSelector("#helpCenterModal .modal-header button")); }
}
