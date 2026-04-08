package com.erp.backend.controller;

import com.erp.backend.entity.Course;
import com.erp.backend.entity.Enrollment;
import com.erp.backend.entity.Grades;
import com.erp.backend.entity.User;
import com.erp.backend.repository.CourseRepository;
import com.erp.backend.repository.EnrollmentRepository;
import com.erp.backend.repository.GradesRepository;
import com.erp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GradesRepository gradesRepository;

    // Get current authenticated user
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    // Get all enrollments for current student
    @GetMapping("/my-courses")
    public ResponseEntity<List<Map<String, Object>>> getMyEnrollments() {
        User student = getCurrentUser();
        if (student == null) {
            return ResponseEntity.status(401).build();
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        List<Map<String, Object>> coursesList = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            Map<String, Object> courseInfo = new HashMap<>();
            courseInfo.put("enrollmentId", enrollment.getId());
            courseInfo.put("courseId", course.getId());
            courseInfo.put("courseCode", course.getCourseCode());
            courseInfo.put("courseName", course.getCourseName());
            courseInfo.put("description", course.getDescription());
            courseInfo.put("instructor", course.getTeacher() != null ? course.getTeacher().getUsername() : "N/A");
            courseInfo.put("schedule", course.getSchedule());
            courseInfo.put("location", course.getLocation());
            int capacity = course.getCapacity() != null ? course.getCapacity() : 0;
            courseInfo.put("credits", course.getCredits());
            courseInfo.put("semester", course.getSemester());
            courseInfo.put("capacity", capacity);
            courseInfo.put("prerequisites", course.getPrerequisites());
            courseInfo.put("enrolledCount", course.getEnrolledCount());
            courseInfo.put("availableSeats", Math.max(0, capacity - course.getEnrolledCount()));
            courseInfo.put("isAvailable", course.isAvailable());
            courseInfo.put("status", enrollment.getStatus());
            courseInfo.put("enrolledDate", enrollment.getEnrolledAt());
            coursesList.add(courseInfo);
        }

        return ResponseEntity.ok(coursesList);
    }

    // Register student for a course
    @PostMapping("/register/{courseId}")
    public ResponseEntity<Map<String, Object>> registerForCourse(@PathVariable Long courseId) {
        User student = getCurrentUser();
        if (student == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Course course = courseOpt.get();

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "You are already enrolled in this course"));
        }

        // Check if course has available seats
        if (!course.isAvailable()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Course is full. No available seats"));
        }

        try {
            Enrollment enrollment = new Enrollment(student, course);
            Enrollment saved = enrollmentRepository.save(enrollment);

            // Auto-initialize a Grades entry for this student-course pairing
            if (gradesRepository.findByStudentAndCourse(student, course).isEmpty()) {
                Grades grades = new Grades(student, course);
                gradesRepository.save(grades);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully registered for course",
                    "enrollmentId", saved.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error registering for course: " + e.getMessage()));
        }
    }

    // Admin registering a student for a course
    @PostMapping("/admin/register")
    public ResponseEntity<Map<String, Object>> adminRegisterForCourse(@RequestBody Map<String, Object> payload) {
        Object studentIdObj = payload.get("studentId");
        Object courseIdObj = payload.get("courseId");

        if (studentIdObj == null || courseIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "studentId and courseId are required"));
        }

        Long studentId = Long.valueOf(studentIdObj.toString());
        Long courseId = Long.valueOf(courseIdObj.toString());

        Optional<User> studentOpt = userRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (!studentOpt.isPresent() || !courseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User student = studentOpt.get();
        Course course = courseOpt.get();

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Student is already enrolled in this course"));
        }

        try {
            Enrollment enrollment = new Enrollment(student, course);
            Enrollment saved = enrollmentRepository.save(enrollment);

            // Auto-initialize a Grades entry
            if (gradesRepository.findByStudentAndCourse(student, course).isEmpty()) {
                Grades grades = new Grades(student, course);
                gradesRepository.save(grades);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Successfully enrolled student in course",
                    "enrollmentId", saved.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Error enrolling student: " + e.getMessage()));
        }
    }


    // Drop a course
    @PostMapping("/{enrollmentId}/drop")
    public ResponseEntity<Map<String, String>> dropCourse(@PathVariable Long enrollmentId) {
        User student = getCurrentUser();
        if (student == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (!enrollmentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Enrollment enrollment = enrollmentOpt.get();

        // Verify it belongs to current student
        if (!enrollment.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(403).build();
        }

        try {
            enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
            enrollmentRepository.save(enrollment);

            return ResponseEntity.ok(Map.of("message", "Course dropped successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error dropping course: " + e.getMessage()));
        }
    }

    // Get enrollments for a specific course (for instructors)
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<Map<String, Object>>> getCourseEnrollments(@PathVariable Long courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByCourse(courseOpt.get());
        List<Map<String, Object>> students = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {
            User student = enrollment.getStudent();
            Map<String, Object> studentInfo = new HashMap<>();
            studentInfo.put("studentId", student.getId());
            studentInfo.put("username", student.getUsername());
            studentInfo.put("email", student.getEmail());
            studentInfo.put("enrolledDate", enrollment.getEnrolledAt());
            students.add(studentInfo);
        }

        return ResponseEntity.ok(students);
    }

    // Get enrollment statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEnrollmentStats() {
        User student = getCurrentUser();
        if (student == null) {
            return ResponseEntity.status(401).build();
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);
        int activeCount = (int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                .count();
        int completedCount = (int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .count();
        int droppedCount = (int) enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.DROPPED)
                .count();

        return ResponseEntity.ok(Map.of(
                "totalEnrollments", enrollments.size(),
                "activeEnrollments", activeCount,
                "completedCourses", completedCount,
                "droppedCourses", droppedCount
        ));
    }
}
