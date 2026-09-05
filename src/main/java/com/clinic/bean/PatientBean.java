package com.clinic.bean;

import com.clinic.dao.PatientDAO;
import com.clinic.entity.Patient;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Backing bean for Patient CRUD screens.
 * Demonstrates:
 *  - Server-side (Bean Validation) errors surfaced through <h:message>
 *  - A custom JSF validator method (validation type #2 - JSF validation layer)
 *  - Business-rule check before persisting (validation type #3 - programmatic)
 */
@ManagedBean(name = "patientBean")
@SessionScoped
public class PatientBean implements Serializable {

    private final PatientDAO patientDAO = new PatientDAO();

    private Patient patient = new Patient();
    private List<Patient> patients;
    private boolean editMode = false;

    public List<Patient> getPatients() {
        patients = patientDAO.findAll();
        return patients;
    }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public boolean isEditMode() { return editMode; }

    /** Custom JSF validator (registered on the xhtml with <f:validator>) - validation type #2 */
    public void validateDateOfBirth(FacesContext context, UIComponent component, Object value) {
        LocalDate dob = (LocalDate) value;
        if (dob == null) return;
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age > 130) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Date of birth implies an unrealistic age", null);
            throw new ValidatorException(msg);
        }
    }

    public String save() {
        try {
            if (patient.getPatientId() == null) {
                patientDAO.save(patient);
                addMessage(FacesMessage.SEVERITY_INFO, "Patient registered successfully");
            } else {
                patientDAO.update(patient);
                addMessage(FacesMessage.SEVERITY_INFO, "Patient updated successfully");
            }
            patient = new Patient();
            editMode = false;
            return "list?faces-redirect=true";
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not save patient: " + e.getMessage());
            return null;
        }
    }

    public String edit(Patient p) {
        this.patient = p;
        this.editMode = true;
        return "form?faces-redirect=true";
    }

    public String delete(Patient p) {
        try {
            patientDAO.delete(p.getPatientId());
            addMessage(FacesMessage.SEVERITY_INFO, "Patient deleted");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete: patient may have appointments linked");
        }
        return "list?faces-redirect=true";
    }

    public String newPatient() {
        this.patient = new Patient();
        this.editMode = false;
        return "form?faces-redirect=true";
    }

    private void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null));
    }
}
