import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.io.*;

public class ServerHandler {

	private Socket socket = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	public IndexFile index;
	public LocalDateTime lastTime;

	private ArrayList<String> receipt = new ArrayList<>();

	// Protocol - Connection failed
	public static final int REQUEST_FAIL = 050;
	public static final int AUTHORIZED = 051;
	public static final int NOT_AUTHORIZED = 052;
	public static final int CONTENT_START = 053;

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

	public static final int SAME_FILENAME_EXIST = 113;

	public static final int REQUEST_UPLOAD_END = 199;

	// Protocol - Download
	public static final int REQUEST_DOWNLOAD = 201;
	public static final int ALLOW_DOWNLOAD = 202;
	public static final int NOT_ALLOW_DOWNLOAD = 203;

	public static final int CHUNK_START = 204;
	public static final int CHUNK_END = 205;

	public static final int DOWNLOAD_SUCCESS = 206;
	public static final int DOWNLOAD_FAIL = 207;

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

	// Protocol - Login
	public static final int REQUEST_LOGIN = 501;
	public static final int LOGIN_SUCCESS = 502;
	public static final int NO_USERNAME = 503;
	public static final int PASSWORD_WRONG = 504;

	// Protocol - Create User
	public static final int REQUEST_CREATE_USER = 601;
	public static final int USER_CREATED = 602;
	public static final int USERNAME_EXIST = 603;

	// Protocol - Logout
	public static final int REQUEST_LOGOUT = 701;
	public static final int LOGOUT_SUCCESS = 702;
	public static final int LOGOUT_FAIL = 703;

	// Protocol - Error Code
	public static final int NO_ERROR = 900;
	public static final int REQUIRE_LOGIN = 901;
	public static final int UNKNOWN_ERROR = 999;

	public static final int NEUTRAL = 000;
	public static final int SUCCESS = 001;
	public static final int FAIL = 002;

