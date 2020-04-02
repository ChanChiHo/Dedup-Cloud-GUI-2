import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;

/* This purpose of this class is to provide a way that have better readability 
 * 		when calculate the rabin-karp fingerprint
 * 
 * By using ArrayDeque but not arraylist, to provide a quicker way to calculate
 * 
 *  https://publicobject.com/2010/07/07/caliper_confirms_reality_linked_list_vs_array_list/
 *  
 * */

public class RabinKarpSlided {
	public long lastRfp = -999;
	public ArrayDeque<Byte> contentWindow;
	public int base;
	public int windowSize;
	public int modulus;
	public byte previous = 0;
	
	public RabinKarpSlided(int windowSize, int base, int modulus) {
		contentWindow = new ArrayDeque<>(windowSize+1);
		this.base = base;
		this.windowSize = windowSize;
		this.modulus = modulus;
	}
	
	public int rfpFirst() {
		long sum = 0;
		
		//Timer rabinchecker5 = new Timer("Rabin - clone data");
		//rabinchecker5.start();
		ArrayDeque<Byte> data = contentWindow.clone();
		//rabinchecker5.stop();
			
		System.out.println("RabinKarpSlided[rfp] - Calculating first rfp");
		for (int i = 1;i<=windowSize;i++) {
				
			int value1 = (int)data.pop();
			long value2 = (long) Math.pow(base,windowSize-i) % modulus;
				
			sum += (long)value1 * value2;
			//System.out.println("i="+i+" "+sum);
		}
		System.out.println("RabinKarpSlided[rfp] - Calculating first rfp completed.");
		
		//Timer rabinchecker4 = new Timer("Rabin - rfp part3");
		//rabinchecker4.start();
		
		sum = sum % modulus;
		//System.out.print("RabinKarpSlided - Long Rfp = "+sum);
		//System.out.println(", Final Rfp = "+result);
		int result = (int) sum;
		lastRfp = sum;
		
		//rabinchecker4.stop();

		return (int)sum;
	}
	
	public int rfp() {

		long sum = 0;
		
		//Timer rabinchecker = new Timer("Rabin - rfp part1");
		//rabinchecker.start();
		long power1 = (long) (Math.pow(base,windowSize-1) % modulus);
		//rabinchecker.stop();
			
		//Timer rabinchecker2 = new Timer("Rabin - rfp part2");
		//rabinchecker2.start();
		sum = (long) base * (lastRfp - power1 * previous) + contentWindow.getLast();
		//rabinchecker2.stop();
		
		
		//Timer rabinchecker4 = new Timer("Rabin - rfp part3");
		//rabinchecker4.start();
		
		sum = sum % modulus;
		//System.out.print("RabinKarpSlided - Long Rfp = "+sum);
		//System.out.println(", Final Rfp = "+result);
		int result = (int) sum;
		lastRfp = sum;
		
		//rabinchecker4.stop();

		return (int)sum;
	}
		
	// Input: content
	// Output: the rfp value of current content window
	public int pushByte(byte content) {
		contentWindow.add(content);
		//System.out.println("cw "+contentWindow.size());
		
		
		if (contentWindow.size() < windowSize) {
			return -1;
		} else if (contentWindow.size() > windowSize) {
			previous = contentWindow.poll();
			return rfp();
			//System.out.println("get previous = "+previous);
		} else if (contentWindow.size() == windowSize) {
			//System.out.println("size equal");
			return rfpFirst();
		}
		
		return -1;
	}

}
