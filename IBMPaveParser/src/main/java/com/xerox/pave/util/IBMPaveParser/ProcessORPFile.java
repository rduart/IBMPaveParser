package com.xerox.pave.util.IBMPaveParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;


/**
 * Description @  SDN 16008B – Process the ORP rtf file in to MainFram format (order should be as per copy book)
 * @version 	PAVE V1 24 Apr 2017
 * @author 	Prakash Patil , Anusha Maddali
 */

public class ProcessORPFile {

	public void processORPFile(String path, String fileName, String outputpath) throws Exception {
		BufferedReader in;
		try {
			String inputTextFileName = path + fileName;
			
			String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
			
			//System.out.println("fileNameWithOutExt::"+fileNameWithOutExt);
			
			String lIndexORPfilename = "PSF071";
			in = new BufferedReader(new FileReader(inputTextFileName));
			String ltempProfileID = "";
			String ltempprovname = "";
			String lacc = "";
			String ltempinputdocdt = "";
			String lsequenceNumber = "0001";
			String lsubType = "1";
			String ltemReviewDueDate = "";
			String ltempActReq = "";
			String ltempDocType = "";
			String str = ""; //in.readLine();
			String lprovtype = "PROV TYPE:", ltempprovtype = "";
			String lORPfiletype = "PAVE ORP INPUT DOCUMENT", sfiletype = "";
			// Field length variables ORP
			String lProvNum = "PROVIDER NUM:", ltempProvNum = "";
			String legalName = "LEGAL NAME:", ltempblegalName = "";
			String lprefix = "TITLE:", ltempprefix = "";
			String llastname = "LAST NAME:", ltemplastname = "";
			String lfirstname = "FIRST NAME:", ltempfirstname = "";
			String lmiddlename = "MIDDLE:", ltempmiddlename = "";
			String lsuffix = "SUFFIX:", ltempsuffix = "";
			String ltempssn = "";
			String lstatcode = "ENROLLMENT STAT. CODE:", loldstatcode = "STAT. CODE:", ltempstatecode = "";
			String lstateeffdate = "STAT. EFF DATE:", ltempstateeffdate = "";
			String lLICno = "LIC. NO:", ltempLICno = "";
			int lLICnolen = 25;
			String lLICeffdate = "LIC EFF DT:", ltempLICeffdate = "";
			String lprovenr = "PROV ENR:", ltempprovenr = "";
			String lprovenrdt = "PROV ENR DT:", ltempprovenrdt = "";
			String lreenrind = "RE ENR IND:", ltempreenrind = "";
			String lreenrinddt = "RE ENR IND DT:", ltempreenrinddt = "";
			
			String Indexfiletype = "ORP";

			Map<Integer, String> addressMap = new HashMap<Integer, String>();
//			StringBuffer output = new StringBuffer();
			StringBuffer finalOutput = new StringBuffer();
			StringBuffer address = new StringBuffer();

			// Tracking variables
			int lOwnnumlen = 2;
			int lLocNumlen = 3;
			String tr_LocNum = spaces(lLocNumlen);
			String tr_Ownnum = spaces(lOwnnumlen);
			String lnmp_type = spaces(1);
			boolean addressSection =false;

			//boolean bfiletype = str.contains(lORPfiletype);
			//if (bfiletype == true) {
			//	sfiletype = "O1";
				int lineNumber = 0;
				while ((str = in.readLine()) != null) {
					if (str.contains("PAVE ORP")) {
						sfiletype = "O1";
					}
					
					// extract account number and profile Id
					if (str.contains("Page")) {
						// Start New logic
						//Pattern pattern = Pattern.compile("\\w+([0-9]+)\\w+([0-9]+)");
						Pattern pattern = Pattern.compile("\\w+([0-9]*[-]*[0-9]+)\\w+([0-9]*[-]*[0-9]+)"); 
						Matcher matcher = pattern.matcher(str);
						String[] tokens = new String[10];
						for (int i = 0; i < matcher.groupCount(); i++) {
							matcher.find();
							tokens[i] = matcher.group();
						}
						// end new logic
						lacc = formatField(13, tokens[0]);
						ltempProfileID = formatField(10, tokens[1]);
					}

					// 10 OID-INPUT-RECEIVE-DATE PIC 9(08).
					if (fileName != null) {
						String[] tokens = fileName.split("_");
						String inputdocdt = tokens[2].substring(0, 8);
						//ltempinputdocdt = convertdate(inputdocdt);
						ltempinputdocdt = convertInputDate(inputdocdt);
					}
					
					// PROV-NAME - 50
					boolean blprovname = str.contains("PROV NAME FOR SCAN");
					if (blprovname == true) {
						String provname = splitStringBasedOnDelimiter(str);
						ltempprovname = formatField(50, provname);

					}
					// REVIEW-DUE-DATE - 08
					boolean bReviewDueDate = str.contains("REVIEW DUE DATE");
					if (bReviewDueDate == true) {
						String reviewDt = splitStringBasedOnDelimiter(str);
						ltemReviewDueDate = null;
						if (reviewDt.length() != 0) {
							ltemReviewDueDate = convertdate(reviewDt);
						} else {
							ltemReviewDueDate = formatField(8, "");
						}
					}
					
					// ACTION-REQUESTED - 01
					boolean blActReq = str.contains("ACTION REQUESTED");
					if (blActReq == true) {
						String lActReq = splitStringBasedOnDelimiter(str);
						ltempActReq = formatField(1, lActReq);
					}
					
					// DOC-TYPE - 01
					boolean blDocType = str.contains("DOC TYPE");
					if (blDocType == true) {
						String lDocType = splitStringBasedOnDelimiter(str);
						ltempDocType = formatField(1, lDocType);
					}
					// PROVIDER-NUM - 10
					boolean blProvNum = str.contains(lProvNum);
					if (blProvNum == true) {
						String provNum = splitStringBasedOnDelimiter(str);
						ltempProvNum = formatField(10, provNum);
						/*
						if(provNum.isEmpty())
						{
							ltempProvNum = formatField(10, "0000000000");
						}
						else
						{
							ltempProvNum = formatField(10, provNum);
						}
						*/
					}
					
					// LEGAL-NAME 50
					boolean blegalName = str.contains(legalName);
					if (blegalName == true) {
						String legalname = splitStringBasedOnDelimiter(str);
						ltempblegalName = formatField(50, legalname);
						/*
						int legalNamelen = 50;
						String legalname = str.substring(legalName.length(), str.length());
						ltempblegalName = legalname.trim();

						if (legalName.length() == str.trim().length()) {
							ltempblegalName = formatField(50, "");
						} else if (ltempblegalName.length() < legalNamelen) {
							ltempblegalName = formatField(50, ltempblegalName);
						}
						System.out.println("ltempblegalName::"+ltempblegalName.length());
						*/
					}
					
					// PREFIX - 04
					boolean bprefix = str.contains(lprefix);
					if (bprefix == true) {
						String lPrefix = splitStringBasedOnDelimiter(str);
						ltempprefix = formatField(4, lPrefix);
					} else {
						// this is for old ORP rtf files
						ltempprefix = formatField(4, "");
					}
					
					// LAST-NAME -25
					boolean blastname = str.contains(llastname);
					if (blastname == true) {
						String lastName = splitStringBasedOnDelimiter(str);
						ltemplastname = formatField(25, lastName);
					}else {
						// this is for old ORP rtf files
						ltemplastname = formatField(25, "");
					}
					
					// FIRST-NAME 15
					boolean bfirstname = str.contains(lfirstname);
					if (bfirstname == true) {
						String firstName = splitStringBasedOnDelimiter(str);
						ltempfirstname = formatField(15, firstName);
					}
					else {
						// this is for old ORP rtf files
						ltempfirstname = formatField(15, "");
					}
						
					// MIDDLE 15
					boolean bmiddlename = str.contains(lmiddlename);
					if (bmiddlename == true) {
						String middlename = splitStringBasedOnDelimiter(str);
						ltempmiddlename = formatField(15, middlename);
					}
					else {
						// this is for old ORP rtf files
						ltempmiddlename = formatField(15, "");
					}
					
					// SUFFIX - 04
					boolean bsuffix = str.contains(lsuffix);
					if (bsuffix == true) {
						String suffix = splitStringBasedOnDelimiter(str);
						ltempsuffix = formatField(4, suffix);
					} else {
						// this is for old ORP rtf files
						ltempsuffix = formatField(4, "");
					}
					
					// SSN 09
					if (str.contains("SOCIAL SECURITY")) {
						String ssn = splitStringBasedOnDelimiter(str);
						ltempssn = formatField(9, ssn);
						/*
						if(ssn.isEmpty())
						{
							ltempssn = formatField(9, "000000000");
						}
						else
						{
						ltempssn = formatField(9, ssn);
						}
						*/
					}
					
					// For OLD ORP format
					String ltempp = "PSS055 PROVIDER DETAIL SCREEN";
					boolean provtypeold = str.contains(ltempp);
					if (provtypeold == true) {
						// prov type
							String provType = splitStringBasedOnDelimiter(str);
							//ltempprovtype = formatField(3, provType);
							
							if(provType.isEmpty())
							{
								ltempprovtype = formatField(3, "000");
							}
							else
							{
							ltempprovtype = formatField(3, provType);
							}
							
					}
					if (provtypeold == false) {
						// PROV-TYPE 03
						boolean bprovtype = str.contains(lprovtype);
						if (bprovtype == true) {
							String oldprovType = splitStringBasedOnDelimiter(str);
							//ltempprovtype = formatField(3, oldprovType);
							
							if(oldprovType.isEmpty())
							{
								ltempprovtype = formatField(3, "000");
							}
							else
							{
							ltempprovtype = formatField(3, oldprovType);
							}
							
						}
					}
					
					
					// STAT-CODE - 01
					boolean bstatcode = str.contains(lstatcode);
					boolean boldstatcode = str.contains(loldstatcode);
					if (bstatcode == true || boldstatcode == true) {
						
						String lstateCode = splitStringBasedOnDelimiter(str);
						ltempstatecode = formatField(1, lstateCode);
						/*
						if(lstateCode.isEmpty())
						{
							ltempstatecode = formatField(1, "0");
						}
						else
						{
						ltempstatecode = formatField(1, lstateCode);
						}
						*/
					}
					
					// STAT-EFF-DATE 08
					boolean bstateeffdate = str.contains(lstateeffdate);
					if (bstateeffdate == true) {
						String effDt = splitStringBasedOnDelimiter(str);
						ltempstateeffdate = null;
						if (effDt.length() != 0) {
							ltempstateeffdate = convertdate(effDt);
						} else {
							ltempstateeffdate = formatField(8, "");
						}
						
					}
					// LIC-NO - 25
					boolean blLICno = str.contains(lLICno);
					if (blLICno == true) {
						String licNo = splitStringBasedOnDelimiter(str);
						ltempLICno = formatField(lLICnolen, licNo);
					}
					
					// LIC-EFF-DT
					boolean bLICeffdate = str.contains(lLICeffdate);
					if (bLICeffdate == true) {
						String LICeffdate = splitStringBasedOnDelimiter(str);
						ltempLICeffdate = null;
						if (LICeffdate.length() != 0) {
							ltempLICeffdate = convertdate(LICeffdate);
						} else {
							ltempLICeffdate = formatField(8, "");
						}
					}
					
					// PROV-ENR 02
					boolean bprovenr = str.contains(lprovenr);
					if (bprovenr == true) {
						String provenr = splitStringBasedOnDelimiter(str);
						ltempprovenr = formatField(2, provenr);
					}

					// PROV-ENR-DATE
					boolean bprovenrdt = str.contains(lprovenrdt);
					if (bprovenrdt == true) {
						String provenrdt = splitStringBasedOnDelimiter(str);
						//System.out.println("provenrdt::"+provenrdt);
						ltempprovenrdt = null;
						if (provenrdt.length() != 0) {
							ltempprovenrdt = convertdate(provenrdt);
						} else {
							ltempprovenrdt = formatField(8, "");
						}
					}
					
					boolean breenrind = str.contains(lreenrind);
					if (breenrind == true) {
						ltempreenrind = splitStringBasedOnDelimiter(str);
						ltempreenrind = formatField(1, ltempreenrind);
						//System.out.println("ltempreenrind::"+ltempreenrind+ "lenght:"+ltempreenrind.length());
					}
					
					boolean breenrinddt = str.contains(lreenrinddt);
					if (breenrinddt == true) {
						String lrendt = str.trim();
						//System.out.print("lrendt>>>"+lrendt);
						String lrendtstr = splitStringBasedOnDelimiter(lrendt);
						
						//lrendtstr= lrendtstr.replaceAll("/", "");
						//System.out.println("lrendtstr:::"+lrendtstr);
						if (lrendtstr.length() != 0) {
							ltempreenrinddt = convertdate(lrendtstr);
							
						} else {
							ltempreenrinddt = formatField(8, "");
						}
					}

					// service address
					if (str.contains("ATN")) {
						addressSection=true;
						if (((str = in.readLine()) != null) && !str.contains("LN 1")) {
							String formattedString = readAndFormatAddressFiled(24, str);
							// addressMap.put(1, formattedString);
						} else {
							addressMap.put(1, formatField(24, ""));
							addressMap.put(7, formatField(24, ""));
						}
						if (!str.contains("LN 1")) {
							if (((str = in.readLine()) != null) && !str.contains("LN 1")) {
								String formattedString = readAndFormatAddressFiled(24, str);
								addressMap.put(1, formattedString);
							} else {
								addressMap.put(1, formatField(24, ""));
								addressMap.put(7, formatField(24, ""));
							}
						}
						if (!str.contains("LN 1")) {
							if (((str = in.readLine()) != null) && !str.contains("LN 1")) {
								String formattedString = readAndFormatAddressFiled(24, str);
								addressMap.put(7, formattedString);
							} else {
								addressMap.put(7, formatField(24, ""));
							}
						}
					}

					if (str.contains("LN 1")) {
						if (((str = in.readLine()) != null) && !str.contains("LN 2")) {
							String formattedString = readAndFormatAddressFiled(64, str);
							// addressMap.put(2, formattedString);
						} else {
							addressMap.put(2, formatField(64, ""));
							addressMap.put(8, formatField(64, ""));
						}
						if (!str.contains("LN 2")) {
							if (((str = in.readLine()) != null) && !str.contains("LN 2")) {
								String formattedString = readAndFormatAddressFiled(64, str);
								addressMap.put(2, formattedString);
							} else {
								addressMap.put(2, formatField(64, ""));
								addressMap.put(8, formatField(64, ""));
							}
						}

						if (!str.contains("LN 2")) {
							if (((str = in.readLine()) != null) && !str.contains("LN 2")) {
								String formattedString = readAndFormatAddressFiled(64, str);
								addressMap.put(8, formattedString);
							} else {
								addressMap.put(8, formatField(64, ""));
							}
						}
					}

					if (str.contains("LN 2")) {
						if (((str = in.readLine()) != null) && !str.contains("CITY:")) {
							String formattedString = readAndFormatAddressFiled(64, str);
							// addressMap.put(3, formattedString);
						} else {
							addressMap.put(3, formatField(64, ""));
							addressMap.put(9, formatField(64, ""));
						}
						if (!str.contains("CITY:")) {
							if (((str = in.readLine()) != null) && !str.contains("CITY:")) {
								String formattedString = readAndFormatAddressFiled(64, str);
								addressMap.put(3, formattedString);
							} else {
								addressMap.put(3, formatField(64, ""));
								addressMap.put(9, formatField(64, ""));
							}
						}
						if (!str.contains("CITY:")) {
							if (((str = in.readLine()) != null) && !str.contains("CITY:")) {
								String formattedString = readAndFormatAddressFiled(64, str);
								addressMap.put(9, formattedString);
							} else {
								addressMap.put(9, formatField(64, ""));
							}
						}
					}

					//
					if (str.contains("CITY:")) {
						if (((str = in.readLine()) != null) && !str.contains("STATE:")) {
							String formattedString = readAndFormatAddressFiled(30, str);
							//System.out.println("CITY!!!"+formattedString);
							// addressMap.put(4, formattedString);
						} else {
							addressMap.put(4, formatField(30, ""));
							addressMap.put(10, formatField(30, ""));
						}
						if (!str.contains("STATE:")) {
							if (((str = in.readLine()) != null) && !str.contains("STATE:")) {
								String formattedString = readAndFormatAddressFiled(30, str);
								//System.out.println("CITY 1::"+formattedString);
								addressMap.put(4, formattedString);
							} else {
								addressMap.put(4, formatField(30, ""));
								addressMap.put(10, formatField(30, ""));
							}
						}
						if (!str.contains("STATE:")) {
							if (((str = in.readLine()) != null) && !str.contains("STATE:")) {
								String formattedString = readAndFormatAddressFiled(30, str);
								//System.out.println("CITY 2::"+formattedString);
								addressMap.put(10, formattedString);
							} else {
								addressMap.put(10, formatField(30, ""));
							}
						}
					}

					if (str.contains("STATE:") & addressSection) {
						if (((str = in.readLine()) != null) && !str.contains("ZIP")) {
							String formattedString = readAndFormatAddressFiled(2, str);
							//System.out.println("STATE!!!"+formattedString);
							// addressMap.put(5, formattedString);
						} else {
							addressMap.put(5, formatField(2, ""));
							addressMap.put(11, formatField(2, ""));
						}

						if (!str.contains("ZIP")) {
							if (((str = in.readLine()) != null) && !str.contains("ZIP")) {
								//System.out.println("Line text:::"+str);
								String formattedString = readAndFormatAddressFiled(2, str);
							//	System.out.println("STATE 1: "+formattedString);
								addressMap.put(5, formattedString);
							} else {
								addressMap.put(5, formatField(2, ""));
								addressMap.put(11, formatField(2, ""));
							}
						}
						if (!str.contains("ZIP")) {
							if (((str = in.readLine()) != null) && !str.contains("ZIP")) {
								String formattedString = readAndFormatAddressFiled(2, str);
							//	System.out.println("STATE 2: "+formattedString);
								addressMap.put(11, formattedString);
							} else {
								addressMap.put(11, formatField(2, ""));
							}
						}
					}

					if (str.contains("ZIP")) {
						if (((str = in.readLine()) != null)) {
							String zipcode = str.replaceAll("[\\s\\-()]", "");
							String formattedString = readAndFormatAddressFiled(9, zipcode);
							// addressMap.put(6, formattedString);
						} else {
							addressMap.put(6, formatField(9, ""));
							addressMap.put(12, formatField(9, ""));
						}

						if (((str = in.readLine()) != null)) {
							String zipcode = str.replaceAll("[\\s\\-()]", "");
							
							String formattedString = readAndFormatZipCode(9, zipcode);
							addressMap.put(6, formattedString);
						} else {
							addressMap.put(6, formatField(9, ""));
							addressMap.put(12, formatField(9, ""));
						}
						
						
						if (((str = in.readLine()) != null)) {
							String zipcode = str.replaceAll("[\\s\\-()]", "");
							String formattedString = readAndFormatZipCode(9, zipcode);
							addressMap.put(12, formattedString);
						} else {
							addressMap.put(12, formatField(9, ""));
						}
						
						addressSection=false;
					}
					//
					lineNumber++;
				}
				address = appendAddress(addressMap);
				//System.out.println("address>>>"+address.length());

		//	}
			String lIndex = fileNameWithOutExt + "|" + Indexfiletype + "|" + lacc.trim() + "|" + ltempinputdocdt + "|"
					+ ltempProfileID.trim() + "|" + ltempProvNum.trim() + "|" + tr_Ownnum.trim() + "|" + tr_LocNum.trim() + "|" + ltempprovtype.trim()
					+ "|"+ "|";

			String FinalString = "" + sfiletype + "" + lacc + "" + ltempinputdocdt + "" + lsequenceNumber + ""
					+ lsubType + "" + ltempProfileID + "" + ltempprovname + "" + ltemReviewDueDate + "" + ltempActReq
					+ "" + ltempDocType + "" + ltempProvNum + "" + ltempblegalName + "" + ltempprefix + ""
					+ ltemplastname + "" + ltempfirstname + "" + ltempmiddlename + "" + ltempsuffix + "" + ltempssn + ""
					+ ltempprovtype + "" + ltempstatecode + "" + ltempstateeffdate + "" + ltempLICno + ""
					+ ltempLICeffdate + "" + "" + ltempprovenr + "" + ltempprovenrdt + "" + ltempreenrind + ""
					+ ltempreenrinddt;
			// FinalString = FinalString.toUpperCase();
			finalOutput.append(FinalString);
			finalOutput.append(address);
			//System.out.println("address::"+address);
			System.out.print("ORP Ouput Length>>>" + finalOutput.length() +" # "+ fileName +"\n");
			String lORPfilename = "Output_ORP_" + getSystemDate() + ".txt";
			String lIndexfilename = "Output_IND_" + getSystemDate() + ".txt";
			writeOutputToFile(outputpath, finalOutput.toString().toUpperCase(), lORPfilename);
			writeOutputToFile(outputpath, lIndex, lIndexfilename);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * comments writeOutputToFile method Use for to write the output file MF
	 * format
	 * 
	 * @param path,
	 *            output, fileName
	 */
	private void writeOutputToFile(String path, String output, String fileName) throws IOException {
		String ouputFileName = path + fileName;
		Writer outputWriter = new BufferedWriter(new FileWriter(ouputFileName, true));
		outputWriter.write(output);
		outputWriter.write("\r\n");
		outputWriter.close();
	}

	/**
	 * comments getSystemDate method Use for to get the system date
	 * 
	 */
	private String getSystemDate() {
		DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");
		Date date = new Date();
		String SysDate = dateFormat.format(date);
		return SysDate;
	}
	/**
	 * comments splitStringBasedOnDelimiter method Use for to Delimiter the string :
	 * 
	 */
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
	
	
	/**
	 * comments splitStringBasedOnDelimiter method Use for to Delimiter the string : for Legal Name
	 * 
	 */
	private String splitStringBasedOnDelimiterLegalName(String currentLine) {
		String[] tokens = currentLine.split(":");
		String data = "";
		StringBuffer ldata = new StringBuffer();
		//System.out.println("tokens.length::"+tokens.length);
		if (tokens.length > 1) {
			for (int i=1; i < tokens.length; i++)
			{
				System.out.println(i);
				data = tokens[i].trim();
				ldata.append(":"+data);
				
				
			}
		
			data = ldata.substring(1);
		}// else {
			//ldata = "";
		//}
		
		System.out.println("ldata::"+ldata.substring(1));
		return data;
	}

	// Method use for to add white space
	private static String spaces(int numberOfSpaces) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numberOfSpaces; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

	// Method used for to convert the mmddyyy to yyyymmdd
	private static String convertdate(String ldate) {
		ldate.trim();
		String finaldate ="";
		ldate= ldate.replaceAll("/", "");
		//System.out.println("ldate::"+ldate);
		if(ldate.length()==8)
		{
		//System.out.println(ldate);
		String lmonth = ldate.substring(0, 2);
		String lday = ldate.substring(2, 4);
		String lyear = ldate.substring(4, 8);
		finaldate = lyear + "" + lmonth + "" + lday;
		//System.out.println(finaldate);
		}
		else{
			
			finaldate = "12345678";
			
		}
		// Return the string
		return finaldate;
	}

	public String formatField(int totalLength, String str) {
		String paddedString = String.format("%-" + totalLength + "s", str);
		paddedString = paddedString.substring(0, totalLength);
		return paddedString;
	}

	private String readAndFormatAddressFiled(int maxlegth, String line) {
		String text = line.trim();
		String formattedString = formatField(maxlegth, text);
		return formattedString;
	}

	/*
	 * convertDate Method used for to convert the date MM/dd/yyyy to yyyyMMdd
	 */
	private String convertDate(String date) {
		String dateString = date;
		String formattedDateString = null;
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		//df.setLenient(false);
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

	private StringBuffer appendAddress(Map<Integer, String> addressMap) {
		StringBuffer addressString = new StringBuffer();
		Set<Entry<Integer, String>> s = addressMap.entrySet();
		Iterator<Entry<Integer, String>> it = s.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) it.next();
			String value = (String) entry.getValue();
			addressString.append(value);
		}
		return addressString;

	}
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
		
		private String readAndFormatZipCode(int maxlegth, String line) {
			String text = line.trim();
			String formattedString = formatZipCode(maxlegth, text);
			return formattedString;
		}
		

}

