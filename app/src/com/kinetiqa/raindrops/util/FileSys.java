package com.kinetiqa.raindrops.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileSys {
	/**
	 * Given a parent directory, return a list of files with the given extension
	 * 
	 * @param parentDir
	 * @param extension
	 * @return
	 */
	public static List<File> getListOfFiles(File parentDir, String extension) {
		ArrayList<File> inFiles = new ArrayList<File>();
		File[] files = parentDir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				inFiles.addAll(getListOfFiles(file, extension));
			} else {
				if (file.getName().endsWith(extension)) {
					inFiles.add(file);
				}
			}
		}
		return inFiles;
	}
}
