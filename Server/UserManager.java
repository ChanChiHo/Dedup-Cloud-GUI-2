
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;

public class UserManager {
	
	private IndexFile index;
	
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
	
	public void getUserList() throws IOException {
		readIndex();

		System.out.println("==User Reader==");
		System.out.println("userPassList:");
		Iterator<Map.Entry<String, String>> iterator = index.userPassList.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> pair = iterator.next();
			String temp2 = pair.getKey() + " " + pair.getValue();

			System.out.println(temp2);

			iterator.remove();
		}
	}
	
	public void deleteUser(String username) throws IOException {
		readIndex();
		index.deleteUser(username);
		writeIndex();
	}
	
	public void clearSession() throws IOException{
		readIndex();
		index.sessionUserList.clear();
		writeIndex();
	}
	
	public void getSessionList() throws IOException{
		readIndex();
		
		Iterator<Map.Entry<String, String>> iterator = index.sessionUserList.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> pair = iterator.next();
			String temp2 = pair.getKey() + " " + pair.getValue()+" "+index.sessionExpiryTimeList.get(pair.getKey());

			System.out.println(temp2);

			iterator.remove();
		}
	}

	public static void main(String[] args) throws IOException {
		UserManager obj = new UserManager();
		if (args.length == 0) {
			obj.getUserList();
			return;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
			obj.deleteUser(args[1]);
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("session")) {
			obj.getSessionList();
		}
		if (args[0].equalsIgnoreCase("session") && args[0].equalsIgnoreCase("clear")) {
			obj.clearSession();
		}
		System.out.println(args[0]);
	}
}
