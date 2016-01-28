package com.pramati.scraper.google_grp_scraper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import com.pramati.scraper.util.RecoveryUtil;

public class CollectLink {
	private Set<String> collectedLinkSet = new HashSet<String>();
	private BlockingQueue<String> linksSharedQueueForDownload = new ArrayBlockingQueue<String>(
			100000);
	private URL urlOfGrp;
	private String groupName;
	private int noOfWorkers;
	private String downloadDirectory = "Download";
	private String directorySeparator = "/";
	private RecoveryUtil recoveryUtil = new RecoveryUtil();
	private Set<String> recoveredLinks;

	public void init(URL url, int noOfWorker) throws Exception {
		this.urlOfGrp = url;
		this.noOfWorkers = noOfWorker;
		setGroupName(urlOfGrp);
	}

	public void scrap() throws InterruptedException {
		WebDriver groupBrowser = new FirefoxDriver();
		this.startDownloader();
		this.startCrawl(groupBrowser);
	}

	@SuppressWarnings("static-access")
	private void startCrawl(WebDriver groupBrowser) throws InterruptedException {
		groupBrowser.navigate().to(urlOfGrp);
		this.performFailureRecovery();

		Thread.currentThread().sleep(5000);
		Actions clickAction = new Actions(groupBrowser);
		WebElement scrollablePane = groupBrowser.findElement(By
				//.className("G3J0AAD-b-F"));
				.className("IVILX2C-b-D"));
		clickAction.moveToElement(scrollablePane).click().build().perform();

		Set<String> links = null;
		boolean shouldContinueScroll;
		do {
			Actions scrollAction = new Actions(groupBrowser);
			scrollAction.keyDown(Keys.CONTROL).sendKeys(Keys.END).perform();
			Thread.currentThread().sleep(5000);
			links = getNewLinksFromPage(groupBrowser.getPageSource());
			shouldContinueScroll = (links.size() > 0 ? true : false);
			links.removeAll(recoveredLinks);
			linksSharedQueueForDownload.addAll(links);
		} while (shouldContinueScroll);

		for (int i = 0; i < noOfWorkers; i++) {
			linksSharedQueueForDownload.add("POISON");
		}
		Thread.currentThread().sleep(5000);
		groupBrowser.close();
	}

	private void performFailureRecovery() {
		recoveredLinks = recoveryUtil.getDownloadedLinks(downloadDirectory
				+ directorySeparator + groupName);

	}

	private Set<String> getNewLinksFromPage(String pageContent) {
		String hyperlinkRegex = "\\s*(?i)href\\s*=\\s*\"(#%21topic(.*?))\"";
		Set<String> links = new HashSet<String>();
		Pattern pattern = Pattern.compile(hyperlinkRegex);
		Matcher matcher = pattern.matcher(pageContent);
		while (matcher.find()) {
			String str = matcher.group(1);
			if (str != null && str.length() > 0) {
				String completeUrlString = getCompleteUrlFromHyperlink(str);
				if (completeUrlString != null
						&& !collectedLinkSet.contains(completeUrlString)) {
					links.add(completeUrlString);
					collectedLinkSet.add(completeUrlString);
				}
			}
		}
		return links;
	}

	private void setGroupName(URL urlOfGrp) throws Exception {
		String grpNameRegex = "https://groups.google.com/forum/#(.*?)forum/(.*?)";
		Set<String> links = new HashSet<String>();
		Pattern pattern = Pattern.compile(grpNameRegex);
		Matcher matcher = pattern.matcher(urlOfGrp.toString());
		if (matcher.matches()) {
			groupName = matcher.group(2);
		} else {
			throw new Exception("INVALID GROUP URL");
		}
	}

	private String getCompleteUrlFromHyperlink(String replace) {
		URL completeUrl = null;
		try {
			completeUrl = new URL(urlOfGrp, replace);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return (completeUrl != null) ? completeUrl.toString() : null;
	}

	private void startDownloader() {
		DownloadWorker downloadWorker = null;
		for (int i = 0; i < noOfWorkers; i++) {
			downloadWorker = new DownloadWorker(linksSharedQueueForDownload,
					groupName);
			Thread dowloaderThread = new Thread(downloadWorker);
			dowloaderThread.start();
		}
	}
}
