import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "src/drivers/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @AfterClass
    public void tearDown() throws InterruptedException {
        Thread.sleep(10000); // To observe the results visually (not recommended for actual test automation)
        driver.quit();
    }

    public void waitPageChange(String previousUrl) {
        // This is for waiting for the page change
        wait.until(d -> !d.getCurrentUrl().equals(previousUrl));
    }

    @Test
    public void testLogin() {
        // Open the Periplus homepage
        driver.get("https://www.periplus.com/");

        // Locate and click the "Sign In" button
        WebElement signInButton = driver.findElement(By.cssSelector("#nav-signin-text a"));
        signInButton.click();

        // Locate and fill the email and password inputs
        WebElement emailField = driver.findElement(By.name("email"));
        WebElement passwordField = driver.findElement(By.id("ps"));

        emailField.sendKeys("emailbuator@gmail.com");
        passwordField.sendKeys("Testaccount123");

        // Locate and click the Login button
        WebElement loginButton = driver.findElement(By.id("button-login"));
        loginButton.click();

        waitPageChange("https://www.periplus.com/account/Login");
    }

    @Test
    public void testAddToCart() {
        driver.get("https://www.periplus.com/");

        WebElement searchButton = driver.findElement(By.cssSelector("button.btnn"));
        searchButton.click();

        // Locate the first product's link
        WebElement firstProductLink = driver.findElement(By.cssSelector(".single-product a"));
        String productLink = firstProductLink.getAttribute("href");
        firstProductLink.click();

        // Locate and click the "ADD TO CART" button
        WebElement addToCartButton = driver.findElement(By.cssSelector("button.btn-add-to-cart"));
        addToCartButton.click();

        // Wait for the popup to appear and then disappear
        By popupSelector = By.cssSelector("div.modal-body");
        wait.until(ExpectedConditions.visibilityOfElementLocated(popupSelector)); // Wait for the popup to be visible
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupSelector)); // Wait for the popup to disappear

        // Locate and click the "SHOPPING CART" link
        WebElement shoppingCart = driver.findElement(By.id("show-your-cart"));
        shoppingCart.click();

        // Locate all product rows in the shopping cart
        List<WebElement> productRows = driver.findElements(By.cssSelector("div.row-cart-product"));

        // Ensure there is at least one product
        Assert.assertFalse(productRows.isEmpty(), "No products found in the shopping cart.");

        // Get the last product row
        WebElement lastProductRow = productRows.get(productRows.size() - 1);

        // Locate the <a> tag inside the last product row
        WebElement productLinkElement = lastProductRow.findElement(By.cssSelector("a[href]"));
        String selectedProductLink = productLinkElement.getAttribute("href");

        // Check if the product link is the same as the previous product link
        String regex = "/p/(\\d+)";
        Pattern pattern = Pattern.compile(regex);

        // Extract ID from the first link
        Matcher matcher1 = pattern.matcher(productLink);
        String firstProductId = null;
        if (matcher1.find()) {
            firstProductId = matcher1.group(1);
        }

        // Extract ID from the selected link
        Matcher matcher2 = pattern.matcher(selectedProductLink);
        String selectedProductId = null;
        if (matcher2.find()) {
            selectedProductId = matcher2.group(1);
        }

        // Compare the IDs and assert the result
        Assert.assertNotNull(firstProductId, "First product ID is null.");
        Assert.assertEquals(firstProductId, selectedProductId, "The product IDs do not match.");
    }
}
