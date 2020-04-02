import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import java.io.IOException;
import java.math.BigInteger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Client {

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;

	public ArrayList<Byte> fingerprintList = new ArrayList<>();
	ArrayList<String[]> fileList = new ArrayList<String[]>();

	// Protocol - Connection failed
	public static final int REQUEST_FAIL = 050;

	// Protocol - Upload
	public static final int REQUEST_UPLOAD = 101;
	public static final int ALLOW_UPLOAD = 102;
	public static final int NOT_ALLOW_UPLOAD = 103;

	public static final int REQUEST_CHECK_CHUNK = 104;
	public static final int CHUNK_EXIST = 105;
	public static final int CHUNK_NOT_EXIST = 106;

	public static final int REQUEST_CHECK_ZERO_CHUNK = 107;
	public static final int LOGICAL_CHUNK_INCREMENT = 108;
	public static final int ZERO_CHUNK_NOT_EXIST = 109;

	public static final int REQUEST_SAVE_CHUNK = 110;
	public static final int CHUNK_SAVED = 111;
	public static final int CHUNK_NOT_SAVED = 112;

	public static final int REQUEST_UPLOAD_END = 199;

	// Protocol - Download
	public static final int REQUEST_DOWNLOAD = 201;
	public static final int ALLOW_DOWNLOAD = 202;
	public static final int NOT_ALLOW_DOWNLOAD = 203;

	public static final int CHUNK_START = 204;
	public static final int CHUNK_END = 205;

	public static final int REQUEST_DOWNLOAD_END = 299;

	// Protocol - Delete
	public static final int REQUEST_DELETE = 301;
	public static final int DELETE_SUCCESS = 302;
	public static final int DELETE_NOT_SUCCESS = 303;
	public static final int DELETE_FILE_NOT_EXIST = 304;

	public static final int REQUEST_DELETE_END = 399;

	// Protocol - List
	public static final int REQUEST_LIST = 401;
	public static final int LIST_NO_FILE = 402;
	public static final int LIST_ALLOW = 403;
	public static final int LIST_RETREIVED = 404;
	public static final int LIST_RETREIVE_FAILED = 405;

	public static final int REQUEST_LIST_END = 499;
	
	// Protocol - Error Code
	public static final int NO_ERROR = 900;
	public static final int SAME_FILENAME_EXIST = 901;
	public static final int UNKNOWN_ERROR = 999;
	

	public static String translateMsgCode(int msgCode) {
		switch (msgCode) {
		case REQUEST_FAIL:
			return "REQUEST_FAIL";
		case REQUEST_UPLOAD:
			return "REQUEST_UPLOAD";
		case ALLOW_UPLOAD:
			return "ALLOW_UPLOAD";
		case NOT_ALLOW_UPLOAD:
			return "NOT_ALLOW_UPLOAD";
		case REQUEST_CHECK_CHUNK:
			return "REQUEST_CHECK_CHUNK";
		case CHUNK_EXIST:
			return "CHUNK_EXIST";
		case CHUNK_NOT_EXIST:
			return "CHUNK_NOT_EXIST";
		case REQUEST_CHECK_ZERO_CHUNK:
			return "REQUEST_CHECK_ZERO_CHUNK";
		case LOGICAL_CHUNK_INCREMENT:
			return "LOGICAL_CHUNK_INCREMENT";
		case ZERO_CHUNK_NOT_EXIST:
			return "ZERO_CHUNK_NOT_EXIST";
		case REQUEST_SAVE_CHUNK:
			return "REQUEST_SAVE_CHUNK";
		case CHUNK_SAVED:
			return "CHUNK_SAVED";
		case CHUNK_NOT_SAVED:
			return "CHUNK_NOT_SAVED";
		case REQUEST_UPLOAD_END:
			return "REQUEST_UPLOAD_END";
		case REQUEST_DOWNLOAD:
			return "REQUEST_DOWNLOAD";
		case ALLOW_DOWNLOAD:
			return "ALLOW_DOWNLOAD";
		case NOT_ALLOW_DOWNLOAD:
			return "NOT_ALLOW_DOWNLOAD";
		case CHUNK_START:
			return "CHUNK_START";
		case CHUNK_END:
			return "CHUNK_END";
		case REQUEST_DOWNLOAD_END:
			return "REQUEST_DOWNLOAD_END";
		case REQUEST_DELETE:
			return "REQUEST_DELETE";
		case DELETE_SUCCESS:
			return "DELETE_SUCCESS";
		case DELETE_NOT_SUCCESS:
			return "DELETE_NOT_SUCCESS";
		case REQUEST_DELETE_END:
			return "REQUEST_DELETE_END";
		case DELETE_FILE_NOT_EXIST:
			return "DELETE_FILE_NOT_EXIST";
		case REQUEST_LIST:
			return "REQUEST_LIST";
		case LIST_NO_FILE:
			return "LIST_NO_FILE";
		case LIST_ALLOW:
			return "LIST_ALLOW";
		case REQUEST_LIST_END:
			return "REQUEST_LIST_END";
		default:
			return "ERROR";
		}
	}

	public Client(String host, int port) throws UnknownHostException, IOException {
		System.out.println("Client - start connection..");
		this.socket = new Socket(host, port);
		this.dis = new DataInputStream(this.socket.getInputStream());
		this.dos = new DataOutputStream(this.socket.getOutputStream());
		System.out.println("Client - connection has set up.");
	}

	public void sendString(String msg) throws IOException {
		this.dos.writeUTF(msg);
		// System.out.println(this.dis.readUTF());
	}

	public int getUploadPermission(String filepath) throws IOException {
		this.dos.writeInt(REQUEST_UPLOAD);
		// System.out.println(this.dis.readUTF()); //[FROM Server] Message Code
		// received. Content:REQUEST_UPLOAD

		File file = new File(filepath);
		this.dos.writeUTF(file.getName());
		this.dos.writeInt((int) file.length());

		int received = this.dis.readInt();
		// System.out.println("[FROM Server] Message Code =
		// "+translateMsgCode(received));

		return received;
	}

	public int getDownloadPermission(String filename) throws IOException {
		this.dos.writeInt(REQUEST_DOWNLOAD);
		// System.out.println(this.dis.readUTF()); //[FROM Server] Message Code
		// received. Content:REQUEST_DOWNLOAD

		this.dos.writeUTF(filename);

		int received = this.dis.readInt();
		System.out.println("[FROM Server] Message Code = " + translateMsgCode(received));

		return received;
	}

	public int getChunkStatus(String checksum) throws IOException {
		this.dos.writeInt(REQUEST_CHECK_CHUNK);
		// System.out.println(this.dis.readUTF()); //[FROM Server] Message Code
		// received. Content:REQUEST_CHECK_CHUNK

		this.dos.writeUTF(checksum);
		int received = this.dis.readInt();
		// System.out.println("[FROM Server] Message Code =
		// "+translateMsgCode(received));

		// TODO change return value
		return received;
	}

	public void sendPureRequest(int msgCode) throws IOException {
		this.dos.writeInt(msgCode);
		//System.out.println(this.dis.readUTF());
	}

	public void endUpload() throws IOException {
		this.dos.writeInt(REQUEST_UPLOAD_END);
		// System.out.println(this.dis.readUTF()); //[FROM Server] Message Code
		// received. Content:REQUEST_UPLOAD_END

		System.out.println(this.dis.readUTF()); // Report Output:
		System.out.println(this.dis.readUTF()); // "Total number of logical chunks in storage:"+this.index.logicalChunk
		System.out.println(this.dis.readUTF()); // "Number of unique physical chunks in
												// storage:"+this.index.physicalChunk
		System.out.println(this.dis.readUTF()); // Number of bytes in storage with
												// deduplication:"+this.index.getPhysicalTotalStorage()
		System.out.println(this.dis.readUTF()); // Number of bytes in storage without deduplication:
												// this.index.getLogicalTotalStorage()
		System.out.println(this.dis.readUTF()); // "Space saving:"+this.index.getSpaceSave()

		System.out.println(this.dis.readUTF()); // [Server] End Connection

	}

	public void sendFile(String file) throws IOException {
		FileInputStream in = new FileInputStream(file);

		// Send file size first
		File tempFile = new File(file);
		if (!tempFile.exists()) {
			System.out.println("File not exist.");
			in.close();
			return;
		}

		this.dos.writeLong(tempFile.length());
		// Send File size end

		byte[] buffer = new byte[4096];

		while (in.read(buffer) > 0) {
			this.dos.write(buffer);
		}

		in.close();
	}

	public void createSingleChunk(ArrayList<Byte> content, String checksum) throws IOException {
		this.dos.writeInt(REQUEST_SAVE_CHUNK);
		//System.out.println(this.dis.readUTF()); // [FROM Server] Message Code received. Content:REQUEST_SAVE_CHUNK
		this.dos.writeInt(content.size());

		for (int i = 0; i < content.size(); i++) {
			this.dos.write(content.get(i));
		}

		int received = this.dis.readInt();
		// System.out.println("[FROM Server] Message Code =
		// "+translateMsgCode(received));

		// TODO A measure that triggered when chunk not properly save

	}

	public void close() throws IOException {
		this.dis.close();
		this.dos.close();
		this.socket.close();
		System.out.println("Client - Stream and socket closed.");
	}

	public void reconnect(String host, int port) throws IOException {
		System.out.println("Client - Start reconnecting...");
		this.socket = new Socket(host, port);
		this.dis = new DataInputStream(this.socket.getInputStream());
		this.dos = new DataOutputStream(this.socket.getOutputStream());
		System.out.println("Client - Reconnect complete. Connection set up.");
	}

	public int receiveMsgCode() throws IOException {
		if (this.socket == null) {
			System.out.println("[MSG] null");
			return 1;
		}

		int received = this.dis.readInt();
		// this.dos.writeUTF("[FROM Client] Message Code received. Content:" +
		// translateMsgCode(received));
		// System.out.println("[FROM Server] Message code received.
		// Content:"+translateMsgCode(received));
		return received;
	}

	public int upload(String path, int min_chunk, int d, int average_chunk, int max_chunk, JProgressBar bar, JLabel label)
			throws IOException, NoSuchAlgorithmException {
		Timer timer = new Timer("upload");
		timer.start();
		System.out.println("Client : Action - Upload");
		int permission = this.getUploadPermission(path);
		if (permission == ALLOW_UPLOAD) {
			// TODO modify the min_chunk, d, average_chunk, max_chunk
			sendChunks(path, min_chunk, d, average_chunk, max_chunk, bar,label);
			return NO_ERROR;
		} else if (permission == NOT_ALLOW_UPLOAD) {
			System.out.println("Filename exist. Not allow to upload this file.");
			return SAME_FILENAME_EXIST;
		}
		return UNKNOWN_ERROR;
	}

	public void download(String filename, String localFilePath, 
			JLabel current, JLabel total, JLabel time, JProgressBar bar) throws IOException {		
		System.out.println("Client : Action - Download");
		int permission = this.getDownloadPermission(filename);
		FileOutputStream out = null;

		if (permission == ALLOW_DOWNLOAD) {
			int numberOfChunk = this.dis.readInt();
			System.out.println("[FROM Server] Number of chunks = " + numberOfChunk);
			
			Long startTime = System.nanoTime();
			bar.setMaximum(numberOfChunk);
			total.setText(String.valueOf(numberOfChunk));

			File targetFile = new File(localFilePath);
			targetFile.createNewFile();
			out = new FileOutputStream(targetFile);

			for (int i = 0; i < numberOfChunk; i++) {
				int chunkSize = -1;
				if (receiveMsgCode() == CHUNK_START) {
					chunkSize = this.dis.readInt();
					//System.out.println("[FROM Server] the chunk size = " + chunkSize);

					int read = 0;
					int remaining = chunkSize;
					int totalread = 0;
					byte[] buffer = new byte[4096];

					while ((read = this.dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
						totalread += read;
						remaining -= read;
						//System.out.println("read " + totalread + " bytes.");
						out.write(buffer, 0, read);
					}

					// To ensure the chunk is complete
					if (receiveMsgCode() == CHUNK_END) {
						System.out.println("Progress : "+(i+1)+"/"+numberOfChunk);
						Long passedTime = System.nanoTime() - startTime;
						Long remainingTime = passedTime * numberOfChunk / (i+1)- passedTime;
						Long remainingSecond = TimeUnit.SECONDS.convert(remainingTime, TimeUnit.NANOSECONDS);
						current.setText(String.valueOf(i+1));
						time.setText(remainingSecond+"s");
						bar.setValue(i+1);
						continue;
					} else {
						System.out.println("Chunk transfer broken.");
						this.dis.close();
						out.close();
						return;
					}
				}

			}
			sendPureRequest(REQUEST_DOWNLOAD_END);
			System.out.println(this.dis.readUTF()); // [Server] End Connection
			if (out != null) {
				out.close();
				System.out.println(localFilePath + " Success Downloaded.");
				time.setText("Success Downloaded");
			}
			this.socket.close();

		} else if (permission == NOT_ALLOW_DOWNLOAD) {
			System.out.println("The file is not exist in server.");
		}
	}

	public void sendChunks(String pathname, int min_chunk, int base, int modulus, int max_chunk, JProgressBar bar, JLabel label)
			throws IOException, NoSuchAlgorithmException {
		Timer chunksTimer = new Timer("sendChunks");
		chunksTimer.start();
		
		File file = new File(pathname);
		FileInputStream in = null;
		ArrayList<Byte> content = new ArrayList<>();
		
		RabinKarpSlided rabinKarp = new RabinKarpSlided(min_chunk,base,modulus);

		int counter = 0;
		int zeroCounter = 0;
		String checksum;
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		int progressMax = (int) file.length() + min_chunk;
		int progressOnePercent = (int) Math.min(progressMax / 100, 10000);
		bar.setMaximum(progressMax);
		
		Long startTime = (long) 0;
		label.setText("Estimating...");

		System.out.println("Client[Upload] - progressMax = "+progressMax);
		
		try {
			in = new FileInputStream(file);

			int b;
			byte c;
			while ((b = in.read()) != -1) {
				
				
				c = (byte) b;
				
				int result = rabinKarp.pushByte(c);
				
				//System.out.println("Client[Upload] - Counter = "+counter+"/"+progressMax+" rfp = "+result);
				content.add(c);
				counter++;
				
				if (counter == min_chunk)
					startTime = System.nanoTime();
				
				
				if (counter % progressOnePercent == 0 && counter != 0 && counter >= min_chunk){
					bar.setValue(counter);
					
					Long passedTime = System.nanoTime() - startTime;
					Long remainingTime = passedTime * progressMax / counter - passedTime;
					Long remainingSecond = remainingTime / 1000000000;
					label.setText(remainingSecond+"s");
					System.out.println("Client[Upload] - Counter = "+counter+"/"+progressMax);
					
				}
				
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

							StringBuilder sb = new StringBuilder();
							for (int i = 0;i<checksumByte.length;i++) {
								sb.append(Integer.toString((checksumByte[i] & 0xff) + 0x100, 16).substring(1));
							}
							checksum = sb.toString();
							// TODO Remove the below line for optimization
							//System.out.println("Checksum = " + checksum);

							int reply = getChunkStatus(checksum);
							if (reply == CHUNK_EXIST) {
								// System.out.println("========Chunk exist========");
							} else if (reply == CHUNK_NOT_EXIST) {
								// System.out.println("===========================");
								createSingleChunk(content, checksum);
							}
							content.clear();
							continue;
						}

						content.removeIf(n -> (n != 48));

						for (int i = 0; i < content.size(); i++) {
							sendPureRequest(REQUEST_CHECK_ZERO_CHUNK);
						}
						content.clear();
						continue;
					}
					
				} else {
					if (zeroCounter >= min_chunk) {
						sendPureRequest(LOGICAL_CHUNK_INCREMENT);
					}
					zeroCounter = 0;
				}
				
				if (counter >= 0 && zeroCounter < min_chunk) {
					if ((result == 0 && content.size() >= min_chunk) || (content.size() == max_chunk)) {
						
						System.out.println("Client[Upload] - Packing chunk..");
						
						md.update(content.toString().getBytes());
						byte[] checksumByte = md.digest();

						StringBuilder sb = new StringBuilder();
						for (int i = 0;i<checksumByte.length;i++) {
							sb.append(Integer.toString((checksumByte[i] & 0xff) + 0x100, 16).substring(1));
						}
						checksum = sb.toString();

						System.out.println("Client[Upload] - checksum generated.");
						// TODO Remove the below line for optimization
						//System.out.println("Checksum = " + checksum);

						int reply = getChunkStatus(checksum);
						if (reply == CHUNK_EXIST) {
							// System.out.println("========Chunk exist========");
						} else if (reply == CHUNK_NOT_EXIST) {
							// System.out.println("===========================");
							createSingleChunk(content, checksum);
						}
						content.clear();
						
						System.out.println("Client[Upload] - chunk sent.");
					}
				}


			}
		} catch (FileNotFoundException fe) {
			System.out.println("The upload file not exist.");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (in != null)
			in.close();
		
		if (content.size() != 0) {
			
			md.update(content.toString().getBytes());
			byte[] checksumByte = md.digest();
			
			// credit to https://github.com/mist3rr0b0t/Rabin-Fingerprint-Deduplication/blob/master/MyDedup.java
			StringBuilder sb = new StringBuilder();
			for (int i = 0;i<checksumByte.length;i++) {
				sb.append(Integer.toString((checksumByte[i] & 0xff) + 0x100, 16).substring(1));
			}
			checksum = sb.toString();
			
			// TODO Remove the below line for optimization
			System.out.println("Checksum = " + checksum);
			
			int reply = getChunkStatus(checksum);
			if (reply == CHUNK_EXIST) {
				// System.out.println("========Chunk exist========");
			} else if (reply == CHUNK_NOT_EXIST) {
				// System.out.println("===========================");
				createSingleChunk(content, checksum);
			}
			content.clear();
		}

		// close the upload process by sending REQUEST_UPLOAD_END
		bar.setValue(bar.getMaximum());
		label.setText("Upload Complete");
		endUpload();
	}

	

	public void delete(String filename) throws IOException {
		System.out.println("Client : Action - Delete");

		this.dos.writeInt(REQUEST_DELETE);
		// System.out.println(this.dis.readUTF()); //[FROM Server] Message Code
		// received. Content:REQUEST_DELETE

		this.dos.writeUTF(filename);
		int received = receiveMsgCode();
		if (received == DELETE_FILE_NOT_EXIST) {
			System.out.println("The File is not exist in server");
		} else if (received == DELETE_SUCCESS) {
			System.out.println("File - " + filename + " is deleted.");
		} else {
			System.out.println("Delete Not Success. Please retry");
		}

	}

	public int list() throws IOException {
		System.out.println("Client : Action - List file");
		sendPureRequest(REQUEST_LIST);
		
		fileList.clear();

		int received = receiveMsgCode();
		if (received == LIST_NO_FILE) {
			System.out.println("Client - No file in the server");
			return LIST_NO_FILE;
		} else if (received == LIST_ALLOW) {
			System.out.println("Client - retreiving file in the server");
			System.out.println("Filename           Filesize");
			String filename = this.dis.readUTF();
			while (!filename.equals("??end")) {
				
				String filesize = this.dis.readUTF();
				System.out.println(filename+"        "+filesize);
				
				String[] info = new String[2];
				info[0] = filename;
				info[1] = filesize;
				
				fileList.add(info);
				
				filename = this.dis.readUTF();
			}
			
			return LIST_RETREIVED;
		}
		else {
			System.out.println("Client - List file - Unknown Error Occur.");
			return LIST_RETREIVE_FAILED;
		}
		

	}
}