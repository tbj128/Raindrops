package com.kinetiqa.raindrops.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kinetiqa.raindrops.components.Message;
import com.kinetiqa.raindrops.database.DatabaseHelper;
import com.kinetiqa.raindrops.util.TimeConversion;
import com.kinetiqa.raindrops.util.Writer;

public class ConnectionManager extends Activity {

	public static final String URL_LOGIN = "app_check_login.php";
	public static final String URL_INCOMING_MESSAGES = "app_send_messages.php";
	public static final String URL_INCOMING_MESSAGE_MEDIA = "app_send_message_media.php";
	public static final String URL_OUTGOING_MESSAGES = "app_receive_messages.php";
	public static final String URL_INCOMING_PERMISSIONS = "app_send_permissions.php";
	public static final String URL_PROCESS_DATA = "app_upload_data.php";
	public static final String URL_PROCESS_MESSAGE_MEDIA = "app_upload_message_media.php";

	private Context c;
	private SharedPreferences sharedPreferences;
	private File sdCard;

	public ConnectionManager(Context c) {
		this.c = c;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
		sdCard = Environment.getExternalStorageDirectory();
	}

	// Facades
	public void sync() {
		if (Connections.isConnectedToInternet(c)) {
			String address = sharedPreferences.getString("address", null);
			String username = sharedPreferences.getString("username", null);
			String password = sharedPreferences.getString("password", null);
			if (address == null || username == null || password == null) {
				return;
			}

			new Thread() {
				public void run() {
					syncData();
					syncInbox();
					syncOutbox();
					syncPermissions();
				}
			}.start();
		}
	}

