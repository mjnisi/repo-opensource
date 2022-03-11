package eu.trade.repo.id;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class IDGeneratorImpl implements IDGenerator{

	private final MessageDigest messageDigester;
	
	public IDGeneratorImpl () {
		try {
			messageDigester = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized String next() {

		String salt = "salt";
		String seed = System.nanoTime() + salt;
		
		BigInteger bigInt = new BigInteger(1, messageDigester.digest(seed.getBytes()));
		return bigInt.toString(16);
		
	}

	
	public void reset() {
		
	}
}
