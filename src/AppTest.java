import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
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

    @Test(dependsOnMethods = "testLogin")
    public void testAddToCartWithEdgeCase() {
        driver.get("https://www.periplus.com/");
    
        // Locate the cart total count before adding the product
        WebElement cartTotalBefore = driver.findElement(By.cssSelector("#cart_total"));
        int totalCountBefore = Integer.parseInt(cartTotalBefore.getText());
    
        // Perform a product search and locate the first product
        WebElement searchButton = driver.findElement(By.cssSelector("button.btnn"));
        searchButton.click();
    
        // Click the first product's link and get its ID
        WebElement firstProductLink = driver.findElement(By.cssSelector(".single-product a"));
        String productLink = firstProductLink.getAttribute("href");
        firstProductLink.click();
    
        // Extract the product ID from the link
        String productIdRegex = "/p/(\\d+)";
        Pattern pattern = Pattern.compile(productIdRegex);
        Matcher matcher = pattern.matcher(productLink);
        String productId = null;
        if (matcher.find()) {
            productId = matcher.group(1);
        }
        Assert.assertNotNull(productId, "Product ID is null.");
    
        // Open the shopping cart dropdown to check if the product is already in the cart
        WebElement shoppingCart = driver.findElement(By.id("show-your-cart"));
        Actions actions = new Actions(driver);
        actions.moveToElement(shoppingCart).perform();
        System.out.println("move to cart");

        // Locate the list of cart items
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".shopping-list li"));
        boolean productAlreadyInCart = false;
        int initialProductQuantity = 0;

        for (WebElement item : cartItems) {
            // Find the product link within the cart item
            WebElement productLinkElement = item.findElement(By.cssSelector("a.cart-img"));
            String cartProductLink = productLinkElement.getAttribute("href");
    
            // Check if this item's link matches the product's link
            if (cartProductLink.contains(productId)) {
                productAlreadyInCart = true;
    
                // Get the initial quantity of this product
                WebElement quantityElement = item.findElement(By.cssSelector(".quantity"));
                initialProductQuantity = Integer.parseInt(quantityElement.getText().split(" ")[0]);
                break;
            }
        }

        System.out.println("productAlreadyInCart: " + productAlreadyInCart);
        System.out.println("initialProductQuantity: " + initialProductQuantity);

        WebElement outside = driver.findElement(By.id("mainImg"));
        actions.moveToElement(outside).perform();
        System.out.println("move outside");

        // Locate and click the "ADD TO CART" button
        System.out.println("find add to cart");
        WebElement addToCartButton = driver.findElement(By.cssSelector("button.btn-add-to-cart"));
        addToCartButton.click();
        System.out.println("clicking add to cart");
    
        // Wait for the popup to appear and then disappear
        By popupSelector = By.cssSelector("div.modal-body");
        wait.until(ExpectedConditions.visibilityOfElementLocated(popupSelector)); // Wait for the popup to be visible
        wait.until(ExpectedConditions.invisibilityOfElementLocated(popupSelector)); // Wait for the popup to disappear
    
        actions.moveToElement(shoppingCart).perform();
        System.out.println("move to cart");

        // Recheck the cart items after adding the product
        cartItems = driver.findElements(By.cssSelector(".shopping-list li"));
        boolean productFoundAfterAddition = false;
        int finalProductQuantity = 0;
    
        for (WebElement item : cartItems) {
            // Find the product link within the cart item
            WebElement productLinkElement = item.findElement(By.cssSelector("a.cart-img"));
            String cartProductLink = productLinkElement.getAttribute("href");
    
            // Check if this item's link matches the product's link
            if (cartProductLink.contains(productId)) {
                productFoundAfterAddition = true;
    
                // Get the final quantity of this product
                WebElement quantityElement = item.findElement(By.cssSelector(".quantity"));
                finalProductQuantity = Integer.parseInt(quantityElement.getText().split(" ")[0]);
                break;
            }
        }

        System.out.println("productFoundAfterAddition: " + productFoundAfterAddition);
        System.out.println("finalProductQuantity: " + finalProductQuantity);

        // Assertions
        if (productAlreadyInCart) {
            // If the product was already in the cart, check if the quantity increased by 1
            Assert.assertTrue(productFoundAfterAddition, "Product was not found in the cart after addition.");
            Assert.assertEquals(finalProductQuantity, initialProductQuantity + 1, "Product quantity did not increase correctly.");
        } else {
            // If the product was not in the cart, check if it is now present with quantity 1
            Assert.assertTrue(productFoundAfterAddition, "Product was not added to the cart.");
            Assert.assertEquals(finalProductQuantity, 1, "Product quantity is not correct for a new addition.");
        }
    
        // Verify the total count has increased by 1
        WebElement cartTotalAfter = driver.findElement(By.cssSelector("#cart_total"));
        int totalCountAfter = Integer.parseInt(cartTotalAfter.getText());
        Assert.assertEquals(totalCountAfter, totalCountBefore + 1, "Cart total count did not increase correctly.");
    }
}
