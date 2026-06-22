# MediSync
A comprehensive Hospital Management System for managing patients, doctors, and appointments.

## 1. Business Requirements
The following list outlines the core functional requirements of the MediSync system:

1. **Patient Self-Registration**: Unregistered users must be able to create a patient account with personal details (Name, Date of Birth, Gender, Contact Info) and secure credentials.
2. **Doctor Management**: Admins can add new doctors to the system and manage their profile info.
3. **Appointment Booking**: Patients must be able to search for available doctors by department and book open time slots. The system must prevent double-booking.
4. **Appointment Lifecycle**: The system must track the status of an appointment through defined stages: SCHEDULED, COMPLETED, CANCELLED, or NO_SHOW.
5. **Cancellation**: Patients and doctors can cancel appointments. A check must ensure appointments cannot be cancelled if they are already completed.
6. **Medical Records**: Upon completing an appointment, doctors must be able to generate a medical record containing a diagnosis, prescription, and treatment plan.
7. **Patient Health Profile**: The system must maintain a central profile for patients, including a list of Allergies (Medication, Food, etc.) to ensure safe treatment.
8. **Allergy Management**: The system must provide endpoints for performing CRUD actions for the Allergy entity.
9. **Doctor Schedule Management**: Admins must be able to define recurring weekly working hours (e.g., Monday 09:00-17:00) for doctors to prevent bookings outside available shifts.
10. **User Management**: Admins must have the ability to activate or deactivate doctor and patient accounts (soft delete) to manage system access without losing data.
11. **Department Management**: Admins can add, update and delete departments.
12. **Login**: The system must provide endpoints for JWT authentication.

---

## 2. MVP Features

