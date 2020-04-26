import java.io.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings("serial")
class IndexFile implements java.io.Serializable {
	
	//TODO Change the timeout
	public static final int SESSION_DURATION_IN_MIN = 1;

	public int logicalChunk;
	public int physicalChunk;
	public long storageBefore;
	public long storageAfter;

	public HashMap<String, Integer> referenceList;
	public HashMap<String, Long> chunkSizeList;
	public HashMap<String, ArrayList<String>> fileReceipt;
	public HashMap<String, Long> filesizeList;
	
	public HashMap<String, String> userPassList;
	public HashMap<String, ArrayList<String>> userFileList;
	
	public HashMap<String, String> sessionUserList;
	public HashMap<String, LocalDateTime> sessionExpiryTimeList;

	public IndexFile(){
		referenceList = new HashMap<>();
		fileReceipt = new HashMap<>();
		chunkSizeList = new HashMap<>();
		filesizeList = new HashMap<>();
		userFileList = new HashMap<>();
		userPassList = new HashMap<>();
		sessionUserList = new HashMap<>();
		sessionExpiryTimeList = new HashMap<>();
		
		logicalChunk = 0;
		physicalChunk = 0;
		storageAfter = 0;
		storageBefore =0;
	}

	public boolean hasData(){
		return !this.fileReceipt.isEmpty();
	}

	public boolean hasFile(String filename){
		return this.fileReceipt.containsKey(filename);
	}

	public boolean hasChunk(String chunkChecksum){
		return this.referenceList.containsKey(chunkChecksum);
	}

	public int getChunkReference(String chunkChecksum){
		//System.out.println("chunkChecksum = "+chunkChecksum);
		return this.referenceList.get(chunkChecksum);
	}

	public long getChunkSize(String chunkChecksum){
		if (chunkChecksum.equals("0")) {
			return 1;
		}
		else {
			return this.chunkSizeList.get(chunkChecksum);
		}
	}

	public void recordNewChunk(String chunkChecksum){
		this.referenceList.put(chunkChecksum,1);
		this.logicalChunk++;
		this.physicalChunk++;
	}
	
	public void recordNewZeroChunk() {
		this.referenceList.put("0", 1);
	}

	public void recordNewChunkSize(String chunkChecksum,long filesize){
		this.chunkSizeList.put(chunkChecksum,filesize);
	}

	public void recordFileSize(String filename, long filesize){
		this.filesizeList.put(filename, filesize);
	}

	public void recordNewFile(String filename, long filesize, String owner) {
		this.filesizeList.put(filename, filesize);
		this.userFileList.get(owner).add(filename);
	}
	
	public void recordDupChunk(String chunkChecksum){
		this.referenceList.put(chunkChecksum,this.getChunkReference(chunkChecksum)+1);
		this.logicalChunk++;
	}
	
	public void recordDupZeroChunk() {
		this.referenceList.put("0",this.getChunkReference("0")+1);
	}

	public void deleteChunk(String chunkChecksum){
		this.referenceList.replace(chunkChecksum, this.getChunkReference(chunkChecksum)-1);
		if (this.getChunkReference(chunkChecksum) <= 0){
			StringBuilder sb = new StringBuilder("data/chunk_");
			String chunkPath = sb.append(chunkChecksum).append(".chunk").toString();
			File chunk = new File(chunkPath);
			chunk.delete();
			System.out.println("chunk "+chunkChecksum+" is deleted");
			this.referenceList.remove(chunkChecksum);
			this.chunkSizeList.remove(chunkChecksum);
		}
	}

	public void deleteFile(String filename){
		this.fileReceipt.remove(filename);
		this.filesizeList.remove(filename);
	}

	public void deleteFile(String filename, String owner) {
		this.fileReceipt.remove(filename);
		this.filesizeList.remove(filename);
		
		this.userFileList.get(owner).remove(filename);
	}
	
	public long getPhysicalTotalStorage(){
		long total = (long) 0.0;
		for (Map.Entry<String, Long> entry : this.chunkSizeList.entrySet()){
			total += entry.getValue();
		}
		return total;
	}

	public long getLogicalTotalStorage(){
		long total = (long) 0.0;
		for (Map.Entry<String, Long> entry : this.chunkSizeList.entrySet()){
			total += entry.getValue() * this.getChunkReference(entry.getKey());
		}
		return total;
	}

	public double getSpaceSave(){
		return (double)1 - (double)this.getPhysicalTotalStorage()/this.getLogicalTotalStorage();
	}
	
	public ArrayList<String> getFileReceipt(String filename){
		return this.fileReceipt.get(filename);
	}

	public void printStatus(){
		System.out.println("Existing Files: ");
		long totalMemory = 0;
		for (Map.Entry<String, Long> entry : this.filesizeList.entrySet()){
			System.out.println(entry.getKey()+"("+entry.getValue()+" bytes)");
			totalMemory += entry.getValue();
		}
		System.out.println("Total Memory = "+totalMemory+" bytes.");
	}
	
	public long getFilesize(String filename) {
		return this.filesizeList.get(filename);
	}
	
	// User Management Function:
	
	public void createUser(String username, String passwordHash) {
		this.userPassList.put(username, passwordHash);
		this.userFileList.put(username, new ArrayList<String>());
	}
	
	public void deleteUser(String username) {
		this.userPassList.remove(username);
		this.userFileList.remove(username);
	}
	
	public boolean hasUser(String username) {
		return this.userPassList.containsKey(username);
	}
	
	public String getPasswordHash(String username) {
		return this.userPassList.get(username);
	}
	
	public boolean checkUserPass(String username, String passwordHash) {
		if (!this.hasUser(username)) {
			return false;
		}
		return this.userPassList.get(username).equals(passwordHash);
	}
	
	public void recordNewSession(String username, String key) {
		this.sessionUserList.put(key, username);
		this.sessionExpiryTimeList.put(key, LocalDateTime.now().plusMinutes(IndexFile.SESSION_DURATION_IN_MIN));
	}
	
	public String getUserFromSessionId(String key) {
		return this.sessionUserList.get(key);
	}
	
	public boolean hasSession(String key) {
		return this.sessionUserList.containsKey(key);
	}
	
	public void updateSession(String key) {
		this.sessionExpiryTimeList.replace(key, LocalDateTime.now().plusMinutes(IndexFile.SESSION_DURATION_IN_MIN));
	}
	
	public void removeExpiredSession() {
		System.out.println("IndexFile - Remove Session.");
		Iterator<String> it = this.sessionExpiryTimeList.keySet().iterator();
		
		while (it.hasNext()) {
			String key = it.next();
			System.out.println("Item : "+key+" "+this.sessionExpiryTimeList.get(key));
			
			LocalDateTime now = LocalDateTime.now();
			System.out.println("Now  : "+now);
			
			System.out.println("Bool : "+now.isAfter(this.sessionExpiryTimeList.get(key)));
			
			//TODO Change the timeout to 15 mins
			if (now.isAfter(this.sessionExpiryTimeList.get(key))) {
				it.remove();
				String user = this.sessionUserList.remove(key);
				System.out.println("IndexFile - "+user+" session terminated.");
			}
		}
		
	}
	
	public ArrayList<String> getFileList(String owner){
		return this.userFileList.get(owner);
	}

}