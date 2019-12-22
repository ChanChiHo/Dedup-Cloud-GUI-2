import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.net.InetAddress;
import java.io.*;

public class Server extends Thread {

	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private DataInputStream dis = null;
	private DataOutputStream dos = null;
	public IndexFile index;
	private ArrayList<String> receipt = new ArrayList<>();

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
			return "ERROR";
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
			} else {
				createIndex();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Server(int port) {
		initIndex();
		try {
			this.serverSocket = new ServerSocket(port);
			System.out.println("The data server is still running... ");
			System.out.println("Running at : " + InetAddress.getLocalHost().getHostAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				this.socket = serverSocket.accept();
				initIndex();
				
				this.dis = new DataInputStream(this.socket.getInputStream());
				this.dos = new DataOutputStream(this.socket.getOutputStream());

				int msgCode = receiveMsgCode();
				//System.out.println("[FROM Client] Message Code: " + translateMsgCode(msgCode));
				
				switch (msgCode) {
				case REQUEST_UPLOAD:
					receipt.clear();
					String filename = this.dis.readUTF();
					System.out.println("[FROM Client] Filename = " + filename);
					int filesize = this.dis.readInt();
					System.out.println("[FROM Client] Filesize = "+ filesize);
					msgCode = checkUploadPermission(filename);
					if (msgCode == ALLOW_UPLOAD) {
						msgCode = receiveMsgCode();
						while (msgCode != REQUEST_UPLOAD_END) {
							switch (msgCode) {
							case REQUEST_CHECK_CHUNK:
								String checksum = this.dis.readUTF();
								System.out.println("[FROM Client] The checksum is "+checksum);
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
							this.index.recordFileSize(filename, filesize);
							
							
							System.out.println("Report Output:");
							this.dos.writeUTF("Report Output:");
							
							System.out.println("Total number of logical chunks in storage:"+this.index.logicalChunk);
							this.dos.writeUTF("Total number of logical chunks in storage:"+this.index.logicalChunk);
							
							System.out.println("Number of unique physical chunks in storage:"+this.index.physicalChunk);
							this.dos.writeUTF("Number of unique physical chunks in storage:"+this.index.physicalChunk);
							
							System.out.println("Number of bytes in storage with deduplication:"+this.index.getPhysicalTotalStorage());
							this.dos.writeUTF("Number of bytes in storage with deduplication:"+this.index.getPhysicalTotalStorage());
							
							System.out.println("Number of bytes in storage without deduplication:"+this.index.getLogicalTotalStorage());
							this.dos.writeUTF("Number of bytes in storage without deduplication:"+this.index.getLogicalTotalStorage());
							
							System.out.println("Space saving:"+this.index.getSpaceSave());
							this.dos.writeUTF("Space saving:"+this.index.getSpaceSave());
							
							System.out.println("[Server] End Connection");
							this.dos.writeUTF("[Server] End Connection");
							
							
							writeIndex();
						}
						
					}
					this.socket.close();
					System.out.println("[Server] End Connection");
					break;
					
				case REQUEST_DOWNLOAD:
					System.out.println("Starting download Process.");
					String filenameDownload= this.dis.readUTF();
					System.out.println("[FROM Client] Filename = " + filenameDownload);
					msgCode = checkDownloadPermission(filenameDownload);
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
								this.dos.writeInt((int)this.index.getChunkSize(checksum));
								System.out.println("Chunk "+checksum+" - Size = "+(int)this.index.getChunkSize(checksum));
							}
							
							// Sending the content of file
							StringBuilder sb = new StringBuilder("data/chunk_");
							sb.append(checksum);
							sb.append(".chunk");
							String chunkPath = sb.toString();
							File chunk = new File(chunkPath);
							
							try{
								
								if (checksum.equals("0")) {
									this.dos.write(48);
								}
								else {
									FileInputStream in = new FileInputStream(chunk);
									int c;
									while ((c = in.read())!= -1){
										this.dos.write(c);
									}	
									in.close();
								}
							}
							catch (FileNotFoundException e){
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
					String filenameDelete = this.dis.readUTF();
					System.out.println("Delete filename = "+filenameDelete);
					System.out.println("Starting Delete Process..");
					if (this.index.hasFile(filenameDelete)) {
						ArrayList<String> fileReceipt = (ArrayList<String>) this.index.getFileReceipt(filenameDelete);
						for (int i = 0;i < fileReceipt.size();i++) {
							this.index.deleteChunk(fileReceipt.get(i));
						}
						this.index.deleteFile(filenameDelete);
						writeIndex();
						this.dos.writeInt(DELETE_SUCCESS);
						System.out.println("The file is deleted");
					}
					else {
						this.dos.writeInt(DELETE_FILE_NOT_EXIST);
					}
					//System.out.println(this.dis.readUTF());
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;
					
				case REQUEST_LIST:
					System.out.println("[Client] Request List file.");
					if (this.index.filesizeList.isEmpty()) {
						System.out.println("Noticed the client that have no file");
						sendMsgCode(LIST_NO_FILE);
					}
					else {
						System.out.println("Allow Listing file");
						sendMsgCode(LIST_ALLOW);
						for (Map.Entry<String, Long> entry : this.index.filesizeList.entrySet()) {
							this.dos.writeUTF(entry.getKey());
							StringBuilder sb = new StringBuilder("");
							if (entry.getValue() >= 1024*1024) {
								sb.append(entry.getValue()/1024/1024);
								sb.append(" MB");
							} else if (entry.getValue() >= 1024) {
								sb.append(entry.getValue()/1024);
								sb.append(" KB");
							} else if (entry.getValue() < 1024) {
								sb.append(entry.getValue());
								sb.append(" B");
							}
							
							this.dos.writeUTF(sb.toString());
						}
						this.dos.writeUTF("##end");
					}
					this.socket.close();
					System.out.println("[Server] End Connection.");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int checkDownloadPermission(String filename) throws IOException {
		// TODO Auto-generated method stub
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[MSG] null");
			return REQUEST_FAIL;
		}
		
		// Checking the filename exist or not
		// If exist -> Allow, not exist -> not allow
		if (this.index.hasFile(filename)) {
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
			System.out.println("[Server] above checksum, "+translateMsgCode(CHUNK_EXIST));
			this.dos.writeInt(CHUNK_EXIST);
			return CHUNK_EXIST;
		} else {
			System.out.println("[Server] above checksum, "+translateMsgCode(CHUNK_NOT_EXIST));
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
		System.out.println("[FROM Client] The chunk size = "+chunkSize);
		
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
		//this.dos.writeUTF("[FROM Server] Message Code received. Content:" + translateMsgCode(received));
		//System.out.println("[FROM Client] Message code received. Content:"+translateMsgCode(received));
		return received;
	}
	
	public void sendMsgCode(int msgCode) throws IOException{
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("No connection.");
		}
		this.dos.writeInt(msgCode);
		//System.out.println(this.dis.readUTF());
	}

	public String receiveString() throws IOException {
		if (this.socket == null || this.dis == null || this.dos == null) {
			System.out.println("[String] null");
			return "NULL";
		}
		String received = this.dis.readUTF();
		//this.dos.writeUTF("[FROM Server] String received. Content:" + received);
		return received;
	}

	public static void main(String[] args) throws IOException {
		Server server = new Server(59090);
		server.start();
	}
}