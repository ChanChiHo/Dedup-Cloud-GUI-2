import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@SuppressWarnings("serial")
class IndexFile implements java.io.Serializable {

	public int logicalChunk;
	public int physicalChunk;
	public long storageBefore;
	public long storageAfter;

	public HashMap<String, Integer> referenceList;
	public HashMap<String, Long> chunkSizeList;
	public HashMap<String, ArrayList<String>> fileReceipt;
	public HashMap<String, Long> filesizeList;

	public IndexFile(){
		referenceList = new HashMap<>();
		fileReceipt = new HashMap<>();
		chunkSizeList = new HashMap<>();
		filesizeList = new HashMap<>();
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
}