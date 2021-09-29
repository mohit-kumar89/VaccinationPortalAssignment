package com.wipro.covaxin.controller;

import com.wipro.covaxin.entity.Applicant;
import com.wipro.covaxin.exceptionhandling.DuplicateResourceException;
import com.wipro.covaxin.exceptionhandling.ResourceNotFoundException;
import com.wipro.covaxin.service.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/")
public class RestController {
    @Autowired
    private ApplicantService service;

//    In this application applicants can be registered for vaccination, view all registered applications, view an application by id, delete an application, update details (exluding aadhar details), create applicant/(s) from json file and write registered applicant's details to desktop
//    For registrations several conditions have to be fulfilled: age must be more than 45 years, vaccination time slot can not be past today, aadhar id has to be unique, while updating aadhar details can not be updated.
//    The postman collection, single applicant json and multiple applicant json files are in the data folder in resources.

    @GetMapping("/applicants")
    private List<Applicant> getAllApplicants() throws ResourceNotFoundException {
        return service.getAllApplicants();
    }

    @GetMapping("/applicant/{id}")
    private Applicant getApplicantById(@PathVariable long id) throws ResourceNotFoundException {
        return service.getApplicantById(id);
    }

    @GetMapping("/writeToFileSystem")
    private ResponseEntity<Object> writeToFileSystemOnDesktop() throws IOException {
        return service.writeToFileSystemOnDesktop();
    }

    @PostMapping("/applicants")
    private ResponseEntity<Object> createApplicant(@RequestBody Applicant newApplicant) throws DuplicateResourceException {
        return service.createApplicant(newApplicant);
    }

    @PostMapping("/upload")
    private ResponseEntity<Object> createApplicantFromJsonFile(@RequestParam("file") MultipartFile file) throws IOException, DuplicateResourceException {
        return service.createApplicantFromJsonFile(file);
    }

    @PutMapping("/updateApplicant/{id}")
    private ResponseEntity<Object> updateApplicant(@RequestBody Applicant applicant, @PathVariable long id) throws ResourceNotFoundException {
        return service.updateApplicant(applicant,id);
    }

    @DeleteMapping("/removeApplicant/{id}")
    private ResponseEntity<Object> removeApplicant(@PathVariable long id) throws ResourceNotFoundException {
        return service.removeApplicant(id);
    }
}