	// JSON File Transfers
	public boolean checkLogin(String username, String password) {
		String address = sharedPreferences.getString("address", null);
		String protocol = sharedPreferences.getString("url_protocol", "http");
		if (address == null) {
			// TODO: initialize setup if something goes wrong here
			return false;
		} else {
			String serverURL = protocol + "://" + address + "/" + URL_LOGIN
					+ "?u=" + username + "&p=" + password;
			String loginJSON = makeJSONQuery(serverURL);
			JSONObject loginObj;
			try {
				loginObj = (JSONObject) new JSONTokener(loginJSON).nextValue();
				int loginStatus = loginObj.getInt("status");
				if (loginStatus == 1) {
					return true;
				} else {
					return false;
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				return false;
			}
		}
	}

	public boolean syncData() {

		Calendar calendar = Calendar.getInstance();
		long currTime = calendar.getTimeInMillis();
		Date currDateTime = new Date(currTime);
		Date currDate = TimeConversion.convertDateTimetoDate(currDateTime);
		Date yesterdayDate = new Date(currDate.getTime() - 1000);

		long lastIndividualData = sharedPreferences.getLong(
				"last_upload_individual_participant", 0);
		Date lastUploadDateTime = new Date(lastIndividualData);
		Date lastUploadDate = TimeConversion
				.convertDateTimetoDate(lastUploadDateTime);

		if (currDate.compareTo(lastUploadDate) != 0) {

			boolean isExtraWheeling = sharedPreferences.getBoolean(
					"ExtraWheeling", false);

			JSONObject jsonObjIndividual = DatabaseHelper.getInstance(c)
					.getIndividualParticipantDataJSON(c, lastUploadDate,
							yesterdayDate, isExtraWheeling);

			String fullPath = Writer.writeJSONToFile("logfiles",
					"individual_participant_", true, jsonObjIndividual);

			transferMediaToServer(false, fullPath);

			sharedPreferences.edit()
					.putLong("last_upload_individual_participant", currTime)
					.commit();

			long lastTrainingBehaviour = sharedPreferences.getLong(
					"last_upload_training_behaviour", 0);
			Date lastTrainingBehaviourDate = new Date(lastTrainingBehaviour);
			JSONObject jsonObjTraining = DatabaseHelper.getInstance(c)
					.getIndividualTrainingBehaviourJSON(c,
							lastTrainingBehaviourDate);

			String fullPathTraining = Writer.writeJSONToFile("logfiles",
					"training_behaviour_", true, jsonObjTraining);

			transferMediaToServer(false, fullPathTraining);

			sharedPreferences.edit()
					.putLong("last_upload_training_behaviour", currTime)
					.commit();

			JSONObject jsonObjSummaryTraining = DatabaseHelper.getInstance(c)
					.getSummaryTrainingBehaviourJSON(c);

			String fullPathSummaryTraining = Writer
					.writeJSONToFile("logfiles", "summary_training_behaviour",
							false, jsonObjSummaryTraining);
			transferMediaToServer(false, fullPathSummaryTraining);
		}
		return false;
	}

	public boolean syncPermissions() {
		String address = sharedPreferences.getString("address", null);
		String protocol = sharedPreferences.getString("url_protocol", "http");
		String username = sharedPreferences.getString("username", null);
		String password = sharedPreferences.getString("password", null);
		if (address == null || username == null || password == null) {
			return false;
		}

		String serverURL = protocol + "://" + address + "/"
				+ URL_INCOMING_PERMISSIONS + "?u=" + username + "&p="
				+ password;
		String messagesJSON = makeJSONQuery(serverURL);

		JSONArray messagesArray;
		try {
			messagesArray = (JSONArray) new JSONTokener(messagesJSON)
					.nextValue();
			for (int i = 0; i < messagesArray.length(); i++) {
				JSONObject messageObj = messagesArray.getJSONObject(i);
				String cid = messageObj.getString("cid");
				Integer locked = messageObj.getInt("l");

				DatabaseHelper.getInstance(c).setPermission(cid, locked);
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			return false;
		}
		return true;
	}

	public boolean syncInbox() {
		String address = sharedPreferences.getString("address", null);
		String protocol = sharedPreferences.getString("url_protocol", "http");
		String username = sharedPreferences.getString("username", null);
		String password = sharedPreferences.getString("password", null);
		if (address == null || username == null || password == null) {
			return false;
		}

		String serverURL = protocol + "://" + address + "/"
				+ URL_INCOMING_MESSAGES + "?u=" + username + "&p=" + password;
		String messagesJSON = makeJSONQuery(serverURL);

		JSONArray messagesArray;
		List<String> mediaToDownload = new LinkedList<String>();
		try {
			messagesArray = (JSONArray) new JSONTokener(messagesJSON)
					.nextValue();
			for (int i = 0; i < messagesArray.length(); i++) {
				JSONObject messageObj = messagesArray.getJSONObject(i);
				String from = messageObj.getString("from");
				String title = messageObj.getString("title");
				String content = messageObj.getString("content");
				String link = messageObj.getString("link");
				int type = messageObj.getInt("type");

				if (link != null) {
					if (!link.equals("")) {
						mediaToDownload.add(link);
					}
				}

				DatabaseHelper.getInstance(c).addIncomingMessage(type, from,
						link, title, content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
			return false;
		}

		// TODO: consider the situation where download is interrupted.

		if (mediaToDownload.size() > 0) {
			// take CPU lock to prevent CPU from going off if the user
			// presses the power button during download
			PowerManager pm = (PowerManager) c
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wl.acquire();

			try {
				InputStream input = null;
				OutputStream output = null;
				HttpURLConnection connection = null;

				for (int i = 0; i < mediaToDownload.size(); i++) {
					try {
						URL url = new URL(protocol + "://" + address + "/"
								+ URL_INCOMING_MESSAGE_MEDIA + "?u=" + username
								+ "&p=" + password + "&file="
								+ mediaToDownload.get(i));
						System.out.println(protocol + "://" + address + "/"
								+ URL_INCOMING_MESSAGE_MEDIA + "?u=" + username
								+ "&p=" + password + "&file="
								+ mediaToDownload.get(i));
						connection = (HttpURLConnection) url.openConnection();
						connection.connect();

						// expect HTTP 200 OK, so we don't mistakenly save error
						// report
						// instead of the file
						if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
							System.out.println("Server returned HTTP "
									+ connection.getResponseCode() + " "
									+ connection.getResponseMessage());
							return false;
						}

						// download the file
						input = connection.getInputStream();

						File mediaDir = new File(sdCard.getAbsolutePath()
								+ "/raindrops/messages/media");
						mediaDir.mkdirs();
						File outputFile = new File(mediaDir,
								mediaToDownload.get(i));
						System.out.println("Writing to: " + mediaDir + "/"
								+ mediaToDownload.get(i));
						try {
							outputFile.createNewFile();
						} catch (IOException e) {
							// Not much we can do if we can't write
							System.out.println(e.toString());
						}

						output = new FileOutputStream(outputFile);

						byte data[] = new byte[4096];
						int count;
						while ((count = input.read(data)) != -1) {
							output.write(data, 0, count);
						}

					} catch (Exception e) {
						System.out.println(e.toString());
						return false;
					} finally {
						try {
							if (output != null)
								output.close();
							if (input != null)
								input.close();
						} catch (IOException ignored) {
						}

						if (connection != null)
							connection.disconnect();
					}
				}
			} finally {
				wl.release();
			}
		}

		return true;
	}

	public boolean syncOutbox() {
		List<Message> messages = DatabaseHelper.getInstance(c)
				.getUnsentMessages();
		for (Message message : messages) {
			sendMessage(message);
		}
		return true;
	}

	public boolean sendMessage(Message message) {
		String address = sharedPreferences.getString("address", null);
		String protocol = sharedPreferences.getString("url_protocol", "http");
		String username = sharedPreferences.getString("username", null);
		String password = sharedPreferences.getString("password", null);
		if (address == null || username == null || password == null) {
			return false;
		}

		try {

			transferMediaToServer(true, sdCard.getAbsolutePath()
					+ "/raindrops/messages/media/" + message.getLocation());

			JSONObject messageObj = new JSONObject();
			messageObj.put("u", username);
			messageObj.put("p", password);
			messageObj.put("title", message.getTitle());
			messageObj.put("desc", message.getDescription());
			messageObj.put("link", message.getLocation());
			messageObj.put("type", message.getType());

			String serverURL = protocol + "://" + address + "/"
					+ URL_OUTGOING_MESSAGES;

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(new URL(serverURL).toURI());

			// Prepare JSON to send by setting the entity
			httpPost.setEntity(new StringEntity(messageObj.toString(), "UTF-8"));

			// Set up the header types needed to properly transfer JSON
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setHeader("Accept-Encoding", "application/json");
			httpPost.setHeader("Accept-Language", "en-US");

			// Execute POST
			HttpResponse response = httpClient.execute(httpPost);
			System.out.println("Response from server:" + response);

			DatabaseHelper.getInstance(c).setMessageAsSent(message.getId());

			return true;
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return false;
	}

	private String makeJSONQuery(String urlStr) {
		try {
			System.out.println("make JSON query");
			URL url = new URL(urlStr);
			System.out.println("Asking for " + urlStr);
			HttpURLConnection client = (HttpURLConnection) url.openConnection();
			// client.setRequestProperty("accept", "application/json");
			InputStream in = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String returnString = br.readLine();
			client.disconnect();
			System.out.println("return is " + returnString);
			return returnString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// ==========================
	// File Transfers

	public boolean transferMediaToServer(boolean isMessage, String pathToMedia) {

		String fileName = pathToMedia;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(pathToMedia);

		if (!sourceFile.isFile()) {
			return false;
		} else {
			try {

				String address = sharedPreferences.getString("address", null);
				String protocol = sharedPreferences.getString("url_protocol",
						"http");
				String username = sharedPreferences.getString("username", null);
				String password = sharedPreferences.getString("password", null);
				if (address == null || username == null || password == null) {
					return false;
				}

				String destProcessPage = "";
				if (isMessage) {
					destProcessPage = URL_PROCESS_MESSAGE_MEDIA;
				} else {
					destProcessPage = URL_PROCESS_DATA;
				}

				String serverURL = protocol + "://" + address + "/"
						+ destProcessPage + "?u=" + username + "&p=" + password;

				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				URL url = new URL(serverURL);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);
				// conn.setRequestProperty("u", username);
				// conn.setRequestProperty("p", password);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
						+ fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				int serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				if (serverResponseCode == 200) {
					System.out.println(serverResponseMessage);
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {

				ex.printStackTrace();
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
				return false;
			} catch (Exception e) {

				e.printStackTrace();

				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
				return false;
			}

			return true;

		} // End else block

	}
}
