package bm.main.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import bm.main.engines.exceptions.EngineException;
import bm.main.engines.requests.EngineRequest;
import bm.main.engines.requests.FileEngine.DeleteStringFEReq;
import bm.main.engines.requests.FileEngine.FileEngineRequest;
import bm.main.engines.requests.FileEngine.FileEngineRequestType;
import bm.main.engines.requests.FileEngine.GetInputStreamFEReq;
import bm.main.engines.requests.FileEngine.InsertToFileFEReq;
import bm.main.engines.requests.FileEngine.OverwriteFileFEReq;
import bm.tools.VersionManager;

/**
 * <b>FileEngine</b><br><br>
 * 
 * The FileEngine is an outsourced processRequest which came from multiple objects that needed to handle files. This
 * object was created to outsource the processRequest of accessing a file within the system. The FileEngine has three main
 * functions: <b>
 * <ul>
 * 	<li>Accessing a specified file.
 * 		<ul>
 * 			<li>Provide the Reader and Writer to a specified file.</li>
 * 			<li>Provide a method for saving the a Properties object to a file.</li>
 * 		</ul>
 * 	</li>
 * 	<!--<li>Manage the existing InputStreams and OutputStreams:
 * 		<ul>
 * 			<li>Closing unused InputStreams and OutputStreams</li>
 * 		</ul>
 * 	</li>-->
 * 	<li>Handle exceptions in accessing the specified file.
 * 		<ul>
 * 			<li>Invalid file path.</li>
 * 		</ul>
 * 	</li>
 * </ul>
 * @author carlo
 *
 */
public class FileEngine extends AbstEngine {
	private File file;
	private String filepath;
	private VersionManager vm;
	
	public FileEngine(String filepath, String name, String logDomain, String errorLogDomain, int maxVersions) 
			throws FileNotFoundException {
		super(logDomain, errorLogDomain, name, FileEngine.class.toString());
		this.filepath = filepath;
		update();
		try {
			this.vm = VersionManager.deserialize(file);
			this.vm.setLogger(LOG);
		} catch (Exception e) {
			LOG.warn("No version manager found for this FileEngine. New version manager created!");
			this.vm = new VersionManager(LOG, file, maxVersions);
			this.vm.setLogger(LOG);
		}
		LOG.info("FileEngine for file '" + file.getName() + "' started!");
//		this.vm = new VersionManager();
//		try {
//			this.vm = vm.deserialize();
//			if(vm == null) {
//				this.vm = new VersionManager(maxVersions);
//			}
//		} catch (Exception e) {
//			LOG.warn("No version manager found for this FileEngine. New version manager created!");
//			this.vm = new VersionManager(maxVersions);
//		}
	}
	
