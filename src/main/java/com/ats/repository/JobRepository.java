package com.ats.repository;

import com.ats.model.Job;
import com.ats.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(User employer);
} 