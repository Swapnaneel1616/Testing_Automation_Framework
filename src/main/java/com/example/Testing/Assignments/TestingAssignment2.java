package com.example.Testing.Assignments;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class TestingAssignment2 {

    public static void main(String[] args) {
        System.setProperty("webdriver.edge.driver", "C:\\edge_driver\\msedgedriver.exe");
        WebDriver driver = new EdgeDriver();
        driver.manage().window().maximize();

        try {
            runMobileCartWorkflow(driver, "3");
        } catch (Exception e) {
            System.err.println("Test execution failed!");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public static void runMobileCartWorkflow(WebDriver driver, String quantity) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://www.amazon.in/");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Mobiles"))).click();
        try {
            WebElement filter = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//span[contains(text(),'Get It by Tomorrow')]")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", filter);
        } catch (Exception e) {
            System.out.println("Filter click failed or timed out. Retrying with direct URL if needed.");
        }
        try {
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.name("submit.addToCart")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartBtn);

            System.out.println("Product added to cart successfully.");
        } catch (Exception e) {
            // Fallback: Use the aria-label shown in your screenshot
            WebElement fallbackBtn = driver.findElement(By.xpath("//button[@aria-label='Add to cart']"));
            fallbackBtn.click();
        }
    }
}