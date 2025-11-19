package com.course.modules.session.repository;

import com.course.modules.session.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByCourseId(Long courseId);
    Optional<Session> findByIdAndQrToken(Long id, String qrToken);
}
