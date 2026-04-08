package com.erp.backend.controller;

import com.erp.backend.repository.CourseRepository;
import com.erp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        long totalStudents = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("STUDENT"))
                .count();
        long totalTeachers = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("TEACHER"))
                .count();
        long totalCourses = courseRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", totalStudents);
        stats.put("totalTeachers", totalTeachers);
        stats.put("totalCourses", totalCourses);
        stats.put("activeSessions", 12); // Mock for now

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/teacher/courses/{username}")
    public ResponseEntity<Map<String, Object>> getTeacherCourses(@PathVariable String username) {
        var courses = courseRepository.findAll().stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getUsername().equals(username))
                .map(this::mapCourseMinimal)
                .toList();
        return ResponseEntity.ok(Map.of("success", true, "data", courses));
    }

    private Map<String, Object> mapCourseMinimal(com.erp.backend.entity.Course course) {
        Map<String, Object> res = new HashMap<>();
        res.put("id", course.getCourseCode());
        res.put("name", course.getCourseName());
        return res;
    }
}
