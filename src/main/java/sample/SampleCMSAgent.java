package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.sas.mkt.agent.sdk.CI360Agent;
import com.sas.mkt.agent.sdk.CI360AgentException;
import com.sas.mkt.agent.sdk.CI360CMSAgent;
import com.sas.mkt.agent.sdk.CI360StreamClosedInterface;
import com.sas.mkt.agent.sdk.CMSInterface;
import com.sas.mkt.agent.sdk.CMSSystemRep;
import com.sas.mkt.agent.sdk.ErrorCode;

/**
 * This class contains sample code used to demonstrate the usage of the CI360 Agent SDK 
 * {@link CI360Agent} to process CMS messages from CI360.   The sample will connect to the CI360 event stream
 * and forward messages to the CMS with the URL defined in the ci360.cmsURL property. 
 * 
 * @author magibs
 *
 */
public class SampleCMSAgent {

	static boolean exiting=false;
	public static void main(String[] args) {
		try {
		    List<CMSSystemRep> cmsConfigs=new ArrayList<CMSSystemRep>();
		    CMSSystemRep cmsConfig=new CMSSystemRep();
		    cmsConfig.setId("wordPress");
		    cmsConfig.setName("WordPress CMS");
		    cmsConfig.setSupportFolders(false);
		    cmsConfig.setSupportSearch(false);
		    cmsConfigs.add(cmsConfig);
		    
			String cmsURL=System.getProperty("ci360.cmsURL");
			if (cmsURL==null) {
				System.err.println("Invalid Content Management System URL");
				System.exit(-1);
			}
		    CMSInterface cmsProcessor=new SampleCMSMessageProcessor(cmsURL);		    
			Map<String, CMSInterface> cmsConnectors= new HashMap<String, CMSInterface>();
			cmsConnectors.put("wordPress", cmsProcessor);
			
			final CI360CMSAgent agent=new CI360CMSAgent(cmsConfigs, cmsConnectors);
			CI360StreamClosedInterface streamClosedHandler=new CI360StreamClosedInterface() {
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
							agent.startStream(this);
						} catch (CI360AgentException e) {
							System.err.println("ERROR " + e.getErrorCode() + ": " + e.getMessage());
							System.exit(-1);
						}
					}
				}
		    };

		    
			
			agent.startStream(streamClosedHandler);
			
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
			} else if (input.startsWith("ping")) {
					try {
						String message=agent.ping();
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
				} else if (input.startsWith("diag")) {
					try {
						String message=agent.diagnostics();
						System.out.println("SUCCESS: " + message);
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


}
