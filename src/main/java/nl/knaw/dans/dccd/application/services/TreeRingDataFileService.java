/*******************************************************************************
 * Copyright 2015 DANS - Data Archiving and Networked Services
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package nl.knaw.dans.dccd.application.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.knaw.dans.dccd.model.DccdTreeRingData;

import org.apache.log4j.Logger;
import org.tridas.io.AbstractDendroCollectionWriter;
import org.tridas.io.AbstractDendroFileReader;
import org.tridas.io.IDendroFile;
import org.tridas.io.TridasIO;
import org.tridas.io.exceptions.ConversionWarning;
import org.tridas.io.exceptions.ConversionWarningException;
import org.tridas.io.exceptions.ImpossibleConversionException;
import org.tridas.io.exceptions.InvalidDendroFileException;
import org.tridas.io.exceptions.NothingToWriteException;
import org.tridas.io.naming.INamingConvention;
import org.tridas.schema.TridasProject;
//import org.tridas.treeringdatafileio.TucsonFile;

/**
 * For loading and saving files (DccdTreeRingData)
 *
 * @author paulboon
 *
 */
public class TreeRingDataFileService  {
	private static Logger logger = Logger.getLogger(TreeRingDataFileService.class);

	// disallow construction
	protected TreeRingDataFileService() {} 

	// These are format identifying strings
	public static List<String> getReadingFormats() 
	{
		List<String> list = new ArrayList<String>();
		// make sure it's not a fixed sized list
		list.addAll(Arrays.asList(TridasIO.getSupportedReadingFormats())); 
		return list;
	}
	
	// These are format identifying strings
	public static List<String> getWritingFormats() 
	{
		List<String> list = new ArrayList<String>();
		// make sure it's not a fixed sized list
		list.addAll(Arrays.asList(TridasIO.getSupportedWritingFormats())); 
		
		// FIXME
		// This workaround exclude some problematic formats; CARTRAS and Sheffield 
		// give errors up to and including version 1.0.7-SNAPSHOT
//		list.remove("CATRAS");
//		list.remove("Sheffield");
		
		return list;
	}
	
	/**
	 * Load the file with the given format and convert to DccdTreeRingData
	 * 
	 * @param file
	 * @param formatString
	 * @return The DccdTreeRingData from the file
	 * @throws TreeRingDataFileServiceException
	 */
	public static DccdTreeRingData load(File file, String formatString) throws TreeRingDataFileServiceException {
		DccdTreeRingData data = new DccdTreeRingData();
		data.setFileName(file.getName()); // store the original name (without path)

		AbstractDendroFileReader reader = TridasIO.getFileReader(formatString);
		
		// Note:  maybe create our own Defaults for the readers
	
	    try
		{

	    	// Note that the path must be without the filename
	    	// why not just a filename (complete with path?), less params is better
	    	String fileName = file.getName();
	    	String filePath = file.getParent();
	    	logger.debug("Start loading file: " + fileName + " from " + filePath );

	    	// Note: need to seperate filename and path, why not a File param on the load or read?

	    	reader.loadFile(filePath, fileName);//, defaults);
	    	TridasProject[] projects = reader.getProjects();
	    	if(projects.length == 0)
	    	{
	    		throw new TreeRingDataFileServiceException("Conversion from file did not produce project data");
	    	}
	    	else if (projects.length > 1)
	    	{
	    		logger.warn("Conversion of the file: " + fileName + 
	    				"produced " + projects.length +
	    				" projects, taking the first and ignoring the rest");
	    	}
		    TridasProject project = projects[0];// take the first, ignore others
		    data.setTridasProject(project);
		}
		catch (IOException e)
		{
			throw new TreeRingDataFileServiceException(e);
		}
		catch (InvalidDendroFileException e)
		{
			// get more info...
			// hmm getWarnings is not part of the Interface
			//List<ConversionWarning> warnings = reader.getWarnings();
		   ConversionWarning[] warnings = reader.getWarnings(); 
		   for(ConversionWarning warning : warnings)
		   {
			   logger.debug("warning: " + warning.getMessage());
		   }

			throw new TreeRingDataFileServiceException(e);
		}

		return data;
	}
	
