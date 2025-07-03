package publicdata.hackathon.diplomats.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import publicdata.hackathon.diplomats.domain.entity.User;
import publicdata.hackathon.diplomats.jwt.CustomUserDetails;
import publicdata.hackathon.diplomats.service.CustomUserDetailsService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "사용자", description = "사용자 관련 API")
public class UserController {

	private final CustomUserDetailsService userDetailsService;

}
