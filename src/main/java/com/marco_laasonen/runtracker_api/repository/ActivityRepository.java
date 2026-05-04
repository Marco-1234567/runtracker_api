package com.marco_laasonen.runtracker_api.repository;

import com.marco_laasonen.runtracker_api.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    // All activities for a user, newest first
    List<Activity> findByUserIdOrderByActivityDateDesc(UUID userId);

    // Activities within a date range (e.g. for weekly overview)
    List<Activity> findByUserIdAndActivityDateBetweenOrderByActivityDateDesc(
            UUID userId,
            Instant from,
            Instant to
    );

    // Check if a Strava activity already exists (avoid duplicates on sync)
    Optional<Activity> findByStravaId(String stravaId);

    boolean existsByStravaId(String stravaId);
}