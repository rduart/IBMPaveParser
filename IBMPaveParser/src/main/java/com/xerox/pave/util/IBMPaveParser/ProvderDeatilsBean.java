package com.xerox.pave.util.IBMPaveParser;


import java.util.ArrayList;

public class ProvderDeatilsBean {

	/*10  BID-APP-DATE           PIC X(08).                       CL*2
    10  BID-DTL-PROV-TYPE      PIC X(03).                       CL*3
    10  BID-PRACTICE           PIC X(02).                       CL*3
    10  BID-STAT-CODE          PIC X(01).                       CL*4
    10  BID-STAT-EFF-DATE      PIC X(08).                       CL*2
    10  BID-REJT-RSN           PIC X(02).                       CL*4   ---------24
    10  BID-CATEGORIES-OF-SERVICE                               CL*2
            OCCURS 20 TIMES.                                    CL*1
        15  BID-CATEGORY       PIC X(03).                       CL*4
        15  BID-CAT-BEGIN-DATE PIC X(08).                       CL*2
        15  BID-CAT-END-DATE   PIC X(08).                       CL*2   --------380
    10  BID-CHDP-PROV-NO       PIC X(10).
    10  BID-LIC-NO             PIC X(25).
    10  BID-LIC-EFF-DT         PIC X(08).                       CL*2
    10  BID-CLIA-NO            PIC X(10).
    10  BID-SPEC-PROC-TYPE     PIC X(01).
    10  BID-PROV-ENR           PIC X(02).
    10  BID-PROV-ENR-DATE      PIC X(08).                       CL*2
    10  BID-RE-ENR-IND         PIC X(01).
    10  BID-RE-ENR-IND-DATE    PIC X(08).                       CL*2  --------73*/
	
	
	private String appDate;
	private String provType;
	private String practice;
	private String statCode;
	private String statEffDate;
	private String rejectReason;
	private StringBuffer categoryOfServicesString = new StringBuffer();
	private String chdpProvNo;
	private String licNo;
	private String licEffDate;
	private String cliaNo;
	private String specProcType;
	private String provEnr;
	private String provEnrDate;
	private String reEnrInd;
	private String reEnrInddate;
	
	
	private ArrayList<StringBuffer> categoryOfServices=new ArrayList<StringBuffer>();
	
	
	@Override
	public String toString() {
		
		
		return "ProvderDeatilsBean [appDate=" + appDate + ", provType=" + provType + ", practice=" + practice
				+ ", statCode=" + statCode + ", statEffDate=" + statEffDate + ", rejectReason=" + rejectReason
				+ ", chdpProvNo=" + chdpProvNo + ", licNo=" + licNo + ", licEffDate=" + licEffDate + ", cliaNo="
				+ cliaNo + ", specProcType=" + specProcType + ", provEnr=" + provEnr + ", provEnrDate=" + provEnrDate
				+ ", reEnrInd=" + reEnrInd + ", reEnrInddate=" + reEnrInddate + ", categoryOfServices="
				+ categoryOfServices + "]";
	}
	
	public String createProviderSection() {
		//System.out.println(toString());
		String providerSection= appDate+provType+practice+statCode+statEffDate+rejectReason+categoryOfServicesString.toString()+chdpProvNo+licNo+licEffDate+cliaNo
				                 +specProcType+provEnr+provEnrDate+reEnrInd+reEnrInddate;
		//System.out.println("providerSection::"+providerSection.length());
		return providerSection;
	}
	public String getAppDate() {
		return appDate;
	}
	public void setAppDate(String appDate) {
		this.appDate = appDate;
	}
	public String getProvType() {
		return provType;
	}
	public void setProvType(String provType) {
		this.provType = provType;
	}
	public String getPractice() {
		return practice;
	}
	public void setPractice(String practice) {
		this.practice = practice;
	}
	public String getStatCode() {
		return statCode;
	}
	public void setStatCode(String statCode) {
		this.statCode = statCode;
	}
	public String getStatEffDate() {
		return statEffDate;
	}
	public void setStatEffDate(String statEffDate) {
		this.statEffDate = statEffDate;
	}
	public String getRejectReason() {
		return rejectReason;
	}
	public void setRejectReason(String rejectReason) {
		this.rejectReason = rejectReason;
	}
	public String getChdpProvNo() {
		return chdpProvNo;
	}
	public void setChdpProvNo(String chdpProvNo) {
		this.chdpProvNo = chdpProvNo;
	}
	public String getLicNo() {
		return licNo;
	}
	public void setLicNo(String licNo) {
		this.licNo = licNo;
	}
	public String getLicEffDate() {
		return licEffDate;
	}
	public void setLicEffDate(String licEffDate) {
		this.licEffDate = licEffDate;
	}
	public String getCliaNo() {
		return cliaNo;
	}
	public void setCliaNo(String cliaNo) {
		this.cliaNo = cliaNo;
	}
	public String getSpecProcType() {
		return specProcType;
	}
	public void setSpecProcType(String specProcType) {
		this.specProcType = specProcType;
	}
	public String getProvEnr() {
		return provEnr;
	}
	public void setProvEnr(String provEnr) {
		this.provEnr = provEnr;
	}
	public String getProvEnrDate() {
		return provEnrDate;
	}
	public void setProvEnrDate(String provEnrDate) {
		this.provEnrDate = provEnrDate;
	}
	public String getReEnrInd() {
		return reEnrInd;
	}
	public void setReEnrInd(String reEnrInd) {
		this.reEnrInd = reEnrInd;
	}
	public String getReEnrInddate() {
		return reEnrInddate;
	}
	public void setReEnrInddate(String reEnrInddate) {
		this.reEnrInddate = reEnrInddate;
	}
	public ArrayList<StringBuffer> getCategoryOfServices() {
		return categoryOfServices;
	}
	public void setCategoryOfServices(ArrayList<StringBuffer> categoryOfServices) {
		this.categoryOfServices = categoryOfServices;
	}
	
	public StringBuffer getCategoryOfServicesString() {
		return categoryOfServicesString;
	}
	public void setCategoryOfServicesString(StringBuffer categoryOfServicesString) {
		this.categoryOfServicesString = categoryOfServicesString;
	}
}
