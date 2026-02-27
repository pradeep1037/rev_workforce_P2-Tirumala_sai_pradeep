package com.rev.app.repository;

import com.rev.app.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByDesigNameIgnoreCase(String desigName);

    boolean existsByDesigNameIgnoreCase(String desigName);
}
