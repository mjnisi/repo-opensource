package eu.europa.ec.digit.cmisrepo.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author bentsth
 */
public class MockupUtil {

    private static final String zeros = "000000000000000000000000000";
    private static final String cChars = "abcdefghijklmnopqrstuvwxyz";
    private static final String cCharsUpper = cChars.toUpperCase();
    private static final int nMaxIndexChars = cChars.length() - 1;

    private static String[] stopWords;
    private static Set<String> cSetStopWords;
    

    public static void main(String[] args) {
        loadStopWords();
        List<String> cLst = extractUniqueWords("It's found this although wxxki front computer neither be each six formerly what moreover");
        System.out.println(cLst);
    }

    public static int getRandomInt(int a_nBeginIncl, int a_nEndIncl) {
        assert (a_nBeginIncl < a_nEndIncl);
        float vRand = (float) Math.random();
        int nRand = Math.round(vRand * (a_nEndIncl - a_nBeginIncl));
        return a_nBeginIncl + nRand;
    }

    public static String addLeadingZeros(int number, int digitCount) {
        String val = zeros + number;
        return val.substring(val.length() - digitCount);
    }

    public static char getRandomChar() {
        return cChars.charAt(getRandomInt(0, nMaxIndexChars));
    }

    public static char getRandomCharUpper() {
        return cCharsUpper.charAt(getRandomInt(0, nMaxIndexChars));
    }
    
    public static String beginCap(String a_cWord) {
    	return a_cWord.substring(0, 1).toUpperCase() + a_cWord.substring(1);
    }

    public static String getRandomWord(boolean a_bBeginCap) {
    	String cWord = null;
        if (Math.random() > .2d) {
            loadStopWords();
            int id = (int) (stopWords.length * Math.random());
            cWord = stopWords[id];
        } else {
            int nLenght = getRandomInt(4, 13);
            StringBuffer cBuf = new StringBuffer();
            
            for (int i = 0; i < nLenght; i++) {
                cBuf.append(getRandomChar());
            }

            cWord = cBuf.toString();
        }
        
        return a_bBeginCap ? beginCap(cWord) : cWord;
    }
    
    
    public static List<String> extractUniqueWords(String a_cPage){
    	a_cPage = a_cPage.replace(".", "");
    	String[] acWords = a_cPage.split(" ");
    	List<String> cLst = new ArrayList<>();
    	for (String cWord : acWords) {
			if(!cSetStopWords.contains(cWord.toLowerCase()))
				cLst.add(cWord);
		}
    	
    	return cLst;
    }

    private static synchronized void loadStopWords() {
        if (stopWords != null)
            return;

        try {
            File fileStopWords = new File("./resources/stop-word-list.txt");
            BufferedReader reader = new BufferedReader(new FileReader(fileStopWords));
            ArrayList<String> cLst = new ArrayList<String>();
            String word = null;
            while ((word = reader.readLine()) != null) {
                cLst.add(word);
            }

            stopWords = cLst.toArray(new String[0]);
            cSetStopWords = new HashSet<>(cLst);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getRandomSentence(int a_nLength) {
        StringBuffer cBuf = new StringBuffer();
        cBuf.append(getRandomWord(true)).append(' ');

        for (int i = 0; i < a_nLength - 1; i++) {
            cBuf.append(getRandomWord(false)).append(' ');
        }

        cBuf.setLength(cBuf.length() - 1);
        cBuf.append('.').append(' ');

        return cBuf.toString();
    }
}