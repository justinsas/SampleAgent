package sample;

import java.util.Scanner;

import com.sas.mkt.agent.sdk.CI360Agent;
import com.sas.mkt.agent.sdk.CI360AgentException;
import com.sas.mkt.agent.sdk.CI360StreamInterface;
import com.sas.mkt.agent.sdk.ErrorCode;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;

import com.clivern.racter.BotPlatform;

import com.clivern.racter.receivers.*;
import com.clivern.racter.receivers.webhook.*;

import com.clivern.racter.senders.*;
import com.clivern.racter.senders.templates.*;

/**
 * This class contains sample code used to demonstrate the usage of the CI360 Agent SDK 
 * {@link CI360Agent} to interact with CI360.   The sample will connect to the CI360 event stream
 * and will print out all events that arrive from CI360.   It also accepts a few command from standard
 * input.   
 * <br> <br>
 * exit - exits the sample agent
 * <br> <br>
 * send - sends an external event to CI360.   following the send command is the event to be injected.
 * The event is in JSON.  See {@link CI360Agent#injectEvent(String)}.
 * <br> <br>
 * bulk - requests a Signed S3 URL be returned for uploaded events into CI360.   Following the "bulk" command
 * is the application ID to use.   See {@link CI360Agent#requestBulkEventURL(String)}.
 * 
 * @author magibs
 *
 */
public class SampleAgent {

	static boolean exiting=false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			final CI360Agent agent=new CI360Agent();
			CI360StreamInterface streamListener=new CI360StreamInterface() {
				public boolean processEvent(final String event) {
					Thread eventThread=new Thread() {
						public void run() {
							//System.out.println("Event: " + event);
							sendMessage(event);
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
							//Try to reconnect to the event stream.
							agent.startStream(this, true);
						} catch (CI360AgentException e) {
							System.err.println("ERROR " + e.getErrorCode() + ": " + e.getMessage());
						}
					}
				}
			};
			agent.startStream(streamListener, true);
			
			// Continue until user enters "exit" to standard input.
			Scanner in =new Scanner(System.in);
			while (true) {
				String input=in.nextLine();
				if (input.equalsIgnoreCase("exit")) {
					exiting=true;
					agent.stopStream();
					in.close();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						
					}
					System.exit(0);;				
				} else if (input.startsWith("send ")) {
					try {
						String message=agent.injectEvent(input.substring(5));
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("ping")) {
					try {
						String message=agent.ping();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("config")) {
					try {
						String message=agent.getAgentConfig();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("healthcheck")) {
					try {
						String message=agent.healthcheck();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("connection")) {
					boolean status=agent.isConnected();
					System.out.println("Connection Status: " + (status?"UP":"DOWN"));
				} else if (input.startsWith("diag")) {
					try {
						String message=agent.diagnostics();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("bulk ")) {
					try {
						String message=agent.requestBulkEventURL(input.substring(5));
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
 * Facebook Messenger
 */	
	public static void sendMessage(String event) {
		try {
			//jsonパース
        	JSONObject jsonObject = new JSONObject(event);
			String subjectID = jsonObject.getJSONObject("attributes").getString("subject_id");
			String eventName = jsonObject.getJSONObject("attributes").getString("eventName");
			
			//Creativeコンテンツ
        	String creativeContent = jsonObject.getJSONObject("attributes").getString("creative_content");
			int jpgStart = creativeContent.indexOf("src") ;
			int jpgEnd = creativeContent.indexOf(".jpg");
			
			creativeContent = creativeContent.substring(jpgStart + 5, jpgEnd + 4);
			//System.out.println(creativeContent);
			
			Map<String, String> options = new HashMap<String, String>();
			
			//propertiesディレクトリ・ファイル
        	String dir=System.getProperty("dir");
        	String source=System.getProperty("source");
        	URLClassLoader urlLoader = new URLClassLoader(new URL[]{new File(dir).toURI().toURL()});
        	ResourceBundle bundle = ResourceBundle.getBundle(source, Locale.getDefault(), urlLoader);
        	        	
			// 出力ファイルの設定 
        	String app_id = bundle.getString("app_id");
        	String verify_token = bundle.getString("verify_token");
        	String page_access_token = bundle.getString("page_access_token");

			options.put("app_id", app_id);
			options.put("verify_token", verify_token);
			options.put("page_access_token", page_access_token);
			BotPlatform platform = new BotPlatform(options);
			
			MessageTemplate message_tpl = platform.getBaseSender().getMessageTemplate();
			message_tpl.setRecipientId(subjectID);
			//Send text campaign
			if (eventName.equals("FBMsgOutgoing_1")) {
				message_tpl.setMessageText("Text campaign information from CI360 agent, buy 1 get 1 free.");
			} 
			//Send image campaign
			else if (eventName.equals("FBMsgOutgoing_2")) {
				message_tpl.setAttachment("image", creativeContent, false);
				message_tpl.setNotificationType("SILENT_PUSH");
			}
			platform.getBaseSender().send(message_tpl);
			System.out.println(eventName + "が実行されました。");
		} catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ファイル出力に失敗しました。");
        }
	}
}

