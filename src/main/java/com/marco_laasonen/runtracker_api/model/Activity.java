package com.marco_laasonen.runtracker_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "strava_id", unique = true)
    private String stravaId;

    private String name;

    @Column(name = "distance_km")
    private Float distanceKm;

    @Column(name = "duration_sec")
    private Integer durationSec;

    @Column(name = "pace_min_km")
    private Float paceMinKm;

    @Column(name = "elevation_m")
    private Float elevationM;

    // e.g. "Run", "VirtualRun", "Walk"
    private String type;

    @Column(name = "activity_date")
    private Instant activityDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}