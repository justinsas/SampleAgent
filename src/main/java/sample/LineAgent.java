package sample;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.json.JSONObject;

import com.sas.mkt.agent.sdk.CI360Agent;
import com.sas.mkt.agent.sdk.CI360AgentException;
import com.sas.mkt.agent.sdk.CI360StreamInterface;
import com.sas.mkt.agent.sdk.ErrorCode;

/**
 * This class contains sample code used to demonstrate the usage of the CI360
 * Agent SDK {@link CI360Agent} to interact with CI360. The sample will connect
 * to the CI360 event stream and will print out all events that arrive from
 * CI360. It also accepts a few command from standard input. <br>
 * <br>
 * exit - exits the sample agent <br>
 * <br>
 * send - sends an external event to CI360. following the send command is the
 * event to be injected. The event is in JSON. See
 * {@link CI360Agent#injectEvent(String)}. <br>
 * <br>
 * bulk - requests a Signed S3 URL be returned for uploaded events into CI360.
 * Following the "bulk" command is the application ID to use. See
 * {@link CI360Agent#requestBulkEventURL(String)}.
 * 
 * @author magibs
 *
 */
public class LineAgent {

	static boolean exiting = false;

	final static String emoji = "\\uDBC0\\uDC";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			final CI360Agent agent = new CI360Agent();
			CI360StreamInterface streamListener = new CI360StreamInterface() {
				public boolean processEvent(final String event) {
					Thread eventThread = new Thread() {
						public void run() {
							System.out.println("Event: " + event);
							sendLineMessage(event);
							if (event.startsWith("CFG")) {
								throw new RuntimeException("oops");
							}
						}
					};
					eventThread.start();
					return true;
				}

				public void streamClosed(ErrorCode errorCode, String message) {
					if (exiting) {
						System.out.println("Stream closed");
					} else {
						System.out.println("Stream closed " + errorCode + ": " + message);
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {

						}
						try {
							// Try to reconnect to the event stream.
							agent.startStream(this, true);
						} catch (CI360AgentException e) {
							System.err.println("ERROR " + e.getErrorCode() + ": " + e.getMessage());
						}
					}
				}
			};
			agent.startStream(streamListener, true);

			// Continue until user enters "exit" to standard input.
			Scanner in = new Scanner(System.in);
			while (true) {
				String input = in.nextLine();
				if (input.equalsIgnoreCase("exit")) {
					exiting = true;
					agent.stopStream();
					in.close();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {

					}
					System.exit(0);
					;
				} else if (input.startsWith("send ")) {
					try {
						String message = agent.injectEvent(input.substring(5));
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("ping")) {
					try {
						String message = agent.ping();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("config")) {
					try {
						String message = agent.getAgentConfig();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("healthcheck")) {
					try {
						String message = agent.healthcheck();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("connection")) {
					boolean status = agent.isConnected();
					System.out.println("Connection Status: " + (status ? "UP" : "DOWN"));
				} else if (input.startsWith("diag")) {
					try {
						String message = agent.diagnostics();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("bulk ")) {
					try {
						String message = agent.requestBulkEventURL(input.substring(5));
						System.out.println("SUCCESS  URL: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("sendmessage ")) {
					try {
						agent.sendWebSocketMessage(input.substring(12).trim());
						System.out.println("SUCCESS: " + input.substring(12).trim());
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				}
			}

		} catch (CI360AgentException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * Line Message
	 */
	public static void sendLineMessage(String event) {
		try {
			// jsonパース
			JSONObject jsonObject = new JSONObject(event);
			String subjectID = jsonObject.getJSONObject("attributes").getString("subject_id");
			String text = jsonObject.getJSONObject("attributes").getString("text");
			String replyToken = jsonObject.getJSONObject("attributes").getString("replytoken");
			// Creativeコンテンツ
			String creativeContent = jsonObject.getJSONObject("attributes").getString("creative_content");
			int jpgStart = creativeContent.indexOf("src");
			int jpgEnd = creativeContent.indexOf(".jpg");
			creativeContent = creativeContent.substring(jpgStart + 5, jpgEnd + 4);

			// propertiesディレクトリ・ファイル
			String dir = System.getProperty("dir");
			String source = System.getProperty("source");
			URLClassLoader urlLoader = new URLClassLoader(new URL[] { new File(dir).toURI().toURL() });
			ResourceBundle bundle = ResourceBundle.getBundle(source, Locale.getDefault(), urlLoader);

			String access_token = bundle.getString("access_token");

			LineSender line;
			LineTemplate template = new LineTemplate();

			if (text.equals("push campaign")) {
				line = new LinePusher();
				template.setMessage_type("image");
				template.setRecipientId(capitalize(subjectID));
				template.setImage_url(creativeContent);
			} else {
				line = new LineReplyer();
				template.setMessage_type("text");
				template.setReply_token(replyToken);
				int emojiNumber;
				try {
					emojiNumber = Integer.parseInt(text.substring(0, text.indexOf(" emoji")));
				} catch (Exception e) {
					emojiNumber = 5;
				}
				template.setMessageText(createEmoji(emojiNumber));
			}
			line.setAccessToken(access_token);

			line.send(template.build());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Line送信に失敗しました。");
		}
	}

	private static String capitalize(final String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	private static String createEmoji(int emojiNumber) {
		String[] emojiList = { "78", "79", "7A", "7B", "7C", "7D", "7E", "7F", "8C", "8D", "8E", "8F", "91", "92", "93",
				"94", "95", "9A", "9B", "9C", "9D", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "B0", "B1", "B2", "B3", "3B", "3C", "3D", "35" };
		int emojiLength = emojiList.length;
		Random rnd = new Random();
		String retEmoji = "";
		for (int i = 0; i < emojiNumber; i++) {
			int val = rnd.nextInt(emojiLength);
			retEmoji += emoji + emojiList[val];
		}
		return retEmoji;
	}
}
