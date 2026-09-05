# Clinic Patient & Appointment System — JSF + Hibernate CRUD Module

Practical implementation for Phase-1, Part 2: full CRUD on **Patient** and
**Appointment** entities using JavaServer Faces (Mojarra) and Hibernate ORM,
with three types of validation and three types of CSS applied.

## Tech Stack
- Java 11, Maven, WAR packaging
- JSF 2.3 (Mojarra)
- Hibernate ORM 5.6 (JPA annotations)
- PostgreSQL
- Deploy target: Apache Tomcat 9 (or any Servlet 4.0 container) / GlassFish / WildFly

## Setup
1. Create a PostgreSQL database: `createdb clinic_db`
2. Edit `src/main/resources/hibernate.cfg.xml` with your DB username/password.
3. Build: `mvn clean package`
4. Deploy the generated `target/clinic-app.war` to Tomcat's `webapps/` folder.
5. Visit `http://localhost:8080/clinic-app/`

Hibernate is configured with `hbm2ddl.auto=update`, so tables are created
automatically on first run — no manual SQL needed.

## Entities implemented with full CRUD
- **Patient** — register, list, update, delete
- **Appointment** — book, list, update, delete (linked to a Patient)

## Validation types applied (as required by the assignment)
1. **Server-side / Bean Validation** — `@NotBlank`, `@Pattern`, `@Past`,
   `@FutureOrPresent`, `@Size`, `@Email` annotations on the entity classes
   (`Patient.java`, `Appointment.java`), enforced by Hibernate Validator.
2. **JSF built-in & custom validation** — `required="true"`,
   `<f:validateLength>`, `<f:validateRegex>`, and custom validator methods
   (`validateDateOfBirth`, `validateAppointmentTime`) bound directly in the
   managed beans and referenced from the `.xhtml` forms.
3. **Client-side JavaScript validation** — `onsubmit` handlers in
   `patients/form.xhtml` and `appointments/form.xhtml` that check phone
   format and business hours before the form is even submitted.

## CSS types applied
1. **External CSS** — `src/main/webapp/resources/css/style.css`, linked via
   `<h:outputStylesheet library="css" name="style.css"/>` on every page.
2. **Internal CSS** — `<style>` blocks inside `<h:head>` on `index.xhtml`,
   `patients/form.xhtml`, `appointments/list.xhtml`, `appointments/form.xhtml`.
3. **Inline CSS** — `style="..."` attributes directly on JSF components,
   e.g. the form containers and the status badges in `appointments/list.xhtml`.

## Project structure
```
clinic-app/
├── pom.xml
├── src/main/java/com/clinic/
│   ├── entity/        Patient.java, Appointment.java
│   ├── dao/            PatientDAO.java, AppointmentDAO.java
│   ├── bean/           PatientBean.java, AppointmentBean.java
│   └── util/           HibernateUtil.java
├── src/main/resources/hibernate.cfg.xml
└── src/main/webapp/
    ├── index.xhtml
    ├── resources/css/style.css
    ├── patients/list.xhtml, form.xhtml
    ├── appointments/list.xhtml, form.xhtml
    └── WEB-INF/web.xml, faces-config.xml
```
