package com.felix.webmaintenance;

/**
 * Interface for the server that shall be maintained.
 * 
 * @author burkhardt.felix
 * 
 */
public interface MaintainedServer {
	public void reInitializeServer();

	public void executeCommand(String command);

	public String showInfo(String infoDescriptor);
}
