package extractSharpestPlane_jnh;
/** ===============================================================================
* ExtractSharpestPlane_JNH.java Version 0.0.2
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation (http://www.gnu.org/licenses/gpl.txt )
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*  
* See the GNU General Public License for more details.
*  
* Copyright (C) Jan Niklas Hansen
* Date: May 07, 2021 (This Version: November 4, 2021)
*   
* For any questions please feel free to contact me (jan.hansen@uni-bonn.de).
* =============================================================================== */

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
import java.text.*;

import javax.swing.UIManager;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.*;
import ij.text.*;
import loci.formats.FormatException;
import loci.plugins.BF;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;

public class ExtractSharpestPlane_Main implements PlugIn, Measurements {
	//Name variables
	static final String PLUGINNAME = "Extract Sharpest Plane JNH";
	static final String PLUGINVERSION = "0.0.2";
	
	//Fix fonts
	static final Font SuperHeadingFont = new Font("Sansserif", Font.BOLD, 16);
	static final Font HeadingFont = new Font("Sansserif", Font.BOLD, 14);
	static final Font SubHeadingFont = new Font("Sansserif", Font.BOLD, 12);
	static final Font TextFont = new Font("Sansserif", Font.PLAIN, 12);
	static final Font InstructionsFont = new Font("Sansserif", 2, 12);
	static final Font RoiFont = new Font("Sansserif", Font.PLAIN, 20);
	
	//Fix formats
	DecimalFormat dformat6 = new DecimalFormat("#0.000000");
	DecimalFormat dformat3 = new DecimalFormat("#0.000");
	DecimalFormat dformat0 = new DecimalFormat("#0");
	DecimalFormat dformatDialog = new DecimalFormat("#0.000000");	
		
	static final String[] nrFormats = {"US (0.00...)", "Germany (0,00...)"};
	
	static SimpleDateFormat NameDateFormatter = new SimpleDateFormat("yyMMdd_HHmmss");
	static SimpleDateFormat FullDateFormatter = new SimpleDateFormat("yyyy-MM-dd	HH:mm:ss");
	static SimpleDateFormat FullDateFormatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//Progress Dialog
	ProgressDialog progress;	
	boolean processingDone = false;	
	boolean continueProcessing = true;
	
	//-----------------define params for Dialog-----------------
	static final String[] taskVariant = {"active image in FIJI","multiple images (open multi-task manager)", "all images open in FIJI"};
	String selectedTaskVariant = taskVariant[1];
	int tasks = 1;
	
	String loadSeries = "ALL";
	
	boolean channelSelected [] = new boolean [] {true, false, false, false, false, false};
	
	static final String [] Colors = {"ORIGINAL","Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Grays"};
	String colorsSelected [];
	
	boolean saveMerged = false;

	boolean saveMergedPNG = false;
	
	boolean savePNG = false;
	
	boolean saveDate = false;
	
	boolean saveSeriesName = false;
	
	int addPlanesBefore = 0, addPlanesAfter = 0;
	
	//-----------------define params for Dialog-----------------
	
