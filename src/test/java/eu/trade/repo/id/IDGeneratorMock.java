package eu.trade.repo.id;


public final class IDGeneratorMock implements IDGenerator{

	private int id = 1;
	
	public synchronized String next() {
		return "generatedId-" + id++;
	}
	
	public synchronized void reset() {
		id = 1;
	}
}
