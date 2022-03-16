package eu.trade.repo.policyimpl;

import java.util.ArrayList;
import java.util.List;

public class PolicyMonitor {
	
	private int counter = 0;
	private final List<String> messages = new ArrayList<String>();
	
	public void reset() {
		counter = 0;
		messages.clear();
	}
	
	public int getCounter() {
		return counter;
	}
	
	
	public void incrementCounter() {
		counter++;
	}
	
	public void addMessage(String message) {
		messages.add(message);
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
}