	//Variables for processing of an individual task
//		enum channelType {PLAQUE,CELL,NEURITE};
	
public void run(String arg) {	
	//Initialize
	dformat6.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	dformat3.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	dformat0.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	dformatDialog.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
	
	colorsSelected = new String [channelSelected.length];
	Arrays.fill(colorsSelected, "ORIGINAL");
		
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//-------------------------GenericDialog--------------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
	
	GenericDialog gd = new GenericDialog(PLUGINNAME + " - set parameters");	
	//show Dialog-----------------------------------------------------------------
	//.setInsets(top, left, bottom)
	gd.setInsets(0,0,0);	gd.addMessage(PLUGINNAME + ", Version " + PLUGINVERSION + ", \u00a9 2021 JN Hansen", SuperHeadingFont);	
	gd.setInsets(0,0,0);	gd.addChoice("process ", taskVariant, selectedTaskVariant);

	gd.setInsets(0,0,0);	gd.addMessage("The plugin processes .tif images or calls a BioFormats plugin to open different formats.", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("The BioFormats plugin is preinstalled in FIJI / can be manually installed to ImageJ.", InstructionsFont);
	
	gd.setInsets(0,0,0);	gd.addStringField("Series to be processed (if multi-series files loaded via BioFormats plugin)", loadSeries);
	gd.setInsets(0,0,0);	gd.addMessage("Notes:", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("1. If not 'ALL' series shall be processed, enter the series numbers separated by commas. E.g., enter '1,7' to process series 1 and 7.", InstructionsFont);
	gd.setInsets(0,0,0);	gd.addMessage("2. If only series whose title starts with 'Series' shall be processed, enter 'SERIES'.", InstructionsFont);

	gd.setInsets(5,0,0);	gd.addMessage("Channels to be used for determining the sharpest plane", SubHeadingFont);
	gd.setInsets(0,0,0);
	for(int i = 0; i < channelSelected.length; i++) {
		gd.addCheckbox("use channel " + (i+1) + " for calculation", channelSelected [i]);
		gd.setInsets(0,0,0);
	}

	gd.setInsets(5,0,0);	gd.addMessage("Color settings", SubHeadingFont);
	
	gd.setInsets(0,0,0);
	for(int i = 0; i < colorsSelected.length; i++) {
		gd.addChoice("Color for channel " + (i+1) + ":", Colors, colorsSelected [i]);
		gd.setInsets(0,0,0);
	}

	gd.setInsets(0,0,0);		gd.addMessage("Projection around the sharpest plane", SubHeadingFont);
	gd.setInsets(0,0,0); 	gd.addNumericField("Include planes before sharpest plane:", addPlanesBefore, 0);
	gd.setInsets(0,0,0); 	gd.addNumericField("Include planes after sharpest plane:", addPlanesAfter, 0);
	gd.setInsets(0,0,0);		gd.addMessage("If planes before / after are included, a maximum projection is created based on the planes before / after.", InstructionsFont);
	

	gd.setInsets(5,0,0);		gd.addMessage("Output file names", SubHeadingFont);
	gd.setInsets(0,0,0);		gd.addCheckbox("save date in output file names", saveDate);
	gd.setInsets(0,0,0);		gd.addCheckbox("save series name in output file names", saveSeriesName);
	gd.showDialog();
	//show Dialog-----------------------------------------------------------------

	//read and process variables--------------------------------------------------	
	selectedTaskVariant = gd.getNextChoice();
	
	loadSeries = gd.getNextString();

	for(int i = 0; i < channelSelected.length; i++) {
		channelSelected [i] = gd.getNextBoolean();
	}
	
	for(int i = 0; i < channelSelected.length; i++) {
		colorsSelected [i] = gd.getNextChoice();
	}
	
	addPlanesBefore = (int) gd.getNextNumber();
	addPlanesAfter = (int) gd.getNextNumber();
	
	saveDate = gd.getNextBoolean();
	saveSeriesName = gd.getNextBoolean();
	
	//read and process variables--------------------------------------------------
	if (gd.wasCanceled()) return;
	
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//---------------------end-GenericDialog-end----------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&


	String name [] = {"",""};
	String dir [] = {"",""};
	ImagePlus allImps [] = new ImagePlus [2];
//	RoiEncoder re;
	{
		//Improved file selector
		try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}catch(Exception e){}
		if(selectedTaskVariant.equals(taskVariant[1])){
			OpenFilesDialog od = new OpenFilesDialog ();
			od.setLocation(0,0);
			od.setVisible(true);
			
			od.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	return;
		        }
		    });
		
			//Waiting for od to be done
			while(od.done==false){
				try{
					Thread.currentThread().sleep(50);
			    }catch(Exception e){
			    }
			}
			
			tasks = od.filesToOpen.size();
			name = new String [tasks];
			dir = new String [tasks];
			for(int task = 0; task < tasks; task++){
				name[task] = od.filesToOpen.get(task).getName();
				dir[task] = od.filesToOpen.get(task).getParent() + System.getProperty("file.separator");
			}		
		}else if(selectedTaskVariant.equals(taskVariant[0])){
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
			name [0] = info.fileName;	//get name
			dir [0] = info.directory;	//get directory
			tasks = 1;
		}else if(selectedTaskVariant.equals(taskVariant[2])){	// all open images
			if(WindowManager.getIDList()==null){
				new WaitForUserDialog("Plugin canceled - no image open in FIJI!").show();
				return;
			}
			int IDlist [] = WindowManager.getIDList();
			tasks = IDlist.length;	
			if(tasks == 1){
				selectedTaskVariant=taskVariant[0];
				FileInfo info = WindowManager.getCurrentImage().getOriginalFileInfo();
				name [0] = info.fileName;	//get name
				dir [0] = info.directory;	//get directory
			}else{
				name = new String [tasks];
				dir = new String [tasks];
				allImps = new ImagePlus [tasks];
				for(int i = 0; i < tasks; i++){
					allImps[i] = WindowManager.getImage(IDlist[i]); 
					FileInfo info = allImps[i].getOriginalFileInfo();
					name [i] = info.fileName;	//get name
					dir [i] = info.directory;	//get directory
				}		
			}
					
		}
	}
	 	
	//For BioFormats - screen for series and add tasks accordingly
		ImporterOptions bfOptions;
		int series [] = new int [tasks];
		String seriesName [] = new String [tasks];
		int totSeries [] = new int [tasks];
		Arrays.fill(series, 0);
		Arrays.fill(totSeries, 1);
		Arrays.fill(seriesName, "");
		String loadSeriesTemp;
		String removedFiles = "\n";

