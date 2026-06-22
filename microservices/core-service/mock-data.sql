-- =========================================================================
-- MEDISYNC Mock Data Script (Perfectly Aligned to Java Enums)
-- =========================================================================

-- 1. CRITICAL: Clear out old data to prevent ID conflicts!
TRUNCATE TABLE medical_records, appointments, doctor_schedules, patients_allergies, allergies, patients, doctors, departments, users CASCADE;

-- 2. USERS (Role Enum matches ADMIN, DOCTOR, PATIENT)
INSERT INTO users (id, email, password, role, is_active, created_at, updated_at) VALUES
(1, 'admin@medisync.com', '$2a$10$8.UnVuG9HLROJEvLuYwXheT1x88B.r78xL6Xn.g2x34m3461zL.Cq', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'g.house@medisync.com', '$2a$10$8.UnVuG9HLROJEvLuYwXheT1x88B.r78xL6Xn.g2x34m3461zL.Cq', 'DOCTOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 's.strange@medisync.com', '$2a$10$8.UnVuG9HLROJEvLuYwXheT1x88B.r78xL6Xn.g2x34m3461zL.Cq', 'DOCTOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'j.doe@email.com', '$2a$10$8.UnVuG9HLROJEvLuYwXheT1x88B.r78xL6Xn.g2x34m3461zL.Cq', 'PATIENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'j.smith@email.com', '$2a$10$8.UnVuG9HLROJEvLuYwXheT1x88B.r78xL6Xn.g2x34m3461zL.Cq', 'PATIENT', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. DEPARTMENTS
INSERT INTO departments (id, name, created_at, updated_at) VALUES
(1, 'Cardiology', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Neurology', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Pediatrics', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. DOCTORS (Updated Specialization and AppointmentDuration Enums)
INSERT INTO doctors (id, user_id, department_id, first_name, last_name, specialization, appointment_duration, created_at, updated_at) VALUES
(1, 2, 1, 'Gregory', 'House', 'CARDIOLOGY', 'MINUTES_30', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 2, 'Stephen', 'Strange', 'NEUROLOGY', 'MINUTES_15', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

UPDATE departments SET head_doctor_id = 1 WHERE id = 1;
UPDATE departments SET head_doctor_id = 2 WHERE id = 2;

-- 5. PATIENTS (Assuming Gender is also a standard Enum like MALE/FEMALE)
INSERT INTO patients (id, user_id, first_name, last_name, date_of_birth, gender, phone_number, address, city, county, country, created_at, updated_at) VALUES
(1, 4, 'John', 'Doe', '1985-06-15', 'MALE', '555-0201', '123 Main St', 'Bucharest', 'Bucharest', 'Romania', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 5, 'Jane', 'Smith', '1992-11-23', 'FEMALE', '555-0202', '456 Oak Ave', 'Cluj-Napoca', 'Cluj', 'Romania', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. ALLERGIES (Updated AllergyCategory Enum)
INSERT INTO allergies (id, name, category, code, created_at, updated_at) VALUES
(1, 'Penicillin', 'MEDICATION', 'AL-PEN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Peanuts', 'FOOD', 'AL-PNT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 7. PATIENTS_ALLERGIES
INSERT INTO patients_allergies (patient_id, allergies_id) VALUES
(1, 1),
(2, 1),
(2, 2);

-- 8. DOCTOR_SCHEDULES
INSERT INTO doctor_schedules (id, doctor_id, day_of_week, start_time, end_time, created_at, updated_at) VALUES
(1, 1, 'MONDAY', '09:00:00', '17:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'TUESDAY', '09:00:00', '17:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'WEDNESDAY', '10:00:00', '14:00:00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 9. APPOINTMENTS (Updated AppointmentStatus Enum)
INSERT INTO appointments (id, doctor_id, patient_id, appointment_time, status, reason, created_at, updated_at) VALUES
(1, 1, 1, '2026-05-10 09:00:00', 'COMPLETED', 'Routine checkup for chest pain.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 2, '2026-05-11 10:30:00', 'SCHEDULED', 'Migraine consultation.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, '2026-05-12 14:00:00', 'CANCELLED', 'Patient requested cancellation.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 10. MEDICAL_RECORDS
INSERT INTO medical_records (id, appointment_id, diagnosis, prescription, treatment_plan, created_at, updated_at) VALUES
(1, 1, 'Mild hypertension and stress.', 'Lisinopril 10mg daily.', 'Monitor blood pressure, low sodium diet, follow up in 3 months.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =========================================================================
-- Reset Auto-Increment Sequences
-- =========================================================================
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));
SELECT setval('doctors_id_seq', (SELECT MAX(id) FROM doctors));
SELECT setval('patients_id_seq', (SELECT MAX(id) FROM patients));
SELECT setval('allergies_id_seq', (SELECT MAX(id) FROM allergies));
SELECT setval('doctor_schedules_id_seq', (SELECT MAX(id) FROM doctor_schedules));
SELECT setval('appointments_id_seq', (SELECT MAX(id) FROM appointments));
SELECT setval('medical_records_id_seq', (SELECT MAX(id) FROM medical_records));