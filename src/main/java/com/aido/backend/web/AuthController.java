package com.aido.backend.web;

import com.aido.backend.oauth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @Autowired
    private OAuthAvailabilityService oAuthAvailabilityService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "message", required = false) String message,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", message != null ? message : "Authentication failed");
        }
        
        // OAuth 가용성 정보 전달
        model.addAttribute("googleAvailable", oAuthAvailabilityService.isGoogleAvailable());
        model.addAttribute("kakaoAvailable", oAuthAvailabilityService.isKakaoAvailable());
        model.addAttribute("appleAvailable", oAuthAvailabilityService.isAppleAvailable());
        model.addAttribute("anyOAuthAvailable", oAuthAvailabilityService.isAnyOAuthAvailable());
        
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal userPrincipal,
                           @RequestParam(value = "success", required = false) String success,
                           Model model) {
        if (userPrincipal != null) {
            model.addAttribute("user", userPrincipal);
            model.addAttribute("userName", userPrincipal.getDisplayName());
            model.addAttribute("userEmail", userPrincipal.getEmail());
        }
        
        if (success != null) {
            model.addAttribute("success", true);
            model.addAttribute("successMessage", "로그인에 성공했습니다!");
        }
        
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        if (userPrincipal != null) {
            model.addAttribute("user", userPrincipal);
            model.addAttribute("userId", userPrincipal.getId());
            model.addAttribute("userName", userPrincipal.getDisplayName());
            model.addAttribute("userEmail", userPrincipal.getEmail());
        }
        return "profile";
    }
}