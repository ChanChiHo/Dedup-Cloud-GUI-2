import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

	// Protocol - Connection failed
	private static final int REQUEST_FAIL = 050;

	// Protocol - Upload
	private static final int REQUEST_UPLOAD = 101;
	private static final int ALLOW_UPLOAD = 102;
	private static final int NOT_ALLOW_UPLOAD = 103;

	private static final int REQUEST_CHECK_CHUNK = 104;
	private static final int CHUNK_EXIST = 105;
	private static final int CHUNK_NOT_EXIST = 106;

	private static final int REQUEST_CHECK_ZERO_CHUNK = 107;
	private static final int LOGICAL_CHUNK_INCREMENT = 108;
	private static final int ZERO_CHUNK_NOT_EXIST = 109;

	private static final int REQUEST_SAVE_CHUNK = 110;
	private static final int CHUNK_SAVED = 111;
	private static final int CHUNK_NOT_SAVED = 112;

	private static final int REQUEST_UPLOAD_END = 199;

	// Protocol - Download
	private static final int REQUEST_DOWNLOAD = 201;
	private static final int ALLOW_DOWNLOAD = 202;
	private static final int NOT_ALLOW_DOWNLOAD = 203;

	private static final int CHUNK_START = 204;
	private static final int CHUNK_END = 205;

	private static final int REQUEST_DOWNLOAD_END = 299;

	// Protocol - Delete
	private static final int REQUEST_DELETE = 301;
	private static final int DELETE_SUCCESS = 302;
	private static final int DELETE_NOT_SUCCESS = 303;
	private static final int DELETE_FILE_NOT_EXIST = 304;

	private static final int REQUEST_DELETE_END = 399;

	// Protocol - List
	private static final int REQUEST_LIST = 401;
	private static final int LIST_NO_FILE = 402;
	private static final int LIST_ALLOW = 403;

	private static final int REQUEST_LIST_END = 499;

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

	public Client(String host, int port) throws UnknownHostException, IOException {
		
		this.socket = new Socket(host, port);
		this.dis = new DataInputStream(this.socket.getInputStream());
		this.dos = new DataOutputStream(this.socket.getOutputStream());
		
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
	}

	public void reconnect(String host, int port) throws IOException {
		this.socket = new Socket(host, port);
		this.dis = new DataInputStream(this.socket.getInputStream());
		this.dos = new DataOutputStream(this.socket.getOutputStream());
	}

	public static boolean checkArgs(String[] args) {
		if (args.length <= 1) {
			System.err.println("client [Server IP] [Action] [filename]");
			return false;
		}
		switch (args[1]) {
		case "upload":
			if (args.length != 7) {
				System.out.println("Wrong input args format!");
				System.err.println("client [Server IP] upload [filename] [min_chunk] [d] [average_chunk] [max_chunk]");
				return false;
			}
			return true;
		case "download":
			if (args.length != 4) {
				System.out.println("Wrong input args format!");
				System.out.println("client [Server IP] download [cloud file Path] [local file Path]");
				return false;
			}
			return true;
		case "delete":
			if (args.length != 3) {
				System.out.println("Wrong input args format!");
				System.out.println("Client [Server IP] delete [filename]");
				return false;
			}
			return true;
		case "list":
			if (args.length != 2) {
				System.out.println("Wrong input args format!");
				System.out.println("Client [Server IP] list");
				return false;
			}
			return true;
		default:
			return false;
		}
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

	public void upload(String path, int min_chunk, int d, int average_chunk, int max_chunk)
			throws IOException, NoSuchAlgorithmException {
		System.out.println("Client : Action - Upload");
		int permission = this.getUploadPermission(path);
		if (permission == ALLOW_UPLOAD) {
			// TODO modify the min_chunk, d, average_chunk, max_chunk
			sendChunks(path, min_chunk, d, average_chunk, max_chunk);
		} else if (permission == NOT_ALLOW_UPLOAD) {
			System.out.println("Filename exist. Not allow to upload this file.");
		}
	}

	public void download(String filename, String localFilePath) throws IOException {
		System.out.println("Client : Action - Download");
		int permission = this.getDownloadPermission(filename);
		FileOutputStream out = null;

		if (permission == ALLOW_DOWNLOAD) {
			int numberOfChunk = this.dis.readInt();
			System.out.println("[FROM Server] Number of chunks = " + numberOfChunk);

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
			}
			this.socket.close();

		} else if (permission == NOT_ALLOW_DOWNLOAD) {
			System.out.println("The file is not exist in server.");
		}
	}

	public void sendChunks(String pathname, int min_chunk, int base, int modulus, int max_chunk)
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

				if (b == 48) {
					zeroCounter++;
					if (zeroCounter >= min_chunk) {

						@SuppressWarnings("unused")
						byte dummy = rfp(window, counter, min_chunk, base, modulus);

						// first output the content that not are zero
						@SuppressWarnings("unchecked")
						ArrayList<Byte> beforeContent = (ArrayList<Byte>) content.clone();
						beforeContent.removeIf(n -> (n == 48));

						if (beforeContent.size() > 0) {
							md.update(content.toString().getBytes());
							byte[] checksumByte = md.digest();

							checksum = String.format("%064x", new BigInteger(1, checksumByte));
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
							if (window.size() >= min_chunk + 1) {
								window.remove(0);
							}
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
					byte result = rfp(window, counter, min_chunk, base, modulus);

					// TODO optimize the progress bar
					if (progressOnePercent != 0 && counter % progressOnePercent == 0
							&& counter / progressOnePercent != 100)
						System.out.println("Progess = " + counter + " / " + (int) progressMax + "     "
								+ counter / progressOnePercent + "%");

					if ((result == 0 && content.size() >= min_chunk) || (content.size() == max_chunk)) {
						md.update(content.toString().getBytes());
						byte[] checksumByte = md.digest();

						checksum = String.format("%064x", new BigInteger(1, checksumByte));

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
				}

				if (window.size() >= min_chunk + 1) {
					window.remove(0);
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

			String temp = content.toString();
			md.update(temp.getBytes());
			byte[] checksumByte = md.digest();

			checksum = String.format("%064x", new BigInteger(1, checksumByte));
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
		System.out.println("Progess = " + progressMax + " / " + progressMax + "    100%");
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
		}

	}

	public ArrayList<String[]> list() throws IOException {
		System.out.println("Client : Action - List file");
		sendPureRequest(REQUEST_LIST);
		
		ArrayList<String[]> list = new ArrayList<String[]>();

		int received = receiveMsgCode();
		if (received == LIST_NO_FILE) {
			System.out.println("No file in the server.");
			String[] info = new String[2];
			info[0] = "No file in the server.";
			info[1] = "/";
			list.add(info);
			return list;
		} else if (received == LIST_ALLOW) {
			
			System.out.println("Filename           Filesize");
			String filename = this.dis.readUTF();
			while (!filename.equals("##end")) {
				
				String filesize = this.dis.readUTF();
				System.out.println(filename+"        "+filesize);
				
				String[] info = new String[2];
				info[0] = filename;
				info[1] = filesize;
				
				list.add(info);
				
				filename = this.dis.readUTF();
			}
			
			return list;
		}
		else {
			String[] info = new String[2];
			info[0] = "Error Occur.";
			info[1] = "/";
			list.add(info);
			return list;
		}
		

	}

	public static void main(String[] args) throws IOException {
		if (!checkArgs(args)) {
			return;
		}

		Client client = new Client(args[0], 59090);
		try {
			switch (args[1]) {
			case "upload":
				client.upload(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
						Integer.parseInt(args[6]));
				break;
			case "download":
				client.download(args[2], args[3]);
				break;
			case "delete":
				client.delete(args[2]);
				break;
			case "list":
				client.list();
				break;
			default:
				System.out.println("No command");
			}
			// client.sendFile("test1.txt");
			// System.out.println("File - "+"test1.txt"+" sent successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}