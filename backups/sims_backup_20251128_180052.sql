-- SIMS Database Backup
-- Generated on: Fri Nov 28 18:00:53 EET 2025
-- Backup interval: Every 15 minutes

-- Backup of users table
DELETE FROM users;

INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('1', 'admin', 'admin1234', 'admin', 'System Administrator', 'admin@university.edu', '2025-11-21 12:29:00.543', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('2', 'a001', 'password123', 'student', 'Joy AlAhel', 'joyelahel@gmail.com', '2025-11-22 11:50:01.45', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('3', 'a002', 'password123', 'student', 'Maria Makary', 'mariamakary@gmail.com', '2025-11-22 11:55:57.28', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('4', 'i001', 'password123', 'instructor', 'Mariam Harfouch', 'mariamharfouch@gmail.com', '2025-11-22 14:28:29.327', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('5', 'a003', 'password123', 'student', 'Joe Rohban', 'joerohban@gmail.com', '2025-11-22 15:12:52.137', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('6', 'i002', 'password123', 'instructor', 'Georges Abdallah', 'abdallahgeorges@gmail.com', '2025-11-22 15:13:31.96', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('7', 'a004', 'password123', 'student', 'Tonio Jreige', 'toniojreige@gmail.com', '2025-11-23 10:27:01.567', '1');
INSERT INTO users (id, username, password, role, full_name, email, created_date, is_active) VALUES ('8', 'i003', 'password123', 'instructor', 'Serena Dib', 'serenadib@gmail.com', '2025-11-23 13:35:17.34', '1');
-- 8 records backed up

-- Backup of students table
DELETE FROM students;

INSERT INTO students (student_id, first_name, last_name, email, date_of_birth, major) VALUES ('A001', 'Joy', 'AlAhel', 'joyelahel@gmail.com', '2005-06-20', 'Software Engineering');
INSERT INTO students (student_id, first_name, last_name, email, date_of_birth, major) VALUES ('A002', 'Maria', 'Makary', 'mariamakary@gmail.com', '2005-04-20', 'CCE');
INSERT INTO students (student_id, first_name, last_name, email, date_of_birth, major) VALUES ('A003', 'Joe', 'Rohban', 'joerohban@gmail.com', '2005-02-23', 'Computer Engineering');
INSERT INTO students (student_id, first_name, last_name, email, date_of_birth, major) VALUES ('A004', 'Tonio', 'Jreige', 'toniojreige@gmail.com', '2005-12-20', 'CCE');
-- 4 records backed up

-- Backup of instructors table
DELETE FROM instructors;

INSERT INTO instructors (instructor_id, first_name, last_name, email, department, hire_date) VALUES ('I001', 'Mariam', 'Harfouch', 'mariamharfouch@gmail.com', 'CCE', '2022-08-20');
INSERT INTO instructors (instructor_id, first_name, last_name, email, department, hire_date) VALUES ('I002', 'Georges', 'Abdallah', 'abdallahgeorges@gmail.com', 'CCE', '2022-12-12');
INSERT INTO instructors (instructor_id, first_name, last_name, email, department, hire_date) VALUES ('I003', 'Serena', 'Dib', 'serenadib@gmail.com', 'CCE', '2021-07-20');
-- 3 records backed up

-- Backup of courses table
DELETE FROM courses;

INSERT INTO courses (course_id, course_name, course_code, instructor_id, credits, department, description) VALUES ('C001', 'Web Programming 1', '4040', 'I001', '3', 'CCE', 'JAVASCRIPT
EXPRESS.JS
Node.js
AJAX
PHP');
INSERT INTO courses (course_id, course_name, course_code, instructor_id, credits, department, description) VALUES ('C002', 'Database Design', '4041', 'I002', '3', 'CCE', 'SQL');
INSERT INTO courses (course_id, course_name, course_code, instructor_id, credits, department, description) VALUES ('C003', 'MathLab', '4042', 'I003', '3', 'CCE', 'Programming- Project');
-- 3 records backed up

-- Backup of enrollments table
DELETE FROM enrollments;

INSERT INTO enrollments (enrollment_id, student_id, course_id, grade, status) VALUES ('1', 'A001', 'C001', '95.00', 'enrolled');
INSERT INTO enrollments (enrollment_id, student_id, course_id, grade, status) VALUES ('2', 'A001', 'C002', '12.00', 'enrolled');
INSERT INTO enrollments (enrollment_id, student_id, course_id, grade, status) VALUES ('1002', 'A001', 'C003', '93.00', 'enrolled');
-- 3 records backed up


-- Backup completed successfully
