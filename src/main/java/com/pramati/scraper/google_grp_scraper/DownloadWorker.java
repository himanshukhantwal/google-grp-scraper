package com.pramati.scraper.google_grp_scraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.pramati.scraper.util.FileUtil;
import com.pramati.scraper.util.RecoveryUtil;

public class DownloadWorker implements Runnable {

	private BlockingQueue<String> linksSharedQueueForDownload;
	private WebClient client;
	private String downloadDirectory = "Download";
	private String failureRecoveryDirectory = "Recovery";
	private String topicDirectory = "Topics";
	private String directorySeparator = "/";
	private FileUtil fileUtil = new FileUtil();
	private String groupName;
	private RecoveryUtil recoveryUtil = new RecoveryUtil();

	public DownloadWorker(
			BlockingQueue<String> topicLinksSharedQueueForDownload,
			String groupName) {
		this.linksSharedQueueForDownload = topicLinksSharedQueueForDownload;
		this.groupName = groupName;
	}

	public void run() {
		createClient();
		while (true) {
			String linkForDownload = null;
			try {
				linkForDownload = linksSharedQueueForDownload.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (linkForDownload.equalsIgnoreCase("POISON")) {
				break;
			}
			if (linkForDownload != null) {
				download(linkForDownload);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void download(String linkForDownload) {

		WebRequest webReq;
		String titleOfTopic = "";
		String contentOfTopic = "";
		List<HtmlSpan> spanElement = null;
		List<HtmlDivision> divElement = null;
		try {
			webReq = new WebRequest(new URL(linkForDownload));
			HtmlPage page = client.getPage(webReq);

			spanElement = (List<HtmlSpan>) page
					.getByXPath("//span[@id=\"t-t\"]");
			divElement = (List<HtmlDivision>) page
					.getByXPath("//div[@class=\"G3J0AAD-nb-P\"]");

			if (!spanElement.isEmpty()) {
				HtmlSpan span = (HtmlSpan) spanElement.get(0);
				titleOfTopic = span.asText();
				contentOfTopic = "SUBJECT IS :" + titleOfTopic + "\n\n";
			}

			for (HtmlDivision div : divElement) {
				if (div != null && !div.asText().equals("")
						&& div.asText() != null) {
					contentOfTopic = contentOfTopic + div.asText() + "\n";
					contentOfTopic = contentOfTopic
							+ "----------------------------------------------------------------------------\n";
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (!titleOfTopic.equals("")) {
				if (titleOfTopic.length() > 25) {
					titleOfTopic = titleOfTopic.substring(0, 20);
				}
				fileUtil.createFileAndWriteTxt(titleOfTopic, downloadDirectory
						+ directorySeparator + groupName + directorySeparator
						+ topicDirectory, contentOfTopic);

				recoveryUtil.maintainRecoveryList(downloadDirectory
						+ directorySeparator + groupName, linkForDownload);
			} else {
				System.out.println("Nothing to download");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.closeAllWindows();
	}

	private void createClient() {
		client = new WebClient(BrowserVersion.FIREFOX_24);
		client.getOptions().setJavaScriptEnabled(true);
		client.getOptions().setRedirectEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setUseInsecureSSL(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.setAjaxController(new NicelyResynchronizingAjaxController());
		client.setJavaScriptTimeout(36000);
	}

}
