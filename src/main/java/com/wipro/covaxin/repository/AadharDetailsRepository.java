package com.wipro.covaxin.repository;

import com.wipro.covaxin.entity.AadharDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AadharDetailsRepository extends JpaRepository<AadharDetails, Long> {
}