package sample;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.json.JSONObject;

import com.sas.mkt.agent.sdk.CI360Agent;
import com.sas.mkt.agent.sdk.CI360AgentException;
import com.sas.mkt.agent.sdk.CI360StreamInterface;
import com.sas.mkt.agent.sdk.ErrorCode;

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
public class FileAgent {

	static boolean exiting=false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			final CI360Agent agent=new CI360Agent();
			CI360StreamInterface streamListener=new CI360StreamInterface() {
				public boolean processEvent(final String event) {
					Thread eventThread=new Thread() {
						public void run() {
							System.out.println("Event: " + event);
							exportCsv(event); //�t�@�C������
							//sendMessage(event);
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
 * �t�@�C������
 */	
	public static void exportCsv(String event){
        try { 
        	//json�p�[�X
        	JSONObject jsonObject = new JSONObject(event);
        	String eventName = jsonObject.getJSONObject("attributes").getString("eventName");
            System.out.println(eventName);
        	
        	String creativeContent = jsonObject.getJSONObject("attributes").getString("creative_content");
        	System.out.println("creativeContent="+creativeContent);
        	
        	//properties�f�B���N�g���E�t�@�C��
        	String dir=System.getProperty("dir");
        	String source=System.getProperty("source");
        	URLClassLoader urlLoader = new URLClassLoader(new URL[]{new File(dir).toURI().toURL()});
        	ResourceBundle bundle = ResourceBundle.getBundle(source, Locale.getDefault(), urlLoader);
        	        	
			// �o�̓t�@�C���̐ݒ� 
        	String separateFileByEventNm = bundle.getString("separateFileByEventNm");
        	String outputPath = bundle.getString("outputPath");
        	String outputFileName = bundle.getString("outputFileName");
        	
        	if(separateFileByEventNm.equals("1")){
        		outputFileName = eventName + "_" + outputFileName;
            	System.out.println(separateFileByEventNm);        		
        	}
        	File outputFile = new File(outputPath + outputFileName);        		
        	
			// �g���q�擾 
			int extensionIndex = outputFileName.lastIndexOf("."); 
			String extension = outputFileName.substring(extensionIndex);

			// �o�̓t�@�C�����̍쐬�i�}�Ԃ���j 
			String outputFileBranch = outputFileName.substring(0, extensionIndex) + "_";

			// �����t�@�C�������݂���ꍇ�A�o�̓t�@�C���Ɏ}�Ԃ�t�^���鏈�� 
			int branch = 0; 
			while (outputFile.exists()) { 
			    branch++; 
			    outputFile = new File(outputPath + outputFileBranch + branch + extension); 
			}

            // �o�̓t�@�C���̍쐬
            FileWriter f = new FileWriter(outputFile.getAbsolutePath(), false);
        	BufferedWriter b = new BufferedWriter(f);  
        	
            // ���e���Z�b�g���� 
        	b.write(event);
        	b.newLine(); //���s
        	
            // �t�@�C���ɏ����o������
            b.close();
 
            System.out.println("�t�@�C���o�͊����I");
 
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("�t�@�C���o�͂Ɏ��s���܂����B");
        }
         
    }
}

