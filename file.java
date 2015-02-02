
import java.util.*;
import java.io.*;


/**
* Convenience class for quickly using files
*
* // FROM JDK API: For top efficiency, consider wrapping an InputStreamReader  within a BufferedReader.
* //               For example:
* //               BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
*
*
* // note- will leave off a single trailing /n in reading files not saved by this file class
*
* // JON-  Note-  saveStuff adds a line separator at end of argument!
*
* // additional method file.clear() - sets contents to "".
*
* //  NOTE NEW METHOD!! (formerly saveStuff now saveStuff & saveStuffLn)
*
*/
public class file {
	private String encoding = "UTF-8";
	//private String encoding = "Cp1252"; // - we might want to have a list of encodings...
	private int numLines;
	private FileInputStream fis; // the 3 musketeers
	private InputStreamReader isr;
	private BufferedReader br;
	private FileOutputStream fos; // their 3 brothers
	private OutputStreamWriter osw;
	private BufferedWriter bw;
	private boolean lineNumberCoherence;
	private boolean frozen = false;
	public String fileName = "";
	File test; // used for functionality of Java's "File" class

	public file(String filename) { // here's the constructor
		lineNumberCoherence = false;
		fileName = filename;
		numLines = 0;
	}

	public file() {
		lineNumberCoherence = false;
		fileName = "";
		numLines = 0;
	}

	private void o(String message) {
		System.out.println(message);
	}

