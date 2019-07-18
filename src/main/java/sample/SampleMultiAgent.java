package sample;

import java.util.Scanner;

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
public class SampleMultiAgent {

	static boolean exiting=false;
	public static void main(String[] args) {
		String gatewayHost1=System.getProperty("ci360.gatewayHost1");
		if (gatewayHost1==null) {
			System.err.println("Invalid CI360 Gateway Host #1");
			System.exit(-1);
		}
		String tenantID1=System.getProperty("ci360.tenantID1");
		if (tenantID1==null) {
			System.err.println("Invalid tenant ID #1");
			System.exit(-1);
		}
		String clientSecret1=System.getProperty("ci360.clientSecret1");
		if (clientSecret1==null) {
			System.err.println("Invalid client secret #1");
			System.exit(-1);
		}

		String gatewayHost2=System.getProperty("ci360.gatewayHost2");
		if (gatewayHost2==null) {
			System.err.println("Invalid  Gateway Host #2");
			System.exit(-1);
		}
		String tenantID2=System.getProperty("ci360.tenantID2");
		if (tenantID2==null) {
			System.err.println("Invalid tenant ID #2");
			System.exit(-1);
		}
		String clientSecret2=System.getProperty("ci360.clientSecret2");
		if (clientSecret2==null) {
			System.err.println("Invalid client secret #2");
			System.exit(-1);
		}
		
		try {
			final CI360Agent agent1=new CI360Agent(gatewayHost1, tenantID1, clientSecret1);
			CI360StreamInterface streamListener1=new CI360StreamInterface() {
				public boolean processEvent(String event) {
					System.out.println("Event1: " + event);
					return true;
				}

				public void streamClosed(ErrorCode errorCode, String message) {
					if (exiting) {
						System.out.println("Stream1 closed");
					} else {
						System.out.println("Stream1 closed " + errorCode + ": " + message);
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							
						}
						try {
							//Try to reconnect to the event stream.
							agent1.startStream(this, true);
						} catch (CI360AgentException e) {
							System.err.println("ERROR1 " + e.getErrorCode() + ": " + e.getMessage());
						}
					}
				}
			};
			agent1.startStream(streamListener1, true);
			
			final CI360Agent agent2=new CI360Agent(gatewayHost2, tenantID2, clientSecret2);
			CI360StreamInterface streamListener2=new CI360StreamInterface() {
				public boolean processEvent(String event) {
					System.out.println("Event2: " + event);
					return true;
				}

				public void streamClosed(ErrorCode errorCode, String message) {
					if (exiting) {
						System.out.println("Stream2 closed");
					} else {
						System.out.println("Stream2 closed " + errorCode + ": " + message);
						try {
							Thread.sleep(15000);
						} catch (InterruptedException e) {
							
						}
						try {
							//Try to reconnect to the event stream.
							agent2.startStream(this, true);
						} catch (CI360AgentException e) {
							System.err.println("ERROR2 " + e.getErrorCode() + ": " + e.getMessage());
						}
					}
				}
			};
			agent2.startStream(streamListener2, true);

			// Continue until user enters "exit" to standard input.
			Scanner in =new Scanner(System.in);
			while (true) {
				String input=in.nextLine();
				if (input.equalsIgnoreCase("exit")) {
					exiting=true;
					agent1.stopStream();
					agent2.stopStream();
					in.close();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						
					}
					System.exit(0);;				
				} else if (input.startsWith("send1 ")) {
					try {
						String message=agent1.injectEvent(input.substring(6));
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("send2 ")) {
					try {
						String message=agent2.injectEvent(input.substring(6));
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("ping1")) {
					try {
						String message=agent1.ping();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("ping2")) {
					try {
						String message=agent2.ping();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("healthcheck1")) {
					try {
						String message=agent1.healthcheck();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("healthcheck2")) {
					try {
						String message=agent2.healthcheck();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("diag1")) {
					try {
						String message=agent1.diagnostics();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("diag2")) {
					try {
						String message=agent2.diagnostics();
						System.out.println("SUCCESS: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("bulk1 ")) {
					try {
						String message=agent1.requestBulkEventURL(input.substring(6));
						System.out.println("SUCCESS  URL: " + message);
					} catch (CI360AgentException e) {
						System.err.println("ERROR: " + e.getMessage());
					}
				} else if (input.startsWith("bulk2 ")) {
					try {
						String message=agent2.requestBulkEventURL(input.substring(6));
						System.out.println("SUCCESS  URL: " + message);
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
