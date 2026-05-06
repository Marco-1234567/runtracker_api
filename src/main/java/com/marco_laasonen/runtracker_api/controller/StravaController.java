package com.marco_laasonen.runtracker_api.controller;

import com.marco_laasonen.runtracker_api.service.StravaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/strava")
@RequiredArgsConstructor
public class StravaController {

    private final StravaService stravaService;

    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.redirect-uri}")
    private String redirectUri;

    @Value("${strava.auth-url}")
    private String authUrl;

    /**
     * Step 2-3: Build the Strava authorization URL and return it to the frontend.
     * The React app will redirect the user to this URL.
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect() {
        String url = authUrl
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&approval_prompt=auto"
                + "&scope=read,activity:read_all";

        return ResponseEntity.ok(Map.of("authUrl", url));
    }

    /**
     * Step 5: Strava redirects back here with ?code=abc123
     * We exchange the code for tokens and return a JWT to the user.
     */
    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(@RequestParam String code) {
        String jwt = stravaService.handleCallback(code);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}