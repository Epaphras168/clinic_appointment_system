package com.clinic.api.repository;

import com.clinic.api.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatient_PatientId(Long patientId);

    List<Appointment> findByDoctor_DoctorId(Long doctorId);

    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateAndAppointmentTimeAndStatus(
            Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime, String status);

    boolean existsByPatient_PatientIdAndStatus(Long patientId, String status);

    boolean existsByDoctor_DoctorIdAndStatus(Long doctorId, String status);
}