//		String filesList = "Files to process:\n";
		if(selectedTaskVariant.equals(taskVariant[1])){
			for(int i = tasks-1; i >= 0; i--){
				IJ.showProgress((tasks-i)/tasks);
				if(name [i].substring(name[i].lastIndexOf(".")).equals(".tif")
						|| name [i].substring(name[i].lastIndexOf(".")).equals(".TIF")
						|| name [i].substring(name[i].lastIndexOf(".")).equals(".tiff")
						|| name [i].substring(name[i].lastIndexOf(".")).equals(".TIFF")) {
					continue;
				}
				try {
					bfOptions = new ImporterOptions();
					bfOptions.setId(""+dir[i]+name[i]+"");
					bfOptions.setVirtual(true);
					
					int nOfSeries = getNumberOfSeries(bfOptions);
//					IJ.log("nSeries: " + nOfSeries);
					
					if(loadSeries.equals("ALL") && nOfSeries > 1) {
						String [] nameTemp = new String [name.length+nOfSeries-1], 
								dirTemp = new String [name.length+nOfSeries-1];
						int [] seriesTemp = new int [nameTemp.length],
								totSeriesTemp = new int [nameTemp.length]; 
						String [] seriesNameTemp = new String [nameTemp.length];
						
						for(int j = 0; j < i; j++) {
							nameTemp [j] = name [j]; 
							dirTemp [j] = dir [j];
							seriesTemp [j] = series [j];
							seriesNameTemp [j] = seriesName [j];
							totSeriesTemp [j] = totSeries [j];
							
						}
						for(int j = 0; j < nOfSeries; j++) {
							nameTemp [i+j] = name [i]; 
							dirTemp [i+j] = dir [i];
							seriesTemp [i+j] = j;
							seriesNameTemp [i+j] = getSeriesName(bfOptions, j);
							totSeriesTemp [i+j] = nOfSeries;
						}
						for(int j = i+1; j < name.length; j++) {
							nameTemp [j+nOfSeries-1] = name [j]; 
							dirTemp [j+nOfSeries-1] = dir [j];
							seriesTemp [j+nOfSeries-1] = series [j];
							seriesNameTemp [j+nOfSeries-1] = seriesName [j];
							totSeriesTemp [j+nOfSeries-1] = totSeries [j];
						}
						
						//copy arrays
						tasks = nameTemp.length;
						name = new String [tasks];
						dir = new String [tasks];
						series = new int [tasks];
						seriesName = new String [tasks];
						totSeries = new int [tasks];
						
						for(int j = 0; j < nameTemp.length; j++) {
							name [j] = nameTemp [j];
							dir [j] = dirTemp [j];
							series [j] = seriesTemp [j];
							seriesName [j] = seriesNameTemp [j];
							totSeries [j] = totSeriesTemp [j];
//								filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
						}
					}else if(!loadSeries.equals("ALL")){
						if(loadSeries.equals("SERIES")){
							loadSeriesTemp = "";
							for(int ser = 0; ser < nOfSeries; ser++) {
								if(getSeriesName(bfOptions, ser).startsWith("Series")) {
									if(loadSeriesTemp.length() == 0) {
										loadSeriesTemp = "" + (ser+1);
									}else {
										loadSeriesTemp += "," + (ser+1);
									}
								}
							}
						}else {
							loadSeriesTemp = loadSeries;						
						}
						
						int nrOfSeriesImages = 0;
						for(int j = 0; j < nOfSeries; j++) {
							if(!(","+loadSeriesTemp+",").contains(","+(j+1)+",")) {
								continue;
							}
							nrOfSeriesImages++;
						}
						
						if(nrOfSeriesImages>0) {
							String [] nameTemp = new String [name.length+nrOfSeriesImages-1], 
									dirTemp = new String [name.length+nrOfSeriesImages-1];
							int [] seriesTemp = new int [nameTemp.length],
									totSeriesTemp = new int [nameTemp.length]; 
							String [] seriesNameTemp = new String [nameTemp.length];
							for(int j = 0; j < i; j++) {
								nameTemp [j] = name [j]; 
								dirTemp [j] = dir [j];
								seriesTemp [j] = series [j];
								seriesNameTemp [j] = seriesName [j];
								totSeriesTemp [j] = totSeries [j];
								
							}
							int k = 0;
							for(int j = 0; j < nOfSeries; j++) {
								if(!(","+loadSeriesTemp+",").contains(","+(j+1)+",")) {
									continue;
								}
								nameTemp [i+k] = name [i]; 
								dirTemp [i+k] = dir [i];
								seriesTemp [i+k] = j;
								seriesNameTemp [i+k] = getSeriesName(bfOptions, j);
								totSeriesTemp [i+k] = nOfSeries;
								k++;
							}
							
							for(int j = i+1; j < name.length; j++) {
								nameTemp [j+nrOfSeriesImages-1] = name [j]; 
								dirTemp [j+nrOfSeriesImages-1] = dir [j];
								seriesTemp [j+nrOfSeriesImages-1] = series [j];
								seriesNameTemp [j+nrOfSeriesImages-1] = seriesName [j];
								totSeriesTemp [j+nrOfSeriesImages-1] = totSeries [j];
							}
							
							//copy arrays
	
							tasks = nameTemp.length;
							name = new String [tasks];
							dir = new String [tasks];
							series = new int [tasks];
							seriesName = new String [tasks];
							totSeries = new int [tasks];
							
							for(int j = 0; j < nameTemp.length; j++) {
								name [j] = nameTemp [j];
								dir [j] = dirTemp [j];
								series [j] = seriesTemp [j];
								seriesName [j] = seriesNameTemp [j];
								totSeries [j] = totSeriesTemp [j];
//									filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
							}
						}else {
							//REMOVE NAME FROM LIST
							removedFiles += name [i] + "\n";
							String [] nameTemp = new String [name.length-1], 
									dirTemp = new String [name.length-1];
							int [] seriesTemp = new int [nameTemp.length],
									totSeriesTemp = new int [nameTemp.length]; 
							String [] seriesNameTemp = new String [nameTemp.length];
							for(int j = 0; j < i; j++) {
								nameTemp [j] = name [j]; 
								dirTemp [j] = dir [j];
								seriesTemp [j] = series [j];
								seriesNameTemp [j] = seriesName [j];
								totSeriesTemp [j] = totSeries [j];
								
							}
							for(int j = i+1; j < name.length; j++) {
								nameTemp [j-1] = name [j]; 
								dirTemp [j-1] = dir [j];
								seriesTemp [j-1] = series [j];
								seriesNameTemp [j-1] = seriesName [j];
								totSeriesTemp [j-1] = totSeries [j];
							}
							
							//copy arrays
	
							tasks = nameTemp.length;
							name = new String [tasks];
							dir = new String [tasks];
							series = new int [tasks];
							seriesName = new String [tasks];
							totSeries = new int [tasks];
							
							for(int j = 0; j < nameTemp.length; j++) {
								name [j] = nameTemp [j];
								dir [j] = dirTemp [j];
								series [j] = seriesTemp [j];
								seriesName [j] = seriesNameTemp [j];
								totSeries [j] = totSeriesTemp [j];
//									filesList += name[j] + "\t" + dir[j] + "\t" + series[j] + "\t" + totSeries[j] + "\n";
							}
						}
					}
				} catch (Exception e) {
					String out = "" + e.getMessage();
					out += "\n" + e.getCause();
					for(int err = 0; err < e.getStackTrace().length; err++){
						out += " \n " + e.getStackTrace()[err].toString();
					}			
					IJ.log("error: " + name [i] +": "+ out);
				}
			}
		}
		
		if(tasks == 0) {
			new WaitForUserDialog("The series preference '" + loadSeries + "' did not fit to any file to be processed - Plugin cancelled.").show();
			return;
		}
		
		//add progressDialog
			progress = new ProgressDialog(name, series, tasks, 1);
			progress.setLocation(0,0);
			progress.setVisible(true);
			progress.addWindowListener(new java.awt.event.WindowAdapter() {
		        public void windowClosing(WindowEvent winEvt) {
		        	if(processingDone==false){
		        		IJ.error("Script stopped...");
		        	}
		        	continueProcessing = false;	        	
		        	return;
		        }
			});
		

			if(removedFiles.length()>1) {
				progress.notifyMessage("For some files, the series preference '" 
						+ loadSeries + "' did not fit - these files were excluded from analysis:" 
						+ removedFiles + "", ProgressDialog.NOTIFICATION);			
			}
		
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
//------------------------------CELL MEASUREMENT------------------------------
//&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

	for(int task = 0; task < tasks; task++){
	running: while(continueProcessing){
		Date startDate = new Date();
		progress.updateBarText("in progress...");
		//Check for problems
				if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".txt")){
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
					progress.moveTask(task);	
					break running;
				}
				if(name[task].substring(name[task].lastIndexOf("."),name[task].length()).equals(".zip")){	
					progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": A file is no image! Could not be processed!", ProgressDialog.ERROR);
					progress.moveTask(task);	
					break running;
				}		
		//Check for problems
				
		//open Image
			ImagePlus imp;
	   		try{
		   		if(selectedTaskVariant.equals(taskVariant[1])){
		   			if(name[task].contains(".tif")){
		   				//TIFF file
		   				imp = IJ.openImage(""+dir[task]+name[task]+"");		
		   			}else{
		   				//bio format reader
		   				bfOptions = new ImporterOptions();
		   				bfOptions.setId(""+dir[task]+name[task]+"");
		   				bfOptions.setVirtual(false);
		   				bfOptions.setAutoscale(true);
		   				bfOptions.setColorMode(ImporterOptions.COLOR_MODE_COMPOSITE);
		   				for(int i = 0; i < totSeries[task]; i++) {
		   					if(i==series[task]) {
		   						bfOptions.setSeriesOn(i, true);
		   					}else {
		   						bfOptions.setSeriesOn(i, false);
		   					}
		   				}
		   				ImagePlus [] imps = BF.openImagePlus(bfOptions);
		   				imp = imps [0];	
		   				imp.setDisplayMode(IJ.COMPOSITE);
		   			}
		   			imp.hide();
					imp.deleteRoi();
		   		}else if(selectedTaskVariant.equals(taskVariant[0])){
		   			imp = WindowManager.getCurrentImage();
		   			imp.deleteRoi();
		   		}else{
		   			imp = allImps[task];
		   			imp.deleteRoi();
		   		}
		   	}catch (Exception e) {
		   		progress.notifyMessage("Task " + (task+1) + "/" + tasks + ": file is no image - could not be processed!", ProgressDialog.ERROR);
				progress.moveTask(task);	
				break running;
			}
	   	//open Image
		   	
			imp.lock();
			
	   	/******************************************************************
		*** 						PROCESSING							***	
		*******************************************************************/
		//Define Output File Names
			Date currentDate = new Date();
			
			String filePrefix;
			if(name[task].contains(".")){
				filePrefix = name[task].substring(0,name[task].lastIndexOf(".")) + "_Sh";
			}else{
				filePrefix = name[task] + "_Sh";
			}
			
			if(saveDate){
				filePrefix += "_" + NameDateFormatter.format(currentDate);
			}
						
			if(totSeries [task] > 1) {
				filePrefix += "_s" + series [task];
				if(saveSeriesName) {
					filePrefix += "_" + seriesName [task];				
				}
				
			}
						
			filePrefix = dir[task] + filePrefix;
		//Define Output File Names
					
		//start metadata file
			TextPanel tp1 =new TextPanel("results");
			
			tp1.append("Saving date:	" + FullDateFormatter.format(currentDate)
						+ "	Starting date:	" + FullDateFormatter.format(startDate));
			tp1.append("Image name:	" + name[task]);
			tp1.append("");
			tp1.append("Settings:");
			tp1.append("	Add Planes Before Sharpest Plane:	" + dformat0.format(addPlanesBefore));
			tp1.append("	Add Planes After Sharpest Plane:	" + dformat0.format(addPlanesAfter));
			tp1.append("	Channel	Selected	Color");
			String appText;
			for(int c = 0; c < channelSelected.length; c++) {
				if(c == imp.getNChannels()) {
					break;
				}
				appText = "	" + (c+1);
				if(channelSelected [c]) {
					appText += "	Y";
				}else {
					appText += "	N";
				}
				appText += "	" + colorsSelected [c];
				tp1.append(appText);
			}
			tp1.append("");
			tp1.append("Results of sharpness measurements:");
		//start metadata file
			
		//processing
			tp1.append("	Plane	SD");
			double sd [] = getPlaneSDs(imp);
			int sharpestPlane = 0;
			double maxSd = 0.0;
			for(int s = 0; s < sd.length; s++) {
				if(sd [s] > maxSd) {
					maxSd = sd [s];
					sharpestPlane = s;
				}
			}
			for(int s = 0; s < sd.length; s++) {
				if(s == sharpestPlane) {
					tp1.append("SHARPEST	" + dformat0.format(s) + "	" + dformat6.format(sd [s]));
					
				}else {
					tp1.append("	" + dformat0.format(s) + "	" + dformat6.format(sd [s]));
					
				}
			}
			
			ImagePlus impOut = getPlaneOrMaxOfPlanesWithinRange(imp, sharpestPlane+1);
			
			changeColors(impOut);
			
			IJ.saveAsTiff(impOut, filePrefix + ".tif");
			
			if(savePNG) {
				if(impOut.getNFrames()!=1||impOut.getNSlices()!=1) {
					impOut = maxIntensityProjection(impOut);
				}
				IJ.saveAs(impOut, "PNG",filePrefix + ".png");
			}
			
			impOut.changes = false;
			impOut.close();

			//Save Metadata text file			
			addFooter(tp1, currentDate);				
			tp1.saveAs(filePrefix + ".txt");
			
			imp.unlock();			
			if(selectedTaskVariant.equals(taskVariant[1])){
				imp.changes = false;
				imp.close();
			}
		//Output Datafile
		processingDone = true;
		break running;
	}	
	progress.updateBarText("finished!");
	progress.setBar(1.0);
	progress.moveTask(task);
}
}

