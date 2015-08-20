package hci.biominer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.web.multipart.MultipartFile;

public class ModelUtil {
	
	public static final Pattern TAB = Pattern.compile("\\t");
	
	/**Calls garbage collection then returns memory used by app.*/
	public static long fetchUsedMemory(){
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    System.gc(); System.gc(); System.gc(); System.gc();
	    return Runtime.getRuntime().totalMemory() -
	      Runtime.getRuntime().freeMemory();
	}
	
	/**Prints the string to System.err and exits with 1.*/
	public static void errorExit(String s){
		System.err.println(s);
		System.exit(1);
	}

	/**Given a String of ints delimited by something, will parse or return null.*/
	public static int[] stringArrayToInts(String s, String delimiter){
		String[] tokens = s.split(delimiter);
		int[] num = new int[tokens.length];
		try {
			for (int i=0; i< tokens.length; i++){
				num[i] = Integer.parseInt(tokens[i]);
			}
			return num;
		} catch (Exception e){
			return null;
		}
	}
	
	/**Returns a String separated by commas for each bin.*/
	public static String stringArrayToString(String[] s, String separator){
		if (s==null) return "";
		int len = s.length;
		if (len==1) return s[0];
		if (len==0) return "";
		StringBuffer sb = new StringBuffer(s[0]);
		for (int i=1; i<len; i++){
			sb.append(separator);
			sb.append(s[i]);
		}
		return sb.toString();
	}	
	
	/**Returns a gz zip or straight file reader on the file based on it's extension.*/
	public static BufferedReader fetchBufferedReader( File txtFile) throws IOException{
		BufferedReader in;
		String name = txtFile.getName().toLowerCase();
		if (name.endsWith(".zip")) {
			ZipFile zf = new ZipFile(txtFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			in = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
		}
		else if (name.endsWith(".gz")) {
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(txtFile))));
		}
		else in = new BufferedReader (new FileReader (txtFile));
		return in;
	}
	
	

	public static String arrayListToString(@SuppressWarnings("rawtypes") ArrayList al, String seporator) {
		StringBuilder sb = new StringBuilder();
		sb.append(al.get(0).toString());
		int num = al.size();
		for (int i=1; i< num; i++){
			sb.append(seporator);
			sb.append(al.get(i).toString());
		}
		return sb.toString();
	}
	
	/**Saves an object to disk.*/
	public static boolean saveObject(File file, Object ob) {
		try {
			ObjectOutputStream out =
				new ObjectOutputStream(new FileOutputStream(file));
			out.writeObject(ob);
			out.close();
			return true;
		} catch (Exception e) {
			System.out.println("There appears to be a problem with saving this file: "+ file);
			e.printStackTrace();
		}
		return false;
	}
	
	/**Loads a serialized Object saved to disk.
	 * Can be zip/gz compressed too.*/
	public static Object loadObject(File file) {
		Object a = null;
		try {
			ObjectInputStream in;
			if (file.getName().endsWith(".zip")){
				ZipFile zf = new ZipFile(file);
				ZipEntry ze = (ZipEntry) zf.entries().nextElement();
				in = new ObjectInputStream( zf.getInputStream(ze));
			}
			else if (file.getName().endsWith(".gz")) {
				in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
			}
			else in = new ObjectInputStream(new FileInputStream(file));
			a = in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem fetchObject() "+file);
		}
		return a;
	}

}
