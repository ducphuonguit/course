package com.course.modules.course.service;

import com.course.modules.course.model.Course;
import com.course.modules.course.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    // Lấy danh sách tất cả khóa học
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // Lấy chi tiết một khóa học theo ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    // Tạo khóa học mới
    @Transactional
    public Course createCourse(Course course) {
        // Kiểm tra nếu mã khóa học đã tồn tại
        if (courseRepository.findByCode(course.getCode()).isPresent()) {
            throw new RuntimeException("Course code already exists: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    // Cập nhật khóa học
    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);

        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());

        return courseRepository.save(course);
    }

    // Xóa khóa học
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }
}