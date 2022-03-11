package eu.trade.repo.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.trade.repo.model.Repository;
import eu.trade.repo.model.Word;
import eu.trade.repo.util.Constants;

public class WordSelector extends BaseSelector {

	public List<Word> getWords(Set<String> words, Repository repo) {
    	return getEntityManager()
    			.createNamedQuery("Words.byWordAndRepo", Word.class)
    			.setParameter("words", words)
    			.setParameter("repositoryId", repo.getId())
    			.getResultList();
    }
	
	public List<Integer> getWordIds(Set<String> words, Repository repo) {
		return toIdList( getWords(words, repo) );
	}		
	
	private List<Integer> toIdList(List<Word> wordList){
		List<Integer> wordIds = new ArrayList<>();
		for(Word w : wordList) {
			wordIds.add(w.getId());
		}
		return wordIds;
	}
}
