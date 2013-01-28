package org.movsim.network;

import java.io.File;

public class NetworkMain {

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File network = new File("/home/kesting/workspace/movsim/sim/games", "routing.xodr");
		
		System.out.println("Hello world");
		
		NetworkUnMarshaller unMarshaller = new NetworkUnMarshaller();
		unMarshaller.marshall();
		unMarshaller.unMarshall(network);

	}

}
