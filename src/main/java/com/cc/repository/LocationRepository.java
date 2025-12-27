package com.cc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cc.modals.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByStateIgnoreCase(String state);

    boolean existsByStateAndDistrict(String state, String district);

}