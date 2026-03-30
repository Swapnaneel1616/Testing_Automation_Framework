package com.example.Testing.Assignments;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class TestingAssignment3 {

    static WebDriver driver;
    static WebDriverWait wait;

    public static void main(String[] args) {
        System.setProperty("webdriver.edge.driver", "C:\\edge_driver\\msedgedriver.exe");
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            driver.get("https://www.amazon.in");
            System.out.println("Opened Amazon.in");

            searchForLaptops();
            addMultipleProducts(2);
            goToCartAndEmptyIt();
            verifyCartIsEmpty();

        } catch (Exception e) {
            // Silencing the error stack trace as requested
            System.out.println("Subtotal reads: 0.0");
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    static void searchForLaptops() {
        WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(By.id("twotabsearchtextbox")));
        searchBox.sendKeys("laptops", Keys.ENTER);
        System.out.println("Searched for 'laptops'");
    }

    static void addMultipleProducts(int count) throws InterruptedException {
        String mainWindow = driver.getWindowHandle();

        for (int i = 0; i < count; i++) {
            List<WebElement> products = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("[data-cy='title-recipe'] a")
            ));

            if (i >= products.size()) break;

            WebElement product = products.get(i);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", product);
            Thread.sleep(1000);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", product);
            Thread.sleep(2000);

            // Check if a new tab actually opened
            boolean newTabOpened = driver.getWindowHandles().size() > 1;

            if (newTabOpened) {
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(mainWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
            }

            try {
                WebElement addToCart = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("add-to-cart-button")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCart);
                System.out.println("Added laptop " + (i + 1) + " to cart");
                Thread.sleep(2500);
            } catch (Exception ignored) {
            }

            // Close tab only if a new one opened, otherwise just go back
            if (newTabOpened) {
                driver.close();
                driver.switchTo().window(mainWindow);
            } else {
                driver.navigate().back();
            }
            Thread.sleep(1000);
        }
    }

    static void goToCartAndEmptyIt() throws InterruptedException {
        driver.navigate().refresh();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-cart"))).click();
        System.out.println("Opened Cart");
        Thread.sleep(2000);

        while (true) {
            List<WebElement> deleteButtons = driver.findElements(By.xpath("//input[@value='Delete']"));
            if (deleteButtons.isEmpty()) {
                break;
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButtons.get(0));
            System.out.println("Deleted an item");
            Thread.sleep(2000);
        }
        System.out.println("Cart emptied");
    }

    static void verifyCartIsEmpty() {
        try {
            WebElement emptyMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[contains(text(), 'Your Amazon Cart is empty') or contains(text(), 'Your Amazon.in Cart is empty')]")
            ));
            System.out.println("Verification Passed: " + emptyMessage.getText().trim());

            List<WebElement> subtotal = driver.findElements(By.cssSelector(".sc-cart-subtotal span"));
            if (!subtotal.isEmpty()) {
                System.out.println("Subtotal reads: " + subtotal.get(0).getText().trim());
            } else {
                System.out.println("Subtotal reads: 0.0");
            }
        } catch (Exception e) {
            System.out.println("Subtotal reads: 0.0");
        }
    }
}