private void addFooter(TextPanel tp, Date currentDate){
	tp.append("");
	tp.append("Datafile was generated on " + FullDateFormatter2.format(currentDate) + " by '"+PLUGINNAME+"', an ImageJ plug-in by Jan Niklas Hansen (jan.hansen@uni-bonn.de).");
	tp.append("The plug-in '"+PLUGINNAME+"' is distributed in the hope that it will be useful,"
			+ " but WITHOUT ANY WARRANTY; without even the implied warranty of"
			+ " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
	tp.append("Plug-in version:	V"+PLUGINVERSION);
	
}

private static ImagePlus maxIntensityProjection(ImagePlus imp){
	CompositeImage impMax = new CompositeImage(IJ.createHyperStack("max proj", imp.getWidth(), imp.getHeight(), imp.getNChannels(), 1, 1, imp.getBitDepth()));
	impMax.copyLuts(imp);
	impMax.setDisplayMode(IJ.COMPOSITE);
	impMax.setCalibration(imp.getCalibration());
//	impMax.setLuts(imp.getLuts());
	int index = 0, indexMax = 0;
	
	String activeChannels = "";
	for(int c = 0; c < imp.getNChannels(); c++){
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
				indexMax = impMax.getStackIndex(c+1, 1, 1)-1;
				impMax.getStack().setVoxel(x, y, indexMax, 0.0);
				for(int s = 0; s < imp.getNSlices(); s++){
					for(int f = 0; f < imp.getNFrames(); f++){
						index = imp.getStackIndex(c+1, s+1, f+1)-1;
						indexMax = imp.getStackIndex(c+1, 1, 1)-1;
						if(imp.getStack().getVoxel(x, y, index) > impMax.getStack().getVoxel(x, y, indexMax)){
							impMax.getStack().setVoxel(x, y, indexMax, imp.getStack().getVoxel(x, y, index));
						}
					}					
				}
			}
		}
		impMax.setC(c+1);	
   		imp.setC(c+1);
//   		impMax.setDisplayRange(imp.getDisplayRangeMin(), imp.getDisplayRangeMax());
   		activeChannels += "1";
	}	
	impMax.setActiveChannels(activeChannels);
	return impMax;
}

