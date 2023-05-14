import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.Duration;

import static org.awaitility.Awaitility.await;

public class Auto {

    @FindBy(css = ".mvp-fonts.mvp-fonts-play")
    public WebElement playButton;

    @FindBy(css = ".mvp-volume-control-btn")
    public WebElement audio;

    @FindBy(css = ".mvp-play-rate-btn")
    public WebElement rate;

    @FindBy(xpath = "//div[contains(text(),'2.0X')]")
    public WebElement rate2;

    @FindBy(css = ".next.ng-binding.ng-scope")
    public WebElement nextButton;

    @FindBy(css = "video")
    public WebElement video;

    static RemoteWebDriver chromeDriver;

    public static void main(String[] args) throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("debuggerAddress", "localhost:9222");
        chromeDriver = new ChromeDriver(chromeOptions);
        Auto auto = new Auto();
        PageFactory.initElements(chromeDriver, auto);
        try {
            auto.start();
        } finally {
            chromeDriver.quit();
        }
    }//https://nodejs.org/en/docs/guides/debugging-getting-started
    //https://chromedevtools.github.io/devtools-protocol/v8/


    public void start() throws Exception {
        int ex = 0;
        while (haseNext()) {
            ex--;
            if (isVideo() && !isPlayed()) {
                ex = 0;
                play();
            }
            if (ex < -10) { // 如果反复往下多次没有找到视频资源，可能需要人为答题
                break;
            }
            next();
        }
        if (isVideo() && !isPlayed()) {
            play();
        }


    }

    public void play() {
        WebElement element = chromeDriver.findElement(By.cssSelector(".title.ng-binding"));
        String currentTime = video.getAttribute("currentTime");
        String duration = video.getAttribute("duration");
        if (currentTime.equals("0") || !currentTime.equals(duration)) {
            System.out.println("current playing:" + element.getText());
            dialogProcess();
            await().alias("播放按钮可以点击").ignoreException(NoSuchElementException.class).until(() -> {
                return playButton != null && playButton.isEnabled() && playButton.isDisplayed();
            });
            playButton.click();
            audio.click();
            rate.click();
            rate2.click();
            await().pollDelay(Duration.ofSeconds(10))
                    .atMost(Duration.ofMinutes(30))
                    .until(this::isPlayed);
            await().until(() -> Boolean.parseBoolean(video.getAttribute("paused")));
            System.out.println("playing done:" + element.getText());
        }
    }

    public boolean haseNext() {
        try {
            nextButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void next() {
        dialogProcess();
        nextButton.click();

    }

    public boolean isVideo() {
        try {
            chromeDriver.findElement(By.cssSelector("video")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isPlayed() {
        try {
            await().until(() -> StringUtils.isNoneBlank(video.getAttribute("currentTime")));
            await().until(() -> StringUtils.isNoneBlank(video.getAttribute("duration")));
            String currentTime = video.getAttribute("currentTime").split("\\.")[0];
            return currentTime.equals(video.getAttribute("duration").split("\\.")[0]);
        } catch (Exception e) {
            return false;
        }

    }

    public void dialogProcess() {
        chromeDriver.findElements(By.cssSelector(".form-buttons > button")).stream()
                .filter(WebElement::isDisplayed).findFirst().ifPresent(WebElement::click);
    }
}