	@Override
	protected Object processRequest(EngineRequest er) {
		FileEngineRequest fer = (FileEngineRequest) er;
		if(fer.getType().equals(FileEngineRequestType.readAllLines)) {
			try {
				return readAllLines();
			} catch (SecurityException e) {
				EngineException error = new EngineException(this, "Security manager denied access to "
						+ "the file!", e);
				return error;
			} catch (IOException e) {
				EngineException error = new EngineException(this, "File cannot be accessed, found, "
						+ "or read!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.overwrite)) {
			OverwriteFileFEReq offer = (OverwriteFileFEReq) fer;
			try {
				overwriteFile(offer.getLines());
				return true;
			} catch (IOException e) {
				EngineException error = new EngineException(this, "File cannot be written to!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.clear)) {
			try {
				clearFile();
				return true;
			} catch (IOException e) {
				EngineException error = new EngineException(this, "File cannot be cleared!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.insert)) {
			InsertToFileFEReq itffer = (InsertToFileFEReq) fer;
			try {
				insertToFile(itffer.getLine(), itffer.getLineMarker(), itffer.isAfterLineMarker());
				return true;
			} catch(IOException e) {
				EngineException error = new EngineException(this, "File cannot be inserted into!", e);
				return error;
			} catch(SecurityException e) {
				EngineException error = new EngineException(this, "Access to file denied by security manager!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.delete)) {
			DeleteStringFEReq dsfer = (DeleteStringFEReq) fer;
			try {
				deleteStringFromFile(dsfer.getString());
				return true;
			} catch (IOException e) {
				EngineException error = new EngineException(this, "File contents cannot be deleted!", e);
				return error;
			} catch (SecurityException e) {
				EngineException error = new EngineException(this, "Access to file denied by security manager!", e);
				return error;
			} 
		} else if(fer.getType().equals(FileEngineRequestType.getInputStream)) {
			try {
				return getInputStream();
			} catch (FileNotFoundException e) {
				EngineException error = new EngineException(this, "File not found!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.update)) {
			try {
				update();
				return true;
			} catch(FileNotFoundException e) {
				EngineException error = new EngineException(this, "File not found!", e);
				return error;
			}
		} else if(fer.getType().equals(FileEngineRequestType.versionize)) {
			try {
				vm.versionize();
			} catch(IOException e) {
				EngineException error = new EngineException(this, "File cannot be versioned!", e);
				return error;
			} catch(SecurityException e) {
				EngineException error = new EngineException(this, "Access to versioned file denied by security "
						+ "manager!", e);
				return error;
			}
			return true;
		}
		else {
			EngineException error = new EngineException(this, "Invalid FileEngineRequestType!");
			return error;
		}
	}
	
	/**
	 * Updates the reference of this FileEngine to the file it handles.
	 * 
	 * @throws FileNotFoundException The file this FileEngine handles cannot be found.
	 */
	protected void update() throws FileNotFoundException {
		file = new File(filepath);
		if(!file.exists()) {
			throw new FileNotFoundException("File " + filepath + " not found!");
		}
	}
	
	/**
	 * Reads all lines from the file
	 * 
	 * @return A String array containing all the lines read from the file
	 * @throws SecurityException if the file has a security manager and it does not allow access to 
	 * 		the file
	 * @throws IOException if the file cannot be found, accessed, or read
	 */
	protected String[] readAllLines() throws SecurityException, IOException {
		LOG.trace("Reading all lines from " + file.getName() + "...");
		BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));
		Vector<String> lines = new Vector<String>(10, 10);
		
		while(reader.ready()) {
			lines.add(reader.readLine());
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	/**
	 * Accesses the file and returns a BufferedReader for the file. <br><br>
	 * 
	 * @param filepath The path of the file. Can be absolute or relative.
	 * @return a BufferedReader of the file that is specified by the <b>filepath</b>.
	 * @throws FileNotFoundException if the file could not be found or accessed
	 */
	protected InputStream getInputStream() throws FileNotFoundException {
		LOG.trace("Getting BufferedReader for '" + file.getName() + "...");
		InputStream in = new FileInputStream(file);
		
		return in;
	}
	
	/**
	 * Creates a BufferedWriter for the file.
	 * 
	 * @param filepath
	 * @return a BufferedWriter of the file that is specified by the <b>filepath</b>.
	 * @throws IOException
	 */
	private BufferedWriter getFileWriter() throws IOException { 
		LOG.trace("Getting BufferedWriter for the file...");
		FileWriter fw = new FileWriter(file.getAbsolutePath());
		BufferedWriter writer = new BufferedWriter(fw);
		
		return writer;
	}
	
	/**
	 * Inserts the specified string into the file. The string is inserted either before or after the line that 
	 * contains the specified line marker.
	 * 
	 * @param str The string to be inserted into the file
	 * @param lineMarker A string which will be used to see where the string will be inserted to. The first line 
	 * 		that contains this <b>lineMarker</b> will be chosen as the <i>reference line</i> where 
	 * 		the string will be inserted to.
	 * @param afterLineMarker <b><i>True</i></b> if the string will be inserted <b>after</b> the <i>reference 
	 * 		line</i>, <b><i>false</b></i> if the string will be inserted <b>before</b> the <i>reference line</i>
	 * @throws IOException if file cannot be read from
	 * @throws SecurityException if file cannot be accessed
	 */
	protected void insertToFile(String str, String lineMarker, boolean afterLineMarker) throws SecurityException, 
			IOException {
		LOG.trace("Inserting \"" + str + "\" to " + file.getPath() + "...");
		String[] rawLines = readAllLines();
		Vector<String> lines = new Vector<String>(rawLines.length + 1);
		lines.addAll(Arrays.asList(readAllLines()));
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if(line.contains(lineMarker)) {
				if(afterLineMarker) {
					LOG.trace("Inserting \"" + str + "\" to line " + (i+1));
					lines.add(i++, str);
				} else {
					LOG.trace("Inserting \"" + str + "\" to line " + (i));
					lines.add(i, str);
				}
				break;
			}
		}
		LOG.trace("Insert successful!");
		update();
		overwriteFile(lines.toArray(new String[0]));
	}
	
	protected void deleteStringFromFile(String str) throws SecurityException, IOException {
		LOG.trace("Deleting string \"" + str + " from " + file.getPath() + "...");
		String[] rawLines = readAllLines();
		Vector<String> lines = new Vector<String>(rawLines.length + 1);
		lines.addAll(Arrays.asList(readAllLines()));
		for(int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if(line.contains(str)) {
				line = line.replace(str, "");
				lines.add(i, line);
				lines.remove(i + 1);
			}
		}
		LOG.trace("Delete successful!");
		update();
		overwriteFile(lines.toArray(new String[0]));
	}
	
	/**
	 * Appends the specified String to the file. Adds a new line first before appending the String.
	 * 
	 * @param str The String to be appended
	 * @throws IOException 
	 */
	protected void appendToFile(String str) throws IOException {
		LOG.trace("Appending ''" + str + "'' to " + file.getPath());
		FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
		BufferedWriter writer = new BufferedWriter(fw);
		writer.newLine();
		writer.write(str);
		writer.close();
		update();
		LOG.trace("Append successful!");
	}
	
	/**
	 * Writes the specified String to the file. Overwrites all pre-existing contents of the file.
	 * @param str The array of lines to be written
	 * @throws IOException if overwriting fails
	 */
	protected void overwriteFile(String[] lines) throws IOException {
		LOG.trace("Overwriting '" + file.getPath() + "'...");
		FileWriter fw = new FileWriter(file.getAbsolutePath(), false);
		BufferedWriter writer = new BufferedWriter(fw);
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			writer.write(line);
			writer.newLine();
		}
		writer.close();
		update();
		LOG.trace("Overwrite successful!");
	}
	
	protected void clearFile() throws IOException {
		LOG.trace("Clearing contents of " + file.getName());
		getFileWriter().write("test");
		update();
	}
	
	/**
	 * Checks the if the file extension is the same with the extension specified.
	 * 
	 * @param extension The specified file extension. <b>MUST NOT</b> contain the period before the extension.
	 * @return <b><i>true</i></b> if the file has the same extension specified. 
	 * 		<b><i>false</i></b> if not.
	 */
	public boolean checkExtension(String extension) {
		LOG.trace("Checking extension...");
		try {
			return file.getCanonicalPath().split("\\.")[file.getCanonicalPath().split("\\.").length - 1]
					.equalsIgnoreCase(extension);
		} catch (IOException e) {
			LOG.error("Cannot check extension!", e);
			return false;
		}
	}
	
	/**
	 * Returns the File this FileHandler handles.
	 * 
	 * @return The file;
	 */
	public File getFile() {
		return file;
	}
}