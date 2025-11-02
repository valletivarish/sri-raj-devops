package com.lostfound.controller;

import com.lostfound.entity.User;
import com.lostfound.repository.UserRepository;
import com.lostfound.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Development/Demo Authentication Controller
 * SECURITY: This endpoint is ONLY enabled in demo profile for academic/testing purposes.
 * Auto-login bypasses password verification and should NEVER be enabled in production.
 */
@RestController
@RequestMapping("/api/auth")
public class DevAuthController {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Value("${app.demo-users.enabled:false}")
	private boolean demoModeEnabled;

	public DevAuthController(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * Auto-login endpoint for demo/testing purposes.
	 * SECURITY WARNING: This bypasses password authentication!
	 * Only accessible when app.demo-users.enabled=true
	 */
	@PostMapping("/auto-login")
	public ResponseEntity<Map<String, Object>> autoLogin(@RequestBody Map<String, Long> body) {
		// Security check: Disable in production - return 404 early
		if (!demoModeEnabled) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Endpoint not available", "mode", "prod"));
		}

		// Early return for invalid requests - still indicate dev mode is enabled
		Long userId = body == null ? null : body.get("userId");
		if (userId == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "userId required", "mode", "dev"));
		}
		User user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.badRequest()
					.body(Map.of("error", "User not found", "mode", "dev"));
		}
		List<SimpleGrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
				.map(r -> new SimpleGrantedAuthority(r.getName()))
				.collect(Collectors.toList());
		var auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
		String token = jwtTokenProvider.generateToken(auth);
		return ResponseEntity.ok(Map.of(
				"accessToken", token,
				"tokenType", "Bearer",
				"mode", "dev",
				"user", Map.of("id", user.getId(), "name", user.getName(), "email", user.getEmail())
		));
	}
}
