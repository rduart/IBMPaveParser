package com.xerox.pave.util.IBMPaveParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;

public class ProcessBillingFile {
	
	public void processBilling(String path, String fileName, String outputpath) throws IOException {
		FileInputStream fis;
		InputStreamReader reader;
		BufferedReader br = null;
		// String fileName = fileName1;
		System.out.println("fileName:::::::::::::::::::::" + fileName);

		String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);

		String inputTextFileName = path + fileName;

		try {
			fis = new FileInputStream(new File(inputTextFileName));
			reader = new InputStreamReader(fis);
			br = new BufferedReader(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer output = new StringBuffer();
		BillingHeader headerBean = new BillingHeader();
		ProvderDeatilsBean providerDetailsBean = new ProvderDeatilsBean();

		String[] fileNametokens = fileName.split("_");
		String inputDateToken = fileNametokens[fileNametokens.length - 1];
		String reviewinputDate = inputDateToken.substring(0, 8);
		String formattedReviewInputDate = convertInputDate(reviewinputDate);

		String currentLine = null;
		String prevLine = null;
		String section = "";
		// String FormattedSsn = null;
		boolean sanctionOccurances = false;
		String FormattedLicNo = null;
		String formattedCliaNo = null;
		String formattedLicEffDate = null;
		String formattedDate = null;
		List<String> sanctionsList = new ArrayList<String>();
		String nameSection = "";
		final String sequenceNum = "0001";
		final String indexBilldocType = "Billing/Renderer";
		StringBuffer ownerSection = new StringBuffer();
		StringBuffer locationSection = new StringBuffer();
		StringBuffer providerDetailSection = new StringBuffer();
		String formattedAccNum = "";

		String lFormattedProvType = "";
		String FormattedProvType = null;

		// Index
		StringBuffer indexBillRecord = new StringBuffer();

		StringBuffer labSection = new StringBuffer();
		StringBuffer taxonomySection = new StringBuffer();
		ArrayList<StringBuffer> taxonomyList = new ArrayList<StringBuffer>();
		StringBuffer physicianCertificationStr = new StringBuffer();
		List<StringBuffer> nmpDetailsScreen = new ArrayList<StringBuffer>();
		StringBuffer nmpNonPhysician = null;
		ArrayList<StringBuffer> labSpecalityList = new ArrayList<StringBuffer>();
		
		StringBuffer nmpSNonPhysician = null;

		// Billing S-record
		StringBuffer srecord = new StringBuffer();
		StringBuffer tempSrecord = new StringBuffer();
		final String sRecordProvType = "035,040,041,042,043,044,045,046,047,048,049,052,053,058,075";
		int lownnum = 100;
		String sRecordSubType = "5";
		boolean bProvType = false;

		// RNDERING
		StringBuffer commanHeaderREN = new StringBuffer();
		ArrayList<StringBuffer> memberToGroupList = new ArrayList<StringBuffer>();
		ArrayList<StringBuffer> grpmemberList = new ArrayList<StringBuffer>();
		int grpcount = 0;
		int lRendcounter = 0;

		// MOCA
		StringBuffer commanHeaderMOCA = new StringBuffer();
		StringBuffer commonHeader = new StringBuffer();
		String SubType_Moca = "4";
		ArrayList<StringBuffer> mocaList = new ArrayList<StringBuffer>();
		ArrayList<StringBuffer> mocadetailList = new ArrayList<StringBuffer>();
		StringBuffer lMocaliststr = new StringBuffer();
		StringBuffer lMocadetails = new StringBuffer();
		StringBuffer lMOCAFinal = new StringBuffer();
		ArrayList<StringBuffer> lMocafinal = new ArrayList<StringBuffer>();

		while (((currentLine = br.readLine()) != null)) {

			// Start: BID HEADER SECTION
			if (currentLine.contains("PAVE INPUT")) {

				headerBean.setSubType("1");
				indexBillRecord.append(fileNameWithOutExt);
				indexBillRecord.append(getPipe() + indexBilldocType);
			}
			// Read account and profile id
			if (currentLine.contains("Page")) {
				String[] tokens = getAccountIdAndProfileId(currentLine);
				if (tokens.length > 0) {
					formattedAccNum = formatField(13, tokens[0]);
					headerBean.setAccountNum(formattedAccNum);
					// System.out.println(formattedAccNum);
					String formattedProfileId = formatField(10, tokens[1]);
					headerBean.setProfId(formattedProfileId);
					String fileDate = formatField(8, getDateFromFileName(fileName));
					// String fileDate= getDateFromFileName(fileName);
					headerBean.setFileDate(fileDate);
					headerBean.setSeqNum(sequenceNum);

					commonHeader.append(formattedProfileId);

					indexBillRecord.append(getPipe() + formattedAccNum.trim());
					indexBillRecord.append(getPipe() + fileDate.trim());
					indexBillRecord.append(getPipe() + formattedProfileId.trim());

					tempSrecord.append(formattedAccNum);
					tempSrecord.append(fileDate);
					tempSrecord.append(String.format("%04d", 0001));
					tempSrecord.append(sRecordSubType);  // subtype -5 for S-Record
					tempSrecord.append(formattedProfileId);
				}
			}

			// read prov name - 4 th field, length 50
			if (currentLine.contains("PROV NAME")) {
				String provName = splitStringBasedOnDelimiter(currentLine);
				String paddedprovName = formatField(50, provName);
				headerBean.setProvName(paddedprovName);
				tempSrecord.append(paddedprovName);
			}

			if (currentLine.contains("REVIEW DUE")) {
				String reviewDueDate = splitStringBasedOnDelimiter(currentLine);
				// System.out.println("reviewDueDate::"+reviewDueDate);
				String formattedReviewDueDate = null;
				if (reviewDueDate.length() != 0) {
					formattedReviewDueDate = convertBilldate(reviewDueDate);
				} else {
					formattedReviewDueDate = formatField(8, reviewDueDate);
				}
				// System.out.println("formattedReviewDueDate::::"+formattedReviewDueDate);
				headerBean.setReviewDuedate(formattedReviewDueDate);
				tempSrecord.append(formattedReviewDueDate);
			}

			// Action requested. need to check for not null
			if (currentLine.contains("ACTION REQUESTED")) {
				String actionRequested = splitStringBasedOnDelimiter(currentLine);
				String paddedActionRequested = formatField(1, actionRequested);
				headerBean.setActionRequested(paddedActionRequested);
			}

			// DOC TYPE

			if (currentLine.contains("DOC TYPE")) {
				String docType = splitStringBasedOnDelimiter(currentLine);
				String paddedDocType = formatField(1, "M"); // for BIL docType
															// is always M
				headerBean.setDocType(paddedDocType);
				commonHeader.append(paddedDocType);
			}

			// provider num
			if (currentLine.contains("PROVIDER NUM")) {
				String provNum = splitStringBasedOnDelimiter(currentLine);
				String FormattedProvNum = formatField(10, provNum);
				// System.out.println("FormattedProvNum::::::::::"+FormattedProvNum);
				headerBean.setProviderNumber(FormattedProvNum);

				commonHeader.append(FormattedProvNum);
				indexBillRecord.append(getPipe() + FormattedProvNum.trim());
				tempSrecord.append(FormattedProvNum);
			}

			// owner num
			if (currentLine.contains("OWN NUM")) {
				String ownerNum = splitStringBasedOnDelimiter(currentLine);
				String FormattedOwnerNum = null;
				FormattedOwnerNum = formatField(2, ownerNum);
				// System.out.println("FormattedOwnerNum::::::::::"+FormattedOwnerNum);
				headerBean.setOwnNum(FormattedOwnerNum);
				commonHeader.append(FormattedOwnerNum);
				indexBillRecord.append(getPipe() + FormattedOwnerNum.trim());

				// For S-Record OWN Number = 100 - current owner number
				if (FormattedOwnerNum != null && FormattedOwnerNum.trim().length() > 0) {
					int ltempOwnerNum = Integer.parseInt(FormattedOwnerNum);
					int lOwnerNum = lownnum - ltempOwnerNum;
					// System.out.println("S-Record lOwnerNum::"+lOwnerNum);
					tempSrecord.append(lOwnerNum);
				}
			}

			// local number
			if (currentLine.contains("LOC NUM")) {
				String localNum = splitStringBasedOnDelimiter(currentLine);
				String FormattedLocalNum = null;
				FormattedLocalNum = formatField(3, localNum);
				headerBean.setLocNum(FormattedLocalNum);
				commonHeader.append(FormattedLocalNum);
				indexBillRecord.append(getPipe() + FormattedLocalNum.trim());
				tempSrecord.append(FormattedLocalNum);
			}

			// prov type
			if (currentLine.contains("PROV TYPE") && prevLine.contains("LOC NUM")) {
				String provType = splitStringBasedOnDelimiter(currentLine);
				//String FormattedProvType = null;
				FormattedProvType = formatField(3, provType);
				headerBean.setProvType(FormattedProvType);
				// commonHeader.append(FormattedProvType);
				indexBillRecord.append(getPipe() + FormattedProvType.trim() + getPipe());
				indexBillRecord.append(getPipe());
				
				// For S-Record
				//System.out.println("FormattedProvType:::::::::::::::::::::::::::"+FormattedProvType);
				bProvType = sRecordProvType.matches("(.*)"+FormattedProvType+"(.*)"); 
				//System.out.println("bProvType::"+bProvType);
				  if(bProvType == true)
				  {
					 //StringBuffer nmpSRecordDetailsScreenStr = new StringBuffer(nmpNonPhysician);
					 //System.out.println(FormattedProvType);
					 srecord.append("B1"); 
					 srecord.append(tempSrecord);
					 srecord.append(FormattedProvType);
					// srecord.append(nmpSRecordDetailsScreenStr);
					 srecord.append(formatField(596, ""));
					 //System.out.println("srecord::"+srecord); 
					 //System.out.println("srecord lenght::"+srecord.length());
				 }
				  

				/*
				   //For To Create S-Record for specific PROV TYPE 
				   bProvType = sRecordProvType.matches("(.*)"+FormattedProvType+"(.*)"); 
				  if(bProvType == true)
				  {
				  //System.out.println(FormattedProvType);
				  srecord.append("B1"); 
				  srecord.append(tempSrecord);
				  nmpSNonPhysician = new StringBuffer();
				  nmpSNonPhysician = processNonPhysicianParams(br); 
				  System.out.println("S-Record ::"+nmpSNonPhysician);
				  srecord.append(nmpSNonPhysician);
				 System.out.println("srecord::"+srecord); 
				 System.out.println("srecord lenght::"+srecord.length());
				 }
				 */
				 
			}

			// End: BID HEADER SECTION

			// Start: OWNER SECTION

			if (currentLine.contains("PSS059")) {
				section = "OWNER";
			}

			if (currentLine.contains("LEGAL NAME") && section.contains("OWNER")) {
				String legalname = splitStringBasedOnDelimiter(currentLine);
				String FormattedLegalname = formatField(50, legalname);
				// System.out.println("OWNER
				// FormattedLegalname::"+FormattedLegalname);
				ownerSection.append(FormattedLegalname);
				nameSection = "title";
				continue;
			}

			// prefix
			if (currentLine.contains("TITLE") || currentLine.contains("PREFIX")) {
				String prefix = currentLine.substring(6).trim();
				String formattedPrefix = formatField(4, prefix);
				// System.out.println("OWNER
				// formattedPrefix::"+formattedPrefix);
				ownerSection.append(formattedPrefix);
				nameSection = "";
				continue;
			} else if (nameSection.contains("title")) {
				String formattedPrefix = formatField(4, "");
				ownerSection.append(formattedPrefix);
				nameSection = "lastname";
			}

			// last name
			if (currentLine.contains("LAST NAME")) {
				String lastName = splitStringBasedOnDelimiter(currentLine);
				String formattedLastName = formatField(25, lastName);
				// System.out.println("OWNER
				// formattedLastName::"+formattedLastName);
				ownerSection.append(formattedLastName);
				continue;
			} else if (nameSection.contains("lastname")) {
				String formattedLastName = formatField(25, "");
				ownerSection.append(formattedLastName);
				nameSection = "firstname";
			}

			// first name
			if (currentLine.contains("FIRST NAME")) {
				String firstName = splitStringBasedOnDelimiter(currentLine);
				String formattedFirstName = formatField(15, firstName);
				ownerSection.append(formattedFirstName);
				continue;
			} else if (nameSection.contains("firstname")) {
				String formattedFirstName = formatField(15, "");
				ownerSection.append(formattedFirstName);
				nameSection = "middle";
			}
			// middle
			if (currentLine.contains("MIDDLE")) {
				String middleName = splitStringBasedOnDelimiter(currentLine);
				String formattedMiddleName = formatField(15, middleName);
				ownerSection.append(formattedMiddleName);
				continue;
			} else if (nameSection.contains("middle")) {
				String formattedMiddleName = formatField(15, "");
				ownerSection.append(formattedMiddleName);
				nameSection = "suffix";
			}

			// suffix
			if (currentLine.contains("SUFFIX")) {
				String suffix = splitStringBasedOnDelimiter(currentLine);
				String formattedSuffix = formatField(4, suffix);
				ownerSection.append(formattedSuffix);
				continue;
			} else if (nameSection.contains("suffix")) {
				String formattedSuffix = formatField(4, "");
				ownerSection.append(formattedSuffix);
				nameSection = "";
			}

			if (currentLine.contains("BEGIN DATE") && section.contains("OWNER")) {
				String beginDate = splitStringBasedOnDelimiter(currentLine);
				String formattedBeginDate = null;
				if (beginDate.length() != 0) {
					formattedBeginDate = convertBilldate(beginDate);
				} else {
					formattedBeginDate = formatField(8, beginDate);
				}
				ownerSection.append(formattedBeginDate);
				// System.out.println("formattedBeginDate::"+formattedBeginDate);
			}

			if (currentLine.contains("END DATE") && section.contains("OWNER")) {
				String endDate = splitStringBasedOnDelimiter(currentLine);
				String formattedEndDate = null;
				if (endDate.length() != 0) {
					formattedEndDate = convertBilldate(endDate);
				} else {
					formattedEndDate = formatField(8, endDate);
				}
				// System.out.println("formattedEndDate::"+formattedEndDate);
				ownerSection.append(formattedEndDate);
			}

			if (currentLine.contains("WARRANT") && section.contains("OWNER")) {
				String warrant = splitStringBasedOnDelimiter(currentLine);
				String formattedFieldData = formatField(1, warrant);
				ownerSection.append(formattedFieldData);
				// System.out.println("WARRANT::"+formattedFieldData);
			}

			if (currentLine.contains("FED EMP ID NO/TIN:") && section.contains("OWNER")) {
				// System.out.println("FED EMP ID currentLine::"+currentLine);
				/*
				 * String[] emptokens = currentLine.split(":"); String data; if
				 * (emptokens.length > 1) { data = emptokens[1].trim(); } else {
				 * data = ""; }
				 */
				String empId = splitStringBasedOnDelimiter(currentLine);
				String formattedFieldData = formatField(9, empId);
				ownerSection.append(formattedFieldData);
				// System.out.println("FED EMP ID::" + formattedFieldData);
			}

			if (currentLine.contains("SOCIAL SECURITY NO:") && section.contains("OWNER")) {
				// System.out.println("SOCIAL::"+currentLine);
				String FormattedSsn = null;
				/*
				 * String[] ssntokens = currentLine.split(":"); String ssndata;
				 * if (ssntokens.length > 1) { ssndata = ssntokens[2].trim(); }
				 * else { ssndata = ""; }
				 */

				String ssn = splitStringBasedOnDelimiter(currentLine);
				FormattedSsn = formatField(9, ssn);
				// System.out.println("SSN::" + FormattedSsn);
				ownerSection.append(FormattedSsn);
			}

			if (currentLine.contains("TIN DATE") && section.contains("OWNER")) {

				String tinDate = splitStringBasedOnDelimiter(currentLine);
				String formattedTinDate = null;
				if (tinDate.length() != 0) {
					formattedTinDate = convertBilldate(tinDate);
				} else {
					formattedTinDate = formatField(8, tinDate);
				}
				// System.out.println("formattedTinDate::" + formattedTinDate);
				ownerSection.append(formattedTinDate);

			}

			if (currentLine.contains("IRS UPDATE") && section.contains("OWNER")) {

				String irsUpdate = splitStringBasedOnDelimiter(currentLine);
				String FormattedIrsUpdate = formatField(1, irsUpdate);
				ownerSection.append(FormattedIrsUpdate);
			}

			if (currentLine.contains("FACILITY") && section.contains("OWNER")) {
				String facility = splitStringBasedOnDelimiter(currentLine);
				// System.out.println(facility);
				String FormattedFacility = formatField(1, facility);
				ownerSection.append(FormattedFacility);
				// System.out.println("ownerSection::" +
				// ownerSection.toString());
				// System.out.println("ownerSection length::" +
				// ownerSection.length());
			}

			if (currentLine.contains("SANCTIONS") && section.contains("OWNER")) {
				sanctionOccurances = true;
			}

			if (sanctionOccurances) {
				int counter = 0;
				while (((currentLine = br.readLine()) != null) && !currentLine.contains("PSS070")) {
					sanctionsList.add(currentLine);
					counter++;
					if (counter == 8) { // because we have to read only 8 lines
						break;
					}
				}

				int sanctionOccurancesLength = sanctionsList.size();
				StringBuffer sanctionsString = new StringBuffer();

				for (String sanctions : sanctionsList) {
					sanctionsString.append(formatField(5, sanctions));
				}

				while (sanctionOccurancesLength < 8) {
					sanctionsString.append(formatField(5, ""));
					sanctionOccurancesLength++;
				}
				ownerSection.append(sanctionsString);
				// System.out.println("Sactions String::" + sanctionsString);
				System.out.println("Sanctions String length:" + sanctionsString.length());

				sanctionOccurances = false;
			}

			// END: OWNER SECTION

			// STRAT: LOCATION SCREEN

			if (currentLine.contains("PSS070")) {
				section = "LOCATION";
			}

			if (currentLine.contains("BUSINESS NAME") && section.contains("LOCATION")) {
				String businessName = splitStringBasedOnDelimiter(currentLine);
				String FormattedBusinessName = formatField(50, businessName);
				locationSection.append(FormattedBusinessName);
				continue;
			}

			// telephone number
			if (currentLine.contains("TELEPHONE") && section.contains("LOCATION")) {
				String telephoneNumber = splitStringBasedOnDelimiter(currentLine);
				telephoneNumber = telephoneNumber.replaceAll("[\\s\\-()]", "");
				String FormattedTelephoneNumber = formatField(10, telephoneNumber);
				locationSection.append(FormattedTelephoneNumber);
				continue;
			}

			if (currentLine.contains("MAIL TO ADDRESS") && section.contains("LOCATION")) {
				Map<Integer, String> locationAddressMap = processAddress(br);
				appendMap(locationSection, locationAddressMap);
				continue;
			}

			if (currentLine.contains("OUT OF STATE")) {
				String outOfState = splitStringBasedOnDelimiter(currentLine);
				String formattedoutOFState = formatField(1, outOfState);
				locationSection.append(formattedoutOFState);
				// System.out.println("length of location
				// screen::"+locationSection.length());
				continue;
			}

			if (currentLine.contains("PSS055")) {
				section = "PROVIDER";
				continue;
			}

			if (currentLine.contains("APP DATE") && section.contains("PROVIDER")) {
				String formattedAppDate = processDateWithDelimter(currentLine);
				// providerDetailSection.append(formattedAppDate);
				providerDetailsBean.setAppDate(formattedAppDate);
				continue;
			}

			if (currentLine.contains("PROV TYPE") && section.contains("PROVIDER")) {
				String provType = splitStringBasedOnDelimiter(currentLine);
				// System.out.println(provType);
				// String lFormattedProvType = formatField(3, provType);
				lFormattedProvType = formatField(3, provType);
				commonHeader.append(lFormattedProvType); // for MOCA, RENDERING
															// and S72
				// System.out.println("PSS055
				// FormattedProvType::::::::::::::::"+lFormattedProvType);
				providerDetailsBean.setProvType(lFormattedProvType);

				/*
				 * // For To Create S-Record for specific PROV TYPE bProvType =
				 * sRecordProvType.matches("(.*)"+FormattedProvType+"(.*)"); if
				 * (bProvType == true) {
				 * //System.out.println(FormattedProvType);
				 * srecord.append("B1"); srecord.append(tempSrecord);
				 * nmpNonPhysician = new StringBuffer(); nmpNonPhysician =
				 * processNonPhysicianParams(br); //System.out.println(
				 * "S-Record ::"+nmpNonPhysician);
				 * srecord.append(nmpNonPhysician);
				 * System.out.println("srecord::"+srecord);
				 * System.out.println("srecord::"+srecord.length()); }
				 */

				continue;

			}

			if (currentLine.contains("PRACTICE") && section.contains("PROVIDER")) {
				String practice = splitStringBasedOnDelimiter(currentLine);
				// System.out.println(practice);
				String FormattedPractice = formatField(2, practice);
				providerDetailsBean.setPractice(FormattedPractice);
				continue;
			}

			if (currentLine.contains("CODE") && section.contains("PROVIDER")) {
				String statCode = splitStringBasedOnDelimiter(currentLine);
				String FormattedStatCode = formatField(1, statCode);
				// System.out.println("enrollment status::" +
				// FormattedStatCode);
				providerDetailsBean.setStatCode(FormattedStatCode);
				if (FormattedStatCode.equals("7")) {
					headerBean.setType("B0");
				} else {
					headerBean.setType("B1");
				}
				continue;
			}

			if (currentLine.contains("STAT. EFF") && section.contains("PROVIDER")) {
				String formattedStatEffDate = processDateWithDelimter(currentLine);
				providerDetailsBean.setStatEffDate(formattedStatEffDate);
				continue;
			}

			if (currentLine.contains("REJT RSN") && section.contains("PROVIDER")) {
				String rejtRsn = splitStringBasedOnDelimiter(currentLine);
				String FormattedRejtRsn = formatField(2, rejtRsn);
				providerDetailsBean.setRejectReason(FormattedRejtRsn);
				section = "";
				continue;
			}

			if (currentLine.contains("CATEGORIES")) {
				section = "CATEGORIES";
				continue;
			}
			if (currentLine.contains("END DATE") && section.contains("CATEGORIES")) {

				ArrayList<StringBuffer> categoriesList = new ArrayList<StringBuffer>();
				while ((currentLine = br.readLine()) != null) {
					StringBuffer categoriesString = new StringBuffer();
					if (!currentLine.contains("CHDP")) {
						String formattedCategory = formatField(3, currentLine);
						categoriesString.append(formattedCategory);
					} else {
						break;
					}
					if ((currentLine = br.readLine()) != null && !currentLine.contains("CHDP")) {
						String formattedBeginDate = processDate(currentLine);
						categoriesString.append(formattedBeginDate);
					} else {
						categoriesString.append(formatField(16, ""));
						break;
					}
					if ((currentLine = br.readLine()) != null && !currentLine.contains("CHDP")) {
						String formattedEndDate = processDate(currentLine);
						categoriesString.append(formattedEndDate);
					} else {
						categoriesString.append(formatField(8, ""));
						break;
					}

					categoriesList.add(categoriesString);
				}
				StringBuffer categoryOccurances = processOccurancesList(categoriesList, 20, 19);
				// System.out.println("categoryOccurances:::" +
				// categoryOccurances.length());
				providerDetailsBean.setCategoryOfServicesString(categoryOccurances);
				System.out.println("Categorieslist length::" + categoriesList.size());
				section = "PROVIDER";

			}

			if (currentLine.contains("LIC. NO") && section.contains("PROVIDER")) {
				String licNo = splitStringBasedOnDelimiter(currentLine);
				FormattedLicNo = formatField(25, licNo);
				providerDetailsBean.setLicNo(FormattedLicNo);
				continue;
			}

			if (currentLine.contains("LIC EFF DT") && section.contains("PROVIDER")) {
				formattedLicEffDate = processDateWithDelimter(currentLine);
				providerDetailsBean.setLicEffDate(formattedLicEffDate);
				continue;
			}

			if (currentLine.contains("CLIA NO") && section.contains("PROVIDER")) {
				String cliaNo = splitStringBasedOnDelimiter(currentLine);
				formattedCliaNo = formatField(10, cliaNo);
				providerDetailsBean.setCliaNo(formattedCliaNo);
				continue;
			}

			if (currentLine.contains("CHDP PROV NO") && section.contains("PROVIDER")) {
				String chdpProvNo = splitStringBasedOnDelimiter(currentLine);
				String FormattedChdpProvNo = formatField(10, chdpProvNo);
				providerDetailsBean.setChdpProvNo(FormattedChdpProvNo);
				continue;
			}

			if (currentLine.contains("SPEC PROC") && section.contains("PROVIDER")) {
				String specProc = splitStringBasedOnDelimiter(currentLine);
				String FormattedSpecProc = formatField(1, specProc);
				providerDetailsBean.setSpecProcType(FormattedSpecProc);
				section = "ENR";
				continue;
			}

			if (currentLine.contains("PROV ENR") && section.contains("ENR")) {
				String provEnr = splitStringBasedOnDelimiter(currentLine);
				String formattedProvEnr = formatField(2, provEnr);
				providerDetailsBean.setProvEnr(formattedProvEnr);
				section = "DATE";
				continue;
			}

			if (currentLine.contains("PROV ENR DT") && section.contains("DATE")) {
				formattedDate = processDateWithDelimter(currentLine);
				providerDetailsBean.setProvEnrDate(formattedDate);
				section = "RE ENR";
				continue;
			}

			if (currentLine.contains("RE ENR") && section.contains("RE ENR")) {
				String reEnr = splitStringBasedOnDelimiter(currentLine);
				String formattedReEnr = formatField(1, reEnr);
				providerDetailsBean.setReEnrInd(formattedReEnr);
				section = "DATE";
				continue;
			}

			if (currentLine.contains("IND DT") && section.contains("DATE")) {
				formattedDate = processDateWithDelimter(currentLine.trim());
				// System.out.println("formattedDate:" + formattedDate);
				providerDetailsBean.setReEnrInddate(formattedDate);
				section = "";
				continue;
			}
			if (currentLine.contains("PSS054")) {
				section = "LAB";
				continue;
			}

			if (currentLine.contains("LAB STAT") && section.contains("LAB")) {
				String labStat = splitStringBasedOnDelimiter(currentLine.trim());
				String formattedLabStat = formatField(1, labStat);
				labSection.append(formattedLabStat);
				continue;
			}

			if (currentLine.contains("LAB EFF DT") && section.contains("LAB")) {
				formattedDate = processDateWithDelimter(currentLine.trim());
				// System.out.println("formattedDate:" + formattedDate);
				labSection.append(formattedDate);
				continue;
			}

			// Lab specality occurances

			if (currentLine.contains("LAB SPECIALTY END DT") && section.contains("LAB")) {
				// ArrayList<StringBuffer> labSpecalityList = new
				// ArrayList<StringBuffer>();

				int labSpecalityCounter = 0;
				while ((currentLine = br.readLine()) != null) {
					StringBuffer labSpecaalityString = new StringBuffer();
					if (!currentLine.contains("TAXONOMY")) {
						String formattedLabSpecalityEd = formatField(3, currentLine.trim());
						labSpecaalityString.append(formattedLabSpecalityEd);
					} else {
						break;
					}
					if ((currentLine = br.readLine()) != null && !currentLine.contains("TAXONOMY")) {
						// System.out.println("currentLine::"+currentLine);
						String labSpecalityeffDt = processDate(currentLine.trim());
						labSpecaalityString.append(labSpecalityeffDt);
					} else {
						labSpecaalityString.append(formatField(8, ""));
						break;
					}
					if ((currentLine = br.readLine()) != null && !currentLine.contains("TAXONOMY")) {
						String labSpecalityendDt = processDate(currentLine.trim());
						labSpecaalityString.append(labSpecalityendDt);
					} else {
						labSpecaalityString.append(formatField(8, ""));
						break;
					}

					// added new condition for don't add empty record in list
					if (labSpecaalityString.toString().trim().length() > 0) {
						labSpecalityList.add(labSpecaalityString);
					}
					//System.out.println("labSpecalityList Size" + labSpecalityList.size());
					labSpecalityCounter++;
					if (labSpecalityCounter == 240) {
						break;
					}

				}
				StringBuffer labSpecalityOccurances = processOccurancesList(labSpecalityList, 240, 19);
				//System.out.println("Lab Specality Occurances:::" + labSpecalityOccurances.length());
				labSection.append(labSpecalityOccurances);
				//System.out.println("Lab Specality list length::" + labSpecalityList.size());
				section = "";

			}

			if (currentLine.contains("PSS124")) {
				section = "TAXONOMY";
				while ((currentLine = br.readLine()) != null) {
					if (!currentLine.contains("PSS038")) {
						StringBuffer taxonomyCd = new StringBuffer(formatField(10, currentLine));
						taxonomyList.add(taxonomyCd);

					} else {
						StringBuffer taxonomyOccurances = processOccurancesList(taxonomyList, 15, 10);
						//System.out.println("Taxonomy string:::" + taxonomyOccurances.length());
						taxonomySection.append(taxonomyOccurances);
						break;
					}

				}
			}

			// id physician certification
			if (currentLine.contains("PSS038")) {
				section = "PSS038";
			}

			if (currentLine.contains("EFF DT") && section.contains("PSS038")) {
				int maxOccurances = 3;
				int counter = 0;
				ArrayList<StringBuffer> certficationList = new ArrayList<StringBuffer>();
				while ((currentLine = br.readLine()) != null) {
					StringBuffer certificationString = new StringBuffer();
					if (!currentLine.contains("PSS035")) {
						String formattedCode = formatField(2, currentLine);
						certificationString.append(formattedCode);
					} else {
						break;
					}
					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS035")) {
						String formattedEffDate = processDate(currentLine);
						certificationString.append(formattedEffDate);
					} else {
						certificationString.append(formatField(8, ""));
						break;
					}

					certficationList.add(certificationString);
					counter++;
					if (counter == maxOccurances) {
						break;
					}

				}
				StringBuffer certificationOccurances = processOccurancesList(certficationList, maxOccurances, 10);
				// System.out.println("Physician
				// certificationOccurancesOccurances:::" +
				// certificationOccurances.length());
				physicianCertificationStr.append(certificationOccurances);
				// System.out.println("Physician certificationOccuranceslist
				// length::" + certficationList.size());
				section = "PSS036";
			}

			/*
			 * 
			 * if (currentLine.contains("PSS039")) { System.out.println(
			 * "PSS039 currentLine "+currentLine+"section::"+section);
			 * nmpNonPhysician=new StringBuffer(); //section = "PSS039"; section
			 * = "PSS039N"; System.out.println("PSS039 currentLine "
			 * +currentLine+"section::"+section); nmpNonPhysician =
			 * processNonPhysicianParams(br); }
			 * 
			 * 
			 * 
			 * if (section.contains("PSS039N")) { section = "PSS042";
			 * StringBuffer nmpDetailsScreenStr = new
			 * StringBuffer(nmpNonPhysician);
			 * nmpDetailsScreenStr.append(processNmpDetailsScreen(br));
			 * System.out.println("nmp details screen::"+nmpDetailsScreenStr);
			 * nmpDetailsScreen.add(nmpDetailsScreenStr); }
			 * 
			 * prevLine = currentLine;
			 */

			// Rendering Start
			// PSS035-MEMBER GRPS
			if (currentLine.contains("PSS035")) {
				// System.out.println("SECTION - PSS035");
				section = "PSS035";
			}

			if (currentLine.contains("End Date") && section.contains("PSS035")) {

				// commanHeaderREN.append(commanHeader.toString()); Need to add
				// comman Header
				// System.out.println("Starting IF PSS035::"+commanHeaderREN);
				int maxOccurances = 2000;
				// int counter=0;
				// ArrayList<StringBuffer> memberToGroupList = new
				// ArrayList<StringBuffer>();
				// while ((section.contains("PSS036"))){
				while ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
					StringBuffer memberToGroupStr = new StringBuffer();
					if (!currentLine.contains("PSS036")) {
						String formattedAction = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedAction = formatField(1, "");
						} else {

							formattedAction = formatField(1, currentLine);
						}
						// System.out.println("formattedAction>>"+formattedAction);
						memberToGroupStr.append(formattedAction);
					} else {
						memberToGroupStr.append(formatField(1, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						String formattedProvider = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedProvider = formatField(10, "");
						} else {

							formattedProvider = formatField(10, currentLine);
						}
						memberToGroupStr.append(formattedProvider);
					} else {
						memberToGroupStr.append(formatField(10, ""));
						break;
					}
				
					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						// System.out.println("OWNER currentLine:"+currentLine);
						String formattedOwner = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedOwner = formatField(2, "");
						} else {
							formattedOwner = formatField(2, currentLine);
						}
						memberToGroupStr.append(formattedOwner);
					} else {
						memberToGroupStr.append(formatField(2, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						String formattedProvLocal = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedProvLocal = formatField(2, "");
						} else {
							formattedProvLocal = formatField(3, currentLine);
						}

						// String formattedProvLocal = formatField(3,
						// currentLine);
						memberToGroupStr.append(formattedProvLocal);
					} else {
						memberToGroupStr.append(formatField(3, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						// System.out.println("PROV TYPE :"+currentLine);

						String formattedProvtype = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedProvtype = formatField(3, "");
						} else {
							formattedProvtype = formatField(3, currentLine);
						}

						// String formattedProvtype = formatField(3,
						// currentLine);
						memberToGroupStr.append(formattedProvtype);
					} else {
						memberToGroupStr.append(formatField(3, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						// System.out.println("Start Date:"+currentLine);

						String formattedGRPSstartdt = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPSstartdt = formatField(8, "");
						} else {
							formattedGRPSstartdt = processDate(currentLine);
						}

						// String formattedGRPSstartdt =
						// processDate(currentLine);
						memberToGroupStr.append(formattedGRPSstartdt);
					} else {
						memberToGroupStr.append(formatField(8, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS036")) {
						// System.out.println("END Date:"+currentLine);
						String formattedGRPSenddt = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPSenddt = formatField(8, "");
						} else {
							formattedGRPSenddt = processDate(currentLine);
						}

						// String formattedGRPSenddt = processDate(currentLine);

						memberToGroupStr.append(formattedGRPSenddt);
					} else {
						memberToGroupStr.append(formatField(8, ""));
						break;
					}

					// System.out.println("1111111111memberToGroupStr.length()>>"+memberToGroupStr);
					if (memberToGroupStr.toString().trim().length() != 0) {
						memberToGroupList.add(memberToGroupStr);
					}
					// System.out.println("memberToGroupList:::"+memberToGroupList);
					lRendcounter++;

				}

				// System.out.println("memberToGroupList length::" +
				// memberToGroupList.size());
				section = "PSS036";
			}

			// PSS036-GRP MEMBRS
			if (currentLine.contains("PSS036")) {
				section = "PSS036";
			}

			if (currentLine.contains("End Date") && section.contains("PSS036")) {
				int maxgrpOccurances = 2000;
				// ArrayList<StringBuffer> grpmemberList = new
				// ArrayList<StringBuffer>();
				while ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
					StringBuffer grpmemberStr = new StringBuffer();
					if (!currentLine.contains("PSS039")) {
						
						String formattedGRPAction = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPAction = formatField(1, "");
						} else {

							formattedGRPAction = formatField(1, currentLine.trim());
						}
						
						//String formattedGRPAction = formatField(1, currentLine.trim());
						grpmemberStr.append(formattedGRPAction);
					} else {
						grpmemberStr.append(formatField(1, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						
						String formattedGRPProvider = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPProvider = formatField(10, "");
						} else {

							formattedGRPProvider = formatField(10, currentLine.trim());
						}
						
						//String formattedGRPProvider = formatField(10, currentLine.trim());
						grpmemberStr.append(formattedGRPProvider);
					} else {
						grpmemberStr.append(formatField(10, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						String formattedGRPOwner = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPOwner = formatField(2, "");
						} else {

							formattedGRPOwner = formatField(2, currentLine.trim());
						}
						
						//String formattedGRPOwner = formatField(2, currentLine.trim());
						grpmemberStr.append(formattedGRPOwner);
					} else {
						grpmemberStr.append(formatField(2, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						String formattedGRPProvLocal = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPProvLocal = formatField(3, "");
						} else {

							formattedGRPProvLocal = formatField(3, currentLine.trim());
						}
						
						//String formattedGRPProvLocal = formatField(3, currentLine.trim());
						grpmemberStr.append(formattedGRPProvLocal);
					} else {
						grpmemberStr.append(formatField(3, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						// System.out.println("PROV TYPE :"+currentLine);
						String formattedGRPProvtype = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPProvtype = formatField(3, "");
						} else {

							formattedGRPProvtype = formatField(3, currentLine.trim());
						}
						
						//String formattedGRPProvtype = formatField(3, currentLine);
						grpmemberStr.append(formattedGRPProvtype);
					} else {
						grpmemberStr.append(formatField(3, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						// System.out.println("Start Date:"+currentLine);
						String formattedGRPstartdt = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPstartdt = formatField(3, "");
						} else {

							formattedGRPstartdt = processDate(currentLine.trim());
						}
						//String formattedGRPstartdt = processDate(currentLine.trim());
						grpmemberStr.append(formattedGRPstartdt);
					} else {
						grpmemberStr.append(formatField(8, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS039")) {
						// System.out.println("END Date:"+currentLine);
						String formattedGRPenddt = "";
						if (currentLine.equalsIgnoreCase("«» «»")) {
							formattedGRPenddt = formatField(3, "");
						} else {

							formattedGRPenddt = processDate(currentLine.trim());
						}
						//String formattedGRPenddt = processDate(currentLine);
						grpmemberStr.append(formattedGRPenddt);
					} else {
						grpmemberStr.append(formatField(8, ""));
						break;
					}
					if (grpmemberStr.toString().trim().length() != 0) {
						// System.out.println("grpmemberStr:::"+grpmemberStr);
						grpmemberList.add(grpmemberStr);
					}
					grpcount++;

				}
				// System.out.println("grpmemberList:::"+grpmemberList+" ::Size
				// :"+grpmemberList.size());
				section = "PSS039";
			}
			// End Rendering
			
			//  Code for S72 
			if (currentLine.contains("PSS039")) {
				nmpNonPhysician = new StringBuffer();
				// section = "PSS039";
				section = "PSS039N";
				nmpNonPhysician = processNonPhysicianParams(br);
				
				/*
				// For S-Record
				//System.out.println("FormattedProvType:::::::::::::::::::::::::::"+FormattedProvType);
				bProvType = sRecordProvType.matches("(.*)"+FormattedProvType+"(.*)"); 
				//System.out.println("bProvType::"+bProvType);
				  if(bProvType == true)
				  {
					  StringBuffer nmpSRecordDetailsScreenStr = new StringBuffer(nmpNonPhysician);
				  //System.out.println(FormattedProvType);
				  srecord.append("B1"); 
				  srecord.append(tempSrecord);
				  srecord.append(nmpSRecordDetailsScreenStr);
				  srecord.append(formatField(553, ""));
				  System.out.println("srecord::"+srecord); 
				  System.out.println("srecord lenght::"+srecord.length());
				 }
				 */
			}
		

			if (section.contains("PSS039N")) {
				section = "PSS042";
				StringBuffer nmpDetailsScreenStr = new StringBuffer(nmpNonPhysician);
				nmpDetailsScreenStr.append(processNmpDetailsScreen(br));
				nmpDetailsScreen.add(nmpDetailsScreenStr);
			}
			
			prevLine = currentLine;
			// end code S72
			
			
			// MOCA CODE Started here -
			// PSS047 LIST OF MOCAS
			if (currentLine.contains("PSS047")) {
				section = "PSS047";
				// mocasSection++;
			}

			StringBuffer listOfMocasStrBuf = new StringBuffer();
			if (currentLine.contains("END DT") && section.contains("PSS047")) {
				int mocasSection = 0;

				while ((section.contains("PSS047") && currentLine.contains("END DT"))) {

					if (((currentLine = br.readLine()) != null) && !currentLine.contains("PSS045")) {
						String formattedMocaNPI = formatField(10, currentLine);
						// System.out.println("formattedMocaNPI::"+formattedMocaNPI);
						listOfMocasStrBuf.append(formattedMocaNPI);
					} else {
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS045")) {
						String lMocaSSN = currentLine;
						lMocaSSN = lMocaSSN.replaceAll("-", "");
						String formattedMocaSSN = formatField(9, lMocaSSN);
						listOfMocasStrBuf.append(formattedMocaSSN);
						// System.out.println("formattedMocaSSN::"+formattedMocaSSN);
					} else {
						listOfMocasStrBuf.append(formatField(9, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS045")) {
						String formattedMocaTIN = formatField(9, currentLine);
						// System.out.println("formattedMocaTIN::"+formattedMocaTIN);
						listOfMocasStrBuf.append(formattedMocaTIN);
					} else {
						listOfMocasStrBuf.append(formatField(9, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS045")) {
						// String formattedMocaEffDate =
						// formatField(10,currentLine);
						String formattedMocaEffDate = processDate(currentLine);
						// System.out.println("formattedMocaEffDate::"+formattedMocaEffDate);
						listOfMocasStrBuf.append(formattedMocaEffDate);
					} else {
						listOfMocasStrBuf.append(formatField(8, ""));
						break;
					}

					if ((currentLine = br.readLine()) != null && !currentLine.contains("PSS045")) {
						// String formattedMocaEndDate =
						// formatField(10,currentLine);
						String formattedMocaEndDate = processDate(currentLine);
						// System.out.println("formattedMocaEndDate::"+formattedMocaEndDate);
						listOfMocasStrBuf.append(formattedMocaEndDate);

					} else {
						listOfMocasStrBuf.append(formatField(8, ""));
						break;
					}
					// System.out.println("$$$ listOfMocasStrBuf::" +
					// listOfMocasStrBuf.length());

					if (listOfMocasStrBuf.toString().trim().length() != 0) {
						mocaList.add(listOfMocasStrBuf);
						mocasSection++;
						// System.out.println("listOfMocasStrBuf.toString().trim()
						// ::"+listOfMocasStrBuf.toString().trim().length());
					}
					// mocaList.add(listOfMocasStrBuf);
					// mocasSection++;
					// System.out.println("mocaList Size::>>>" + mocaList.size()
					// + " \n Moca List :" + mocaList);
				}

			}

			if (currentLine.contains("PSS045")) {
				section = "PSS045";
				// mocasdetailSection++;
			}

			StringBuffer lMocadetailScreen = new StringBuffer();
			int lMocadetailCount = 0;
			while ((section.contains("PSS045"))) {

				if (((currentLine = br.readLine()) != null) && currentLine.contains("LEGAL NAME")
						&& section.contains("PSS045")) {
					String legalnamemoca = splitStringBasedOnDelimiter(currentLine);
					String formattedlegalnamemoca = formatField(50, legalnamemoca);
					// System.out.println("formattedlegalnamemoca::" +
					// formattedlegalnamemoca);
					lMocadetailScreen.append(formattedlegalnamemoca);

				} else {
					// lMocadetailScreen.append(formatField(50, ""));
					break;
				}
				/*
				 * // This is for to extra empty line from converted InputDoc if
				 * ((currentLine = br.readLine()) != null &&
				 * !currentLine.contains("DATE OF BIRTH") &&
				 * section.contains("PSS045")) { String ltest =
				 * splitStringBasedOnDelimiter(currentLine);
				 * System.out.println("ltest>>"+ltest);
				 * 
				 * } else { // lMocadetailScreen.append(formatField(8, ""));
				 * break; }
				 */

				if ((currentLine = br.readLine()) != null && currentLine.contains("DATE OF BIRTH")
						&& section.contains("PSS045")) {
					String mocaDtofBirth = splitStringBasedOnDelimiter(currentLine);
					String formattedmocamocaDtofBirth = processDate(mocaDtofBirth);
					lMocadetailScreen.append(formattedmocamocaDtofBirth);

				} else {
					// lMocadetailScreen.append(formatField(8, ""));

					break;
				}

				if ((currentLine = br.readLine()) != null && currentLine.contains("END DT")
						&& section.contains("PSS045")) {
					String lmocaEndDt = splitStringBasedOnDelimiter(currentLine);
					// String formattedmocalmocaEndDt = formatField(10, lmocaEndDt);
					String formattedmocalmocaEndDt = processDate(lmocaEndDt);
					lMocadetailScreen.append(formattedmocalmocaEndDt);

				} else {
					// lMocadetailScreen.append(formatField(8, ""));

					break;
				}
				if (lMocadetailScreen.toString().trim().length() != 0) {
					mocadetailList.add(lMocadetailScreen);
				}
				// mocadetailList.add(lMocadetailScreen);
				lMocadetailCount++;
				// System.out.println("mocadetailList:" + mocadetailList);
				// System.out.println(" mocadetailList Size::" +mocadetailList.size())
			}

			// MOCA End here
		}
		// End of loop
		String header = createBillingHeader(headerBean);
		// System.out.println("header:::::::::::::::::::"+header);
		String lS72header = header.substring(0, 113);
		System.out.println("Header length::" + header.length());
		lS72header = lS72header + lFormattedProvType;
		// System.out.println("lS72header:::::::::::::::::::"+lS72header);

		ArrayList<StringBuffer> listOfNmpRecords = addCommonHeaderAndCreateNmpList(headerBean, nmpDetailsScreen, "B1",
				"3", lFormattedProvType);

		output.append(header);
		output.append(ownerSection);
		System.out.println("Owner section length::" + ownerSection.length());
		output.append(locationSection);
		System.out.println("Location section length::" + locationSection.length());
		output.append(providerDetailsBean.createProviderSection());
		System.out.println("providerDetailSection::" + providerDetailSection.length());

		// change this later
		// output.append(formatField(24, "")); // for here append other counts
		// as well length should be 24
		String memgrpCount = getCount(memberToGroupList);
		String grpmemCount = getCount(grpmemberList);
		String nmpCount = getCount(listOfNmpRecords);
		String mocaCount = getCount(mocaList);
		String mocadtlCount = getCount(mocadetailList);
		String labSpecalityCount = getCount(labSpecalityList);
		// System.out.println("labSpecalityCount::"+labSpecalityCount);
		// System.out.println("memgrpCount::"+memgrpCount+"grpmemCount::"+grpmemCount+
		// " nmpCount>>"+nmpCount+ "mocaCount::"+mocaCount +
		// "mocadtlCount"+mocadtlCount);
		output.append(memgrpCount); // here append other counts as well length
									// should be 24
		output.append(grpmemCount);
		output.append(nmpCount);
		output.append(mocaCount);
		output.append(mocadtlCount);
		output.append(labSpecalityCount);

		System.out.println("lab section length::" + labSection.length());
		output.append(labSection);
		output.append(taxonomySection);
		System.out.println("taxonomySection::" + taxonomySection.length());
		output.append(physicianCertificationStr);
		System.out.println("physicianCertificationStr::" + physicianCertificationStr.length());
		System.out.println("BILL Output Lenght ::" + output.length());
		String OutFileName = "Output_BIL_" + getSystemDate("MMddyyyy") + ".txt";
		String nmpFileName = "Output_S72_" + getSystemDate("MMddyyyy") + ".txt";
		// String outputPath =
		// "C:/Users/IBM_ADMIN/Desktop/Billing/TEST/OutputFiles/";
		writeOutputToFile(outputpath, output.toString().toUpperCase(), OutFileName);

		// For NMP - S72
		System.out.println("listOfNmpRecords::" + listOfNmpRecords.size());
		if (listOfNmpRecords.size() > 0) {
			writeListToFile(outputpath, listOfNmpRecords, nmpFileName);
			System.out.println("S72 Output Lenght ::" + listOfNmpRecords.size());
		}
		// System.out.println("indexBillRecord:::"+indexBillRecord);
		//System.out.println("Bill Index lenght:::" + indexBillRecord.length());
		String indexFileName = "Output_IND_" + getSystemDate("MMddyyyy") + ".txt";
		writeOutputToFile(outputpath, indexBillRecord.toString(), indexFileName);

		// Write the RENDERING FILE
		//System.out.println("commonHeader::" + commonHeader);
		String lRENoutputfile = "Output_REN_" + getSystemDate("MMddyyyy") + ".txt";
		if (memberToGroupList.size() != 0) {
			commanHeaderREN.append("B2");
			commanHeaderREN.append(formattedAccNum);
			commanHeaderREN.append(formattedReviewInputDate);
			String SubType = "1";
			for (int i = 0; i < memberToGroupList.size(); i++) {
				int seq = 1 + i;
				String lseq = String.format("%04d", seq);
				// System.out.println("PSS035 lseq>>" + lseq);
				StringBuffer lrend0035 = memberToGroupList.get(i);
				// System.out.println("lrend0035>>"+lrend0035.length());
				String lRENFinalString = commanHeaderREN.toString().toUpperCase() + lseq + SubType + commonHeader
						+ lrend0035.toString().toUpperCase();
				// System.out.println(" lRENFinalString::" + lRENFinalString);
				// System.out.println("lRENFinalString length::" +
				// lRENFinalString.length());
				writeOutputToFile(outputpath, lRENFinalString.toString().toUpperCase(), lRENoutputfile);
				System.out.println("REN mtog Lenght ::" + lRENFinalString.length());

			}
		}

		// Write the RENDERING FILE

		// String lRENGTOMoutputfile = "Output_REN_" + getSystemDate("MMddyyyy")
		// + ".txt";
		if (grpmemberList.size() != 0) {
			StringBuffer HeaderREN = new StringBuffer();
			HeaderREN.append("B1");
			HeaderREN.append(formattedAccNum);
			HeaderREN.append(formattedReviewInputDate);
			// System.out.println("HeaderREN>>"+HeaderREN.length()+"commanHeader>>"+commanHeader.length());
			String SubType = "2";
			for (int i = 0; i < grpmemberList.size(); i++) {
				int seq = 1 + i;
				String lseq = String.format("%04d", seq);
				StringBuffer lrend0036 = grpmemberList.get(i);
				// System.out.println("lrend0035>>"+lrend0035.length());
				String lRENFinalString = HeaderREN.toString().toUpperCase() + lseq + SubType + commonHeader
						+ lrend0036.toString().toUpperCase();
				System.out.println("REN 0036 Lenght ::" + lRENFinalString.length());
				writeOutputToFile(outputpath, lRENFinalString.toString().toUpperCase(), lRENoutputfile);

			}
		}

		// For MOCA
		commanHeaderMOCA.append("B1");
		commanHeaderMOCA.append(formattedAccNum);
		commanHeaderMOCA.append(formattedReviewInputDate);
		String lMOCAoutputfile = "Output_MOC_" + getSystemDate("MMddyyyy") + ".txt";
		if (mocadetailList.size() != 0 && mocaList.size() != 0 && mocadetailList.size() == mocaList.size() ) {
			for (int i = 0; i < mocadetailList.size(); i++) {

				int seq = 1 + i;
				String lseq = String.format("%04d", seq);
				lMocadetails = mocadetailList.get(i);
				StringBuffer ltemp = mocaList.get(i);
				// System.out.println("lMocadetails::" + lMocadetails);
				String lMOCAFinalString = commanHeaderMOCA.toString() + lseq + SubType_Moca + commonHeader
						+ ltemp.toString() + lMocadetails.toString();
				// System.out.println("with MOCA Details lMOCAFinalString::" +
				// lMOCAFinalString);
				System.out.println("MOCA lenght ::" + lMOCAFinalString.length());
				writeOutputToFile(outputpath, lMOCAFinalString.toString().toUpperCase(), lMOCAoutputfile);
			}
		}
		

		
		 // Write S-Record other then 022 and 024 PROV-TYPE 
		if (bProvType == true) 
		{ 
			String sRecordoutputfile = "Output_SRC_" + getSystemDate("MMddyyyy") + ".txt"; 
			writeOutputToFile(outputpath, srecord.toString().toUpperCase(), nmpFileName);
		 
		 }
		

	}

	protected String getSystemDate(String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		Date date = new Date();
		String SysDate = dateFormat.format(date);
		return SysDate;
	}

	private String createBillingHeader(BillingHeader header) {
		return header.createHeader();
	}

	protected void writeOutputToFile(String path, String output, String fileName) throws IOException {
		String ouputFileName = path + fileName;
		Writer outputWriter = new BufferedWriter(new FileWriter(ouputFileName, true));
		outputWriter.write(output);
		outputWriter.write("\r\n");
		outputWriter.close();
	}

	protected void writeListToFile(String path, List<StringBuffer> listOfRecords, String fileName) throws IOException {
		String ouputFileName = path + fileName;
		Writer outputWriter = new BufferedWriter(new FileWriter(ouputFileName, true));
		for (StringBuffer record : listOfRecords) {
			outputWriter.write(record.toString().toUpperCase());
			outputWriter.write("\r\n");
		}
		outputWriter.close();
	}

	/**
	 * @param currentLine
	 * @return
	 */
	private String[] getAccountIdAndProfileId(String currentLine) {
		Pattern pattern = Pattern.compile("\\w+([0-9]*[-]*[0-9]+)\\w+([0-9]+)");
		Matcher matcher = pattern.matcher(currentLine);
		String[] tokens = new String[10];
		for (int i = 0; i < matcher.groupCount(); i++) {
			matcher.find();
			tokens[i] = matcher.group();
			// System.out.println(matcher.group());
		}
		return tokens;
	}

	/**
	 * @param currentLine
	 * @param output
	 */

	// ************change return type******************
	public String getDateFromFileName(String fileName) {
		String[] fileNametokens = fileName.split("_");
		String inputDateToken = fileNametokens[fileNametokens.length - 1];
		String reviewinputDate = inputDateToken.substring(0, 8);
		String formattedReviewInputDate = convertInputDate(reviewinputDate);
		return formattedReviewInputDate;

		// return "";
	}

	private String processDateWithDelimter(String currentLine) {
		String statEffDate = splitStringBasedOnDelimiter(currentLine);
		return processDate(statEffDate);
	}

	private String processDate(String statEffDate) {
		String formattedStatEffDate = null;
		if (statEffDate.length() != 0 && !statEffDate.isEmpty()) {
			formattedStatEffDate = convertBilldate(statEffDate);

		} else {
			formattedStatEffDate = formatField(8, statEffDate);
		}
		return formattedStatEffDate;
	}

	public String formatField(int totalLength, String str) {
		String paddedString = String.format("%-" + totalLength + "s", str);
		paddedString = paddedString.substring(0, totalLength);
		return paddedString;
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
			// System.out.println(formattedDateString);
		} catch (ParseException e) {
			return formatField(8, "");
		}
		return formattedDateString;
	}

	// Method used for to convert the mmddyyy to ccyymmdd
	private String convertdate(String ldate) {
		ldate = ldate.trim().replaceAll("/", "");
		String finaldate = "";
		// ldate= ldate.replaceAll("/", "");
		// System.out.println("ldate::"+ldate.length());
		if (ldate.length() > 0 && ldate.length() == 8) {
			String lmonth = ldate.substring(0, 2);
			String lday = ldate.substring(2, 4);
			String lyear = ldate.substring(4, 8);
			finaldate = lyear + "" + lmonth + "" + lday;
			// System.out.println(finaldate);
		} else if(ldate.length() < 8 && ldate.length() > 0 ){
			finaldate = formatField(8, "12345678");
		}
		else if (ldate.length() == 0)
		{
			finaldate = formatField(8, "");
		}
		return finaldate;
	}

	// Method used for only BILL file to remove / from date
	private String convertBilldate(String ldate) {
		ldate = ldate.trim().replaceAll("/", "");
		String finaldate = "";
		// ldate= ldate.replaceAll("/", "");
		// System.out.println("ldate::"+ldate);
		if (ldate.length() > 0 && ldate.length() == 8) {
			finaldate = formatField(8, ldate);
		} else {
			finaldate = formatField(8, "");

		}
		// Return the string
		return finaldate;
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

	private String splitStringBasedOnDelimiterLegalName(String currentLine) {
		String[] tokens = currentLine.split(":");
		String data = "";
		StringBuffer ldata = new StringBuffer();
		// System.out.println("tokens.length::"+tokens.length);
		if (tokens.length > 1) {
			for (int i = 1; i < tokens.length; i++) {
				// System.out.println(i);
				data = tokens[i].trim();

				ldata.append(formatField(9, data.trim()));
			}
			data = ldata.substring(1);
		} // else {
			// ldata = "";
			// }

		// System.out.println("ldata::"+ldata.substring(1));
		return data;
	}

	private String readAndFormatAddressFiled(int maxlegth, String line) {
		String text = line.trim();
		String formattedString = formatField(maxlegth, text);
		return formattedString;
	}

	public StringBuffer processOccurancesList(ArrayList<StringBuffer> list, int maxOccurances, int recordLength) {
		StringBuffer totalRecordsString = new StringBuffer();
		int listLength = list.size();
		int noOfTimesToBeAdded = maxOccurances - listLength;
		for (StringBuffer record : list) {
			totalRecordsString.append(record);
		}
		for (int i = 0; i < noOfTimesToBeAdded; i++) {
			totalRecordsString.append(formatField(recordLength, ""));
		}
		// System.out.println(totalRecordsString);
		return totalRecordsString;

	}

	private ArrayList<StringBuffer> addCommonHeaderAndCreateNmpList(BillingHeader header,
			List<StringBuffer> nmpDetailsScreen, String type, String subType, String lFormattedProvType) {

		ArrayList<StringBuffer> nmpList = new ArrayList<StringBuffer>();
		// this needs to be changed
		String accNum = header.getAccountNum();
		String fileDate = header.getFileDate();
		// read after sequence number
		int counter = 1;
		StringBuffer nmpRecord;
		// System.out.println(String.format("%04d", n));

		/*
		 * OID-ID-TYPE 2 OID-ACCT-NUM 13 OID-FILE-DT 8 OID-SEQ-NUM 4
		 * OID-SUB-TYPE 1 OID-PROF-ID 10 OID-REVIEW-DUE-DATE 8 OID-PROV-NAME 50
		 * OID-ACTION-REQUESTED 1 OID-DOC-TYPE 1 OID-PROVIDER-NUM 10
		 */
		for (StringBuffer nmp : nmpDetailsScreen) {
			nmpRecord = new StringBuffer();
			nmpRecord.append(type + accNum + fileDate);
			nmpRecord.append(String.format("%04d", counter));
			nmpRecord.append(subType);
			nmpRecord.append(header.getProfId());
			nmpRecord.append(header.getProvName());
			nmpRecord.append(header.getReviewDuedate());

			nmpRecord.append(header.getProviderNumber());
			nmpRecord.append(header.getOwnNum());
			nmpRecord.append(header.getLocNum());
			// nmpRecord.append(header.getProvType());
			nmpRecord.append(formatField(3, lFormattedProvType));
			// System.out.println("length after adding
			// header::"+nmpRecord.length());
			nmpRecord.append(nmp);
			nmpList.add(nmpRecord);
			// System.out.println("nmp section length::"+nmp.length());
			// System.out.println("final nmp record:"+nmpRecord);
			// System.out.println("nmp record length::"+nmpRecord.length());
			counter++;
		}

		return nmpList;

	}

	// for PSS039 NON –PHYSICIAN MEDICAL PRACTITIONERS
	private StringBuffer processNonPhysicianParams(BufferedReader br) throws IOException {
		String currentLine;
		StringBuffer nonPhysicianSection = new StringBuffer();
		String nextSection = "PSS042";

		while (((currentLine = br.readLine()) != null) && !currentLine.contains(nextSection)) {
			if (currentLine.contains("END DT")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains(nextSection)) {
					String formattedNmpProvider = formatField(10, currentLine);
					nonPhysicianSection.append(formattedNmpProvider);
				} else {
					break;
				}

				if ((currentLine = br.readLine()) != null && !currentLine.contains(nextSection)) {
					String formattedLicense = formatField(25, currentLine);
					nonPhysicianSection.append(formattedLicense);
				} else {
					nonPhysicianSection.append(formatField(42, ""));
					break;
				}

				if ((currentLine = br.readLine()) != null && !currentLine.contains(nextSection)) {
					String formattedtype = formatField(1, currentLine);
					nonPhysicianSection.append(formattedtype);
				} else {
					nonPhysicianSection.append(formatField(17, ""));
					break;
				}

				if ((currentLine = br.readLine()) != null && !currentLine.contains(nextSection)) {
					//String formattedEffDate = processDate(currentLine.trim());
					String formattedEffDate = convertdate(currentLine.trim());
					nonPhysicianSection.append(formattedEffDate);
				} else {
					nonPhysicianSection.append(formatField(8, ""));
					break;
				}

				if ((currentLine = br.readLine()) != null && !currentLine.contains(nextSection)) {
					// System.out.println("currentLine ::"+currentLine);

					String formattedEndDate = null;
					if (currentLine.trim().isEmpty()) {
						formattedEndDate = formatField(8, "20691231");
					} else {
						//formattedEndDate = processDate(currentLine.trim());
						formattedEndDate = convertdate(currentLine.trim());
					}
					nonPhysicianSection.append(formattedEndDate);
					// System.out.println("nonPhysicianSection:" +
					// nonPhysicianSection.toString());
				} else {
					nonPhysicianSection.append(formatField(8, "20691231"));
					break;
				}
				/*
				 * StringBuffer record=getCommonHeader(commonHeader, "");
				 * record.append(nonPhysicianSection); //
				 * nmpDetailsScreen.add(record); System.out.println(
				 * "NMP details screen::"+nmpDetailsScreen.size());
				 */
			}
		}
		// System.out.println("nonPhysicianSection::" +
		// nonPhysicianSection.toString());
		// System.out.println("nonPhysicianSection::" +
		// nonPhysicianSection.length());
		return nonPhysicianSection;
	}

	private StringBuffer processNmpDetailsScreen(BufferedReader br) throws IOException {
		// prefix
		String currentLine;
		StringBuffer nmpDetailsStr = new StringBuffer();
		String nameSection = "";
		String section = "";
		boolean setEndDate = false;
		Map<Integer, String> nmpDetailsMap = new HashMap<Integer, String>();
		Map<Integer, String> addressMap = new HashMap<Integer, String>();
		while ((currentLine = br.readLine()) != null) {
			if (currentLine.contains("LEGAL NAME")) {
				String legalname = splitStringBasedOnDelimiter(currentLine);
				String FormattedLegalname = formatField(50, legalname);
				nmpDetailsStr.append(FormattedLegalname);
				nameSection = "title";
				continue;
			}

			// prefix
			if (currentLine.contains("TITLE") || currentLine.contains("PREFIX")) {
				String prefix = currentLine.substring(6).trim();
				String formattedPrefix = formatField(4, prefix);
				nmpDetailsStr.append(formattedPrefix);
				nameSection = "";
				continue;
			} else if (nameSection.contains("title")) {
				String formattedPrefix = formatField(4, "");
				nmpDetailsStr.append(formattedPrefix);
				nameSection = "lastname";
			}

			// last name
			if (currentLine.contains("LAST NAME")) {
				String lastName = splitStringBasedOnDelimiter(currentLine);
				String formattedLastName = formatField(25, lastName);
				nmpDetailsStr.append(formattedLastName);
				continue;
			} else if (nameSection.contains("lastname")) {
				String formattedLastName = formatField(25, "");
				nmpDetailsStr.append(formattedLastName);
				nameSection = "firstname";
			}

			// first name
			if (currentLine.contains("FIRST NAME")) {
				String firstName = splitStringBasedOnDelimiter(currentLine);
				String formattedFirstName = formatField(15, firstName);
				nmpDetailsStr.append(formattedFirstName);
				continue;
			} else if (nameSection.contains("firstname")) {
				String formattedFirstName = formatField(15, "");
				nmpDetailsStr.append(formattedFirstName);
				nameSection = "middle";
			}
			// middle
			if (currentLine.contains("MIDDLE")) {
				String middleName = splitStringBasedOnDelimiter(currentLine);
				String formattedMiddleName = formatField(15, middleName);
				nmpDetailsStr.append(formattedMiddleName);
				continue;
			} else if (nameSection.contains("middle")) {
				String formattedMiddleName = formatField(15, "");
				nmpDetailsStr.append(formattedMiddleName);
				nameSection = "suffix";
			}

			// suffix
			if (currentLine.contains("SUFFIX")) {
				String suffix = splitStringBasedOnDelimiter(currentLine);
				String formattedSuffix = formatField(4, suffix);
				nmpDetailsStr.append(formattedSuffix);
				// System.out.println("length after suffix::"+output.length());
				continue;
			} else if (nameSection.contains("suffix")) {
				String formattedSuffix = formatField(4, "");
				nmpDetailsStr.append(formattedSuffix);
				nameSection = "";
			}

			// state code == enrollment status
			if (currentLine.contains("STATUS") && section.contains("PSS042")) {
				String stateCode = splitStringBasedOnDelimiter(currentLine);
				String FormattedStateCode = null;
				if (stateCode.isEmpty()) {
					FormattedStateCode = formatField(1, "0");
				} else {
					FormattedStateCode = formatField(1, stateCode);
				}
				nmpDetailsMap.put(1, FormattedStateCode);
				continue;
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
				continue;
			} else if (setEndDate) {
				String formattedEndDt = formatField(8, "");
				setEndDate = false;
				nmpDetailsMap.put(3, formattedEndDt);
			}

			// ssn
			if (currentLine.contains("SOCIAL SECURITY")) {
				String ssn = splitStringBasedOnDelimiter(currentLine);
				String FormattedSsn = null;
				if (ssn.isEmpty()) {
					FormattedSsn = formatField(9, "");
				} else {
					FormattedSsn = formatField(9, ssn);
				}
				nmpDetailsMap.put(4, FormattedSsn);
				continue;
			}
			// prov Enr
			if (currentLine.contains("PROV ENR")) {
				String provEnr = splitStringBasedOnDelimiter(currentLine);
				String formattedProvEnr = formatField(2, provEnr);
				section = "PROV ENR";
				nmpDetailsMap.put(5, formattedProvEnr);
				continue;
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
				continue;
			}

			// RE ENR IND
			if (currentLine.contains("ENR IND")) {
				String reEnrInd = splitStringBasedOnDelimiter(currentLine);
				String formattedReEnrInd = formatField(1, reEnrInd);
				section = "ENR IND";
				nmpDetailsMap.put(7, formattedReEnrInd);
				continue;
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
				continue;
			}

			// service address
			if (currentLine.contains("ATN")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
					String formattedString = readAndFormatAddressFiled(24, currentLine);
					addressMap.put(1, formattedString);
				} else {
					addressMap.put(1, formatField(24, ""));
					addressMap.put(7, formatField(24, ""));
				}
				if (!currentLine.contains("LN 1")) {
					if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
						String formattedString = readAndFormatAddressFiled(24, currentLine);
						addressMap.put(7, formattedString);
					} else {
						addressMap.put(7, formatField(24, ""));
					}
				}
			}

			if (currentLine.contains("LN 1")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(2, formattedString);
				} else {
					addressMap.put(2, formatField(64, ""));
					addressMap.put(8, formatField(64, ""));
				}
				if (!currentLine.contains("LN 2")) {
					if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
						String formattedString = readAndFormatAddressFiled(64, currentLine);
						addressMap.put(8, formattedString);
					} else {
						addressMap.put(8, formatField(64, ""));
					}
				}
			}

			if (currentLine.contains("LN 2")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(3, formattedString);
				} else {
					addressMap.put(3, formatField(64, ""));
					addressMap.put(9, formatField(64, ""));
				}
				if (!currentLine.contains("CITY:")) {
					if (((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
						String formattedString = readAndFormatAddressFiled(64, currentLine);
						addressMap.put(9, formattedString);
					} else {
						addressMap.put(9, formatField(64, ""));
					}
				}
			}

			//
			if (currentLine.contains("CITY:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
					String formattedString = readAndFormatAddressFiled(30, currentLine);
					addressMap.put(4, formattedString);
				} else {
					addressMap.put(4, formatField(30, ""));
					addressMap.put(10, formatField(30, ""));
				}
				if (!currentLine.contains("STATE:")) {
					if (((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
						String formattedString = readAndFormatAddressFiled(30, currentLine);
						addressMap.put(10, formattedString);
					} else {
						addressMap.put(10, formatField(30, ""));
					}
				}
			}

			if (currentLine.contains("STATE:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
					String formattedString = readAndFormatAddressFiled(2, currentLine);
					addressMap.put(5, formattedString);
				} else {
					addressMap.put(5, formatField(2, ""));
					addressMap.put(11, formatField(2, ""));
				}

				if (!currentLine.contains("ZIP")) {
					if (((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
						String formattedString = readAndFormatAddressFiled(2, currentLine);
						addressMap.put(11, formattedString);
					} else {
						addressMap.put(11, formatField(2, ""));
					}
				}
			}

			if (currentLine.contains("ZIP")) {
				if (((currentLine = br.readLine()) != null)) {
					String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
					String formattedString = null;
					formattedString = readAndFormatAddressFiled(9, zipcode);
					addressMap.put(6, formattedString);
				} else {
					addressMap.put(6, formatField(9, ""));
				}

				if (((currentLine = br.readLine()) != null)) {
					String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
					String formattedString = null;
					formattedString = readAndFormatAddressFiled(9, zipcode);
					addressMap.put(12, formattedString);
				} else {
					addressMap.put(12, formatField(9, ""));
				}

				break;
			}
		}
		appendMap(nmpDetailsStr, nmpDetailsMap);
		appendMap(nmpDetailsStr, addressMap);
		// System.out.println("NMP Details Str:" + nmpDetailsStr.toString());
		// System.out.println("Length of nmp details str::" +
		// nmpDetailsStr.length());
		return nmpDetailsStr;

	}

	private StringBuffer appendMap(StringBuffer addressString, Map<Integer, String> addressMap) {
		Map<Integer, String> sortedAddressMap = new TreeMap<Integer, String>(addressMap);
		StringBuffer str = new StringBuffer();
		Set<Entry<Integer, String>> s = sortedAddressMap.entrySet();
		Iterator<Entry<Integer, String>> it = s.iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, String> entry = (Map.Entry<Integer, String>) it.next();
			String value = (String) entry.getValue();
			addressString.append(value);
			str.append(value);
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
			// System.out.println(formattedDateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return formattedDateString;
	}

	private Map<Integer, String> processAddress(BufferedReader br) throws IOException {
		String currentLine = br.readLine();
		Map<Integer, String> addressMap = new HashMap<Integer, String>();
		if (currentLine.contains("ATN")) {
			if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
				String formattedString = readAndFormatAddressFiled(24, currentLine);
				addressMap.put(1, formattedString);
			} else {
				addressMap.put(1, formatField(24, ""));
				addressMap.put(7, formatField(24, ""));
				addressMap.put(13, formatField(24, ""));
			}
			if (!currentLine.contains("LN 1")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
					String formattedString = readAndFormatAddressFiled(24, currentLine);
					addressMap.put(7, formattedString);
				} else {
					addressMap.put(7, formatField(24, ""));
					addressMap.put(13, formatField(24, ""));
				}
			}
			if (!currentLine.contains("LN 1")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 1")) {
					String formattedString = readAndFormatAddressFiled(24, currentLine);
					addressMap.put(13, formattedString);
					currentLine = br.readLine();
				} else {
					addressMap.put(13, formatField(24, ""));
				}
			}

		}

		if (currentLine.contains("LN 1")) {
			if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
				String formattedString = readAndFormatAddressFiled(64, currentLine);
				addressMap.put(2, formattedString);
			} else {
				addressMap.put(2, formatField(64, ""));
				addressMap.put(8, formatField(64, ""));
				addressMap.put(14, formatField(64, ""));
			}
			if (!currentLine.contains("LN 2")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(8, formattedString);
				} else {
					addressMap.put(8, formatField(64, ""));
					addressMap.put(14, formatField(64, ""));
				}
			}

			if (!currentLine.contains("LN 2")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("LN 2")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(14, formattedString);
				} else {
					addressMap.put(14, formatField(64, ""));
				}
			}
			currentLine = br.readLine();
		}

		if (currentLine.contains("LN 2")) {
			if (((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
				String formattedString = readAndFormatAddressFiled(64, currentLine);
				addressMap.put(3, formattedString);
			} else {
				addressMap.put(3, formatField(64, ""));
				addressMap.put(9, formatField(64, ""));
				addressMap.put(15, formatField(64, ""));
			}
			if (!currentLine.contains("CITY:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(9, formattedString);
				} else {
					addressMap.put(9, formatField(64, ""));
					addressMap.put(15, formatField(64, ""));
				}
			}
			if (!currentLine.contains("CITY:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("CITY:")) {
					String formattedString = readAndFormatAddressFiled(64, currentLine);
					addressMap.put(15, formattedString);
				} else {
					addressMap.put(15, formatField(64, ""));
				}
			}
			currentLine = br.readLine();
		}

		//
		if (currentLine.contains("CITY:")) {
			if (((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
				String formattedString = readAndFormatAddressFiled(25, currentLine);
				addressMap.put(4, formattedString);
			} else {
				addressMap.put(4, formatField(25, ""));
				addressMap.put(10, formatField(30, ""));
				addressMap.put(16, formatField(30, ""));
			}
			if (!currentLine.contains("STATE:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
					String formattedString = readAndFormatAddressFiled(30, currentLine);
					addressMap.put(10, formattedString);
				} else {
					addressMap.put(10, formatField(30, ""));
					addressMap.put(16, formatField(30, ""));
				}
			}
			if (!currentLine.contains("STATE:")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("STATE:")) {
					String formattedString = readAndFormatAddressFiled(30, currentLine);
					addressMap.put(16, formattedString);
				} else {
					addressMap.put(16, formatField(30, ""));
				}
			}
			currentLine = br.readLine();
		}

		if (currentLine.contains("STATE:")) {
			if (((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
				String formattedString = readAndFormatAddressFiled(2, currentLine);
				addressMap.put(5, formattedString);
			} else {
				addressMap.put(5, formatField(2, ""));
				addressMap.put(11, formatField(2, ""));
				addressMap.put(17, formatField(2, ""));
			}

			if (!currentLine.contains("ZIP")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
					String formattedString = readAndFormatAddressFiled(2, currentLine);
					addressMap.put(11, formattedString);
				} else {
					addressMap.put(11, formatField(2, ""));
					addressMap.put(17, formatField(2, ""));
				}
			}
			if (!currentLine.contains("ZIP")) {
				if (((currentLine = br.readLine()) != null) && !currentLine.contains("ZIP")) {
					String formattedString = readAndFormatAddressFiled(2, currentLine);
					addressMap.put(17, formattedString);
				} else {
					addressMap.put(17, formatField(2, ""));
				}
			}
			currentLine = br.readLine();
		}

		if (currentLine.contains("ZIP")) {
			if (((currentLine = br.readLine()) != null)) {
				String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
				String formattedString = null;
				formattedString = readAndFormatZipCode(9, zipcode);
				addressMap.put(6, formattedString);
			} else {
				addressMap.put(6, formatField(9, ""));
			}

			if (((currentLine = br.readLine()) != null)) {
				String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
				String formattedString = null;
				formattedString = readAndFormatZipCode(9, zipcode);
				addressMap.put(12, formattedString);
			} else {
				addressMap.put(12, formatField(9, ""));
			}

			if (((currentLine = br.readLine()) != null)) {
				String zipcode = currentLine.replaceAll("[\\s\\-()]", "");
				String formattedString = null;
				formattedString = readAndFormatZipCode(9, zipcode);
				addressMap.put(18, formattedString);
			} else {
				addressMap.put(18, formatField(9, ""));
			}

		}
		return addressMap;
	}

	private String getPipe() {
		return "|";
	}

	private String getCount(ArrayList<StringBuffer> list) {
		return String.format("%04d", list.size());
	}

	// Method for NMP zip code
	private String readAndFormatZipCode(int maxlegth, String line) {
		String text = line.trim();
		String formattedString = formatZipCode(maxlegth, text);
		return formattedString;
	}

	// function: if the zip contains 5 digits then append 0000
	public String formatZipCode(int totalLength, String str) {
		if (str.isEmpty()) {
			String paddedString = String.format("%-" + totalLength + "s", str);
			paddedString = paddedString.substring(0, totalLength);
			return paddedString;
		} else {
			// String paddedString=String.format("%-9s", "78899" ).replace(' ',
			// '0');
			if (str.length() == 5 && str.substring(0, 5).matches("\\d+")) {
				// boolean numericZip = str.substring(0, 5).matches("\\d+");
				// if (numericZip) {
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
	/*
	 * public void convertRTFToText(String inputFileName, String outputFile)
	 * throws IOException, SAXException, TikaException { BodyContentHandler
	 * handler = new BodyContentHandler(); Metadata metadata = new Metadata();
	 * FileInputStream inputstream = new FileInputStream(new
	 * File(inputFileName)); ParseContext pcontext = new ParseContext();
	 * RTFParser rtfparser = new RTFParser(); rtfparser.parse(inputstream,
	 * handler, metadata, pcontext); File tempfile = new File(outputFile);
	 * PrintWriter writer = new PrintWriter(tempfile);
	 * writer.write(handler.toString()); writer.close(); }
	 */

}

