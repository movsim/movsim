package org.movsim.network;

import java.io.File;

public class NetworkMain {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File networkFile = new File("routing.xodr");
		
		OpenDriveNetwork network = new OpenDriveNetwork();
		
		System.out.println("Marshall: ");
		network.marshall();
		
		System.out.println("Unmarshall: ");
		network.unMarshall(networkFile);

	}

}
