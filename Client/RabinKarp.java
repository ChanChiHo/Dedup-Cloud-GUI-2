import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;

public class RabinKarp {
	
	public ArrayList<Byte>fingerprintList = new ArrayList<>();
	
	public byte rfp(ArrayList<Byte> data, int s, int windowSize, int base, int modulus) {
		if (s == 0) {
			int sum = 0;
			// System.out.println("s = 0,");
			for (int i = 1; i <= windowSize; i++) {
				int value = (int) data.get(i - 1);
				int power = (int) (Math.pow(base, windowSize - i));
				sum = sum + value * power;
				// System.out.println("i = "+i+",value = "+value+" power = "+power);
			}
			// System.out.println("S=0, Sum "+sum);
			sum = sum % modulus;
			byte result = (byte) sum;
			this.fingerprintList.add(0, result);
			return result;
		} else {
			int sum = base * (this.fingerprintList.get(s - 1) - (int) (Math.pow(base, windowSize - 1)) * data.get(0))
					+ data.get(windowSize);
			// System.out.println("result "+result );
			sum = sum % modulus;
			if (sum < 0)
				sum = sum + modulus;
			byte result = (byte) sum;
			this.fingerprintList.add(s, result);
			return result;
		}
	}
		
	public void sendChunksOld(String pathname, int min_chunk, int base, int modulus, int max_chunk)
			throws IOException, NoSuchAlgorithmException {
		File file = new File(pathname);
		FileInputStream in = null;
		ArrayList<Byte> window = new ArrayList<>();
		ArrayList<Byte> content = new ArrayList<>();
		
		int counter = -min_chunk;
		int zeroCounter = 0;
		String checksum;
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		int progressMax = (int) file.length() - min_chunk;
		int progressOnePercent = (int) progressMax / 100;

		try {
			in = new FileInputStream(file);
			
			int b;
			byte c;
			while ((b = in.read()) != -1) {
				c = (byte) b;
				window.add(c);
				content.add(c);
				counter++;
				
				//System.out.println("c = "+c);

				if (b == 48) {
					zeroCounter++;
					if (zeroCounter >= min_chunk) {

						@SuppressWarnings("unused")
						byte dummy = rfp(window, counter, min_chunk, base, modulus);
						//System.out.println("window = "+window.toString()+"rfp = "+dummy);

						// first output the content that not are zero
						@SuppressWarnings("unchecked")
						ArrayList<Byte> beforeContent = (ArrayList<Byte>) content.clone();
						beforeContent.removeIf(n -> (n == 48));

						if (beforeContent.size() > 0) {
							md.update(content.toString().getBytes());
							byte[] checksumByte = md.digest();

							checksum = String.format("%064x", new BigInteger(1, checksumByte));
							// TODO Remove the below line for optimization
							System.out.println("Checksum = "+checksum);


							content.clear();
							if (window.size() >= min_chunk + 1) {
								window.remove(0);
							}
							continue;
						}

						content.removeIf(n -> (n != 48));

						content.clear();
						continue;
					}
				} else {

					zeroCounter = 0;
				}

				if (counter >= 0 && zeroCounter < min_chunk) {
					byte result = rfp(window, counter, min_chunk, base, modulus);
					//System.out.println("window = "+window.toString()+" rfp = "+result);

				
					if ((result == 0 && content.size() >= min_chunk) || (content.size() == max_chunk)) {
						md.update(content.toString().getBytes());
						byte[] checksumByte = md.digest();

						checksum = String.format("%064x", new BigInteger(1, checksumByte));
						
						// TODO Remove the below line for optimization
						System.out.println("Checksum = "+checksum);
		
						content.clear();
					}
				}

				if (window.size() >= min_chunk + 1) {
					window.remove(0);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
			if (content.size() != 0) {

				String temp = content.toString();
				md.update(temp.getBytes());
				byte[] checksumByte = md.digest();

				checksum = String.format("%064x", new BigInteger(1, checksumByte));
				// TODO Remove the below line for optimization
				System.out.println("Checksum = "+checksum);

				content.clear();
			}
			// close the upload process by sending REQUEST_UPLOAD_END
			//System.out.println("Progess = " + progressMax + " / " + progressMax+"    100%");
		}

	}
	
	public void sendChunks(String pathname, int min_chunk, int base, int modulus, int max_chunk)
			throws IOException, NoSuchAlgorithmException {
		File file = new File(pathname);
		FileInputStream in = null;
		ArrayDeque<Byte> window = new ArrayDeque<>();
		ArrayList<Byte> content = new ArrayList<>();
		RabinKarpSlided rfp = new RabinKarpSlided(min_chunk,base,modulus);
		
		int counter = -min_chunk;
		int zeroCounter = 0;
		String checksum;
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		
		int progressMax = (int) file.length() - min_chunk;
		int progressOnePercent = (int) progressMax / 100;

		try {
			in = new FileInputStream(file);
			
			int b;
			byte c;
			while ((b = in.read()) != -1) {
				c = (byte) b;
				
				int result = rfp.pushByte(c);
				content.add(c);
				counter++;
				
				//System.out.println("c= "+c);
				//System.out.println("window = "+rfp.contentWindow.toString()+" rfp = "+result);

				if (b == 48) {
					zeroCounter++;
					if (zeroCounter >= min_chunk) {

						// first output the content that not are zero
						@SuppressWarnings("unchecked")
						ArrayList<Byte> beforeContent = (ArrayList<Byte>) content.clone();
						beforeContent.removeIf(n -> (n == 48));

						if (beforeContent.size() > 0) {
							md.update(content.toString().getBytes());
							byte[] checksumByte = md.digest();

							checksum = String.format("%064x", new BigInteger(1, checksumByte));
							// TODO Remove the below line for optimization
							System.out.println("Checksum = "+checksum);

							
							content.clear();
							continue;
						}

						content.removeIf(n -> (n != 48));

						for (int i = 0; i < content.size(); i++) {
							System.out.println("REQUEST_CHECK_ZERO_CHUNK");
						}
						content.clear();
						continue;
					}
				} else {
					if (zeroCounter >= min_chunk) {
						System.out.println("LOGICAL_CHUNK_INCREMENT");
					}
					zeroCounter = 0;
				}

				if (counter >= 0 && zeroCounter < min_chunk) {
					//System.out.println("CHECK 1 = "+Arrays.asList(window.toString()));
					// TODO optimize the progress bar 

					if ((result == 0 && content.size() >= min_chunk) || (content.size() == max_chunk)) {
						md.update(content.toString().getBytes());
						byte[] checksumByte = md.digest();

						checksum = String.format("%064x", new BigInteger(1, checksumByte));
						
						// TODO Remove the below line for optimization
						System.out.println("Checksum = "+checksum);
						
						content.clear();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
			if (content.size() != 0) {

				String temp = content.toString();
				md.update(temp.getBytes());
				byte[] checksumByte = md.digest();

				checksum = String.format("%064x", new BigInteger(1, checksumByte));
				// TODO Remove the below line for optimization
				System.out.println("Checksum = "+checksum);
				content.clear();
			}
			// close the upload process by sending REQUEST_UPLOAD_END
			System.out.println("EndUpload");
		}
	}
	
	public static void main(String[] args) {
		RabinKarp obj = new RabinKarp();
		StopWatch timer = new StopWatch("RabinKarp");
		timer.start();
		// Program Start
		try {
			obj.sendChunks("text1.txt",5,257,100,400);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		timer.stop();
		// Program End;
		//System.out.println(Arrays.asList(obj.fingerprintList.toString()));

	}
}
