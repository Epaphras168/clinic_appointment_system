package com.clinic.api.config;

import com.clinic.api.entity.Doctor;
import com.clinic.api.entity.Patient;
import com.clinic.api.repository.DoctorRepository;
import com.clinic.api.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public DataSeeder(PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        if (doctorRepository.count() == 0) {
            Doctor d1 = new Doctor();
            d1.setFullName("Dr. Alice Uwimana");
            d1.setSpecialization("Cardiology");
            d1.setPhone("0788123456");
            d1.setAvailabilityStartHour(8);
            d1.setAvailabilityEndHour(16);
            doctorRepository.save(d1);

            Doctor d2 = new Doctor();
            d2.setFullName("Dr. Jean Mugisha");
            d2.setSpecialization("Pediatrics");
            d2.setPhone("0788654321");
            d2.setAvailabilityStartHour(9);
            d2.setAvailabilityEndHour(17);
            doctorRepository.save(d2);
        }

        if (patientRepository.count() == 0) {
            Patient p1 = new Patient();
            p1.setFirstName("Eric");
            p1.setLastName("Niyonzima");
            p1.setDateOfBirth(LocalDate.of(1995, 3, 12));
            p1.setGender("Male");
            p1.setPhone("0722334455");
            p1.setEmail("eric@example.com");
            p1.setAddress("Kigali, Rwanda");
            patientRepository.save(p1);

            Patient p2 = new Patient();
            p2.setFirstName("Grace");
            p2.setLastName("Mukamana");
            p2.setDateOfBirth(LocalDate.of(1990, 7, 22));
            p2.setGender("Female");
            p2.setPhone("0733445566");
            p2.setEmail("grace@example.com");
            p2.setAddress("Musanze, Rwanda");
            patientRepository.save(p2);
        }
    }
}
