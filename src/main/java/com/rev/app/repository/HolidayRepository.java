package com.rev.app.repository;

import com.rev.app.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByHolidayDateBetweenOrderByHolidayDateAsc(LocalDate start, LocalDate end);

    boolean existsByHolidayDate(LocalDate date);
}
