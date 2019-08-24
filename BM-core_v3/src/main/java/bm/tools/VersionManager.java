package bm.tools;

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
import java.util.Vector;

import org.apache.log4j.Logger;


public class VersionManager implements Serializable {
	private transient Logger LOG;
	private File file;
	private int maxVersions;
	private String[] records;
	private int totalCopiesMade = 0;
	
	/*
	 * Instantiates a "static" object to access serialize and deserialize methods. 
	 * <b><i>WARNING:</b> this constructor must only be used to access the serialize and 
	 * deserialize methods!</i>
	 
	private VersionManager() {
		
	}*/
	
	public VersionManager(Logger LOG, File file, int maxVersions) {
		this.LOG = LOG;
		this.file = file;
		this.maxVersions = maxVersions;
		records = new String[maxVersions];
		serialize();
	}
	
	public void setLogger(Logger LOG) {
		this.LOG = LOG;
	}
	
	/**
	 * Saves a copy of this file into a version with timestamp. Amount of versioned files to be kept is set 
	 * upon FileEngine instantiation.
	 * 
	 * ex.
	 * 	original file : rules.cir
	 *  versioned file: rules.v1_2016-11-16
	 */
	public void versionize() throws SecurityException, IOException {
		LOG.trace("Versioning file " + file.getPath());
		Timestamp timestamp = new Timestamp(System.currentTimeMillis()); ////2016-11-16 06:43:19.77
		String time = timestamp.toString().split(" ")[0] + "_" + 
				timestamp.toString().split(" ")[1].replaceAll(":", "-"); //timestamp in file format
		String version = "v" + (totalCopiesMade + 1) + "_" + time;
		
		String directory = file.getParent();
		String name = file.getName().substring(0, file.getName().lastIndexOf("."));
		String extension = file.getName().substring(file.getName().lastIndexOf("."));
		String[] contents = readAllLines();
		File copy = new File(directory + "/" + name + "." + version + extension);
		
		if(totalCopiesMade >= maxVersions) { //if max amount of versions were made
			int i = totalCopiesMade % maxVersions;
			File old = new File(directory + "/" + name + "." + records[i] + extension);
			old.delete();
		}
		
		FileWriter fw = new FileWriter(copy);
		BufferedWriter writer = new BufferedWriter(fw);
		for(int i = 0; i < contents.length; i++) {
			writer.write(contents[i]);
			writer.newLine();
		}
		writer.close();
		
		updateRecords(version);
		LOG.trace("Versioning successful!");
	}
	
	private String[] readAllLines() throws IOException {
		LOG.trace("Reading all lines from " + file.getName() + "...");
		InputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		Vector<String> lines = new Vector<String>(10, 10);
		
		while(reader.ready()) {
			lines.add(reader.readLine());
		}
		return lines.toArray(new String[lines.size()]);
	}
	
	private void updateRecords(String version) {
		int i = totalCopiesMade % maxVersions;
		records[i] = version;
		totalCopiesMade++;
		serialize();
	}
	
	private void serialize() {
		try {
			String name = file.getName();//.substring(0, file.getName().lastIndexOf("."));
			FileOutputStream fileOut = new FileOutputStream("tmp/" + name + ".versions");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		}catch(IOException e) {
			LOG.error("Cannot serialize " + file.getName(), e);
	    }
	}
	
	/**
	 * 
	 * @param file the file managed by the VersionManager to be deserialized
	 * @return
	 * @throws Exception
	 */
	public static VersionManager deserialize(File file) throws Exception {
		String name = file.getName();//.substring(0, file.getName().lastIndexOf("."));
		FileInputStream fileIn = new FileInputStream("tmp/" + name + ".versions");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        VersionManager vm = (VersionManager) in.readObject();
        in.close();
        fileIn.close();
        return vm;
	}
}
