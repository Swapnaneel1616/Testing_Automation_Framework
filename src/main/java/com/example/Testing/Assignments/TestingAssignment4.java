package com.example.Testing.Assignments;

import org.apache.poi.xssf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.*;

import java.io.FileOutputStream;
import java.time.Duration;
import java.util.List;

public class TestingAssignment4 {

    static WebDriver driver;
    static WebDriverWait wait;

    public static void main(String[] args) {
        System.setProperty("webdriver.edge.driver", "C:\\edge_driver\\msedgedriver.exe");
        driver = new EdgeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Test Data: {Search Term, Category, Quantity}
        String[][] testData = {
                {"iPhone 15", "Electronics", "2"},
                {"Atomic Habits", "Books", "1"},
                {"Sony Headphones", "Electronics", "1"}
        };

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Test Results");
            XSSFRow header = sheet.createRow(0);
            header.createCell(0).setCellValue("Product");
            header.createCell(1).setCellValue("Category");
            header.createCell(2).setCellValue("Qty");
            header.createCell(3).setCellValue("Status");

            driver.get("https://www.amazon.in");

            for (int i = 0; i < testData.length; i++) {
                String product = testData[i][0];
                String category = testData[i][1];
                int qty = Integer.parseInt(testData[i][2]);
                String status = "Success";

                try {
                    searchWithCategorySwitch(product, category);
                    selectFirstProductAndSwitchTab();
                    setQuantityAndAddToCart(qty);
                } catch (Exception e) {
                    status = "Failed: " + e.getClass().getSimpleName();
                }

                // Write to Excel row
                XSSFRow row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(product);
                row.createCell(1).setCellValue(category);
                row.createCell(2).setCellValue(qty);
                row.createCell(3).setCellValue(status);

                System.out.println("📝 Logged: " + product + " | " + status);

                // Return to main window for next iteration
                closeExtraTabs();
                driver.get("https://www.amazon.in");
            }

            // Save Excel File
            try (FileOutputStream fos = new FileOutputStream("AmazonTestResults.xlsx")) {
                workbook.write(fos);
                System.out.println("✅ Results saved to AmazonTestResults.xlsx");
            }

        } catch (Exception e) {
            System.err.println("Execution failed.");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    static void searchWithCategorySwitch(String product, String category) {
        WebElement categoryDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("searchDropdownBox")));
        Select select = new Select(categoryDropdown);

        // Switch case for categories
        switch (category.toLowerCase()) {
            case "electronics":
                select.selectByVisibleText("Electronics");
                break;
            case "books":
                select.selectByVisibleText("Books");
                break;
            default:
                select.selectByVisibleText("All Categories");
                break;
        }

        WebElement searchBox = driver.findElement(By.id("twotabsearchtextbox"));
        searchBox.clear();
        searchBox.sendKeys(product, Keys.ENTER);
    }

    static void selectFirstProductAndSwitchTab() throws InterruptedException {
        String mainWindow = driver.getWindowHandle();

        WebElement firstItem = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("div[data-component-type='s-search-result'] h2 a")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", firstItem);
        Thread.sleep(500);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstItem);

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(mainWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    static void setQuantityAndAddToCart(int qty) throws InterruptedException {
        if (qty > 1) {
            try {
                WebElement qtyDropdown = driver.findElement(By.id("quantity"));
                new Select(qtyDropdown).selectByValue(String.valueOf(qty));
            } catch (Exception e) {
                // Fallback to stepper if dropdown is missing
                for (int i = 1; i < qty; i++) {
                    try {
                        WebElement plusBtn = driver.findElement(By.xpath("//*[@data-action='a-stepper-plus']"));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plusBtn);
                        Thread.sleep(400);
                    } catch (Exception ex) { break; }
                }
            }
        }

        Thread.sleep(1000);

        WebElement addToCart = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//input[@id='add-to-cart-button'] | //button[@id='add-to-cart-button']")
        ));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addToCart);

        Thread.sleep(3000); // Wait for cart processing
    }

    static void closeExtraTabs() {
        String originalHandle = driver.getWindowHandles().iterator().next();
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        driver.switchTo().window(originalHandle);
    }
}