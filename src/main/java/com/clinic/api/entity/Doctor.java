package com.clinic.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor")
public class Doctor implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Long doctorId;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 80, message = "Full name must be 2-80 characters")
    @Column(name = "full_name", nullable = false, length = 80)
    private String fullName;

    @NotBlank(message = "Specialization is required")
    @Size(min = 2, max = 80, message = "Specialization must be 2-80 characters")
    @Column(name = "specialization", nullable = false, length = 80)
    private String specialization;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+250|0)7[0-9]{8}$", message = "Enter a valid Rwandan phone number, e.g. 07XXXXXXXX")
    @Column(name = "phone", nullable = false, length = 20, unique = true)
    private String phone;

    @NotNull(message = "Availability start hour is required")
    @Min(value = 0, message = "Availability start hour must be between 0 and 23")
    @Max(value = 23, message = "Availability start hour must be between 0 and 23")
    @Column(name = "availability_start_hour", nullable = false)
    private Integer availabilityStartHour;

    @NotNull(message = "Availability end hour is required")
    @Min(value = 0, message = "Availability end hour must be between 0 and 23")
    @Max(value = 23, message = "Availability end hour must be between 0 and 23")
    @Column(name = "availability_end_hour", nullable = false)
    private Integer availabilityEndHour;

    @JsonIgnore
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    public Doctor() {}

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAvailabilityStartHour() { return availabilityStartHour; }
    public void setAvailabilityStartHour(Integer availabilityStartHour) { this.availabilityStartHour = availabilityStartHour; }

    public Integer getAvailabilityEndHour() { return availabilityEndHour; }
    public void setAvailabilityEndHour(Integer availabilityEndHour) { this.availabilityEndHour = availabilityEndHour; }

    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}
