package com.kinetiqa.raindrops.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

import android.os.Environment;

public class Writer {
	/**
	 * Writes a given JSON object to file 
	 * @param folder
	 * @param prefix
	 * @param randomPrefix
	 * @param jsonObj
	 */
	public static String writeJSONToFile(String folder, String prefix,
			boolean randomPrefix, JSONObject jsonObj) {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/raindrops/tmp/"
				+ folder);
		dir.mkdirs();
		
		String fullFilePathPrefix = sdCard.getAbsolutePath()
				+ "/raindrops/tmp/" + folder + "/" + prefix
				+ System.currentTimeMillis() + ".json";
		String fullFilePathNoRandomPrefix = sdCard.getAbsolutePath()
				+ "/raindrops/tmp/" + folder + "/" + prefix
				+ ".json";
		
		File nomedia = new File(dir, ".nomedia");
		try {
			nomedia.createNewFile();

			FileWriter file;
			if (randomPrefix) {
				file = new FileWriter(fullFilePathPrefix);
			} else {
				file = new FileWriter(fullFilePathNoRandomPrefix);
			}
			file.write(jsonObj.toString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		

		if (randomPrefix) {
			return fullFilePathPrefix;
		} else {
			return fullFilePathNoRandomPrefix; 
		}
		
	}
	
	public static void writeStringToFile(String folder, String prefix,
			boolean randomPrefix, String stringToWrite) {
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/raindrops/tmp/"
				+ folder);
		dir.mkdirs();

		File nomedia = new File(dir, ".nomedia");
		try {
			nomedia.createNewFile();

			FileWriter file;
			if (randomPrefix) {
				file = new FileWriter(sdCard.getAbsolutePath()
						+ "/raindrops/tmp/" + folder + "/" + prefix
						+ System.currentTimeMillis() + ".txt");
			} else {
				file = new FileWriter(sdCard.getAbsolutePath()
						+ "/raindrops/tmp/" + folder + "/" + prefix
						+ ".txt");
			}
			file.write(stringToWrite.toString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