	/**
	 * Save the DccdTreeRingData to files with the given format
	 * 
	 * @param data
	 * @param filePath
	 * @param formatString
	 * @return The list of filenames for the files that have been saved
	 * @throws TreeRingDataFileServiceException
	 */
	public static List<String> save(DccdTreeRingData data, File filePath, String formatString) throws TreeRingDataFileServiceException 
	{
		ArrayList<String> filenames = new ArrayList<String>();

		AbstractDendroCollectionWriter writer = TridasIO.getFileWriter(formatString);
		// Note:  maybe create our own Defaults for the writers

		report(filePath, "\nDCCD Export TRiDaS project\'" + data.getTridasProject().getTitle() +  "\' to format: " +formatString);
		filenames.add("export_report.txt"); //???
		
		try
		{
			writer.loadProject(data.getTridasProject());

			if (canSave(writer)) 
			{
				// write the files to disk
				writer.saveAllToDisk(filePath.getPath());
			}
			else
			{	
				report(filePath, "No value files saved, conversion not completely possible");
				logger.debug("No value files saved, conversion not completely possible");
				return filenames; // just an empty list, nothing saved!
			}
		}
		catch (ImpossibleConversionException e)
		{
			report(filePath, "Impossible to convert: " +e.getMessage());
			throw new TreeRingDataFileServiceException(e);
		}
		catch (ConversionWarningException e)
		{
			report(filePath, "TRiDaS Conversion warning: " + e.getWarning().getMessage());
			logger.warn("TRiDaS Conversion warning: " + e.getWarning().getMessage());
			// show warnings
			ConversionWarning[] warnings = writer.getWarnings(); 
			for(ConversionWarning warning : warnings)
			{
				report(filePath, "warning: " + warning.getMessage());
				logger.debug("warning: " + warning.getMessage());
			}
			//throw new TreeRingDataFileServiceException(e);
			// Ignore, it's a warning, 
			// we could also store these warnings in a text file and add it to the downloaded zip
		} 
		catch (NothingToWriteException e) 
		{
			report(filePath, "TRiDaS Conversion: Nothing to write: " +e.getMessage());
			logger.warn("TRiDaS Conversion: Nothing to write");
		}

		INamingConvention nc = writer.getNamingConvention();
		IDendroFile[] files = writer.getFiles();
		for (int i = 0; i < files.length; i++)
		{
			filenames.add(nc.getFilename(files[i]) + "." + files[i].getExtension());
		}
		return filenames;
	}	
	
	public static void report(File folder, String msg)
	{
		// open file and append
		File reportFile = new File(folder.getAbsolutePath() + File.separator + "export_report.txt");
		PrintWriter output;
		try {
			output = new PrintWriter(new FileWriter(reportFile, true));
			output.println(msg);
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Check the writer (files) and determine if we want DCCD to save them
	 * 
	 * @param writer
	 * @return
	 */
	public static boolean canSave(AbstractDendroCollectionWriter writer)
	{
		boolean result = true;
		
		IDendroFile[] files = writer.getFiles();
		for (int i = 0; i < files.length; i++)
		{
			IDendroFile dendroFile = files[i];
			
			try 
			{
				// The next line used to throw a NullPonterException when there is nothing to save 
				// by the Heidelberg writer.
				// Note: this bug is fixed in the DendroFileIO library, but we can check for problems anyway!
				//
				String filename = writer.getNamingConvention().getFilename(dendroFile);
			}
			catch (Exception e)
			{
				// If we can't call the getFilename() save will fail as well!
				result = false;
				break;
			}
			
			/* next code always throws a ClassCastException for Heidelberg files
			if (dendroFile.getSeries() == null || 
				dendroFile.getSeries().length == 0)
			{
				result = false;
				break;
			}
			*/
		}
		
		return result;
	}
}