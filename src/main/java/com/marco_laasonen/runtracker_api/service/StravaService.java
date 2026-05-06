package com.marco_laasonen.runtracker_api.service;

import com.marco_laasonen.runtracker_api.model.Activity;
import com.marco_laasonen.runtracker_api.model.User;
import com.marco_laasonen.runtracker_api.repository.ActivityRepository;
import com.marco_laasonen.runtracker_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StravaService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final AuthService authService;
    private final RestTemplate restTemplate;

    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.client-secret}")
    private String clientSecret;

    @Value("${strava.token-url}")
    private String tokenUrl;

    @Value("${strava.api-base-url}")
    private String apiBaseUrl;

    /**
     * Steps 6-9: Exchange the authorization code for tokens,
     * save/update the user in the database, and return a JWT.
     */
    public String handleCallback(String code) {
        // Step 6: Exchange code for tokens
        Map<String, Object> tokenResponse = exchangeCodeForTokens(code);

        // Extract tokens and athlete info from Strava response
        String accessToken  = (String) tokenResponse.get("access_token");
        String refreshToken = (String) tokenResponse.get("refresh_token");
        Long   expiresAt    = ((Number) tokenResponse.get("expires_at")).longValue();

        @SuppressWarnings("unchecked")
        Map<String, Object> athlete = (Map<String, Object>) tokenResponse.get("athlete");
        String stravaId  = String.valueOf(athlete.get("id"));
        String firstname = (String) athlete.get("firstname");
        String lastname  = (String) athlete.get("lastname");
        String email     = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@strava.placeholder";

        // Step 8: Save or update user in Supabase
        User user = userRepository.findByStravaId(stravaId)
                .orElse(User.builder()
                        .stravaId(stravaId)
                        .email(email)
                        .name(firstname + " " + lastname)
                        .build());

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        user.setTokenExpiresAt(Instant.ofEpochSecond(expiresAt));
        userRepository.save(user);

        // Step 9: Issue a JWT for the user
        return authService.generateJwt(user);
    }

    /**
     * Step 10-11: Fetch all activities from Strava and sync to database.
     * Skips activities already stored (idempotent).
     */
    public void syncActivities(User user) {
        String url = apiBaseUrl + "/athlete/activities?per_page=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getAccessToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url, HttpMethod.GET, request, List.class
        );

        if (response.getBody() == null) return;

        for (Object item : response.getBody()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = (Map<String, Object>) item;

            String stravaId = String.valueOf(raw.get("id"));

            // Skip if already synced
            if (activityRepository.existsByStravaId(stravaId)) continue;

            float distanceMeters = ((Number) raw.get("distance")).floatValue();
            int   durationSec    = ((Number) raw.get("moving_time")).intValue();
            float distanceKm     = distanceMeters / 1000f;
            float paceMinKm      = durationSec > 0 ? (durationSec / 60f) / distanceKm : 0;

            Activity activity = Activity.builder()
                    .user(user)
                    .stravaId(stravaId)
                    .name((String) raw.get("name"))
                    .distanceKm(distanceKm)
                    .durationSec(durationSec)
                    .paceMinKm(paceMinKm)
                    .elevationM(((Number) raw.get("total_elevation_gain")).floatValue())
                    .type((String) raw.get("type"))
                    .activityDate(Instant.parse((String) raw.get("start_date")))
                    .build();

            activityRepository.save(activity);
        }
    }

    // --- Private helpers ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> exchangeCodeForTokens(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "client_id",     clientId,
                "client_secret", clientSecret,
                "code",          code,
                "grant_type",    "authorization_code"
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getBody() == null) {
            throw new RuntimeException("Empty response from Strava token endpoint");
        }
        return response.getBody();
    }
}