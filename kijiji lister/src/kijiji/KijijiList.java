package kijiji;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

public class KijijiList {

	public static final String DIRECTORY = System.getProperty("user.dir") + "\\kijiji\\";
	//private static final String DIRECTORY = "D:\\My stuff\\Dropbox\\kijiji\\";
	public static final String LOCATION = "Waterloo, ON";
	private static WebDriver driver;

	public static void main(String[] args) throws IOException {

		ArrayList<Postable> postings = getlistings();
		int postId = getPostId();
		Postable myPosting;

		// If posting is undefined (out of bounds, go back to the first posting)
		try {
			myPosting = postings.get(postId);
			myPosting.getTitle();
		} catch (IndexOutOfBoundsException e) {
			postId = 0;
			myPosting = postings.get(postId);
		}

		System.out.println("Posting: " + myPosting.getTitle());
		post(myPosting);
		savePostId(postId);

	}

	// Updates postinglist.txt to minimze reposting the same item twice
	private static void savePostId(int postId) {
		postId++;
		PrintWriter p;
		try {
			p = new PrintWriter(new File(DIRECTORY + "posting-list.txt"));
			p.println(postId);
			p.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// postinglist.txt tells us which item to post
	private static int getPostId() {
		Scanner s;
		try {
			s = new Scanner(new File(DIRECTORY + "posting-list.txt"));
			int i = s.nextInt();
			s.close();
			return i;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}

	// Gets all the postable items in the .xlsx file
	private static ArrayList<Postable> getlistings() {
		ArrayList<Postable> postings = new ArrayList<Postable>();
		try {

			// Get the xlsx file of items
			FileInputStream file = new FileInputStream(new File(DIRECTORY
					+ "kijiji.xlsx"));
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			XSSFSheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			// Ignore first row
			if (rowIterator.hasNext()) {
				rowIterator.next();
			}

			while (rowIterator.hasNext()) {
				// Prepare item for processing
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				Postable tempPost = new Postable(new ArrayList<String>(),
						new ArrayList<String>());
				postings.add(tempPost);

				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_NUMERIC:
						tempPost.details.add(String.valueOf(Integer
								.toString((int) cell.getNumericCellValue())));
						break;

					case Cell.CELL_TYPE_STRING:
						tempPost.details.add(cell.getStringCellValue());
						break;
					}

					if (tempPost.details.size() > 9) {
						// System.out.println(cell.getStringCellValue());
						tempPost.photos.add(cell.getStringCellValue());
					}

				}
			}

			file.close();

		} catch (Exception e) {
			System.out.println(e);
		}
		return postings;
	}

	public static void post(Postable item) {

		// Open Kijiji
		driver = new FirefoxDriver();
		driver.manage().window().maximize();
		driver.get("http://www.kijiji.ca/");

		// Login to Kijiji
		driver.findElement(By.id("SignInLink")).click();
		driver.findElement(By.id("LoginEmailOrNickname")).sendKeys(
				item.getEmail());
		driver.findElement(By.id("login-password")).sendKeys(item.getPass());
		driver.findElement(By.id("SignInButton")).click();
		sleep(4000);

		// Delete All Ads
		deleteAd(driver);

		// Pick the category the ad is located in
		driver.get("http://www.kijiji.ca/p-select-category.html");

		// Kijiji has 2 different ways to pick category. Try the 2nd option if
		// first option fails
		try {
			categoryPickerOne(item);
		} catch (NoSuchElementException ex) {
			categoryPickerTwo(item);
		}

		// Select city (if required to submit that info)
		try {
			driver.findElement(By.xpath("//*[@data-loc-id='9004']")).click();
			driver.findElement(By.xpath("//*[@data-loc-id='1700209']")).click();
			driver.findElement(By.xpath("//*[@data-loc-id='1700212']")).click();
			driver.findElement(By.id("LocUpdate")).click();
		} catch (NoSuchElementException ex) {
		}

		// Set the title
		if (item.getTitle().contains("Wanted: ")) {
			item.setTitle(item.getTitle().replace("Wanted: ", ""));
			driver.findElement(By.id("adType2")).click();
		}
		driver.findElement(By.id("postad-title")).sendKeys(item.getTitle());

		// Set price
		if (item.getPrice().contains("contact")) {
			driver.findElement(By.id("priceType3")).click();
		} else if (item.getPrice().contains("free")) {
			driver.findElement(By.id("priceType2")).sendKeys(item.getPrice());
		} else if (item.getPrice().contains("priceType4")) {
			driver.findElement(By.id("priceAmount")).sendKeys(item.getPrice());
		} else {
			driver.findElement(By.id("priceAmount")).sendKeys(item.getPrice());
		}

		// Select dropdown options.
		if (!item.getDropdown1().equals("none")) {
			WebElement dropdown1element = driver.findElements(
					By.xpath("//div[@class='form-section']/select")).get(0);
			Select dropdown1select = new Select(dropdown1element);
			dropdown1select.selectByVisibleText(item.getDropdown1());
		}
		if (!item.getDropdown2().equals("none")) {
			WebElement dropdown2element = driver.findElements(
					By.xpath("//div[@class='form-section']/select")).get(1);
			Select dropdown2select = new Select(dropdown2element);
			dropdown2select.selectByVisibleText(item.getDropdown2());
		}

		// For sale by owner
		driver.findElement(By.id("forsaleby_s")).click();

		// Ad description
		driver.findElement(By.id("pstad-descrptn")).sendKeys(
				item.getDescription());

		// Upload Photos
		if (item.hasPhotos()) {
			try {
				uploadImages(item.getPhotos(), DIRECTORY);
			} catch (Exception e) {
			}
		}

		// Location of Ad
		driver.findElement(By.id("pstad-map-address")).sendKeys(LOCATION);

		// Submit form
		driver.findElement(By.id("pstad-map-address")).submit();

		sleep(6000);

		// Refresh the page
		String URL = driver.getCurrentUrl();
		driver.get(URL);

		// Signout
		driver.get("kijiji.ca//m-logout.html");
		sleep(2000);

		// Close browser
		driver.close();

	}

	// Kijiji has two styles of category selectors, this selects it either way
	private static void categoryPickerTwo(Postable item) {
		WebElement selectMenu = driver.findElement(By
				.id("CategoryManualSelectDrilldownMenu"));
		selectMenu.findElement(By.linkText("buy and sell")).click();
		selectMenu.findElement(By.linkText(item.getCategory1())).click();
		if (!item.getCategory2().equals("none")) {
			selectMenu.findElement(By.linkText(item.getCategory2())).click();
		}
	}

	// Kijiji has two styles of category selectors, this selects it either way
	private static void categoryPickerOne(Postable item) {
		driver.findElement(By.id("Categories"))
				.findElement(By.linkText(item.getCategory1())).click();
		if (!item.getCategory2().equals("none")) {
			driver.findElement(By.id("Categories"))
					.findElement(By.linkText(item.getCategory2())).click();
		}

	}

	private static boolean deleteAd(WebDriver driver) {
		try {
			driver.get("https://www.kijiji.ca/m-my-ads.html");
			sleep(1200);
			int idx = driver.getPageSource().indexOf("id=\"delete-ad-");
			while (idx != -1) {
				String listNumber = (driver.getPageSource().substring(idx + 14,
						idx + 24));
				driver.findElement(By.id("delete-ad-" + listNumber)).click();
				driver.findElement(
						By.xpath("//*[@value = 'FOUND_BUYER_ON_KIJIJI']"))
						.click();
				driver.findElement(By.id("DeleteSurveyOK")).click();
				sleep(500);
				idx = driver.getPageSource().indexOf("id=\"delete-ad-");
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	private static void sleep(int miliseconds) {
		try {
			Thread.sleep(miliseconds);
		} catch (Exception e) {
		}

	}

	private static void uploadImages(List pics, String baseDir)
			throws AWTException {

		Robot robot = new Robot();

		Iterator it = pics.iterator();
		String photoDir = "";
		String uploadString = "";

		// Construct string consisting of photo base directory
		if (pics.size() > 1) {
			photoDir = baseDir + it.next();
		} else {
			photoDir = baseDir;
		}

		// Create string for photos to upload
		while (it.hasNext()) {
			uploadString = uploadString + "\"" + it.next() + "\" ";
		}

		// Open image upload window
		driver.findElement(By.id("ImageUploadButton")).click();

		// Photo directory copied to the clipboard
		StringSelection stringSelection = new StringSelection(photoDir);
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(stringSelection, null);
		sleep(1000);
		// Robot pastes the directory into upload window w/ control+v
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		sleep(500);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_ENTER);
		sleep(500);
		robot.keyRelease(KeyEvent.VK_ENTER);
		sleep(1000);

		// Product photos get saved to clipboard
		StringSelection stringSelection2 = new StringSelection(uploadString);
		Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(stringSelection2, null);
		sleep(1000);
		// Robot pastes the photos into upload window w/ control+v
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		sleep(500);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_ENTER);
		sleep(500);
		robot.keyRelease(KeyEvent.VK_ENTER);

		// Wait for everything to upload
		sleep(pics.size() * 3000 + 15000);
	}
}