package com.wipro.covaxin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wipro.covaxin.entity.AadharDetails;
import com.wipro.covaxin.entity.Applicant;
import com.wipro.covaxin.exceptionhandling.DuplicateResourceException;
import com.wipro.covaxin.exceptionhandling.ResourceNotFoundException;
import com.wipro.covaxin.repository.AadharDetailsRepository;
import com.wipro.covaxin.repository.ApplicantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicantServiceImplTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ApplicantRepository applicantRepo;

    @Mock
    private AadharDetailsRepository aadharDetailsRepo;

    @InjectMocks
    private ApplicantServiceImpl service;

    static Applicant testApplicant;
    static AadharDetails testApplicantAadharDetails;

    @BeforeAll
    static void setVariables() {
        testApplicantAadharDetails = new AadharDetails(1234, "John", Date.from(OffsetDateTime.parse("1961-06-03T13:28:06.419Z").toInstant()),"Male");
        testApplicant = new Applicant("John", Date.from(OffsetDateTime.parse("2021-12-20T13:28:06.419Z").toInstant()), testApplicantAadharDetails);
    }

    @Test
    void getAllApplicantsSuccess() throws ResourceNotFoundException {
        List<Applicant> resultList = new ArrayList<>();
        resultList.add(testApplicant);
        Mockito.when(applicantRepo.findAll()).thenReturn(resultList);
        assertEquals(service.getAllApplicants(),resultList);
        verify(applicantRepo, times(2)).findAll();
    }

    @Test
    void getAllApplicantsException() {
        List<Applicant> resultList = new ArrayList<>();
        Mockito.when(applicantRepo.findAll()).thenReturn(resultList);
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class, service::getAllApplicants);
        assertThat(e).hasMessage("No applicants registered for vaccination");
        verify(applicantRepo, times(1)).findAll();
    }

    @Test
    void getApplicantById() throws ResourceNotFoundException {
        Long id = Long.valueOf("1");
        Mockito.when(applicantRepo.existsById(id)).thenReturn(true);
        testApplicant.setApplicantId(id);
        Mockito.when(applicantRepo.findById(id)).thenReturn(Optional.ofNullable(testApplicant));
        assertEquals(testApplicant, service.getApplicantById(id));
        verify(applicantRepo, times(1)).findById(id);
        verify(applicantRepo, times(1)).existsById(id);
    }

    @Test
    void getApplicantByIdException() {
        Long id = Long.valueOf("1");
        Mockito.when(applicantRepo.existsById(id)).thenReturn(false);
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> service.getApplicantById(id));
        assertThat(e).hasMessage("Applicant with id 1 is not registered");
        verify(applicantRepo, times(1)).existsById(id);
    }

    @Test
    void createApplicant() throws DuplicateResourceException {
        Mockito.when(aadharDetailsRepo.existsById(Mockito.anyLong())).thenReturn(false);
        ResponseEntity<Object> expected = new ResponseEntity<>("Applicant registered", HttpStatus.CREATED);
        assertEquals(expected, service.createApplicant(testApplicant));
    }

    @Test
    void createApplicantDuplicateResourceException() {
        Mockito.when(aadharDetailsRepo.existsById(Mockito.anyLong())).thenReturn(true);
        DuplicateResourceException e =assertThrows(DuplicateResourceException.class, () -> service.createApplicant(testApplicant));
        assertThat(e).hasMessage("Applicant with Aadhar Id 1234 already registered");
        verify(aadharDetailsRepo, times(1)).existsById(Mockito.anyLong());
    }

    @Test
    void createApplicantAgeLessThan45() throws DuplicateResourceException {
        Mockito.when(aadharDetailsRepo.existsById(Mockito.anyLong())).thenReturn(false);
        testApplicantAadharDetails.setDob(Date.from(OffsetDateTime.parse("1999-06-03T13:28:06.419Z").toInstant()));
        testApplicant.setAadharDetails(testApplicantAadharDetails);
        ResponseEntity<Object> expected = ResponseEntity.accepted().body("Applicant not registered, age is less than 45");
        assertEquals(expected, service.createApplicant(testApplicant));
    }

    @Test
    void createApplicantPastVaccinationTimeSlot() throws DuplicateResourceException {
        Mockito.when(aadharDetailsRepo.existsById(Mockito.anyLong())).thenReturn(false);
        testApplicantAadharDetails.setDob(Date.from(OffsetDateTime.parse("1961-06-03T13:28:06.419Z").toInstant()));
        testApplicant.setAadharDetails(testApplicantAadharDetails);
        testApplicant.setVaccinationTimeSlot(Date.from(OffsetDateTime.parse("2020-06-20T13:28:06.419Z").toInstant()));
        ResponseEntity<Object> expected = ResponseEntity.accepted().body("Applicant not registered, vaccination time slot is past current date");
        assertEquals(expected, service.createApplicant(testApplicant));
    }

    @Test
    void updateApplicant() throws ResourceNotFoundException {
        Long id = Long.valueOf("1");
        Applicant temp = new Applicant("John Doe", Date.from(OffsetDateTime.parse("2020-06-20T13:28:06.419Z").toInstant()), null);
        Mockito.when(applicantRepo.existsById(Mockito.anyLong())).thenReturn(true);
        Mockito.when(applicantRepo.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(testApplicant));
        ResponseEntity<Object> expected = ResponseEntity.ok().body("Update Successful");
        assertEquals(expected, service.updateApplicant(temp, id));
    }

    @Test
    void updateApplicantResourceNotFound() {
        Long id = Long.valueOf("1");
        Applicant temp = new Applicant("John Doe", Date.from(OffsetDateTime.parse("2020-06-20T13:28:06.419Z").toInstant()), null);
        Mockito.when(applicantRepo.existsById(Mockito.anyLong())).thenReturn(false);
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> service.updateApplicant(temp, id));
        assertThat(e).hasMessage("Applicant with id "+id+" is not registered");
        verify(applicantRepo, times(1)).existsById(Mockito.anyLong());
    }

    @Test
    void updateApplicantAadharDetailsUpdation() throws ResourceNotFoundException {
        Long id = Long.valueOf("1");
        Applicant temp = new Applicant("John Doe", Date.from(OffsetDateTime.parse("2020-06-20T13:28:06.419Z").toInstant()), null);
        Mockito.when(applicantRepo.existsById(Mockito.anyLong())).thenReturn(true);
        temp.setAadharDetails(testApplicantAadharDetails);
        Mockito.when(applicantRepo.findById(Mockito.anyLong())).thenReturn(Optional.of(temp));
        ResponseEntity<Object> expected = ResponseEntity.accepted().body("Update unsuccessful, aadhar details can not be updated");
        assertEquals(expected, service.updateApplicant(temp, id));
    }

    @Test
    void removeApplicant() throws ResourceNotFoundException {
        Mockito.when(applicantRepo.existsById(Mockito.anyLong())).thenReturn(true);
        Mockito.when(applicantRepo.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(testApplicant));
        ResponseEntity<Object> expected = ResponseEntity.ok().body("Delete Successful");
        assertEquals(expected, service.removeApplicant(Mockito.anyLong()));
    }

    @Test
    void removeApplicantException() {
        Mockito.when(applicantRepo.existsById(Mockito.anyLong())).thenReturn(false);
        ResourceNotFoundException e = assertThrows(ResourceNotFoundException.class,
                () -> service.removeApplicant(1));
        assertThat(e).hasMessage("Applicant with id 1 does not exist, so can not be deleted");
        verify(applicantRepo, times(1)).existsById(Mockito.anyLong());
    }
}