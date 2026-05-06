package com.marco_laasonen.runtracker_api.controller;

import com.marco_laasonen.runtracker_api.model.Activity;
import com.marco_laasonen.runtracker_api.model.User;
import com.marco_laasonen.runtracker_api.repository.ActivityRepository;
import com.marco_laasonen.runtracker_api.repository.UserRepository;
import com.marco_laasonen.runtracker_api.service.StravaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final UserRepository     userRepository;
    private final StravaService      stravaService;

    /**
     * GET /api/activities
     * Returns all activities for the authenticated user, newest first.
     */
    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> getActivities(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        List<Activity> activities =
                activityRepository.findByUserIdOrderByActivityDateDesc(userId);
        return ResponseEntity.ok(activities);
    }

    /**
     * POST /api/strava/sync
     * Triggers a fresh sync from Strava for the authenticated user.
     */
    @PostMapping("/strava/sync")
    public ResponseEntity<Void> syncStrava(Authentication auth) {
        UUID userId = UUID.fromString(auth.getName());
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        stravaService.syncActivities(user);
        return ResponseEntity.ok().build();
    }
}