	public static final int REQUEST_CONNECT = 003;
	public static final int CONNECTED = 004;

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
		case DELETE_FILE_NOT_EXIST:
			return "DELETE_FILE_NOT_EXIST";
		case REQUEST_DELETE_END:
			return "REQUEST_DELETE_END";
		case REQUEST_LIST:
			return "REQUEST_LIST";
		case LIST_NO_FILE:
			return "LIST_NO_FILE";
		case LIST_ALLOW:
			return "LIST_ALLOW";
		case REQUEST_LIST_END:
			return "REQUEST_LIST_END";
		default:
			return Integer.toString(msgCode);
		}
	}

	public void createIndex() throws IOException {
		index = new IndexFile();

		try {
			File file = new File("./data/mydedup.index");
			file.getParentFile().mkdirs();
			file.createNewFile();

			FileOutputStream fileout = new FileOutputStream(file, false);
			ObjectOutputStream out = new ObjectOutputStream(fileout);
			out.writeObject(index);
			out.close();
			fileout.close();
		} catch (IOException i) {
			i.printStackTrace();
		}

	}

	public void writeIndex() throws IOException {

		try {
			File file = new File("./data/mydedup.index");
			file.createNewFile();

			FileOutputStream fileout = new FileOutputStream(file, false);
			ObjectOutputStream out = new ObjectOutputStream(fileout);
			out.writeObject(index);
			out.close();
			fileout.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	public void readIndex() throws IOException {
		index = null;
		try {
			FileInputStream filein = new FileInputStream("./data/mydedup.index");
			ObjectInputStream in = new ObjectInputStream(filein);
			index = (IndexFile) in.readObject();
			in.close();
			filein.close();
		} catch (ClassNotFoundException c) {
			c.printStackTrace();
			return;
		} catch (InvalidClassException e) {
			System.out.println("The program is outdated. Please remove all data and rebuild the project.");
			System.out.println("Using command : make clear");
			System.out.println("Caution : It will delete all the data.");
			return;
		}

	}

	public void initIndex() {
		File file = null;
		try {
			file = new File("./data/mydedup.index");
			if (file.exists()) {
				readIndex();
				System.out.println("Server - IndexFile Read.");
			} else {
				createIndex();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// credit from : https://stackoverflow.com/a/11009612
	public static String sha256(String base) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public String generateSessionString(String username) {
		// chose a Character random from this String
		long time = System.nanoTime();
		String key = username + " " + Long.toString(time);
		return sha256(key);
	}

	public ServerHandler(int port, Socket socket) {
		initIndex();
		this.lastTime = LocalDateTime.now();
		System.out.println("Start Time: " + this.lastTime);
		try {
			this.socket = socket;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {

			System.out.println("ServerThread - Accept Connection [" + LocalDateTime.now() + "]");

			LocalDateTime nowTime = LocalDateTime.now();
			System.out.println("Maintance : " + nowTime + " - Clearing unused session");
			this.index.removeExpiredSession();
			try {
				writeIndex();
			} catch (IOException e) {
				e.printStackTrace();
			}

			this.dis = new DataInputStream(this.socket.getInputStream());
			this.dos = new DataOutputStream(this.socket.getOutputStream());

			int msgCode = receiveMsgCode();
			readIndex();

			String sessionKey;
			String username;
			// System.out.println("[FROM Client] Message Code: " +
			// translateMsgCode(msgCode));

			switch (msgCode) {
			case REQUEST_CONNECT:
				System.out.println("[FROM Client] Request signal.");
				this.dos.writeInt(ServerHandler.CONNECTED);
				System.out.println("Server - Sent connected");
				this.socket.close();
				break;

			case REQUEST_CREATE_USER:
				String newUsername = this.dis.readUTF();
				String newPasswordHash = this.dis.readUTF();
				System.out.println("[FROM Client] Create User: " + newUsername + " & " + newPasswordHash);

				if (this.index.hasUser(newUsername)) {
					System.out.println("Server - Username Exist. Return protocol.");
					this.dos.writeInt(ServerHandler.USERNAME_EXIST);
				} else {
					this.index.createUser(newUsername, newPasswordHash);
					System.out.println("Server - Created User:" + newUsername + ", hash=" + newPasswordHash);
					this.dos.writeInt(ServerHandler.USER_CREATED);

					System.out.println("Server - Saving the index information..");
					writeIndex();
					System.out.println("Server - index Saved");
				}
				this.socket.close();
				System.out.println("[Server] End Connection.");
				break;

			case REQUEST_LOGIN:
				System.out.println("Server - Receive Login Request, current Time:" + LocalDateTime.now());
				String loginUsername = this.dis.readUTF();
				String loginPasswordHash = this.dis.readUTF();
				System.out
						.println("[FROM Client] {Username, passwordHash} :" + loginUsername + "," + loginPasswordHash);

				if (!index.hasUser(loginUsername)) {
					System.out.println("Server - Username not correct.");
					this.dos.writeInt(ServerHandler.NO_USERNAME);
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;

				}

				if (index.checkUserPass(loginUsername, loginPasswordHash)) {
					System.out.println("Server - Username and Password correct.");
					this.dos.writeInt(ServerHandler.LOGIN_SUCCESS);

					String key = generateSessionString(loginUsername);
					System.out.println("Key = " + key);
					this.index.recordNewSession(loginUsername, key);
					writeIndex();

					this.dos.writeUTF(key);
				} else {
					System.out.println("Server -  Password not correct.");
					this.dos.writeInt(ServerHandler.PASSWORD_WRONG);
				}

				// this.dos.writeInt(Server.NEUTRAL);
				this.socket.close();
				System.out.println("[Server] End Connection.");
				break;

			case REQUEST_UPLOAD:
				// Receive the protocol information
				System.out.println("[FROM Client] Request Upload, current Time:" + LocalDateTime.now());
				receipt.clear();
				String filename = this.dis.readUTF();
				System.out.println("[FROM Client] Filename = " + filename);
				int filesize = this.dis.readInt();
				System.out.println("[FROM Client] Filesize = " + filesize);

				// Receive Session Key and verify user
				sessionKey = this.dis.readUTF();
				System.out.println(
						"[Client] Receive Session Key = " + sessionKey + ", start checking the user with this key..");

				if (!this.index.hasSession(sessionKey)) {
					System.out.println("Server - No user with this session key.");
					this.dos.writeInt(ServerHandler.NOT_AUTHORIZED);
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;

				} else {
					username = this.index.getUserFromSessionId(sessionKey);
					this.index.updateSession(sessionKey);
					writeIndex();
					
					System.out.println("Server - Confirm Username = " + username);
					// this.dos.writeInt(Server.AUTHORIZED);
				}

				// Start uploading
				msgCode = checkUploadPermission(filename);
				if (msgCode == ALLOW_UPLOAD) {
					msgCode = receiveMsgCode();
					while (msgCode != REQUEST_UPLOAD_END) {
						switch (msgCode) {
						case REQUEST_CHECK_CHUNK:
							String checksum = this.dis.readUTF();
							System.out.println("[FROM Client] The checksum is " + checksum);
							int returnCode = checkChunkExist(checksum);
							if (returnCode == CHUNK_EXIST) {
								duplicateChunk(checksum);
								receipt.add(checksum);
							} else if (returnCode == CHUNK_NOT_EXIST) {
								int msgCode3 = receiveMsgCode();
								if (msgCode3 == REQUEST_SAVE_CHUNK) {
									saveSingleChunk(checksum);
									System.out.println("Save single chunk completed.");
									receipt.add(checksum);
									this.index.recordNewChunk(checksum);
								}
							}

							break;
						case REQUEST_CHECK_ZERO_CHUNK:
							if (this.index.hasChunk("0")) {
								this.index.recordDupZeroChunk();
								receipt.add("0");
							} else {
								this.index.recordNewZeroChunk();
								receipt.add("0");
							}
							break;
						case LOGICAL_CHUNK_INCREMENT:
							this.index.logicalChunk++;
							break;

						}
						msgCode = receiveMsgCode();

					}

					if (msgCode == REQUEST_UPLOAD_END) {
						this.index.fileReceipt.put(filename, receipt);
						this.index.recordNewFile(filename, filesize, username);

						System.out.println("Report Output:");
						this.dos.writeUTF("Report Output:");

						System.out.println("Total number of logical chunks in storage:" + this.index.logicalChunk);
						this.dos.writeUTF("Total number of logical chunks in storage:" + this.index.logicalChunk);

						System.out.println("Number of unique physical chunks in storage:" + this.index.physicalChunk);
						this.dos.writeUTF("Number of unique physical chunks in storage:" + this.index.physicalChunk);

						System.out.println("Number of bytes in storage with deduplication:"
								+ this.index.getPhysicalTotalStorage());
						this.dos.writeUTF("Number of bytes in storage with deduplication:"
								+ this.index.getPhysicalTotalStorage());

						System.out.println("Number of bytes in storage without deduplication:"
								+ this.index.getLogicalTotalStorage());
						this.dos.writeUTF("Number of bytes in storage without deduplication:"
								+ this.index.getLogicalTotalStorage());

						System.out.println("Space saving:" + this.index.getSpaceSave());
						this.dos.writeUTF("Space saving:" + this.index.getSpaceSave());

						System.out.println("[Server] End Connection");
						this.dos.writeUTF("[Server] End Connection");

						writeIndex();
					}

				}
				this.socket.close();
				System.out.println("[Server] End Connection");
				break;

			case REQUEST_DOWNLOAD:
				System.out.println("[FROM Client] Request Download, current Time:" + LocalDateTime.now());
				System.out.println("Starting download Process.");
				String filenameDownload = this.dis.readUTF();
				System.out.println("[FROM Client] Filename = " + filenameDownload);

				sessionKey = this.dis.readUTF();
				System.out.println(
						"[Client] Receive Session Key = " + sessionKey + ", start checking the user with this key..");

				if (!this.index.hasSession(sessionKey)) {
					System.out.println("Server - No user with this session key.");
					this.dos.writeInt(ServerHandler.NOT_AUTHORIZED);
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;

				} else {
					username = this.index.getUserFromSessionId(sessionKey);
					this.index.updateSession(sessionKey);
					writeIndex();
					
					System.out.println("Server - Confirm Username = " + username);
					// this.dos.writeInt(Server.AUTHORIZED);
				}

				msgCode = checkDownloadPermission(username,filenameDownload);
				if (msgCode == ALLOW_DOWNLOAD) {
					// Write the no. of chunk to client
					ArrayList<String> receipt = this.index.getFileReceipt(filenameDownload);
					this.dos.writeInt(receipt.size());
					for (int i = 0; i < receipt.size(); i++) {
						this.sendMsgCode(CHUNK_START);

						// send the chunk size
						String checksum = receipt.get(i);
						if (checksum == "0") {
							this.dos.writeInt(1);
							System.out.println("Chunk 0 - Size = 1");
						} else {
							this.dos.writeInt((int) this.index.getChunkSize(checksum));
							System.out.println(
									"Chunk " + checksum + " - Size = " + (int) this.index.getChunkSize(checksum));
						}

						// Sending the content of file
						StringBuilder sb = new StringBuilder("data/chunk_");
						sb.append(checksum);
						sb.append(".chunk");
						String chunkPath = sb.toString();
						File chunk = new File(chunkPath);

						try {

							if (checksum.equals("0")) {
								this.dos.write(48);
							} else {
								FileInputStream in = new FileInputStream(chunk);
								int c;
								while ((c = in.read()) != -1) {
									this.dos.write(c);
								}
								in.close();
							}
						} catch (FileNotFoundException e) {
							System.out.println("The File is corrupted. FileNotFoundException occur.");
							e.printStackTrace();
						}

						this.sendMsgCode(CHUNK_END);
					}
				}
				msgCode = receiveMsgCode();
				if (msgCode == REQUEST_DOWNLOAD_END) {
					System.out.println("File Transfer success.");
					System.out.println("[Server] End Connection");
				}
				this.dos.writeUTF("[Server] End Connection");
				this.socket.close();
				System.out.println("[Server] End Connection.");
				break;

			case REQUEST_DELETE:
				System.out.println("[FROM Client] Request Delete File, current Time:" + LocalDateTime.now());
				String filenameDelete = this.dis.readUTF();
				System.out.println("Delete filename = " + filenameDelete);

				sessionKey = this.dis.readUTF();
				System.out.println(
						"[Client] Receive Session Key = " + sessionKey + ", start checking the user with this key..");

				if (!this.index.hasSession(sessionKey)) {
					System.out.println("Server - No user with this session key.");
					this.dos.writeInt(ServerHandler.NOT_AUTHORIZED);
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;

				} else {
					username = this.index.getUserFromSessionId(sessionKey);
					this.index.updateSession(sessionKey);
					writeIndex();
					
					System.out.println("Server - Confirm Username = " + username);
					// this.dos.writeInt(Server.AUTHORIZED);
				}

				System.out.println("Starting Delete Process..");
				if (this.index.hasFile(filenameDelete) && this.index.getFileList(username).contains(filenameDelete)) {
					ArrayList<String> fileReceipt = (ArrayList<String>) this.index.getFileReceipt(filenameDelete);
					for (int i = 0; i < fileReceipt.size(); i++) {
						this.index.deleteChunk(fileReceipt.get(i));
					}
					this.index.deleteFile(filenameDelete, username);
					writeIndex();
					this.dos.writeInt(DELETE_SUCCESS);
					System.out.println("The file is deleted");
				} else {
					this.dos.writeInt(DELETE_FILE_NOT_EXIST);
				}
				// System.out.println(this.dis.readUTF());
				this.socket.close();
				System.out.println("[Server] End Connection.");
				break;

			case REQUEST_LIST:
				System.out.println("[FROM Client] Request List, current Time:" + LocalDateTime.now());

				sessionKey = this.dis.readUTF();
				System.out.println(
						"[Client] Receive Session Key = " + sessionKey + ", start checking the user with this key..");

				if (!this.index.hasSession(sessionKey)) {
					System.out.println("Server - No user with this session key.");
					this.dos.writeInt(ServerHandler.NOT_AUTHORIZED);
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;

				} else {
					username = this.index.getUserFromSessionId(sessionKey);
					this.index.updateSession(sessionKey);
					writeIndex();
					
					System.out.println("Server - Confirm Username = " + username);
					System.out.println(
							"Server - " + username + " Timeout : " + this.index.sessionExpiryTimeList.get(sessionKey));
					// this.dos.writeInt(Server.AUTHORIZED);
				}

				if (this.index.getFileList(username).isEmpty()) {
					System.out.println("Noticed the client that have no file");
					sendMsgCode(LIST_NO_FILE);
				} else {
					System.out.println("Allow Listing file");
					sendMsgCode(LIST_ALLOW);

					ArrayList<String> filelist = this.index.getFileList(username);

					for (int i = 0; i < filelist.size(); i++) {
						// Send Filename
						String filenameL = filelist.get(i);
						this.dos.writeUTF(filenameL);

						// Send File Size String
						DecimalFormat df = new DecimalFormat("0.00");
						StringBuilder sb = new StringBuilder("");
						long filesizeL = this.index.getFilesize(filenameL);
						if (filesizeL >= 1024 * 1024) {
							sb.append(df.format(filesizeL / 1024.0F / 1024.0F));
							sb.append(" MB");
						} else if (filesizeL >= 1024) {
							sb.append(df.format(filesizeL / 1024.0F));
							sb.append(" KB");
						} else if (filesizeL < 1024) {
							sb.append(filesizeL);
							sb.append(" B");
						}

						this.dos.writeUTF(sb.toString());
					}

					this.dos.writeUTF("??end");
				}
				this.socket.close();
				System.out.println("[Server] End Connection.");
				break;

			}
		} catch (NullPointerException npe) {
			System.out.println("The port is used. Please check any program running at 59090");
			npe.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int checkDownloadPermission(String username, String filename) throws IOException {
		// TODO Auto-generated method stub
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[MSG] null");
			return REQUEST_FAIL;
		}

		// Checking the filename exist or not
		// If exist -> Allow, not exist -> not allow
		if (this.index.hasFile(filename) && this.index.getFileList(username).contains(filename)) {
			System.out.println("[Server] Allow Download from Client");
			this.dos.writeInt(ALLOW_DOWNLOAD);
			return ALLOW_DOWNLOAD;
		} else {
			System.out.println("[Server] Not Allow Download from Client");
			this.dos.writeInt(NOT_ALLOW_DOWNLOAD);
			return NOT_ALLOW_DOWNLOAD;
		}
	}

	public int checkUploadPermission(String filename) throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[MSG] null");
			return REQUEST_FAIL;
		}

		// Checking the filename exist or not
		// If exist -> Not Allow, not exist -> Allow
		if (!this.index.hasFile(filename)) {
			System.out.println("[Server] Allow Upload from Client");
			this.dos.writeInt(ALLOW_UPLOAD);
			return ALLOW_UPLOAD;
		} else {
			System.out.println("[Server] Filename exist. Not Allow Upload from Client");
			this.dos.writeInt(NOT_ALLOW_UPLOAD);
			return NOT_ALLOW_UPLOAD;
		}
	}

	public int checkChunkExist(String checksum) throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[MSG] null");
			return REQUEST_FAIL;
		}

		// check the chunk inside index or not
		if (index.hasChunk(checksum)) {
			System.out.println("[Server] above checksum, " + translateMsgCode(CHUNK_EXIST));
			this.dos.writeInt(CHUNK_EXIST);
			return CHUNK_EXIST;
		} else {
			System.out.println("[Server] above checksum, " + translateMsgCode(CHUNK_NOT_EXIST));
			this.dos.writeInt(CHUNK_NOT_EXIST);
			return CHUNK_NOT_EXIST;
		}

	}

	public void duplicateChunk(String checksum) {
		this.index.recordDupChunk(checksum);
	}

	public void saveSingleChunk(String checksum) throws IOException {
		StringBuilder sb = new StringBuilder("data/chunk_");
		sb.append(checksum);
		sb.append(".chunk");
		String pathname = sb.toString();

		FileOutputStream out = new FileOutputStream(pathname);
		int chunkSize = this.dis.readInt();
		System.out.println("[FROM Client] The chunk size = " + chunkSize);

		int read = 0;
		int remaining = chunkSize;
		byte[] buffer = new byte[4096];

		while ((read = this.dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
			remaining -= read;
			out.write(buffer, 0, read);
		}
		out.close();

		this.dos.writeInt(CHUNK_SAVED);
		this.index.recordNewChunkSize(checksum, (long) chunkSize);
	}

	public int receiveMsgCode() throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[MSG] null");
			return 1;
		}
		int received = this.dis.readInt();
		// this.dos.writeUTF("[FROM Server] Message Code received. Content:" +
		// translateMsgCode(received));
		System.out.println("[FROM Client] Message code received. Content:" + translateMsgCode(received));
		return received;
	}

	public void sendMsgCode(int msgCode) throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("No connection.");
		}
		this.dos.writeInt(msgCode);
		// System.out.println(this.dis.readUTF());
	}

	public String receiveString() throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[String] null");
			return "NULL";
		}
		String received = this.dis.readUTF();
		// this.dos.writeUTF("[FROM Server] String received. Content:" + received);
		return received;
	}

}