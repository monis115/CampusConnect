package com.cc.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cc.modals.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Find all active news ordered by creation date (newest first)
    List<News> findByActiveOrderByCreatedAtDesc(boolean active);

    // Find news by username
    List<News> findByUsernameOrderByCreatedAtDesc(String username);

    // Find news for a specific user type
    List<News> findByForUserOrderByCreatedAtDesc(String forUser);

    // Find news by tag
    List<News> findByTagOrderByCreatedAtDesc(String tag);

    // Find news that are currently active (within date range)
    @Query("SELECT n FROM News n WHERE n.active = true AND n.activeFrom <= :currentDate AND n.tillDate >= :currentDate ORDER BY n.createdAt DESC")
    List<News> findCurrentlyActiveNews(@Param("currentDate") LocalDate currentDate);

    // Find currently active news for a specific user type
    @Query("SELECT n FROM News n WHERE n.active = true AND n.forUser = :forUser AND n.activeFrom <= :currentDate AND n.tillDate >= :currentDate ORDER BY n.createdAt DESC")
    List<News> findCurrentlyActiveNewsForUser(@Param("forUser") String forUser,
            @Param("currentDate") LocalDate currentDate);

    // Find news by tag that are currently active
    @Query("SELECT n FROM News n WHERE n.active = true AND n.tag = :tag AND n.activeFrom <= :currentDate AND n.tillDate >= :currentDate ORDER BY n.createdAt DESC")
    List<News> findCurrentlyActiveNewsByTag(@Param("tag") String tag, @Param("currentDate") LocalDate currentDate);

    // Find all news for a specific user type (regardless of date)
    List<News> findByForUserAndActiveOrderByCreatedAtDesc(String forUser, boolean active);
}
