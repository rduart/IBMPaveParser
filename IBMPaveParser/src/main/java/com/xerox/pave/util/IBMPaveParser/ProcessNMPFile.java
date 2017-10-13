package com.xerox.pave.util.IBMPaveParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * Description @  SDN 16008B – Process the NMP rtf file in to MainFram format (order should be as per copy book)
 * @version 	PAVE V1 24 Apr 2017
 * @author 	Prakash Patil , Anusha Maddali
 */
public class ProcessNMPFile {
	

	public StringBuffer readNMPFile(String path, String fileName, String outputpath) throws IOException {
		FileInputStream fis;
		InputStreamReader reader;
		BufferedReader br = null;
		String inputfileName = path + fileName;
		
		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
        
		try {
			fis = new FileInputStream(new File(inputfileName));
			reader = new InputStreamReader(fis);
			br = new BufferedReader(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringBuffer output = new StringBuffer();
		StringBuffer indexRecord = new StringBuffer();
		StringBuffer indexRecord1 = new StringBuffer();
		String[] fileNametokens = fileName.split("_");
		String inputDateToken = fileNametokens[fileNametokens.length - 1];
		String reviewinputDate = inputDateToken.substring(0, 8);
		String formattedReviewInputDate = convertInputDate(reviewinputDate);
		String currentLine;
		String section = "";
		int nmpProviderLine = 0;
		int licenseNum = 0;
		int licType = 0;
		int effDate = 0;
		int endDate = 0;
		int currentLineNum = 0;
		boolean setEndDate = false;
		final String sequenceNum = "0000";
		final String subType = "1";
		final String docType = "N1";
		final String indexdocType = "NMP";
		String nameSection="";
		Map<Integer, String> addressMap = new HashMap<Integer, String>();
		Map<Integer, String> nmpDetailsMap = new HashMap<Integer, String>();
		while (((currentLine = br.readLine()) != null)) {

			if (currentLine.contains("PAVE NMP")) {
				output.append(docType);
				//indexRecord.append(formatField(6,"PSF072"));
				indexRecord.append(fileNameWithOutExt);
				indexRecord.append(getPipe()+indexdocType);
			}

			// extract account number and profile Id
			if (currentLine.contains("Page")) {
				// Start New logic
				//Pattern pattern = Pattern.compile("\\w+([0-9]+)\\w+([0-9]+)");
				//Pattern pattern = Pattern.compile("\\w+([0-9]*[-]*[0-9]+)\\w+([0-9]+)");
                 Pattern pattern = Pattern.compile("\\w+([0-9]*[-]*[0-9]+)\\w+([0-9]*[-]*[0-9]+)");
				Matcher matcher = pattern.matcher(currentLine);
				String[] tokens = new String[10];
				for (int i = 0; i < matcher.groupCount(); i++) {
					matcher.find();
					tokens[i] = matcher.group();
				}
				// end new logic
				String accNumPadded = formatField(13, tokens[0]);
				String formatProfileId = formatField(10, tokens[1]);
				output.append(accNumPadded);
				output.append(formattedReviewInputDate);
				indexRecord.append(getPipe()+accNumPadded.trim());
				indexRecord.append(getPipe()+formattedReviewInputDate.trim());
				output.append(sequenceNum);
				output.append(subType);
				output.append(formatProfileId);
				indexRecord.append(getPipe()+formatProfileId.trim());
                
			}
			// Prov name
			if (currentLine.contains("PROV NAME")) {
				String provName = splitStringBasedOnDelimiter(currentLine);
				String paddedprovName = formatField(50, provName);
				output.append(paddedprovName);
			}

			// review due date
			if (currentLine.contains("REVIEW DUE")) {
				String reviewDueDate = splitStringBasedOnDelimiter(currentLine);
				String formattedReviewDueDate = null;
				if (reviewDueDate.length() != 0) {
					formattedReviewDueDate = convertdate(reviewDueDate);
				} else {
					formattedReviewDueDate = formatField(8, reviewDueDate);
				}
				output.append(formattedReviewDueDate);
			}

			// provider num
			if (currentLine.contains("PROVIDER NUM")) {
				String provNum = splitStringBasedOnDelimiter(currentLine);
			/*	String FormattedProvNum = null;
				if(provNum.isEmpty())
				{
					FormattedProvNum = formatField(10, "0000000000");
				}
				else
				{
					FormattedProvNum = formatField(10, provNum);
				}*/
				String FormattedProvNum = formatField(10, provNum);
				output.append(FormattedProvNum);
				//indexRecord.append(getPipe()+FormattedProvNum.trim());
			}

			// owner num
			if (currentLine.contains("OWN NUM")) {
				String ownerNum = splitStringBasedOnDelimiter(currentLine);
				String FormattedOwnerNum = null;
				FormattedOwnerNum = formatField(2, ownerNum);
				output.append(FormattedOwnerNum);
				indexRecord1.append(getPipe()+FormattedOwnerNum.trim());
			}
			// local num
			if (currentLine.contains("LOC NUM")) {
				String localNum = splitStringBasedOnDelimiter(currentLine);
				String FormattedLocalNum = null;
				FormattedLocalNum = formatField(3, localNum);
				output.append(FormattedLocalNum);
				indexRecord1.append(getPipe()+FormattedLocalNum.trim());
			}

			// prov type
			if (currentLine.contains("PROV TYPE")) {
				String provType = splitStringBasedOnDelimiter(currentLine);
				String FormattedProvType = null;
				  FormattedProvType = formatField(3, provType);
				output.append(FormattedProvType);
               indexRecord1.append(getPipe()+FormattedProvType.trim());
			}

			if (currentLine.contains("PSS039")) {
				section = "PSS039";
			}
			// prov num
			if (currentLine.contains("NMP PROVIDER")) {
				nmpProviderLine = currentLineNum + 5;
			}

			if (currentLineNum == nmpProviderLine && section.contains("PSS039")) {
				String provNum = currentLine.trim();
				String formattedProvType = formatField(10, provNum);
				output.append(formattedProvType);
				indexRecord.append(getPipe()+formattedProvType.trim());
				indexRecord.append(indexRecord1);
				
			}

			// lic
			if (currentLine.contains("LICENSE")) {
				licenseNum = currentLineNum + 5;

			}

			if (currentLineNum == licenseNum && section.contains("PSS039")) {
				String lic = currentLine.trim();
				String formattedLic = formatField(25, lic);
				output.append(formattedLic);
			}

			// type
			if (currentLine.contains("TYPE") && section.contains("PSS039")) {
				licType = currentLineNum + 5;

			}

			if (currentLineNum == licType && section.contains("PSS039")) {
				String type = currentLine.trim();
				String formattedType = formatField(1, type);
				output.append(formattedType);
				indexRecord.append(getPipe()+formattedType.trim()+getPipe());
			}

			// EFT DT
			if (currentLine.contains("EFF DT") && section.contains("PSS039")) {
				effDate = currentLineNum + 5;
			}
			if (currentLineNum == effDate && section.contains("PSS039")) {
				String eftDt = currentLine.trim();
				String formattedEftDt = null;
				if (eftDt.length() != 0) {
					formattedEftDt = convertdate(eftDt);
				} else {
					formattedEftDt = formatField(8, "");
				}
				output.append(formattedEftDt);
			}

			if (currentLine.contains("END DT") && section.contains("PSS039")) {
				endDate = currentLineNum + 5;
			}
			// END DT
			if (currentLineNum == endDate && section.contains("PSS039")) {
				String endDt = currentLine.trim();
				String formattedEndDt = null;
				if (endDt.length() != 0) {
					formattedEndDt = convertdate(endDt);
				} else {
					formattedEndDt = formatField(8, "");
				}
				output.append(formattedEndDt);
			}

			// legal name
			if (currentLine.contains("PSS042")) {
				section = "PSS042";
			}
			
			if (currentLine.contains("LEGAL NAME")) {
				String legalname = splitStringBasedOnDelimiter(currentLine);
				String FormattedLegalname = formatField(50, legalname);
				output.append(FormattedLegalname);
				nameSection="title";
				continue;
			}
			
			// prefix
			if (currentLine.contains("TITLE") || currentLine.contains("PREFIX")) {
				String prefix = currentLine.substring(6).trim();
				String formattedPrefix = formatField(4, prefix);
				output.append(formattedPrefix);
				nameSection="";
				continue;
			}else if(nameSection.contains("title")) {
				String formattedPrefix = formatField(4, "");
				output.append(formattedPrefix);
				nameSection="lastname";
			}

			// last name
			if (currentLine.contains("LAST NAME")) {
				String lastName = splitStringBasedOnDelimiter(currentLine);
				String formattedLastName = formatField(25, lastName);
				output.append(formattedLastName);
				continue;
			}else if(nameSection.contains("lastname")) {
				String formattedLastName = formatField(25, "");
				output.append(formattedLastName);
				nameSection ="firstname";
			}

			// first name
			if (currentLine.contains("FIRST NAME")) {
				String firstName = splitStringBasedOnDelimiter(currentLine);
				String formattedFirstName = formatField(15, firstName);
				output.append(formattedFirstName);
				continue;
			}else if(nameSection.contains("firstname")) {
				String formattedFirstName = formatField(15, "");
				output.append(formattedFirstName);
				 nameSection ="middle";
			}
			// middle
			if (currentLine.contains("MIDDLE")) {
				String middleName = splitStringBasedOnDelimiter(currentLine);
				String formattedMiddleName = formatField(15, middleName);
				output.append(formattedMiddleName);
				continue;
			}else if(nameSection.contains("middle")) {
				String formattedMiddleName = formatField(15, "");
				output.append(formattedMiddleName);
				nameSection="suffix";
			}

			// suffix
			if (currentLine.contains("SUFFIX")) {
				String suffix = splitStringBasedOnDelimiter(currentLine);
				String formattedSuffix = formatField(4, suffix);
				output.append(formattedSuffix);
				//System.out.println("length after suffix::"+output.length());
				continue;
			}else if(nameSection.contains("suffix")) {
				String formattedSuffix = formatField(4, "");
				output.append(formattedSuffix);
				nameSection="";
			}
			
			// state code == enrollment status
			if (currentLine.contains("STATUS") && section.contains("PSS042")) {
				String stateCode = splitStringBasedOnDelimiter(currentLine);
				String FormattedStateCode=null;
				 FormattedStateCode = formatField(1, stateCode);
				nmpDetailsMap.put(1, FormattedStateCode);
			}

			// eff dt
			if (currentLine.contains("EFF DT") && section.contains("PSS042")) {
				String effDt = splitStringBasedOnDelimiter(currentLine);
				String formattedEffDt = null;
				if (effDt.length() != 0) {
					formattedEffDt = convertdate(effDt);
				} else {
					formattedEffDt = formatField(8, "");
				}
				setEndDate = true;
				nmpDetailsMap.put(2, formattedEffDt);
				continue;
			}

			// end dt
			if (currentLine.contains("END DT") && section.contains("PSS042")) {
				String endDt = splitStringBasedOnDelimiter(currentLine);
				String formattedEndDt = null;
				if (endDt.length() != 0) {
					formattedEndDt = convertdate(endDt);
				} else {
					formattedEndDt = formatField(8, "");
				}
				setEndDate = false;
				nmpDetailsMap.put(3, formattedEndDt);
			} else if (setEndDate) {
				String formattedEndDt = formatField(8, "");
				setEndDate = false;
				nmpDetailsMap.put(3, formattedEndDt);
			}

			// ssn
			if (currentLine.contains("SOCIAL SECURITY")) {
				String ssn = splitStringBasedOnDelimiter(currentLine);
				String FormattedSsn = null;
				FormattedSsn = formatField(9, ssn);
				nmpDetailsMap.put(4, FormattedSsn);
			}
			// prov Enr
			if (currentLine.contains("PROV ENR")) {
				String provEnr = splitStringBasedOnDelimiter(currentLine);
				String formattedProvEnr = formatField(2, provEnr);
				section = "PROV ENR";
				nmpDetailsMap.put(5, formattedProvEnr);
			}
			// Prov Enr date || EFF date
			if (currentLine.contains("EFF DT") && section.contains("PROV ENR")) {
				String provEnrdate = splitStringBasedOnDelimiter(currentLine);
				String formattedProvEnrdate = null;
				if (provEnrdate.length() != 0) {
					formattedProvEnrdate = convertdate(provEnrdate);
				} else {
					formattedProvEnrdate = formatField(8, "");
				}
				nmpDetailsMap.put(6, formattedProvEnrdate);
			}

			// RE ENR IND
			if (currentLine.contains("ENR IND")) {
				String reEnrInd = splitStringBasedOnDelimiter(currentLine);
				String formattedReEnrInd = formatField(1, reEnrInd);
				section = "ENR IND";
				nmpDetailsMap.put(7, formattedReEnrInd);
			}

			// RE ENR IND date || EFT date

			if (currentLine.contains("EFF DT") && section.contains("ENR IND")) {
				String reEnrIndDate = splitStringBasedOnDelimiter(currentLine);
				String formattedReEnrIndDate = null;
				if (reEnrIndDate.length() != 0) {
					formattedReEnrIndDate = convertdate(reEnrIndDate);
				} else {
					formattedReEnrIndDate = formatField(8, "");
				}
				section = "PSS042";
				nmpDetailsMap.put(8, formattedReEnrIndDate);
			}

			// service address
			if (currentLine.contains("ATN")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
					String formattedString = readAndFormatAddressFiled(24, currentLine);
					addressMap.put(1, formattedString);
				}else {
					addressMap.put(1, formatField(24,""));
					addressMap.put(7, formatField(24,""));
				}
				if(!currentLine.contains("LN 1")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
					String formattedString = readAndFormatAddressFiled(24, currentLine);
					addressMap.put(7, formattedString);
				}else {
					addressMap.put(7, formatField(24,""));
				}
				}
			}
			
			if (currentLine.contains("LN 1")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(2, formattedString);
				}else {
					addressMap.put(2, formatField(64,""));
					addressMap.put(8, formatField(64,""));
				}
				if (!currentLine.contains("LN 2")) {	
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(8, formattedString);
				}else {
					addressMap.put(8, formatField(64,""));
				}
				}
			}
			
