package com.example.Testing.Assignments;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;

public class TestingAssignment1 {

    public static void main(String[] args) {
        // Dummy data
        String[] laptopNames = {"MacBook Pro", "Dell XPS", "HP Pavilion"};

        System.setProperty("webdriver.edge.driver", "C:\\edge_driver\\msedgedriver.exe");

        WebDriver driver = new EdgeDriver();

        try {
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            for (String laptop : laptopNames) {
                runTestScenario(driver, laptop);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static void runTestScenario(WebDriver driver, String laptopName) {
        driver.get("https://www.amazon.com");

        try {
            //Select Electronics
            WebElement dropdown = driver.findElement(By.id("searchDropdownBox"));
            new Select(dropdown).selectByVisibleText("Electronics");

            //Searching
            WebElement searchBox = driver.findElement(By.id("twotabsearchtextbox"));
            searchBox.clear();
            searchBox.sendKeys(laptopName);
            driver.findElement(By.id("nav-search-submit-button")).click();

            WebElement resultElement = driver.findElement(By.xpath("//h1[contains(@class,'s-desktop-toolbar')]//span[contains(text(),'results')]"));
            String resultText = resultElement.getText();

            int totalResults = 0;

            if (resultText.contains("of")) {
                String cleanCount = resultText.split("of")[1].replaceAll("[^0-9]", "");
                totalResults = Integer.parseInt(cleanCount);
            } else {
                // Fallback
                totalResults = Integer.parseInt(resultText.replaceAll("[^0-9]", ""));
            }

            System.out.println("Input: " + laptopName + " | Raw Text: " + resultText + " | Parsed: " + totalResults);

            if (totalResults > 10) {
                System.out.println("PASS: > 10 results.");
            } else {
                System.out.println("FAIL: <= 10 results.");
            }

        } catch (Exception e) {
            System.out.println("Error processing " + laptopName + ": " + e.getMessage());
        }
        System.out.println("Happy Testing - Shreya Moulick");
    }
}