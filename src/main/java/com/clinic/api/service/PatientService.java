package com.clinic.api.service;

import com.clinic.api.entity.Patient;
import com.clinic.api.exception.BusinessRuleException;
import com.clinic.api.exception.ResourceNotFoundException;
import com.clinic.api.repository.AppointmentRepository;
import com.clinic.api.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    @Transactional
    public Patient create(Patient patient) {
        assertPhoneUnique(patient.getPhone(), null);
        patient.setPatientId(null);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient update(Long id, Patient patient) {
        Patient existing = findById(id);
        assertPhoneUnique(patient.getPhone(), id);
        existing.setFirstName(patient.getFirstName());
        existing.setLastName(patient.getLastName());
        existing.setDateOfBirth(patient.getDateOfBirth());
        existing.setGender(patient.getGender());
        existing.setPhone(patient.getPhone());
        existing.setEmail(patient.getEmail());
        existing.setAddress(patient.getAddress());
        return patientRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        if (appointmentRepository.existsByPatient_PatientIdAndStatus(id, "Scheduled")) {
            throw new BusinessRuleException("Cannot delete patient with active (Scheduled) appointments");
        }
        patientRepository.deleteById(id);
    }

    private void assertPhoneUnique(String phone, Long excludingId) {
        Optional<Patient> existing = patientRepository.findByPhone(phone);
        if (existing.isPresent() && !existing.get().getPatientId().equals(excludingId)) {
            throw new BusinessRuleException("Phone number already in use by another patient: " + phone);
        }
    }
}
