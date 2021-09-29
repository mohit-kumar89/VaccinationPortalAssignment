package com.wipro.covaxin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="applicants")
public class Applicant {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="applicant_id",nullable = false,updatable = false)
    private long applicantId;
    @Column(name="applicant_name")
    private String applicantName;
    @Column(name="vaccination_time_slot")
    private Date vaccinationTimeSlot;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "aadhar_details")
    private AadharDetails aadharDetails;

    public Applicant() {
    }

    public Applicant(String applicantName, Date vaccinationTimeSlot, AadharDetails aadharDetails) {
        this.applicantName = applicantName;
        this.vaccinationTimeSlot = vaccinationTimeSlot;
        this.aadharDetails = aadharDetails;
    }

    public long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Date getVaccinationTimeSlot() {
        return vaccinationTimeSlot;
    }

    public void setVaccinationTimeSlot(Date vaccinationTimeSlot) {
        this.vaccinationTimeSlot = vaccinationTimeSlot;
    }

    public AadharDetails getAadharDetails() {
        return aadharDetails;
    }

    public void setAadharDetails(AadharDetails aadharDetails) {
        this.aadharDetails = aadharDetails;
    }

    @Override
    public String toString() {
        return "{" +
                "applicantId=" + applicantId +
                ", applicantName='" + applicantName + '\'' +
                ", vaccinationTimeSlot=" + vaccinationTimeSlot +
                ", aadharDetails=" + aadharDetails +
                '}';
    }
}