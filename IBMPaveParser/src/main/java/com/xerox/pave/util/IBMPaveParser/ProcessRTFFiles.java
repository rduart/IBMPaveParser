package com.xerox.pave.util.IBMPaveParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;


/**
 * Description @  SDN 16008B – Convert the RTF file in to text 
 * @version 	PAVE V1 24 Apr 2017
 * @author 	Prakash Patil , Anusha Maddali
 */


public class ProcessRTFFiles {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Local
		final String path = "C:\\Users\\IBM_ADMIN\\Desktop\\TEST\\";
		/* DEV */
		//final String path="/home/ppraka01/PAVE/";
		/* SPESIT */
		//final String path="/home/sftp_s_pave/spesit/";
		
		/* SIT */
		//final String path="/home/sftp_s_pave/sit/";
		
		/* UAT */
		//final String path="/home/sftp_i_pave/uat/";
		
		final String propFileName = "DocPath.properties";

		// read the properties file
		String propFileNamepath = path + propFileName;
		String rtfDocpath = "";
		String inputPath = "";
		String outputPath = "";
		String nmpFlag ="";
		String orpFlag="";
		String billFlag="";
		final String initialTrMsg="I - INITIAL VERSION";
		final String rejectedTrMsg="U - UNDEFINED FORMAT";
		final String completedTrMsg="C - CONVERTED";
		
