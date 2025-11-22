package com.course.modules.course.service;

import com.course.core.exception.ConflictException;
import com.course.core.exception.ResourceNotFoundException;
import com.course.modules.course.model.Course;
import com.course.modules.course.repository.CourseRepository;
import com.course.modules.session.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final SessionRepository sessionRepository; // Inject thêm SessionRepo

    public CourseService(CourseRepository courseRepository, SessionRepository sessionRepository) {
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Transactional
    public Course createCourse(Course course) {
        if (courseRepository.findByCode(course.getCode()).isPresent()) {
            throw new ConflictException("Course code already exists: " + course.getCode());
        }
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourseById(id);

        // Kiểm tra nếu đổi mã code trùng với course khác
        if (!course.getCode().equals(courseDetails.getCode()) &&
                courseRepository.findByCode(courseDetails.getCode()).isPresent()) {
            throw new ConflictException("Course code already exists: " + courseDetails.getCode());
        }

        course.setCode(courseDetails.getCode());
        course.setTitle(courseDetails.getTitle());
        course.setDescription(courseDetails.getDescription());
        course.setStartDate(courseDetails.getStartDate());
        course.setEndDate(courseDetails.getEndDate());

        return courseRepository.save(course);
    }

    // TỐI ƯU HÓA: Đảm bảo tính nhất quán dữ liệu
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id: " + id);
        }

        // Kiểm tra xem khóa học có buổi học nào không
        // Cần thêm method existsByCourseId vào SessionRepository
        if (!sessionRepository.findByCourseId(id).isEmpty()) {
            throw new ConflictException(
                    "Cannot delete course. It implies existing sessions. Please delete sessions first.");
        }

        courseRepository.deleteById(id);
    }
}