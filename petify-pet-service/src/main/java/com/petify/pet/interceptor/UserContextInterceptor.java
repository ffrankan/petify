package com.petify.pet.interceptor;

import com.petify.pet.context.UserContext;
import com.petify.pet.context.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String userIdStr = request.getHeader(HEADER_USER_ID);
            String username = request.getHeader(HEADER_USERNAME);
            String roles = request.getHeader(HEADER_USER_ROLES);
            
            if (StringUtils.hasText(userIdStr)) {
                Long userId = Long.parseLong(userIdStr);
                UserContext userContext = new UserContext(userId, username, roles);
                UserContextHolder.setContext(userContext);
                
                log.debug("Set user context - userId: {}, username: {}", userId, username);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID format in request header: {}", request.getHeader(HEADER_USER_ID));
        } catch (Exception e) {
            log.error("Error setting user context", e);
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}