package com.aido.backend.oauth;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import com.aido.backend.repository.UserRepository;
import com.aido.backend.util.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("Error occurred while processing OAuth2 user", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.fromString(registrationId);
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("email", oAuth2UserInfo.getEmail());
        logInfo.put("provider", provider.toString());
        logInfo.put("provider_id", oAuth2UserInfo.getId());
        LoggingUtils.logInfo(logger, "oauth2_user_processing", logInfo);

        // 먼저 provider와 providerId로 사용자를 찾아봄
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getId());
        User user;
        
        if (userOptional.isPresent()) {
            // 기존 OAuth 사용자 - 정보 업데이트
            user = updateExistingUser(userOptional.get(), oAuth2UserInfo);
            Map<String, Object> updateLog = new HashMap<>();
            updateLog.put("user_id", user.getId());
            updateLog.put("email", user.getEmail());
            updateLog.put("provider", provider.toString());
            LoggingUtils.logInfo(logger, "oauth2_user_updated", updateLog);
        } else {
            // 이메일로 기존 사용자 확인
            Optional<User> emailUserOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
            if (emailUserOptional.isPresent()) {
                User existingUser = emailUserOptional.get();
                if (!existingUser.getProvider().equals(provider)) {
                    throw new OAuth2AuthenticationException("Email already registered with " +
                            existingUser.getProvider() + " provider. Please login with " + 
                            existingUser.getProvider() + " account.");
                }
                // 같은 제공자지만 providerId가 다른 경우 - 업데이트
                existingUser.setProviderId(oAuth2UserInfo.getId());
                user = updateExistingUser(existingUser, oAuth2UserInfo);
            } else {
                // 완전히 새로운 사용자 - 회원가입
                user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
                Map<String, Object> registerLog = new HashMap<>();
                registerLog.put("user_id", user.getId());
                registerLog.put("email", user.getEmail());
                registerLog.put("provider", provider.toString());
                LoggingUtils.logInfo(logger, "oauth2_user_registered", registerLog);
            }
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        AuthProvider provider = AuthProvider.fromString(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        
        User user = new User();
        user.setProvider(provider);
        user.setProviderId(oAuth2UserInfo.getId());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        
        Map<String, Object> registerLog = new HashMap<>();
        registerLog.put("email", user.getEmail());
        registerLog.put("provider", user.getProvider().toString());
        registerLog.put("name", user.getName());
        LoggingUtils.logInfo(logger, "oauth2_new_user_creation", registerLog);
        
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        existingUser.setProfileImageUrl(oAuth2UserInfo.getImageUrl());
        
        Map<String, Object> updateLog = new HashMap<>();
        updateLog.put("user_id", existingUser.getId());
        updateLog.put("email", existingUser.getEmail());
        updateLog.put("name_updated", !existingUser.getName().equals(oAuth2UserInfo.getName()));
        LoggingUtils.logInfo(logger, "oauth2_user_update", updateLog);
        
        return userRepository.save(existingUser);
    }
}