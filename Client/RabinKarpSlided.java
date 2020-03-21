import java.util.ArrayDeque;

/* This purpose of this class is to provide a way that have better readability 
 * 		when calculate the rabin-karp fingerprint
 * 
 * By using ArrayDeque but not arraylist, to provide a quicker way to calculate
 * 
 *  https://publicobject.com/2010/07/07/caliper_confirms_reality_linked_list_vs_array_list/
 *  
 * */

public class RabinKarpSlided {
	public ArrayDeque<Integer> rfpList;
	public ArrayDeque<Byte> contentWindow;
	public int base;
	public int windowSize;
	public int modulus;
	public boolean firstCompute = true;
	public byte previous = 0;
	
	public RabinKarpSlided(int windowSize, int base, int modulus) {
		rfpList = new ArrayDeque<>();
		contentWindow = new ArrayDeque<>(windowSize);
		this.base = base;
		this.windowSize = windowSize;
		this.modulus = modulus;
	}
	
	public int rfp() {
		ArrayDeque<Byte> data = contentWindow.clone();
		long sum = 0;
		if (firstCompute) {
			for (int i = 1;i<=windowSize;i++) {
				
				int value1 = (int)data.pop();
				long value2 = (long) Math.pow(base,windowSize-i) % modulus;
				
				sum += (long)value1 * value2;
				//System.out.println("i="+i+" "+sum);
				firstCompute = false;
			}
		} else {
			long power1 = (long) (Math.pow(base,windowSize-1) % modulus);
			sum = (long) base * (rfpList.getLast() - power1 * previous) + data.getLast();
		}
		
		sum = sum % modulus;
		if (sum < 0)
			sum = sum + modulus;
		int result = (int) sum;
		rfpList.add(result);

		return result;
	}
	
	public int currentRfp() {
		return rfpList.getLast();
	}
	
	// Input: content
	// Output: the rfp value of current content window
	public int pushByte(byte content) {
		contentWindow.add(content);
		//System.out.println("cw "+contentWindow.size());
		
		if (contentWindow.size() < windowSize) {
			return -1;
		} else if (contentWindow.size() > windowSize) {
			previous = contentWindow.remove();
		}
		
		return rfp();
	}
}
