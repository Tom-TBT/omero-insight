/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.StatusLabel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.io.File;
import java.util.Map;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorHandler;
import pojos.DataObject;

/**
 * Component displaying the status of a specific import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class StatusLabel 
	extends JLabel
	implements IObserver
{

	/** The text displayed when loading the image to import. */
	public static final String PREPPING_TEXT = "prepping";
	
	/** Bound property indicating that children files have been set. */
	public static final String FILES_SET_PROPERTY = "filesSet";
	
	/** 
	 * Bound property indicating that the file has to be reset
	 * This should be invoked if the log file for example has been selected. 
	 */
	public static final String FILE_RESET_PROPERTY = "fileReset";
	
	/** Bound property indicating that the import of the file has started. */
	public static final String FILE_IMPORT_STARTED_PROPERTY = 
		"fileImportStarted";
	
	/** Bound property indicating that the file is imported. */
	public static final String FILE_IMPORTED_PROPERTY = "fileImported";
	
	/** 
	 * Bound property indicating that the container corresponding to the
	 * folder has been created. 
	 * */
	public static final String CONTAINER_FROM_FOLDER_PROPERTY = 
		"containerFromFolder";
	
	/** Default text when a failure occurred. */
	private static final String		FAILURE_TEXT = "failed";
	
	/** The number of planes. This value is used only for some file formats. */
	private int maxPlanes;
	
	/** The number of imported files. */
	private int numberOfFiles;
	
	/** The number of images in a series. */
	private int seriesCount;
	
	/** The type of reader used. */
	private String readerType;
	
	/** The files associated to the file that failed to import. */
	private String[] usedFiles;
	
	/** The time at which the import started. */
	private long     startTime;
	
	/** The time at which the import ended. */
	private long     endTime;
	
	/** The text if an error occurred. */
	private String	 errorText;
	
	/** Flag indicating that the import has been cancelled. */
	private boolean  markedAsCancel;
	
	/** Creates a new instance. */
	public StatusLabel()
	{
		setForeground(UIUtilities.LIGHT_GREY);
		maxPlanes = 0;
		numberOfFiles = 0;
		seriesCount = 0;
		readerType = "";
		errorText = FAILURE_TEXT;
		setText("pending");
		markedAsCancel = false;
	}
	
	/** Marks the import has cancelled. */
	public void markedAsCancel() { this.markedAsCancel = true; }
	
	/**
	 * Returns <code>true</code> if the import is marked as cancel, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMarkedAsCancel() { return markedAsCancel; }
	
	/**
	 * Returns the text if an error occurred.
	 * 
	 * @return See above.
	 */
	public String getErrorText() { return errorText; }
	
	/**
	 * Returns the duration of the import. 
	 * 
	 * @return See above.
	 */
	public long getDuration() { return endTime-startTime; }
	
	/**
	 * Returns the type of reader used.
	 * 
	 * @return See above.
	 */
	public String getReaderType() { return readerType; }
	
	/**
	 * Returns the files associated to the file failing to import.
	 * 
	 * @return See above.
	 */
	public String[] getUsedFiles() { return usedFiles; }
	
	/** 
	 * Sets the status of the import.
	 * 
	 * @param value The value to set.
	 */
	public void setStatus(String value)
	{
		if (value == null) value = "";
		setText(value);
	}
	
	/** 
	 * Fires a property indicating to import the files.
	 * 
	 * @param files The file to handle.
	 */
	public void setFiles(Map<File, StatusLabel> files)
	{
		firePropertyChange(FILES_SET_PROPERTY, null, files);
	}
	
	/**
	 * Sets the container corresponding to the folder.
	 * 
	 * @param container The container to set.
	 */
	public void setContainerFromFolder(DataObject container)
	{
		firePropertyChange(CONTAINER_FROM_FOLDER_PROPERTY, null, container);
	}
	
	/**
	 * Replaces the initial file by the specified one. This should only be 
	 * invoked if the original file was an arbitrary one requiring to use the
	 * import candidate e.g. <code>.log</code>
	 * 
	 * @param file The new file.
	 */
	public void resetFile(File file)
	{
		firePropertyChange(FILE_RESET_PROPERTY, null, file);
	}
	
	/**
	 * Returns the number of series.
	 * 
	 * @return See above.
	 */
	public int getSeriesCount() { return seriesCount; }
	
	/** 
	 * Fires a property indicating that the file has been imported.
	 * 
	 * @param file The file to import.
	 * @param result The result.
	 */
	public void setFile(File file, Object result)
	{
		Object[] results = new Object[2];
		results[0] = file;
		results[1] = result;
		firePropertyChange(FILE_IMPORTED_PROPERTY, null, results);
	}
	
	/**
	 * Displays the status of an on-going import.
	 * @see IObserver#update(IObservable, ImportEvent)
	 */
	public void update(IObservable observable, ImportEvent event)
	{
		if (event == null) return;
		if (event instanceof ImportEvent.LOADING_IMAGE) {
			startTime = System.currentTimeMillis();
			setText(PREPPING_TEXT);
			firePropertyChange(FILE_IMPORT_STARTED_PROPERTY, null, this);
		} else if (event instanceof ImportEvent.LOADED_IMAGE) {
			setText("analyzing");
		} else if (event instanceof ImportEvent.IMPORT_DONE) {
			if (numberOfFiles == 1) setText("one file");
			else if (numberOfFiles == 0) setText("");
			else setText(numberOfFiles+" files");
			endTime = System.currentTimeMillis();
		} else if (event instanceof ImportEvent.IMPORT_ARCHIVING) {
			setText("archiving");
		} else if (event instanceof ImportEvent.DATASET_STORED) {
			ImportEvent.DATASET_STORED ev = (ImportEvent.DATASET_STORED) event;
			maxPlanes = ev.size.imageCount;
		} else if (event instanceof ImportEvent.IMPORT_STEP) {
			ImportEvent.IMPORT_STEP ev = (ImportEvent.IMPORT_STEP) event;
			if (ev.step <= maxPlanes) {   
				int value = ev.step;
				if (value <= maxPlanes) {
					String text;
					seriesCount = ev.seriesCount;
					int series = ev.series;
					if (seriesCount > 1)
						text = (series+1)+"/"+seriesCount+": "
							+value+"/"+maxPlanes;
					else
						text = value+"/"+maxPlanes;
					setText(text);
				}
            }
		} else if (event instanceof ImportCandidates.SCANNING) {
			ImportCandidates.SCANNING ev = (ImportCandidates.SCANNING) event;
			numberOfFiles = ev.totalFiles;
			setText("scanning");
		} else if (event instanceof ErrorHandler.FILE_EXCEPTION) {
			endTime = System.currentTimeMillis();
			ErrorHandler.FILE_EXCEPTION e = (ErrorHandler.FILE_EXCEPTION) event;
			readerType = e.reader;
			usedFiles = e.usedFiles;
		} else if (event instanceof ErrorHandler.UNKNOWN_FORMAT) {
			errorText = "unknown format";
		} else if (event instanceof ErrorHandler.MISSING_LIBRARY) {
			errorText = "missing required library";
		} else if (event instanceof ImportEvent.IMPORT_THUMBNAILING) {
			setText("Creating thumbnail");
		} 
	}
	
}
