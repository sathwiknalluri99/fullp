package com.erp.backend.controller;

import com.erp.backend.entity.Assignment;
import com.erp.backend.entity.Course;
import com.erp.backend.entity.User;
import com.erp.backend.service.AssignmentService;
import com.erp.backend.service.CourseService;
import com.erp.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/teacher/assignments")
@CrossOrigin(origins = "*")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    /**
     * Create a new assignment
     * POST /teacher/assignments/create
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentRequest request) {
        try {
            // Get teacher from session/context (you can use SecurityContextHolder if using proper auth)
            // For now, we'll get it from the request
            Optional<User> teacherOpt = userService.findById(request.getTeacherId());
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Teacher not found")
                );
            }

            Optional<Course> courseOpt = courseService.findById(request.getCourseId());
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Course not found")
                );
            }

            Assignment assignment = new Assignment();
            assignment.setTitle(request.getTitle());
            assignment.setDescription(request.getDescription());
            assignment.setTeacher(teacherOpt.get());
            assignment.setCourse(courseOpt.get());
            assignment.setDueDate(request.getDueDate());
            assignment.setCreatedAt(LocalDateTime.now());
            assignment.setUpdatedAt(LocalDateTime.now());

            Assignment savedAssignment = assignmentService.createAssignment(assignment);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment created successfully");
            response.put("assignment", savedAssignment);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error creating assignment: " + e.getMessage())
            );
        }
    }

    /**
     * Get assignments for a specific course
     * GET /teacher/assignments/{courseId}
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<?> getAssignmentsByCourse(@PathVariable Long courseId) {
        try {
            Optional<Course> courseOpt = courseService.findById(courseId);
            if (!courseOpt.isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Course not found")
                );
            }

            List<Assignment> assignments = assignmentService.findAssignmentsByCourse(courseOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error fetching assignments: " + e.getMessage())
            );
        }
    }

    /**
     * Get all assignments for a teacher
     * GET /teacher/assignments/teacher/{teacherId}
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<?> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        try {
            Optional<User> teacherOpt = userService.findById(teacherId);
            if (!teacherOpt.isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Teacher not found")
                );
            }

            List<Assignment> assignments = assignmentService.findAssignmentsByTeacher(teacherOpt.get());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error fetching assignments: " + e.getMessage())
            );
        }
    }

    /**
     * Get assignment by ID
     * GET /teacher/assignments/detail/{assignmentId}
     */
    @GetMapping("/detail/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId) {
        try {
            Optional<Assignment> assignmentOpt = assignmentService.findById(assignmentId);
            if (!assignmentOpt.isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Assignment not found")
                );
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignment", assignmentOpt.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error fetching assignment: " + e.getMessage())
            );
        }
    }

    /**
     * Update an assignment
     * PUT /teacher/assignments/{assignmentId}
     */
    @PutMapping("/{assignmentId}")
    public ResponseEntity<?> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody AssignmentRequest request) {
        try {
            Assignment assignmentDetails = new Assignment();
            assignmentDetails.setTitle(request.getTitle());
            assignmentDetails.setDescription(request.getDescription());
            assignmentDetails.setDueDate(request.getDueDate());

            Assignment updatedAssignment = assignmentService.updateAssignment(assignmentId, assignmentDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment updated successfully");
            response.put("assignment", updatedAssignment);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error updating assignment: " + e.getMessage())
            );
        }
    }

    /**
     * Delete an assignment
     * DELETE /teacher/assignments/{assignmentId}
     */
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.deleteAssignment(assignmentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Assignment deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error deleting assignment: " + e.getMessage())
            );
        }
    }

    /**
     * Get upcoming assignments
     * GET /teacher/assignments/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingAssignments() {
        try {
            List<Assignment> assignments = assignmentService.findUpcomingAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error fetching upcoming assignments: " + e.getMessage())
            );
        }
    }

    /**
     * Get overdue assignments
     * GET /teacher/assignments/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueAssignments() {
        try {
            List<Assignment> assignments = assignmentService.findOverdueAssignments();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("assignments", assignments);
            response.put("count", assignments.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("success", false, "message", "Error fetching overdue assignments: " + e.getMessage())
            );
        }
    }

    // DTO for Request
    public static class AssignmentRequest {
        private String title;
        private String description;
        private Long courseId;
        private Long teacherId;
        private LocalDateTime dueDate;

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public Long getTeacherId() {
            return teacherId;
        }

        public void setTeacherId(Long teacherId) {
            this.teacherId = teacherId;
        }

        public LocalDateTime getDueDate() {
            return dueDate;
        }

        public void setDueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
        }
    }
}
