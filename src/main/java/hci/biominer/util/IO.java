package hci.biominer.util;
import java.io.*;
import java.util.zip.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


/**
 * Static methods for Input Output related tasks.
 */
public class IO {
	
	public static boolean isASCII(String test) {
    	byte[] byteArray = test.getBytes();
    	CharsetDecoder decoder = Charset.forName("US-ASCII").newDecoder();
        try {
            CharBuffer buffer = decoder.decode(ByteBuffer.wrap(byteArray));
            return true;
 

        } catch (CharacterCodingException e) {
            return false;
        }
    }


	/**Fetches a BufferedReader from a url or file, zip/gz OK.*/
	public static BufferedReader fetchBufferedReader(String s){
		try {
			if (s.startsWith("http")) return fetchBufferedReader (new URL(s));
			return fetchBufferedReader (new File (s));
		} catch (Exception e) {
			System.out.println("Problem fetching buffered reader fro -> "+s);
			e.printStackTrace();
		} 
		return null;

	}

	/**Fetches a BufferedReader from a url, zip/gz OK.*/
	public static BufferedReader fetchBufferedReader(URL url) throws IOException{
		BufferedReader in = null;
		InputStream is = url.openStream();
		String name = url.toString();
		if (name.endsWith(".gz")) {
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)));
		}
		else if (name.endsWith(".zip")){
			ZipInputStream zis = new ZipInputStream(is);
			zis.getNextEntry();
			in = new BufferedReader(new InputStreamReader(zis));
		}
		else in = new BufferedReader(new InputStreamReader(is));
		return in;
	}

	/**Returns a gz zip or straight file reader on the file based on it's extension.
	 * @author davidnix*/
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

	/**Returns a gz zip or straight input strean on the file based on it's extension compression.*/
	public static InputStream fetchInputStream( File txtFile) throws IOException{
		InputStream in;
		String name = txtFile.getName().toLowerCase();
		if (name.endsWith(".zip")) {
			ZipFile zf = new ZipFile(txtFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			in = zf.getInputStream(ze);
		}
		else if (name.endsWith(".gz")) {
			in = new GZIPInputStream(new FileInputStream(txtFile));
		}
		else in = new FileInputStream(txtFile);
		return in;
	}

	/**Returns a BufferedReader from which you can call readLine() directly from a single entry zipped file without decompressing.
	 * Returns null if there is a problem.*/
	public static BufferedReader fetchReaderOnZippedFile (File zippedFile) {
		try {
			ZipFile zf = new ZipFile(zippedFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			return new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**Returns a BufferedReader from which you can call readLine() directly from a gzipped (.gz) file without decompressing.
	 * Returns null if there is a problem.*/
	public static BufferedReader fetchReaderOnGZippedFile (File gzFile) {
		try {
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gzFile))));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**Returns the standard stack trace in String form.*/
	public static String getStackTrace(Exception e){
		StringBuilder sb = new StringBuilder(e.toString());
		for (StackTraceElement ste: e.getStackTrace()) {
			sb.append("\n\tat ");
			sb.append(ste.toString());
		}
		return sb.toString();
	}

	
}