### Feature 1: Secure Authentication & Authorization
The system utilizes Spring Security and JWTs to secure the API. Users authenticate via the `/api/auth/login` endpoint to receive a token. Role guards and custom guards (AppointmentService ownership checks) ensure that users can only access data they are explicitly authorized to see (Patient A cannot view Patient B's history).

### Feature 2: Appointment Scheduling
The scheduling functionality allows Admins to define working shifts (DoctorSchedule) for doctors. When a patient requests an appointment, the system validates that:
* The doctor is working on that day.
* The requested time falls within the doctor's start/end hours.
* The slot does not overlap with an existing booking.

### Feature 3: Doctor & Department Management
Admins can onboard new medical staff, assign them to specific departments (e.g., Cardiology, Pediatrics), and configure their appointment duration (e.g., 15 mins vs. 30 mins). This directory allows patients to filter doctors based on their specific medical needs.

### Feature 4: Clinical Record Management
Only Doctors with a SCHEDULED appointment can mark it as COMPLETED and create a MedicalRecord. This links the diagnosis and prescription directly to the specific visit, creating a permanent, queryable history for the patient.

### Feature 5: Allergy Tracking
To support clinical decision-making, the system includes a robust Allergy Catalog. Admins can define global allergens (e.g., "Penicillin", "Peanuts"), and these can be linked to specific Patient profiles. This ensures doctors have immediate visibility into patient sensitivities during appointments.

---

## 3. Entity Descriptions and Relationships

### 1. User
Represents the base authentication entity for all system actors (Admin, Doctor, Patient). Stores login credentials and roles.
**Relationships:**
* One-to-One with Patient (A User can be a Patient).
* One-to-One with Doctor (A User can be a Doctor).

### 2. Patient
Stores specific profile data for patients (medical history, contact info, physical attributes).
**Relationships:**
* One-to-One with User (Links to login credentials).
* One-to-Many with Appointment (A patient can book multiple appointments).
* Many-to-Many with Allergy (A patient can have multiple allergies, and an allergy can affect multiple patients). Join Table: `patients_allergies`.

### 3. Doctor
Stores professional details for medical staff (specialization, license info).
**Relationships:**
* One-to-One with User (Links to login credentials).
* Many-to-One with Department (A doctor belongs to one specific department).
* One-to-Many with DoctorSchedule (A doctor has multiple weekly working shifts).
* One-to-Many with Appointment (A doctor attends to many appointments).
* One-to-One with Department (A doctor can be the "Head" of a department).

### 4. Department
Represents a hospital unit (e.g., Cardiology, Pediatrics) to organize doctors.
**Relationships:**
* One-to-Many with Doctor (Contains multiple doctors).
* One-to-One with Doctor (Has one Head of Department).

### 5. DoctorSchedule
Defines the recurring working hours for a doctor (e.g., "Mondays 09:00 - 17:00").
**Relationships:**
* Many-to-One with Doctor (Belongs to a specific doctor).

### 6. Appointment
Represents a scheduled meeting between a patient and a doctor at a specific time.
**Relationships:**
* Many-to-One with Patient (Booked by a patient).
* Many-to-One with Doctor (Assigned to a doctor).
* One-to-One with Medical Record (An appointment results in exactly one medical record upon completion).

### 7. MedicalRecord
Stores the clinical outcome of a visit, including diagnosis, prescription, and treatment plan.
**Relationships:**
* One-to-One with Appointment (Linked directly to the specific visit).

### 8. Allergy
A catalog of known allergens (e.g., Peanuts, Penicillin) used to tag patient profiles.
**Relationships:**
* Many-to-Many with Patient.

<img width="3970" height="2940" alt="medisync" src="/medisync.png" />

## 4. Backend Implementation
The backend is built with Spring Boot 4 and Java 21 using a layered architecture:
* `controller` – REST endpoints and request validation.
* `service` – business rules, appointment validation, and authorization checks.
* `repository` – Spring Data JPA data access.
* `entity` – JPA entities for Patient, Doctor, Appointment, Department, Allergy, DoctorSchedule, MedicalRecord, and User.
* `dto` – data transfer objects for request/response payloads.
* `config` and `security` – JWT authentication, security filters, and OpenAPI configuration.

### Core backend features
* JWT-based authentication at `/api/auth/login`.
* Patient self-registration via `/api/patients`.
* Doctor and department administration via `/api/doctors` and `/api/departments`.
* Appointment booking, cancellation, completion, and no-show management.
* Recurring doctor schedule management to prevent bookings outside working hours.
* Medical record creation by doctors after appointment completion.
* Allergy catalog CRUD and patient allergy tracking.
* Soft activation/deactivation for patients and doctors.

### Security and roles
* `PATIENT` – book appointments, update own profile, view own appointment history.
* `DOCTOR` – view assigned appointments, complete consultations, manage medical records.
* `ADMIN` – manage doctors, departments, schedules, allergies, and activate/deactivate accounts.

## 5. Frontend Implementation

The frontend of MediSync is a modern, responsive Single Page Application (SPA) built using the latest web technologies to ensure a seamless experience for patients, doctors, and administrators. 

### Technologies Used
* **Framework**: Angular 21 (with Angular SSR for Server-Side Rendering)
* **UI Library**: Angular Material & Angular CDK for accessible and responsive UI components
* **Reactivity**: RxJS for reactive programming and state management
* **Language**: TypeScript

### User Interface Showcase

Below is a walkthrough of the MediSync user interface, highlighting the tailored experiences for different roles within the system:

#### 1. Landing Page & Authentication
* **Landing Page**: The public-facing entry point of the application, providing an overview of hospital services and a call-to-action for new patients to register or log in.
  ![Landing Page](/screenshots/landing%20page.png)
* **Login**: A secure authentication portal utilizing JWTs. Based on their credentials, the system automatically routes the user to their designated dashboard (Admin, Doctor, or Patient).
  ![Login](/screenshots/login.png)

#### 2. Admin Experience
Administrators have a bird's-eye view of the hospital's operations and full control over master data.
* **Admin Dashboard**: The central hub for administrators, offering quick links to manage different entities like departments, schedules, and staff.
  ![Admin Dashboard](/screenshots/admin%20dashboard.png)
* **Admin Analytics**: Visualizations and key metrics generated by the reporting microservice, allowing administrators to monitor hospital activity and appointment trends.
  ![Admin Analytics](/screenshots/admin%20analytics.png)
* **Patient Management**: A tabular interface for administrators to search, view, and manage all patient records, including the ability to activate or deactivate accounts.
  ![Patient Management](/screenshots/patient%20management.png)
* **Allergy Catalog Management**: An interface for maintaining the hospital's global database of allergens (food, medication, etc.) which can be assigned to patient profiles.
  ![Allergy Catalog Management](/screenshots/allergy%20catalog%20management.png)

#### 3. Doctor Experience
Doctors have a specialized workflow focused on clinical tasks and daily scheduling.
* **Doctor Dashboard**: Displays the doctor's agenda for the day, allowing them to instantly see upcoming, scheduled, and completed appointments.
  ![Doctor Dashboard](/screenshots/doctor%20dashboard.png)
* **Patient History (Doctor View)**: When reviewing a patient, doctors have a comprehensive view of past medical records, diagnoses, and known allergies to inform their treatment plan.
  ![Patient History (Doctor View)](/screenshots/patient%20history%20(doctor%20view).png)
* **Complete Appointment**: The clinical data entry screen where a doctor concludes a visit by entering a formal diagnosis, prescribing medication, and outlining a treatment plan.
  ![Complete Appointment](/screenshots/complete%20appointment.png)

#### 4. Patient Experience
Patients have a user-friendly portal designed for self-service scheduling and personal health tracking.
* **Patient Profile**: Displays the patient's personal information, contact details, and a summary of their registered allergies.
  ![Patient Profile](/screenshots/patient%20profile.png)
* **Patient History**: A secure timeline of the patient's past appointments and the corresponding medical records and prescriptions provided by their doctors.
  ![Patient History](/screenshots/patient%20history.png)
* **Book Appointment (Step 1)**: The initial step of the scheduling wizard, where patients can filter for available doctors by department or specialization.
  ![Book Appointment](/screenshots/book%20appointment.png)
* **Book Appointment (Step 2)**: The final step of the scheduling process, allowing the patient to pick an available time slot that aligns with the doctor's working schedule and input a reason for their visit.
  ![Book Appointment 2](/screenshots/book%20appointment2.png)

## 6. Backend Setup
### Prerequisites
* Java 21
* Maven (or use the included Maven wrapper)
* PostgreSQL database

### Environment configuration
The backend uses the `dev` Spring profile by default.
The active configuration file is `backend/src/main/resources/application-dev.properties`.

Required environment variables:
* `PSQL_USERNAME` – PostgreSQL user
* `PSQL_PASSWORD` – PostgreSQL password
* `SECRET_KEY` – JWT signing key

Database connection:
* `jdbc:postgresql://localhost:5432/medisync`

### Run locally
From the project root:
```bash
cd backend
# On Windows
mvnw.cmd spring-boot:run
# On macOS/Linux
./mvnw spring-boot:run
```

To build the backend artifact:
```bash
cd backend
# On Windows
mvnw.cmd clean package
# On macOS/Linux
./mvnw clean package
```

If you want to run the backend with Maven directly:
```bash
cd backend
mvn clean package
mvn spring-boot:run
```

### Notes
* `spring.jpa.hibernate.ddl-auto=update` is enabled in development mode.
* SQL initialization is allowed via `spring.sql.init.mode=always`.

## 7. API Documentation
The backend exposes OpenAPI documentation via Springdoc. After starting the backend, open one of these URLs:
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Authentication
* `POST /api/auth/login`
  * Request: `CredentialsDto` with `email` and `password`
  * Response: `AuthTokenDto` with JWT token

### Patients
* `POST /api/patients`
  * Register a new patient. Public endpoint.
* `GET /api/patients/{patientId}`
  * Get patient profile.
* `GET /api/patients`
  * Search active patients. Requires `DOCTOR` or `ADMIN`.
* `PUT /api/patients/{patientId}/`
  * Update patient profile. Requires authenticated patient owner.
* `GET /api/patients/{patientId}/appointments`
  * Get patient appointment history.
* `PUT /api/patients/{patientId}/deactivate`
  * Deactivate patient account. Requires `ADMIN`.
* `PUT /api/patients/{patientId}/activate`
  * Activate patient account. Requires `ADMIN`.

### Doctors
* `GET /api/doctors`
  * List doctors with optional department filtering.
* `GET /api/doctors/{doctorId}`
  * Get doctor profile.
* `GET /api/doctors/{doctorId}/appointments`
  * Get appointments for a doctor. Requires `DOCTOR` or `ADMIN`.
* `GET /api/doctors/{doctorId}/appointments/slots?date={yyyy-MM-dd}`
  * Get available appointment slots for a doctor.
* `POST /api/doctors`
  * Register a new doctor. Requires `ADMIN`.
* `PUT /api/doctors/{doctorId}`
  * Update doctor details. Requires `ADMIN`.
* `PUT /api/doctors/{doctorId}/deactivate`
  * Deactivate doctor. Requires `ADMIN`.
* `PUT /api/doctors/{doctorId}/activate`
  * Activate doctor account. Requires `ADMIN`.

### Appointments
* `GET /api/appointments/{appointmentId}`
  * Get appointment details.
* `POST /api/appointments`
  * Book a new appointment. Requires `PATIENT`.
* `POST /api/appointments/{appointmentId}/complete`
  * Complete an appointment and create a medical record. Requires `DOCTOR`.
* `PUT /api/appointments/{appointmentId}/cancel`
  * Cancel an appointment.
* `PUT /api/appointments/{appointmentId}/no-show`
  * Mark appointment as no-show. Requires `DOCTOR` or `ADMIN`.

### Medical Records
* `PUT /api/medical-records/{medicalRecordId}`
  * Update a medical record. Requires `DOCTOR`.
* `DELETE /api/medical-records/{medicalRecordId}`
  * Delete a medical record. Requires `DOCTOR`.

### Departments
* `GET /api/departments`
  * List all departments.
* `GET /api/departments/{id}`
  * Get department details.
* `POST /api/departments`
  * Create a department. Requires `ADMIN`.
* `PUT /api/departments/{id}`
  * Update a department. Requires `ADMIN`.
* `DELETE /api/departments/{id}`
  * Delete a department. Requires `ADMIN`.

### Allergies
* `GET /api/allergies`
  * List all allergies.
* `GET /api/allergies/{allergyId}`
  * Get allergy details.
* `POST /api/allergies`
  * Create new allergy. Requires `ADMIN`.
* `PUT /api/allergies/{allergyId}`
  * Update allergy. Requires `ADMIN`.
* `DELETE /api/allergies/{allergyId}`
  * Delete allergy. Requires `ADMIN`.

### Doctor Schedules
* `GET /doctors/{doctorId}/schedules`
  * List doctor weekly shifts.
* `GET /doctors/{doctorId}/schedules/{scheduleId}`
  * Get specific schedule item.
* `POST /doctors/{doctorId}/schedules`
  * Create a shift. Requires `ADMIN`.
* `PUT /doctors/{doctorId}/schedules/{scheduleId}`
  * Update a shift. Requires `ADMIN`.
* `DELETE /doctors/{doctorId}/schedules/{scheduleId}`
  * Delete a shift. Requires `ADMIN`.

## 8. Microservices Architecture
The application is structured as a collection of microservices to ensure scalability, resilience, and separation of concerns. They are built on Spring Boot 4 and Java 21:
* **gateway-service**: Acts as the single entry point (API Gateway). It routes incoming HTTP requests with the `/api` prefix to the appropriate backend microservices, handling cross-cutting concerns like CORS and initial request routing.
* **core-service**: The primary domain service containing the core business logic (Patients, Doctors, Appointments, and Medical Records). It interacts directly with the PostgreSQL database.
* **notification-service**: Dedicated to handling system notifications. It operates asynchronously by listening to a Redis queue, parses `NotificationRequest` payloads, and dispatches them via an `EmailService`.
* **reporting-service**: Dedicated to generating data and analytics reports. It integrates with `spring-cloud-starter-openfeign` for inter-service communication and `resilience4j` for circuit breaking and fault tolerance.

## 9. Deployment (Kubernetes & Azure)
The application is fully containerized and deployed using Kubernetes, with all configuration manifests located in the `k8s/` directory.
* **Kubernetes Resources**: The system is composed of several Deployments and ClusterIP Services, managing the frontend, microservices, databases, and monitoring tools. The current deployment topologies (number of pods/instances) are configured as follows:
  * `core-service`: 2 instances
  * `frontend`: 2 instances
  * `gateway-service`: 1 instance
  * `notification-service`: 1 instance
  * `reporting-service`: 1 instance
  * `postgres`: 1 instance
  * `redis`: 1 instance
  * `prometheus`: 1 instance
  * `grafana`: 1 instance
* **Azure Integration**: The cluster integrates with Azure Key Vault via the Secrets Store CSI Driver (`keyvault-provider.yaml`). This securely mounts sensitive credentials (like DB passwords and JWT keys) directly into the pods without hardcoding them in the configurations.
* **Ingress Routing**: External traffic is managed by an NGINX Ingress Controller (`ingress.yaml`). It routes:
  * `/api` to the `gateway-service` (Port 80)
  * `/` to the `frontend` application (Port 80)
  * `/prometheus` to the Prometheus dashboard (Port 9090)
  * `/grafana` to the Grafana dashboards (Port 3000)
* **Health Checks & Resilience**: Services implement Kubernetes `livenessProbe` checks targeting Spring Boot Actuator endpoints (`/api/actuator/health`). This ensures Kubernetes automatically restarts unresponsive containers.
* **Container Registry**: Application images are hosted on and pulled from a private Azure Container Registry (`acrmedisyncvlad2.azurecr.io`).
* **Environment Injection**: Configuration secrets are safely mapped directly to container environment variables (like `SPRING_DATASOURCE_URL`) using `secretKeyRef` which links to the secrets mounted by the Azure CSI driver.
* **Data Persistence Strategy**: To ensure data durability across pod restarts and deployments, the PostgreSQL and Redis components are configured with **Persistent Volume Claims (PVCs)**. Postgres requests a 5Gi volume mounted at `/var/lib/postgresql/data`, and Redis requests a 2Gi volume mounted at `/data`.

## 10. Infrastructure Components
### Redis (Event-Driven Pub/Sub & Caching)
Redis is deployed to facilitate asynchronous communication between microservices as well as data caching.
* **Pub/Sub (Asynchronous Events)**: The **core-service** publishes domain events to Redis channels. The **notification-service** utilizes a `RedisNotificationSubscriber` (implementing `MessageListener`) to pick up these JSON payloads and dispatch emails asynchronously, ensuring the core booking flow remains fast and unblocked.
* **Caching**: Redis is also used by the **core-service** to cache frequently accessed but rarely changing data, such as Hospital Departments (via Spring's `@Cacheable`), significantly improving read performance and reducing database load.

### Prometheus & Grafana (Monitoring & Observability)
A robust observability stack is integrated for centralized metrics scraping and monitoring (`monitoring.yaml`).
* **Metrics Exposure**: Each Spring Boot microservice is equipped with the Micrometer Prometheus registry (`micrometer-registry-prometheus`), exposing detailed JVM and application metrics via the `/actuator/prometheus` endpoint.
* **Prometheus**: A dedicated Prometheus deployment runs inside the cluster. It is configured via a `ConfigMap` to continuously scrape the endpoints of the `core-service`, `gateway-service`, `reporting-service`, and `notification-service` every 15 seconds.
* **Grafana**: A Grafana deployment is spun up alongside Prometheus to provide a visual dashboard for analyzing the scraped metrics and monitoring the overall health of the system.
