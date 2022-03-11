package eu.trade.repo.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum IndexingState {
	/**
	 * Start state. Not yet indexed.
	 */
	NONE(0, "Indexation pending"),
	/**
	 * Full index completed
	 */
	INDEXED(1, "Indexed"),
	/**
	 * Error indexing
	 */
	ERROR(2, "Indexation error"),
	/**
	 * Content type not indexable
	 */
	NOT_INDEXABLE(3, "Content not indexable"),
	/**
	 * Indexed but only the first words until the 'index size limit' is reached
	 */
	PARTIALLY_INDEXED(4, "Content partially indexed. Word limit reached.");


	private final int state;   
	private final String description;
	private static final ConcurrentMap<Integer, IndexingState> states = new ConcurrentHashMap<Integer, IndexingState>();

	IndexingState(int state, String description) {
		this.state = state;
		this.description = description;
	}

	public int getState() {
		return state;
	}

	public String getDescription() {
		return description;
	}

	public static IndexingState get(final int state) {
		if (states.isEmpty()) {
			for (IndexingState indexState : values()){
				states.putIfAbsent(indexState.getState(), indexState);
			}
		}
		return states.get(state);
	}

	public String toString(){
		return new StringBuilder(name()).append(": ").append(getDescription()).toString();
	}
}
