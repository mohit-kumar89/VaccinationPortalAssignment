package com.wipro.covaxin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.wipro.covaxin.entity.Applicant;
import com.wipro.covaxin.exceptionhandling.DuplicateResourceException;
import com.wipro.covaxin.exceptionhandling.ResourceNotFoundException;
import com.wipro.covaxin.repository.AadharDetailsRepository;
import com.wipro.covaxin.repository.ApplicantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ApplicantServiceImpl implements ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepo;

    @Autowired
    private AadharDetailsRepository aadharDetailsRepo;

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Applicant> getAllApplicants() throws ResourceNotFoundException {
        if(applicantRepo.findAll().size()==0){
            throw new ResourceNotFoundException("No applicants registered for vaccination");
        }
        return applicantRepo.findAll();
    }

    @Override
    public Applicant getApplicantById(long id) throws ResourceNotFoundException {
        if(!applicantRepo.existsById(id)){
            throw new ResourceNotFoundException("Applicant with id "+id+" is not registered");
        }
        return applicantRepo.findById(id).get();
    }

    @Override
    public ResponseEntity<Object> writeToFileSystemOnDesktop() throws IOException {
        String Filename = "CovaxinApplicantsMO20170519"+".json";
        String targetDirPath = System.getProperty("user.home")+ File.separator+"Desktop"+File.separator+Filename;
        targetDirPath=targetDirPath.replaceAll("\\s+","");
        File outputFile = new File(targetDirPath);
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(outputFile, applicantRepo.findAll());
        return ResponseEntity.accepted().body("All registered applicants' details are written to a file on Desktop named CovaxinApplicantsMO20170519");
    }

    @Override
    public ResponseEntity<Object> createApplicant(Applicant newApplicant) throws DuplicateResourceException {
        if(applicantRepo.existsById(newApplicant.getApplicantId())){
            throw new DuplicateResourceException("Applicant with Id "+newApplicant.getApplicantId()+" already registered");
        }
        if(aadharDetailsRepo.existsById(newApplicant.getAadharDetails().getAadharId())){
            throw new DuplicateResourceException("Applicant with Aadhar Id "+newApplicant.getAadharDetails().getAadharId()+" already registered");
        }
        Date advanceDate = newApplicant.getAadharDetails().getDob();
        Calendar thisDateCalender = Calendar.getInstance();
        thisDateCalender.setTime(advanceDate);
        thisDateCalender.add(Calendar.YEAR,45);
        advanceDate=thisDateCalender.getTime();
        Date currentdate = new Date();
        if((currentdate.compareTo(advanceDate)>0)){
            if(newApplicant.getVaccinationTimeSlot().compareTo(currentdate)<0){
                return ResponseEntity.accepted().body("Applicant not registered, vaccination time slot is past current date");
            }
            applicantRepo.save(newApplicant);
            aadharDetailsRepo.save(newApplicant.getAadharDetails());
            return new ResponseEntity<>("Applicant registered", HttpStatus.CREATED);
        }else{
            return ResponseEntity.accepted().body("Applicant not registered, age is less than 45");
        }
    }

    @Override
    public ResponseEntity<Object> updateApplicant(Applicant applicant, long id) throws ResourceNotFoundException {
        if(!applicantRepo.existsById(id)){
            throw new ResourceNotFoundException("Applicant with id "+id+" is not registered");
        }
        Applicant thisApplicant = applicantRepo.findById(id).get();
        if(applicant.getAadharDetails()!=null){
            return ResponseEntity.accepted().body("Update unsuccessful, aadhar details can not be updated");
        }
        applicant.setApplicantId(thisApplicant.getApplicantId());
        applicant.setAadharDetails(thisApplicant.getAadharDetails());
        applicantRepo.save(applicant);
        return ResponseEntity.accepted().body("Update Successful");
    }

    @Override
    public ResponseEntity<Object> removeApplicant(long id) throws ResourceNotFoundException {
        if(!applicantRepo.existsById(id)){
            throw new ResourceNotFoundException("Applicant with id "+id+" does not exist, so can not be deleted");
        }
        aadharDetailsRepo.deleteById(applicantRepo.findById(id).get().getAadharDetails().getAadharId());
        applicantRepo.deleteById(id);

        return ResponseEntity.accepted().body("Delete Successful");
    }

    @Override
    public ResponseEntity<Object> createApplicantFromJsonFile(MultipartFile file) throws IOException, DuplicateResourceException {
        BufferedReader br;
        String result = "";
        String line;
        InputStream is = file.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            result+=line;
        }
        result=result.replaceAll("\\s+","");
        List<Applicant> applicants = new ArrayList<Applicant>();
        if(result.charAt(0)=='['){
            applicants = mapper.readValue(result, new TypeReference<List<Applicant>>() {});
        }
        if (result.charAt(0)=='{'){
            applicants.add(mapper.readValue(result, Applicant.class));
        }
        int success =0;
        int failAlreadyExist=0;
        int failAgeless=0;
        int failPastSlot=0;
        for (Applicant newApplicant : applicants) {
            if (applicantRepo.existsById(newApplicant.getApplicantId()) || aadharDetailsRepo.existsById(newApplicant.getAadharDetails().getAadharId())) {
                failAlreadyExist++;
            } else {
                Date advanceDate = newApplicant.getAadharDetails().getDob();
                Calendar thisDateCalender = Calendar.getInstance();
                thisDateCalender.setTime(advanceDate);
                thisDateCalender.add(Calendar.YEAR, 45);
                advanceDate = thisDateCalender.getTime();
                Date currentdate = new Date();
                if ((currentdate.compareTo(advanceDate) > 0)) {
                    if (newApplicant.getVaccinationTimeSlot().compareTo(currentdate) < 0) {
                        failPastSlot++;
                    } else {
                        applicantRepo.save(newApplicant);
                        aadharDetailsRepo.save(newApplicant.getAadharDetails());
                        success++;
                    }
                } else {
                    failAgeless++;
                }
            }
        }
        if(failAgeless==0&&failAlreadyExist==0&&failPastSlot==0){
            return new ResponseEntity<>(applicants.size()+ " applicant/(s) registered successfully", HttpStatus.CREATED);
        }else{
            return ResponseEntity.accepted().body("Total applicants sent for registration: "+ applicants.size()+System.lineSeparator()+"Successfully registered: "+success+System.lineSeparator()+"Failed registrations due to duplicate applicant details: "+ failAlreadyExist+System.lineSeparator()+"Failed registrations due to age less than 45: "+failAgeless+System.lineSeparator()+"Failed registrations due to improper vaccination time slot: "+failPastSlot);
        }
    }
}
