package com.xerox.pave.util.IBMPaveParser;


public class BillingHeader {
	
/*	
	OID-ID-TYPE		2
	OID-ACCT-NUM		13
	OID-FILE-DT		8
	OID-SEQ-NUM		4
	OID-SUB-TYPE		1
	OID-PROF-ID		10
	OID-REVIEW-DUE-DATE		8
	OID-PROV-NAME		50
	OID-ACTION-REQUESTED		1
	OID-DOC-TYPE		1
	OID-PROVIDER-NUM		10*/
	
	private String type;
	private String accountNum;
	private String fileDate;
	private String seqNum;
	private String subType;
	private String profId;
	private String provName;
	private String reviewDuedate;
	private String providerNumber;
	private String ownNum;
	private String locNum;
	private String provType;
	private String actionRequested;
	private String docType;
	
	
	
	
	@Override
	public String toString() {
		return "BillingHeader [type=" + type + ", accountNum=" + accountNum + ", fileDate=" + fileDate + ", seqNum="
				+ seqNum + ", subType=" + subType + ", profId=" + profId + ", provName=" + provName + ", reviewDuedate="
				+ reviewDuedate + ", providerNumber=" + providerNumber + ", ownNum=" + ownNum + ", locNum=" + locNum
				+ ", provType=" + provType + ", actionRequested=" + actionRequested + ", docType=" + docType + "]";
	}


	// follow the order from copy book
	public String createHeader() {
		//System.out.println(toString());
		String header= type+accountNum+fileDate+seqNum+subType+profId+provName+reviewDuedate+actionRequested+docType+providerNumber+ownNum+locNum+provType;
		//System.out.println("Header length:::"+header.length());
		return header;
	}
	

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAccountNum() {
		return accountNum;
	}
	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
	public String getFileDate() {
		return fileDate;
	}
	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}
	public String getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(String seqNum) {
		this.seqNum = seqNum;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public String getProfId() {
		return profId;
	}
	public void setProfId(String profId) {
		this.profId = profId;
	}
	public String getProvName() {
		return provName;
	}
	public void setProvName(String provName) {
		this.provName = provName;
	}
	public String getReviewDuedate() {
		return reviewDuedate;
	}
	public void setReviewDuedate(String reviewDuedate) {
		this.reviewDuedate = reviewDuedate;
	}
	public String getProviderNumber() {
		return providerNumber;
	}
	public void setProviderNumber(String providerNumber) {
		this.providerNumber = providerNumber;
	}
	public String getOwnNum() {
		return ownNum;
	}
	public void setOwnNum(String ownNum) {
		this.ownNum = ownNum;
	}
	public String getLocNum() {
		return locNum;
	}
	public void setLocNum(String locNum) {
		this.locNum = locNum;
	}
	public String getProvType() {
		return provType;
	}
	public void setProvType(String provType) {
		this.provType = provType;
	}

	public String getActionRequested() {
		return actionRequested;
	}

	public void setActionRequested(String actionRequested) {
		this.actionRequested = actionRequested;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	

	
	
	

}