		Properties prop = new Properties();
		try {

			prop.load(new FileInputStream(propFileNamepath));
			rtfDocpath = prop.getProperty("rtfdocpath");
			inputPath = prop.getProperty("inputpath");
			outputPath = prop.getProperty("outputpath");
			nmpFlag = prop.getProperty("orpflag");
			orpFlag = prop.getProperty("nmpflag");
			billFlag = prop.getProperty("billflag");
			
			System.out.println("rtfDocpath>>"+rtfDocpath);
			System.out.println("inputPath>>"+inputPath);
			System.out.println("outputPath>>"+outputPath);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// Code for to convert the RTF to text
		final File folder = new File(rtfDocpath);
		FileInputStream fis;
		InputStreamReader reader;
		BufferedReader br = null;
		ProcessRTFFiles processRTFFiles = new ProcessRTFFiles();
		int countFiles = 0;
		if (folder.exists() && folder.isDirectory()) {
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isFile()
						&& (fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1).equals("rtf"))) {
					String rtfFileName = fileEntry.getName();
					String inputRtffileName = rtfDocpath + rtfFileName;
					int extIndex = rtfFileName.indexOf(".");
					String strFilename = rtfFileName.substring(0, extIndex);

					// log.info(rtfFileName);

					String txtFileName = strFilename + ".txt";
					String inputTextFile = inputPath + txtFileName;

					try {
						processRTFFiles.convertRTFToText(inputRtffileName, inputTextFile);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// log.error("Exception",e);
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						// log.error("Exception",e);
						e.printStackTrace();
					} catch (TikaException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						// log.error("Exception",e);
					}
					countFiles++;
				}
			}

			// log.info("Number of RTf files have been processed :"+
			// countFiles);
		}

		
		final File inputfolder = new File(inputPath);
		
		// If there are no files to process write an empty record in the tracking file
		   String[]	listOfFiles=inputfolder.list();
		    if(listOfFiles.length==0){
		    	try {
					processRTFFiles.writeEmptyTrackingFile(outputPath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
	     // end 
		    
		    /**
			 * Code for to convert the text file to Mainframe format 
			 */
		if (inputfolder.exists() && inputfolder.isDirectory()) {
			for (final File fileEntry : inputfolder.listFiles()) {
				if (fileEntry.isFile()
						&& (fileEntry.getName().substring(fileEntry.getName().lastIndexOf('.') + 1).equals("txt"))) {
					String inputTextFileName = inputPath + fileEntry.getName();
					try {
						fis = new FileInputStream(new File(inputTextFileName));
						reader = new InputStreamReader(fis);
						br = new BufferedReader(reader);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					ProcessNMPFile nmp = new ProcessNMPFile();
					ProcessORPFile orp = new ProcessORPFile();
					ProcessBillingFile bill = new ProcessBillingFile();
					try {
						String lineHeader = br.readLine();
						//System.out.println("lineHeader:::"+lineHeader);
						if ((lineHeader != null) && lineHeader.contains("NMP") && nmpFlag.equals("Y")) {
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "N1",initialTrMsg);
							
							//To Call readNMPFile method to convert into Mainframe format
							nmp.readNMPFile(inputPath, fileEntry.getName(), outputPath);
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "N1",completedTrMsg);
						} else if (lineHeader.contains("ORP") && orpFlag.equals("Y")) {
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "O1",initialTrMsg);
							// To call processORPFile method to convert into Mainframe format
							orp.processORPFile(inputPath, fileEntry.getName(), outputPath);
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "O1",completedTrMsg);
						} else if (lineHeader.contains("PAVE INPUT") && billFlag.equals("Y")) {
							//System.out.println("BILL");
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "B1",initialTrMsg);
							// To call processORPFile method to convert into Mainframe format
							bill.processBilling(inputPath, fileEntry.getName(), outputPath);
							//System.out.println("BILL@@@@@@@@");
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "B1",completedTrMsg);
						}else {
							//System.out.println("ELSE UndefinedIndexFile");
							processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "",initialTrMsg);
							processRTFFiles.writeErrorFile(fileEntry.getName(), outputPath);
							processRTFFiles.writeUndefinedIndexFile(fileEntry.getName(),outputPath,lineHeader);
						}
						
						//else {
						//	processRTFFiles.writeTrackingFile(fileEntry.getName(), outputPath, "",initialTrMsg);
						//	processRTFFiles.writeErrorFile(fileEntry.getName(), outputPath);
						//}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
	}

	/**
	 * comments convertRTFToText Used to convert RTf file to text format
	 * 
	 * @param inputFileName.rtf
	 *            outputFile.txt
	 * 
	 */
	public void convertRTFToText(String inputFileName, String outputFile)
			throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(new File(inputFileName));
		ParseContext pcontext = new ParseContext();
		RTFParser rtfparser = new RTFParser();
		rtfparser.parse(inputstream, handler, metadata, pcontext);
		File tempfile = new File(outputFile);
		PrintWriter writer = new PrintWriter(tempfile);
		writer.write(handler.toString());
		writer.close();
	}

	/**
	 * comments writeTrackingFile Use for to write the Tracking file
	 * 
	 * @param fileName,
	 *            path, type
	 */

	private void writeTrackingFile(String fileName, String outputPath, String type,String trMsg) throws IOException {
		ProcessNMPFile nmp = new ProcessNMPFile();
		StringBuffer trackingText = prepareTrackingRecordFromFileName(fileName, nmp);
		String seqNum = "";
		String lsubtype ="";
		if (type.equals("N1")) {
			seqNum = "0000";
			lsubtype = "1";
		} else if (type.equals("O1")) {
			seqNum = "0001";
			lsubtype = "1";
		}
		trackingText.append(nmp.formatField(2, type));
		trackingText.append(nmp.formatField(1, lsubtype));  // Sub_type
		trackingText.append(nmp.formatField(10, "")); // tr_Provider_No -10
		trackingText.append(nmp.formatField(2, "")); // tr_Ownnum - 02
		trackingText.append(nmp.formatField(3, "")); // tr_LocNum - 03
		trackingText.append(nmp.formatField(3, "")); // tr_PT - 03
		trackingText.append(nmp.formatField(4, seqNum)); // TR-SEQ-NO - 04
		trackingText.append(nmp.formatField(8, "")); // tr_rejdt - 8 today's
														// date for other then
														// O,N and B
		trackingText.append(nmp.formatField(8, "")); // tr_compdt - 8
		trackingText.append(nmp.formatField(8, "")); // tr_Supercededt - 8
		trackingText.append(nmp.formatField(70, trMsg)); // tr_message
																			// -
																			// 50
																			// Reject
																			// Reason
																			// (‘filtered’,
																			// ‘invalid
																			// doc
																			// type’,
																			// etc.)
		trackingText.append(nmp.formatField(8, "")); // tr_program - 8
		trackingText.append(nmp.formatField(6, "")); // tr_paragraph - 6
		trackingText.append(nmp.formatField(6, "")); // tr_instruction - 6
		trackingText.append(nmp.formatField(4, "")); // tr_ABCODE - 4
		trackingText.append(nmp.formatField(85, "")); // tr_DFHEIBLK - 85
		// trackingText.append(nmp.formatField(1, "I")); // tr_recordtype - 01
		// (Ex - I , E , W , F)
		String trackingOutFileName = "Output_TRC_" + nmp.getSystemDate("MMddyyyy") + ".txt";
		nmp.writeOutputToFile(outputPath, trackingText.toString().toUpperCase(), trackingOutFileName);
	}

	/**
	 * comments writeErrorFile method Use for to write the Tracking file for
	 * other types doc
	 * 
	 * @param fileName, path
	 */
	private void writeErrorFile(String fileName, String outputPath) throws IOException {
		ProcessNMPFile nmp = new ProcessNMPFile();
		StringBuffer errorText = prepareTrackingRecordFromFileName(fileName, nmp);
		errorText.append(nmp.formatField(2, ""));
		errorText.append(nmp.formatField(1, ""));  // Sub_type
		errorText.append(nmp.formatField(10, "")); // tr_Provider_No -10
		errorText.append(nmp.formatField(2, "")); // tr_Ownnum - 02
		errorText.append(nmp.formatField(3, "")); // tr_LocNum - 03
		errorText.append(nmp.formatField(3, "")); // tr_PT - 03
		errorText.append(nmp.formatField(4, "")); // TR-SEQ-NO - 04
		String sysdate = nmp.getSystemDate("yyyyMMdd");
		errorText.append(sysdate); // tr_rejdt - 8 today's date for other then
									// O,N and B
		errorText.append(nmp.formatField(8, "")); // tr_compdt - 8
		errorText.append(nmp.formatField(8, "")); // tr_Supercededt - 8
		errorText.append(nmp.formatField(70, "U - UNDEFINED FORMAT")); // tr_message
																		// - 50
																		// Reject
																		// Reason
																		// (‘filtered’,
																		// ‘invalid
																		// doc
																		// type’,
																		// etc.)
		errorText.append(nmp.formatField(8, "")); // tr_program - 8
		errorText.append(nmp.formatField(6, "")); // tr_paragraph - 6
		errorText.append(nmp.formatField(6, "")); // tr_instruction - 6
		errorText.append(nmp.formatField(4, "")); // tr_ABCODE - 4
		errorText.append(nmp.formatField(85, "")); // tr_DFHEIBLK - 85
		// errorText.append("E");
		// System.out.println("Tracking >>"+errorText.toString().toUpperCase());
		String errorOutFileName = "Output_TRC_" + nmp.getSystemDate("MMddyyyy") + ".txt";
		nmp.writeOutputToFile(outputPath, errorText.toString().toUpperCase(), errorOutFileName);
	}
	
	/**
	 * writeUndefinedIndexFile  method use for to prepare a Index record for Undefined files
	 * 
	 */
	private void writeUndefinedIndexFile(String fileName, String outputPath, String lineHeader) throws IOException {
		ProcessNMPFile nmp = new ProcessNMPFile();
		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
		StringBuffer undefinedText =  new StringBuffer();
		
		String[] fileNametokens = fileName.split("_");
		String inputDateToken = fileNametokens[fileNametokens.length - 1];
		String accountNum = fileNametokens[fileNametokens.length - 2];
		String reviewinputDate = inputDateToken.substring(0, 8);
		String formattedReviewInputDate = nmp.convertInputDate(reviewinputDate);
		
		undefinedText.append(fileNameWithOutExt+getPipe());
		undefinedText.append(nmp.formatField(9, "Undefined")+getPipe()); 
		undefinedText.append(accountNum+getPipe());
		undefinedText.append(formattedReviewInputDate+getPipe());
		
		undefinedText.append(getPipe()+getPipe()+getPipe()+getPipe()+getPipe()+getPipe());
		
		//System.out.println("lineHeader::::::::"+lineHeader);
		/*
		if (lineHeader.contains("Page")) {
			System.out.println("zzzzzzzzzzzzzzzzzzzzzzzz"+lineHeader);
			String[] tokens = getAccountIdAndProfileId(lineHeader);
			if (tokens.length > 0 ) {
				String formattedAccNum = nmp.formatField(13, tokens[0]);
				String formattedProfileId = nmp.formatField(10, tokens[1]);
				undefinedText.append(formattedProfileId);
			}
			}*/
		String IndexOutFileName = "Output_IND_" + nmp.getSystemDate("MMddyyyy") + ".txt";
		nmp.writeOutputToFile(outputPath, undefinedText.toString(), IndexOutFileName);
		
	}

	/**
	 * PrepareTrackingRecordFromFileName method use for to prepare the tracking record
	 * 
	 */
	private StringBuffer prepareTrackingRecordFromFileName(String fileName, ProcessNMPFile nmp) {
		return extractAccNumAndDate(fileName, nmp);
	}

	/**
	 * extractAccNumAndDate method use for to extract account
	 * number and date from filename
	 */
	
	private StringBuffer extractAccNumAndDate(String fileName, ProcessNMPFile nmp) {
		StringBuffer trackingText = new StringBuffer();
		String[] fileNametokens = fileName.split("_");
		String inputDateToken = fileNametokens[fileNametokens.length - 1];
		String accountNum = fileNametokens[fileNametokens.length - 2];
		String formattedAccNum = nmp.formatField(13, accountNum);
		String reviewinputDate = inputDateToken.substring(0, 8);
		String formattedReviewInputDate = nmp.convertInputDate(reviewinputDate);
		trackingText.append(formattedAccNum);
		trackingText.append(formattedReviewInputDate);
		return trackingText;
	}
	// for write the empty Tracking file
	private void writeEmptyTrackingFile(String outputPath) throws IOException{
		ProcessNMPFile nmp=new ProcessNMPFile();
		String trackingText=nmp.formatField(249,"");
		String trackingOutFileName = "Output_TRC_" + nmp.getSystemDate("MMddyyyy") + ".txt";
		nmp.writeOutputToFile(outputPath, trackingText, trackingOutFileName);
	} 
	
	private String getPipe() {
		return "|";
	}
}
