package com.pramati.scraper.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

	public void createDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				// logger
			} else {
				// logger error
			}
		}
	}

	public void createFileAndWriteTxt(String fileName, String parentDirPath,
			String textTosave) {
		createDir(parentDirPath);
		fileName = fileName.replaceAll("/", "-or-");
		BufferedWriter bw = null;
		File file = new File(parentDirPath, fileName);
		try {
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(textTosave);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void createFileAndAppendContent(String fileName,
			String parentDirPath, String textToappend) {
		createDir(parentDirPath);
		fileName = fileName.replaceAll("/", "-or-");
		BufferedWriter bw = null;
		File file = new File(parentDirPath, fileName);
		try {
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file, true));
			bw.newLine();
			bw.write(textToappend);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