	public synchronized void initRead() { // this is for manual file access -parsing from a file //
		frozen = true;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
		}catch (UnsupportedEncodingException uee) {
			// get next encoding here?
		}
		catch (Exception e) {
			System.out.println("Error- " + e.toString());
		}
	}

	public synchronized void initWrite(boolean append) {
		frozen = true;
		lineNumberCoherence=false;
			try {
			fos = new FileOutputStream(fileName, append);
			osw = new OutputStreamWriter(fos, encoding);
			bw = new BufferedWriter(osw);
		}catch (Exception e) {
			System.out.println("Error- " + e.toString());
		}
	}

	public synchronized String readLine() {
		try {
			return br.readLine();
		}catch (Exception e) {
			o("Exception in readLine " + e.toString());
			e.printStackTrace();
			closeRead(); // 2.2002
			return null;
		}// returning null is basically the same as throwing an exception anyway
	}

	public synchronized void write(String stuffa) {
		try {
			bw.write(stuffa);
		}catch (Exception e) {
			System.out.println("Error- " + e.toString());
		}
	}

	public synchronized void closeRead() {
		frozen = false;
			try {
			br.close();
			isr.close();
			fis.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void closeWrite() {
		frozen = false;
		try {
			bw.close();
			osw.close();
			fos.close();
		}catch (Exception e) {
			System.out.println("Error- " + e.toString());
		}
	}

	public synchronized void clear () {
		checkFrozen();
		lineNumberCoherence = false;
			try {
			fos = new FileOutputStream(fileName,false);
			osw = new OutputStreamWriter(fos,encoding);
			bw = new BufferedWriter(osw);
			bw.write("");
			bw.close();
			osw.close();
			fos.close();
		} catch (UnsupportedEncodingException uee) {
			// change encoding??
		}catch (Exception k) {
			k.printStackTrace();
		}
	}

	// this should put stuffa (text) in a file
	public synchronized void saveStuff (String stuffa, boolean append) {
		checkFrozen();
		lineNumberCoherence = false;
		try {
			fos = new FileOutputStream(fileName,append);
			osw = new OutputStreamWriter(fos,encoding);
			bw = new BufferedWriter(osw);
			bw.write(stuffa + System.getProperty("line.separator"));
			bw.close();
			osw.close();
			fos.close();
		} catch (UnsupportedEncodingException uee) {
			// change encoding??
		} catch (IOException e) {
			System.out.println("Error 1- " + e.toString());
		} catch (SecurityException b) {
			System.out.println("Go check security stuff");
		}catch (NullPointerException c) {
			System.out.println("Null pointer, dude!"+c.toString());
		}catch (Exception k) { // the rest of 'em
			k.printStackTrace();
		}
	}

	public synchronized void saveStuff(String stuffa) { //leave out the boolean, it's auto-overwrite
		saveStuff(stuffa, false);
	}

	// LUC -- WE can't be appending the extra "\n" to the end of the stuff that
	// we are saving here.  This is hosing the firstTimeDialog where we generate
	// the esto file for the first time. It calls save stuff 3 times in a row, and
	// the file is getting filled with extra blank lines
	// Sorry about the new function here



	// NOTES:
	// this returns stuffa from a text file
	//
	// 1.12.2002 - jon
	// Replace string here with StringBuffer. This improved performance by about
	// 1500%. No exagerration. Test out on a file with about 5000 lines. i guess that
	// += is truly an evil thing.
	//
	public synchronized String readStuff() {
		checkFrozen();
		//String textToBeReturned = "";

		StringBuffer toRet = new StringBuffer("");
		try {
			String line = "";
			numLines = 0;
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
			boolean eof = false;
			line = br.readLine();
			if (line == null) eof = true;
				else {
				//textToBeReturned = line; // so we don't have compares to do as we go through file
				toRet.append(line);
				numLines++;
			}

			while (!eof) {
				line = br.readLine();
				if (line == null) eof = true;
					else {
					//textToBeReturned += System.getProperty("line.separator") + line;
					toRet.append(System.getProperty("line.separator") + line);
					numLines++;
				}
			}
			lineNumberCoherence = true;
			br.close();
			isr.close();
			fis.close();
		}catch (UnsupportedEncodingException uee) {
			// switch encodings?
		}catch (IOException e) {
			System.out.println("Error 5- " + e.toString());
			//e.printStackTrace();
		}catch (SecurityException b) {
			System.out.println("Go check security stuff");
		}catch (Exception e) {
			e.printStackTrace();
		}
		//return textToBeReturned;
		return toRet.toString();
	}

	// this is a little bit better- no tempFile means more memory taken up,
	// but we won't have any huge files here hopefully.
	public synchronized void deleteStuffLn(int lineNumber) {
		checkFrozen();
		// we still have coherence...
		int currentLine=0;
		String textToBeReturned = "";
		try {
			String line = "";
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
			boolean eof = false;
			boolean didFirstLine = false;
			while (!eof) {
				line = br.readLine();
				if (line == null)  eof = true;
				else {
					currentLine++;
					if (currentLine != lineNumber) {
						if (!didFirstLine) {
							textToBeReturned += line;
							didFirstLine = true;
						}
						else textToBeReturned += System.getProperty("line.separator") + line;
					}
				}
			}
			br.close();
			isr.close();
			fis.close();
		}
		catch (IOException e) {
		System.out.println("Error 6- " + e.toString());
		}
		catch (SecurityException b) {
		System.out.println("Go check security stuff");
		}
		catch (NullPointerException n) {
		System.out.println("Null Pointer in DeleteStuffLn");
		}
		catch (Exception e) {
		e.printStackTrace();
		}
		saveStuff(textToBeReturned,false);
		numLines--;
	}


	public synchronized void changeStuffLn(int lineNumber, String newStuff) {
		checkFrozen();
		int currentLine=0;
		String textToBeReturned = null;
		//file tempFile = new file("12345test.test");
		try {
			String line = "";
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
			boolean eof = false;
			line = br.readLine();
			if (line == null) eof = true;
			else {
				currentLine=1;
				if (lineNumber==1) textToBeReturned = newStuff;
				else textToBeReturned = line;
			}
			while (!eof) {
				line = br.readLine();
				if (line == null) eof = true;
				else {
					currentLine++;
					if (currentLine != lineNumber) //just add the line here
						textToBeReturned += System.getProperty("line.separator")+line;
					else // here we add the new line - toss the old
						textToBeReturned += System.getProperty("line.separator")+newStuff;
				}
			}
			br.close();
			isr.close();
			fis.close();
		}catch (UnsupportedEncodingException uee) {
			// try another?
		}catch (IOException e) {
			System.out.println("Error 7- " + e.toString());
		}catch (SecurityException b) {
			System.out.println("Go check security stuff");
		}catch (NullPointerException n) {
			System.out.println("Null Pointer in ChangeStuffLn");
		}catch (Exception e) {
			e.printStackTrace();
		}
		saveStuff(textToBeReturned); // here we save it!
	}

	public synchronized String readStuffLn(int lineNumber) { // Outside range returns ""
		checkFrozen();
		String textToBeReturned = null;
		try {
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
			//LineNumberReader buff = new LineNumberReader(letters);
			String line = "";
			for (int i = 0; i<lineNumber; i++) {
				line = br.readLine();
			}
			//buff.setLineNumber(lineNumber+1);
			//textToBeReturned=buff.readLine();
			textToBeReturned = line;
			br.close();
			isr.close();
			fis.close();
		}catch (UnsupportedEncodingException uee) {
						// try another?
		}catch (IOException e) {
			System.out.println("Error 8- " + e.toString());
		}catch (SecurityException b) {
			System.out.println("Invalid security clearance- prepare to die");
		}catch (NullPointerException n) {
			System.out.println("Null pointer in readStuffLn dude");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return textToBeReturned;
	}


	// use this for adding lines to a file in the fastest possible way
	// make sure there are no new line characters in the array!
	public synchronized void addLines(int firstLineGoesHere, String[] lines) {
		// we still have line coherence, we know how many we are adding
		checkFrozen();
		//System.out.println("starting file.addLInes- first line:" + lines[0]);
		int numNewLines = lines.length;
		int currentLine=1; //this is the syntax. First line = line 1 NOT line 0
		String line = "";
		String toBeReturned = "";
		//boolean done = false;
		try {
			// these are my boys
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis, encoding);
			br = new BufferedReader(isr);
			boolean eof = false;
			line = br.readLine();
			if (firstLineGoesHere==1) {
				toBeReturned = lines[0];
				for(int i=1; i<numNewLines; i++) {
					toBeReturned +=
					System.getProperty("line.separator")+lines[i];
				}
				currentLine += 1; // doesn't matter anymore as long as not equal to firstlinegh
			}
			if (line==null) eof = true;
			else {
				if (currentLine == 1) toBeReturned =line;
				else toBeReturned += System.getProperty("line.separator")+ line;
			}
			while (!eof) { // now go through majority of file QUICKLY
				line=br.readLine();
				currentLine++;
				if(currentLine==firstLineGoesHere) {
					System.out.println("should be doing addLInes NOW");
					for(int i=0; i<numNewLines; i++) {
						toBeReturned +=
						System.getProperty("line.separator")+lines[i];
					}
					currentLine = currentLine + numNewLines;
				}
				// now check for eof
				if (line == null) eof = true;
				else {
					toBeReturned +=
					System.getProperty("line.separator")+line;
				}
			}
			br.close();
			isr.close();
			fis.close();
			saveStuff(toBeReturned);
			numLines = numLines + numNewLines;
		}catch (UnsupportedEncodingException uee) {

			// try another?
		}catch (IOException e) {

			System.out.println("Error - " + e.toString());
		}catch (SecurityException b) {
			System.out.println("Go check security stuff");
		}catch (NullPointerException n) {
			System.out.println("Null Pointer in addLines");
		}catch (Exception e) {
			System.out.println("Error 9- " + e.toString());
		}
	}

	public synchronized void addText(int firstLineGoesHere, String text) {
		checkFrozen();
		lineNumberCoherence = false;
		int currentLine=1; //this is the syntax. First line = line 1 NOT line 0
		String toBeReturned = "";
		String line = "";
		boolean eof = false;
		try {
			// these are my boys
			fis = new FileInputStream(fileName);
			isr = new InputStreamReader(fis,encoding);
			br = new BufferedReader(isr);
			line = br.readLine();
			if (firstLineGoesHere==1) {
				toBeReturned = text;
				currentLine += 1; // doesn't matter anymore as long as not equal to firstlinegh
			}
			if (line==null) eof = true;
			else {
				if (currentLine == 1) toBeReturned = line;
				else toBeReturned += System.getProperty("line.separator")+line;
			}
			while (!eof) {
				line=br.readLine();
				currentLine++;
				// first check if we need to add the goods and do it
				if(currentLine==firstLineGoesHere) {
					toBeReturned += System.getProperty("line.separator")+text;
					currentLine += 1; // doesn't matter again
				}
				// now check for eof
				if (line == null) eof = true;
				else {
					toBeReturned += System.getProperty("line.separator")+line;
				}
			}
			saveStuff(toBeReturned);
		}
		catch (IOException e) {
		System.out.println("Error 0- " + e.toString());
		}
		catch (SecurityException b) {
		System.out.println("Go check security stuff");
		}
		catch (NullPointerException n) {
		System.out.println("Null Pointer in DeleteStuffLn");
		}
		catch (Exception e) {
		System.out.println("Error a- " + e.toString());
		}
	}


	public synchronized int readStuffNumLines() {
		if (lineNumberCoherence) return numLines;
		String test = readStuff(); //file could be changed or readStuff not called yet.
		return numLines;
	}

	public synchronized boolean exists() { // useful exists method
		test = new File(fileName);
		return test.exists();
	}

	public synchronized boolean delete() { // useful delete method
		test = new File(fileName);
		try {return test.delete();}
		catch(Exception ex) {return false;}
	}

	public synchronized long length() { // returns size of file in bytes
		test = new File(fileName);
		return test.length();
	}

	public synchronized void setContents(file gehFile) { // this is quick!
		checkFrozen();
		lineNumberCoherence = false; // of a serious nature!
		File gf = new File(gehFile.fileName);
		test = new File(fileName);
		boolean niceAss = delete();
		try{
			gf.renameTo(test);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkFrozen() {
		int c=0;
		while (frozen) {
			c++; // I'm a wannabe
			if (c==10000) {
				System.out.println("WARNING - something stuck waiting for closeREad or closeWrite! "+fileName);
				frozen = false; // this might hose something...
			}
		}
	}

	public String getName() {
		return fileName;
	}
}
