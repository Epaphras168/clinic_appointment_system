package com.clinic.api.service;

import com.clinic.api.entity.Doctor;
import com.clinic.api.exception.BusinessRuleException;
import com.clinic.api.exception.ResourceNotFoundException;
import com.clinic.api.repository.AppointmentRepository;
import com.clinic.api.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional
    public Doctor create(Doctor doctor) {
        assertAvailabilityValid(doctor);
        assertPhoneUnique(doctor.getPhone(), null);
        doctor.setDoctorId(null);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor update(Long id, Doctor doctor) {
        Doctor existing = findById(id);
        assertAvailabilityValid(doctor);
        assertPhoneUnique(doctor.getPhone(), id);
        existing.setFullName(doctor.getFullName());
        existing.setSpecialization(doctor.getSpecialization());
        existing.setPhone(doctor.getPhone());
        existing.setAvailabilityStartHour(doctor.getAvailabilityStartHour());
        existing.setAvailabilityEndHour(doctor.getAvailabilityEndHour());
        return doctorRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        if (appointmentRepository.existsByDoctor_DoctorIdAndStatus(id, "Scheduled")) {
            throw new BusinessRuleException("Cannot delete doctor with active (Scheduled) appointments");
        }
        doctorRepository.deleteById(id);
    }

    private void assertAvailabilityValid(Doctor doctor) {
        if (doctor.getAvailabilityStartHour() != null && doctor.getAvailabilityEndHour() != null
                && doctor.getAvailabilityStartHour() >= doctor.getAvailabilityEndHour()) {
            throw new BusinessRuleException("Availability start hour must be before end hour");
        }
    }

    private void assertPhoneUnique(String phone, Long excludingId) {
        Optional<Doctor> existing = doctorRepository.findByPhone(phone);
        if (existing.isPresent() && !existing.get().getDoctorId().equals(excludingId)) {
            throw new BusinessRuleException("Phone number already in use by another doctor: " + phone);
        }
    }
}
