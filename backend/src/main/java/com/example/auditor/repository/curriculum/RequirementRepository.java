package com.example.auditor.repository.curriculum;

import com.example.auditor.domain.curriculum.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    List<Requirement> findAllByOrderByIdAsc();
}
