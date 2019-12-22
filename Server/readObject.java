import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

class readObject{

	public static void main(String[] args) throws IOException{
		IndexFile temp = null;
		try{
			FileInputStream filein = new FileInputStream("./data/mydedup.index");
			ObjectInputStream in = new ObjectInputStream(filein);
			temp = (IndexFile) in.readObject();
			in.close();
			filein.close();
		}
		catch (ClassNotFoundException c){
			c.printStackTrace();
			return;
		}
		catch (InvalidClassException e){
			System.out.println("The program is outdated. Please remove all data and rebuild the project.");
			System.out.println("Using command : make rebuild");
			System.out.println("Caution : It will delete all the data.");
			return;
		}

		System.out.println("==Index Reader==");

		System.out.println("referenceList:");
		Iterator<Map.Entry<String, Integer>> iterator = temp.referenceList.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry<String, Integer> pair = iterator.next();
			String temp2 = pair.getKey()+" "+pair.getValue();

			System.out.println(temp2);
			
			iterator.remove();
		} 

		System.out.println("fileReceipt:");
		Iterator<Map.Entry<String, ArrayList<String>>> iterator2 = temp.fileReceipt.entrySet().iterator();
		while (iterator2.hasNext()){
			Map.Entry<String, ArrayList<String>> pair = iterator2.next();
			ArrayList<String> list = pair.getValue();
			String temp2 = pair.getKey()+" "+list.toString();

			System.out.println(temp2);
			
			iterator2.remove();
		} 

		System.out.println("chunkSizeList:");
		Iterator<Map.Entry<String, Long>> iterator3 = temp.chunkSizeList.entrySet().iterator();
		while (iterator3.hasNext()){
			Map.Entry<String, Long> pair = iterator3.next();
			String temp2 = pair.getKey()+" "+pair.getValue();

			System.out.println(temp2);
			
			iterator3.remove();
		} 

		System.out.println("==Index Reader==");
	}

}