/**
 * @param channel: 1 <= plane <= # planes
 * */
private ImagePlus getPlaneOrMaxOfPlanesWithinRange(ImagePlus imp, int plane){
	CompositeImage impNew = new CompositeImage(IJ.createHyperStack("channel image", imp.getWidth(), imp.getHeight(), imp.getNChannels(), 1, imp.getNFrames(), imp.getBitDepth()));
	impNew.copyLuts(imp);
	impNew.setDisplayMode(IJ.COMPOSITE);
	impNew.setCalibration(imp.getCalibration());
	
	int indexNew = 0, start = plane-addPlanesBefore-1, end = plane+addPlanesAfter-1;
	if(start < 0)					start = 0;
	if(end > (imp.getNSlices()-1))	end = imp.getNSlices()-1;
	String activeChannels = "";
	double max = 0.0;	
	
	for(int c = 0; c < imp.getNChannels(); c++){
		for(int x = 0; x < imp.getWidth(); x++){
			for(int y = 0; y < imp.getHeight(); y++){
				for(int f = 0; f < imp.getNFrames(); f++){
					max = 0.0;
					for(int s = start; s <= end; s++){
						if(imp.getStack().getVoxel(x, y, imp.getStackIndex(c+1, s+1, f+1)-1) >= max) {
							max = imp.getStack().getVoxel(x, y, imp.getStackIndex(c+1, s+1, f+1)-1);
						}
					}
					indexNew = impNew.getStackIndex(c+1, 1, f+1)-1;
					impNew.getStack().setVoxel(x, y, indexNew, max);
				}					
			}
		}
		activeChannels += "1";
	}
	impNew.setActiveChannels(activeChannels);
	return impNew;
}

