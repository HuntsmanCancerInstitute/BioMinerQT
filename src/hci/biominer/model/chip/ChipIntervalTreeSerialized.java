package hci.biominer.model.chip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import hci.biominer.model.intervaltree.*;

public class ChipIntervalTreeSerialized {
	
	public static void saveSerializedTree(HashMap<String,IntervalTree<Chip>> invlHash, File directory, String name) throws Exception {
		if (!directory.exists()) {
			throw new Exception(String.format("The specified directory could not be found",directory.getAbsolutePath()));
		}
		
		File saveFile = new File(directory,name);
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(saveFile));
		out.writeObject(invlHash);
		out.close();
	}
	
	public static HashMap<String,IntervalTree<Chip>> getSerializedTree(File filename) throws Exception{
		if (!filename.exists()) {
			throw new Exception(String.format("The specified filename could not be found",filename.getAbsolutePath()));
		}
		HashMap<String,IntervalTree<Chip>> invHash = new HashMap<String,IntervalTree<Chip>>();
		FileInputStream fis = new FileInputStream(filename);
		try {

			ObjectInputStream reader = new ObjectInputStream(fis);
			invHash = (HashMap<String,IntervalTree<Chip>>) reader.readObject();
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				fis.close();
			} catch (Exception ex) {}
		}
		
		return invHash;
	}

}
