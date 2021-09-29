package com.wipro.covaxin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "aadhar_details")
public class AadharDetails {
    @Id
    @Column(name = "aadhar_id")
    private long aadharId;
    @Column(name = "name")
    private String name;
    @Column(name = "dob")
    private Date dob;
    @Column(name = "gender")
    private String gender;

    public AadharDetails() {
    }

    public AadharDetails(long aadharId, String name, Date dob, String gender) {
        this.aadharId = aadharId;
        this.name = name;
        this.dob = dob;
        this.gender = gender;
    }

    public long getAadharId() {
        return aadharId;
    }

    public void setAadharId(long aadharId) {
        this.aadharId = aadharId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "{" +
                "aadharId=" + aadharId +
                ", name='" + name + '\'' +
                ", dob=" + dob +
                ", gender='" + gender + '\'' +
                '}';
    }
}