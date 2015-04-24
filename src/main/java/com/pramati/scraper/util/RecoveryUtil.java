package com.pramati.scraper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class RecoveryUtil {
	private FileUtil fileUtil = new FileUtil();
	private String recoveryFileName = "RecoveryList.txt";
	private String directorySeparator = "/";
	private String failureRecoveryDirectory = "Recovery";

	public Set<String> getDownloadedLinks(String parentDirOfRecoveryDirectory) {
		Scanner reader = null;
		Set<String> fileStrList = new HashSet<String>();
		try {
			reader = new Scanner(new File(parentDirOfRecoveryDirectory
					+ directorySeparator + failureRecoveryDirectory
					+ directorySeparator + recoveryFileName));
			while (reader.hasNext()) {
				String urlString = reader.nextLine().trim();
				if (!urlString.equals("")) {
					fileStrList.add(urlString);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Recovery File [" + parentDirOfRecoveryDirectory
					+ "] does not exist");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				System.err.println("File Loading Failed");
				e.printStackTrace();
			}
		}
		return fileStrList;
	}

	public void maintainRecoveryList(String parentDirOfRecoveryDirectory,
			String content) {
		fileUtil.createFileAndAppendContent(recoveryFileName,
				parentDirOfRecoveryDirectory + directorySeparator
						+ failureRecoveryDirectory, content);

	}
}
