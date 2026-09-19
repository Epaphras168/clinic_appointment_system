package com.clinic.api.service;

import com.clinic.api.dto.AppointmentRequest;
import com.clinic.api.entity.Appointment;
import com.clinic.api.entity.Doctor;
import com.clinic.api.entity.Patient;
import com.clinic.api.exception.BusinessRuleException;
import com.clinic.api.exception.ResourceNotFoundException;
import com.clinic.api.repository.AppointmentRepository;
import com.clinic.api.repository.DoctorRepository;
import com.clinic.api.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    @Transactional
    public Appointment create(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(request.getStatus() == null ? "Scheduled" : request.getStatus());

        validateBusinessRules(appointment, null);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment update(Long id, AppointmentRequest request) {
        Appointment existing = findById(id);

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        existing.setPatient(patient);
        existing.setDoctor(doctor);
        existing.setAppointmentDate(request.getAppointmentDate());
        existing.setAppointmentTime(request.getAppointmentTime());
        existing.setReason(request.getReason());
        existing.setStatus(request.getStatus() == null ? existing.getStatus() : request.getStatus());

        validateBusinessRules(existing, id);
        return appointmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        findById(id);
        appointmentRepository.deleteById(id);
    }

    private void validateBusinessRules(Appointment appointment, Long excludingId) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime());
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Appointment cannot be in the past");
        }

        Doctor doctor = appointment.getDoctor();
        int hour = appointment.getAppointmentTime().getHour();
        if (hour < doctor.getAvailabilityStartHour() || hour >= doctor.getAvailabilityEndHour()) {
            throw new BusinessRuleException(String.format(
                    "Appointment time %s is outside doctor's availability window (%02d:00-%02d:00)",
                    appointment.getAppointmentTime(), doctor.getAvailabilityStartHour(), doctor.getAvailabilityEndHour()));
        }

        if ("Scheduled".equals(appointment.getStatus())) {
            List<Appointment> conflicts = appointmentRepository
                    .findByDoctor_DoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
                            doctor.getDoctorId(), appointment.getAppointmentDate(),
                            appointment.getAppointmentTime(), "Scheduled");
            boolean hasConflict = conflicts.stream()
                    .anyMatch(a -> excludingId == null || !a.getAppointmentId().equals(excludingId));
            if (hasConflict) {
                throw new BusinessRuleException("Doctor already has a scheduled appointment at this date and time");
            }
        }
    }
}
