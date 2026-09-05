package com.clinic.bean;

import com.clinic.dao.AppointmentDAO;
import com.clinic.dao.PatientDAO;
import com.clinic.entity.Appointment;
import com.clinic.entity.Patient;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@ManagedBean(name = "appointmentBean")
@SessionScoped
public class AppointmentBean implements Serializable {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    private Appointment appointment = new Appointment();
    private Long selectedPatientId;

    public List<Appointment> getAppointments() {
        return appointmentDAO.findAll();
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    public Appointment getAppointment() { return appointment; }
    public void setAppointment(Appointment appointment) { this.appointment = appointment; }

    public Long getSelectedPatientId() { return selectedPatientId; }
    public void setSelectedPatientId(Long selectedPatientId) { this.selectedPatientId = selectedPatientId; }

    /** business hours only (08:00 - 17:00) - validation type #2 */
    public void validateAppointmentTime(FacesContext context, UIComponent component, Object value) {
        LocalTime time = (LocalTime) value;
        if (time == null) return;
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 0))) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Appointments can only be booked between 08:00 and 17:00", null);
            throw new ValidatorException(msg);
        }
    }

    public String save() {
        try {
            if (selectedPatientId != null) {
                appointment.setPatient(patientDAO.findById(selectedPatientId));
            }
            if (appointment.getPatient() == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Please select a patient");
                return null;
            }
            if (appointment.getAppointmentId() == null) {
                appointmentDAO.save(appointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Appointment booked successfully");
            } else {
                appointmentDAO.update(appointment);
                addMessage(FacesMessage.SEVERITY_INFO, "Appointment updated successfully");
            }
            appointment = new Appointment();
            selectedPatientId = null;
            return "list?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not save appointment: " + e.getMessage());
            return null;
        }
    }

    public String edit(Appointment a) {
        this.appointment = a;
        this.selectedPatientId = a.getPatient() != null ? a.getPatient().getPatientId() : null;
        return "form?faces-redirect=true";
    }

    public String delete(Appointment a) {
        appointmentDAO.delete(a.getAppointmentId());
        addMessage(FacesMessage.SEVERITY_INFO, "Appointment deleted");
        return "list?faces-redirect=true";
    }

    public String newAppointment() {
        this.appointment = new Appointment();
        this.selectedPatientId = null;
        return "form?faces-redirect=true";
    }

    private void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null));
    }
}