			if (currentLine.contains("LN 2")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(3, formattedString);
				}else {
					addressMap.put(3, formatField(64,""));
					addressMap.put(9, formatField(64,""));
				}
				if(!currentLine.contains("CITY:")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(9, formattedString);
				}else {
					addressMap.put(9, formatField(64,""));
				}
				}
			}
			
			//
            if (currentLine.contains("CITY:")) {
            	if(((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
					String formattedString = readAndFormatAddressFiled(30, currentLine);
					addressMap.put(4, formattedString);
				}else {
					addressMap.put(4, formatField(30,""));
					addressMap.put(10, formatField(30,""));
				}
				if(!currentLine.contains("STATE:")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
					String formattedString = readAndFormatAddressFiled(30, currentLine);
					addressMap.put(10, formattedString);
				}else {
					addressMap.put(10, formatField(30,""));
				}
				}
			}
			
            if (currentLine.contains("STATE:")) {
            	if(((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
					String formattedString = readAndFormatAddressFiled(2, currentLine);
					addressMap.put(5, formattedString);
				}else {
					addressMap.put(5, formatField(2,""));
					addressMap.put(11, formatField(2,""));
				}
				
            	if(!currentLine.contains("ZIP")) {
				if(((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
					String formattedString = readAndFormatAddressFiled(2, currentLine);
					addressMap.put(11, formattedString);
				}else {
					addressMap.put(11, formatField(2,""));
				}
            	}
			}
			
            if (currentLine.contains("ZIP")) {
            	if(((currentLine = br.readLine()) != null)) {
            		String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
            		String formattedString= null;
					 formattedString = readAndFormatZipCode(9, zipcode);
					 addressMap.put(6, formattedString);
				}else {
					addressMap.put(6, formatField(9,""));
				}
				
				if(((currentLine = br.readLine()) != null)) {
					String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
					String formattedString= null;
					 formattedString = readAndFormatZipCode(9, zipcode);
					 addressMap.put(12, formattedString);
				}else {
					addressMap.put(12, formatField(9,""));
				}	
				
				break;
			}
			//
			currentLineNum++;

		}
		appendMap(output, nmpDetailsMap);
		appendMap(output, addressMap);

		System.out.println("NMP Ouput Length:::" + output.length()+" # "+fileName);
		String SysDate = getSystemDate("MMddyyyy");
		String nmpOutputFileName = "Output_NMP_" + SysDate + ".txt";
		writeOutputToFile(outputpath, output.toString().toUpperCase(), nmpOutputFileName);
		String indexFileName = "Output_IND_" + SysDate + ".txt";
		writeOutputToFile(outputpath, indexRecord.toString(), indexFileName);
		return output;
	}

	public String formatField(int totalLength, String str) {
		String paddedString = String.format("%-" + totalLength + "s", str);
		paddedString = paddedString.substring(0, totalLength);
		return paddedString;
	}
	// function: if the zip contains 5 digits then append 0000 
	public String formatZipCode(int totalLength, String str) {
		if(str.isEmpty()) {
			String paddedString = String.format("%-" + totalLength + "s", str);
			paddedString = paddedString.substring(0, totalLength);
			return paddedString;
		}else {
		//	String paddedString=String.format("%-9s", "78899" ).replace(' ', '0');
			if(str.length()==5 && str.substring(0, 5).matches("\\d+")) {
		//	boolean numericZip = str.substring(0, 5).matches("\\d+");
		//	if (numericZip) {
				String paddedString = String.format("%-9s", str).replace(' ', '0');
				paddedString = paddedString.substring(0, totalLength);
				return paddedString;
			} else {
				String paddedString = String.format("%-" + totalLength + "s", str);
				paddedString = paddedString.substring(0, totalLength);
				return paddedString;
			}
		}
	}
	

	private String convertDate(String date) {
		String dateString = date;
		String formattedDateString = null;
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		Date startDate;
		try {
			startDate = df.parse(dateString);
			DateFormat df1 = new SimpleDateFormat("yyyyMMdd");
			formattedDateString = df1.format(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return formattedDateString;
	}
	
	// Method used for to convert the mmddyyy to yyyymmdd added after new requirement 
		private static String convertdate(String ldate) {
			ldate.trim();
			String finaldate ="";
			ldate= ldate.replaceAll("/", "");
			if(ldate.length()==8)
			{
			//System.out.println(ldate);
			String lmonth = ldate.substring(0, 2);
			String lday = ldate.substring(2, 4);
			String lyear = ldate.substring(4, 8);
			finaldate = lyear + "" + lmonth + "" + lday;
			}
			else{
				
				finaldate = "        ";
				
			}
			// Return the string
			return finaldate;
		}

	private String readAndFormatAddressFiled(int maxlegth, String line) {
		String text = line.trim();
		String formattedString = formatField(maxlegth, text);
		return formattedString;
	}

	private String readAndFormatZipCode(int maxlegth, String line) {
		String text = line.trim();
		String formattedString = formatZipCode(maxlegth, text);
		return formattedString;
	}
	
	private StringBuffer appendMap(StringBuffer addressString, Map<Integer, String> addressMap) {
		Set<Entry<Integer, String>> s = addressMap.entrySet();
		Iterator<Entry<Integer, String>> it = s.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer,String> entry = (Map.Entry<Integer,String>) it.next();
			String value = (String) entry.getValue();
			addressString.append(value);
		}
		return addressString;

	}

	protected String convertInputDate(String date) {
		String dateString = date;
		String formattedDateString = null;
		DateFormat df = new SimpleDateFormat("MMddyyyy");
		Date startDate;
		try {
			startDate = df.parse(dateString);
			DateFormat df1 = new SimpleDateFormat("yyyyMMdd");
			formattedDateString = df1.format(startDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return formattedDateString;
	}

	private String splitStringBasedOnDelimiter(String currentLine) {
		String[] tokens = currentLine.split(":");
		String data;
		if (tokens.length > 1) {
			data = tokens[1].trim();
		} else {
			data = "";
		}

		return data;
	}

	protected void writeOutputToFile(String path, String output, String fileName) throws IOException {
		String ouputFileName = path + fileName;
		Writer outputWriter = new BufferedWriter(new FileWriter(ouputFileName, true));
		outputWriter.write(output);
		outputWriter.write("\r\n");
		outputWriter.close();
	}

	protected String getSystemDate(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		String SysDate = dateFormat.format(date);
		return SysDate;
	}
	
	
	
	private String getPipe() {
		return "|";
	}
	
}

