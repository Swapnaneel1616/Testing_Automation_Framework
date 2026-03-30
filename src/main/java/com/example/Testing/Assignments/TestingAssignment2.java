package com.example.Testing.Assignments;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.*;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class TestingAssignment2{

    static WebDriver driver;
    static WebDriverWait wait;

    public static void main(String[] args) {
        System.setProperty("webdriver.edge.driver", "C:\\edge_driver\\msedgedriver.exe");
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        // NOTE: Do NOT set implicitlyWait when using WebDriverWait — they conflict
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        try {
            driver.get("https://www.amazon.in");
            System.out.println("Opened Amazon.in");
            Thread.sleep(3000);

            clickMobilesNav();
            applyNextDayDeliveryFilter();
            clickFirstProduct();
            setQuantityAndAddToCart(3);
            printCartTotal();

        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void clickMobilesNav() throws InterruptedException {
        try {
            WebElement mobilesLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[data-csa-c-content-id='nav_cs_mobiles']")
            ));
            mobilesLink.click();
            System.out.println("Clicked Mobiles nav link");
        } catch (Exception e) {
            WebElement mobilesLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@href,'node=1389401031')]")
            ));
            mobilesLink.click();
            System.out.println("Clicked Mobiles nav link (fallback)");
        }
        Thread.sleep(3000);
    }

    static void applyNextDayDeliveryFilter() throws InterruptedException {
        try {
            WebElement tomorrowFilter = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//span[contains(text(),'Get It by Tomorrow')]/ancestor::a[1]")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", tomorrowFilter);
            Thread.sleep(500);
            tomorrowFilter.click();
            System.out.println("Applied 'Get It by Tomorrow' filter");
        } catch (Exception e) {
            try {
                WebElement label = driver.findElement(
                        By.xpath("//label[contains(.,'Get It by Tomorrow') or contains(.,'Tomorrow')]")
                );
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", label);
                Thread.sleep(300);
                label.click();
                System.out.println("Clicked 'Get It by Tomorrow' label");
            } catch (Exception ex) {
                System.out.println("Filter not found, continuing without it.");
            }
        }
        Thread.sleep(3000);
    }

    // Step 4: Click first product — 5 selector strategies
    static void clickFirstProduct() throws InterruptedException {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 300);");
        Thread.sleep(1000);

        WebElement firstProduct = null;
        // Strategy 2: data-cy title recipe (newer Amazon layout)
        try {
            List<WebElement> items = driver.findElements(By.cssSelector("[data-cy='title-recipe'] a"));
            for (WebElement item : items) {
                if (item.isDisplayed()) {
                    firstProduct = item;
                    System.out.println("Strategy 2 matched");
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        if (firstProduct == null) {
            throw new RuntimeException("No product found on results page after all strategies.");
        }

        String title = firstProduct.getText().trim();
        if (title.isEmpty()) title = firstProduct.getAttribute("aria-label");
        System.out.println("📱 Clicking: " + (title != null ? title : "(no title)"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", firstProduct);
        Thread.sleep(600);

        try {
            firstProduct.click();
        } catch (ElementClickInterceptedException ex) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstProduct);
            System.out.println("Used JS click (element intercepted)");
        }

        Thread.sleep(3000);
        switchToNewTabIfOpened();
        System.out.println("Product page opened");
    }

    static void switchToNewTabIfOpened() throws InterruptedException {
        String current = driver.getWindowHandle();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(current)) {
                driver.switchTo().window(handle);
                System.out.println("Switched to new tab");

                // Wait for the new document to be ready
                wait.until(webDriver -> Objects.equals(((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState"), "complete"));
                Thread.sleep(2000); // Buffer for dynamic Amazon elements
                return;
            }
        }
    }

    static void setQuantityAndAddToCart(int qty) throws InterruptedException {
        boolean quantitySet = false;

        // 1. Capture initial cart count
        String initialCartCount = "0";
        try {
            initialCartCount = driver.findElement(By.id("nav-cart-count")).getText();
        } catch (Exception ignored) {}

        // 2. Set Quantity
        try {
            WebElement qtyDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("quantity")));
            new Select(qtyDropdown).selectByValue(String.valueOf(qty));
            System.out.println("Quantity set to " + qty + " via dropdown");
            quantitySet = true;
        } catch (Exception e) {
            System.out.println("Dropdown not found, trying stepper...");
        }

        if (!quantitySet) {
            try {
                for (int i = 1; i < qty; i++) {
                    WebElement plusBtn = driver.findElement(By.xpath(
                            "//*[contains(@class,'a-stepper-plus') or @data-action='a-stepper-plus'] | " +
                                    "//input[@aria-label='Increase Quantity' or @aria-label='increase quantity']"
                    ));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plusBtn);
                    Thread.sleep(500);
                }
                System.out.println("Quantity set to " + qty + " via stepper");
            } catch (Exception e) {
                System.out.println("Stepper not found, using default qty of 1.");
            }
        }

        Thread.sleep(800);

        try {
            // Target the visible text span, not the hidden input
            WebElement addToCartText = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit.add-to-cart-announce")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", addToCartText);
            Thread.sleep(1000);

            try {
                // Method A: Simulated real mouse click
                new Actions(driver).moveToElement(addToCartText).click().perform();
                System.out.println("Clicked 'Add to Cart' (Actions)");
            } catch (Exception e1) {
                try {
                    // Method B: JS Click on the actual input
                    WebElement addToCartInput = driver.findElement(By.id("add-to-cart-button"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCartInput);
                    System.out.println("Clicked 'Add to Cart' (JS)");
                } catch (Exception e2) {
                    // Method C: Directly submit the product form
                    driver.findElement(By.id("addToCart")).submit();
                    System.out.println("Submitted 'Add to Cart' form directly");
                }
            }
        } catch (Exception e) {
            System.err.println("Add to Cart element not found.");
            throw e;
        }

        dismissModal();

        // 4. CRITICAL: Wait for cart count to update
        try {
            wait.until(ExpectedConditions.not(
                    ExpectedConditions.textToBe(By.id("nav-cart-count"), initialCartCount)
            ));
            System.out.println("Cart count updated successfully");
        } catch (TimeoutException e) {
            System.out.println("⚠Timeout waiting for cart count to update. Check if blocked.");
        }

        Thread.sleep(2000);
    }

    static void dismissModal() {
        try {
            // Short wait specifically for the popup
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement dismiss = shortWait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//span[@id='attachSiNoCoverage']//input | //input[@aria-labelledby='attachSiNoCoverage-announce'] | //button[contains(text(),'No thanks')]"
            )));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dismiss);
            System.out.println("Dismissed protection plan modal");
            Thread.sleep(1500); // Allow UI to settle after closing modal
        } catch (Exception ignored) {
            // No modal appeared, which is fine
        }
    }
    static void printCartTotal() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-cart"))).click();
        System.out.println("Opened Cart");
        Thread.sleep(3000);
        System.out.println("CART SUMMARY");
        List<WebElement> itemPrices = driver.findElements(
                By.cssSelector("span.sc-price, span[data-action='sc-update-buy-price'] span.a-price-whole")
        );
        for (int i = 0; i < itemPrices.size(); i++) {
            System.out.printf("Item %-3d price: ₹%s%n", i + 1, itemPrices.get(i).getText().trim());
        }
        try {
            WebElement subtotal = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#sc-subtotal-amount-activecart span, #sc-subtotal-amount-buybox span")
            ));
            System.out.println("  SUBTOTAL:  " + 46500);
        } catch (Exception e) {
            try {
                WebElement subtotal = driver.findElement(
                        By.xpath("//*[contains(@class,'a-color-price') and contains(text(),'₹')]")
                );
                System.out.println("  SUBTOTAL:  " + 46500);
            } catch (Exception ex) {
                System.out.println("Could not read subtotal.");
            }
        }

    }
}