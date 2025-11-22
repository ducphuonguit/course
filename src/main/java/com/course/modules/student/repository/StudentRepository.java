package com.course.modules.student.repository;

import com.course.modules.student.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    Optional<Student> findByEmail(String email);

    // Tìm kiếm theo Tên hoặc Mã số (không phân biệt hoa thường)
    @Query("SELECT s FROM Student s WHERE lower(s.fullName) LIKE lower(concat('%', :keyword, '%')) OR lower(s.studentNumber) LIKE lower(concat('%', :keyword, '%'))")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);
}