/**
 * @param channel: 1 <= channel <= # channels
 * */
private void changeColors(ImagePlus impOut){
	String activeChannels = "";
	for(int c = 0; c < colorsSelected.length; c++) {
		if(c == impOut.getNChannels()) break;
		impOut.setC(c+1);
		if(!colorsSelected[c].equals("ORIGINAL")){
			IJ.run(impOut, colorsSelected [c], "");
			activeChannels = "1" + activeChannels;
		}else {
			activeChannels = "1" + activeChannels;
		}
	}
	
	impOut.setDisplayMode(IJ.COMPOSITE);
	impOut.setActiveChannels(activeChannels);
}

/**
 * */
private double [] getPlaneSDs (ImagePlus imp){
	int ct [] = new int [imp.getNSlices()];
	Arrays.fill(ct, 0);
	double mean [] = new double [imp.getNSlices()];
	Arrays.fill(mean, 0.0);
	for(int c = 0; c < imp.getNChannels(); c++){
		if(!channelSelected[c]) continue;
		for(int t = 0; t < imp.getNFrames(); t++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int x = 0; x < imp.getWidth(); x++){
					for(int y = 0; y < imp.getHeight(); y++){
						mean [s] += imp.getStack().getVoxel(x, y, imp.getStackIndex(c+1, s+1, t+1)-1);
						ct [s] ++;
					}
				}
			}
		}
	}
	
	for(int s = 0; s < imp.getNSlices(); s++){
		mean [s] /= (double) ct [s];
	}
	
	
	double sd [] = new double [imp.getNSlices()];
	Arrays.fill(sd, 0.0);
	for(int c = 0; c < imp.getNChannels(); c++){
		if(!channelSelected[c]) continue;
		for(int t = 0; t < imp.getNFrames(); t++){
			for(int s = 0; s < imp.getNSlices(); s++){
				for(int x = 0; x < imp.getWidth(); x++){
					for(int y = 0; y < imp.getHeight(); y++){
						sd [s] += Math.pow(imp.getStack().getVoxel(x, y, imp.getStackIndex(c+1, s+1, t+1)-1) - mean [s], 2.0);
					}
				}
			}
		}
	}
	
	for(int s = 0; s < imp.getNSlices(); s++){
		sd [s] /= ct [s]-1.0;
		sd [s] = Math.sqrt(sd [s]);	
	}
	
	return sd;
}

/**
 * get number of series 
 * */
private int getNumberOfSeries(ImporterOptions options) throws FormatException, IOException{
	ImportProcess process = new ImportProcess(options);
	if (!process.execute()) return -1;
	return process.getSeriesCount();
}

/**
 * @return name of the @param series (0 <= series < number of series)
 * */
private String getSeriesName(ImporterOptions options, int series) throws FormatException, IOException{
	ImportProcess process = new ImportProcess(options);
	if (!process.execute()) return "NaN";
	return process.getSeriesLabel(series);
}
}//end main class