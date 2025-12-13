package com.cc.aspect;

import com.cc.modals.User;
import com.cc.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
public class GlobalActivityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(GlobalActivityLoggingAspect.class);

    @Autowired
    private ActivityLogService activityLogService;

    // ----------------------------------------------------
    // Pointcut for Login
    // ----------------------------------------------------
    @AfterReturning(pointcut = "execution(* com.cc.controller.AuthController.login(..))", returning = "result")
    public void logLogin(JoinPoint joinPoint, Object result) {
        try {
            // Get Request Details
            HttpServletRequest request = getCurrentHttpRequest();
            String ipAddress = (request != null) ? getClientIp(request) : "Unknown";

            // Get Input Arguments (User object)
            Object[] args = joinPoint.getArgs();
            String username = "Unknown";
            String role = "Unknown";

            if (args.length > 0 && args[0] instanceof User) {
                User loginRequest = (User) args[0];
                username = loginRequest.getUsername();
            }

            // Check Result
            String details;
            String action = "LOGIN_ATTEMPT";

            if (result instanceof ResponseEntity) {
                ResponseEntity<?> response = (ResponseEntity<?>) result;
                if (response.getStatusCode().is2xxSuccessful()) {
                    action = "LOGIN_SUCCESS";
                    Object body = response.getBody();
                    if (body instanceof User) {
                        User loggedInUser = (User) body;
                        username = loggedInUser.getUsername();
                        role = loggedInUser.getRole();
                    }
                    details = "User logged in successfully.";
                } else {
                    action = "LOGIN_FAILED";
                    details = "Login failed with status: " + response.getStatusCode();
                }
            } else {
                details = "Login returned unexpected type.";
            }

            activityLogService.logActivity(action, username, role, "Auth", details, ipAddress);

        } catch (Exception e) {
            logger.error("Error logging login activity", e);
        }
    }

    // ----------------------------------------------------
    // Pointcut for Modification Methods (Post, Put, Delete)
    // ----------------------------------------------------
    @AfterReturning(pointcut = "execution(* com.cc.controller.*.*(..)) " +
            "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)) " +
            "&& !execution(* com.cc.controller.AuthController.login(..))", returning = "result")
    public void logModification(JoinPoint joinPoint, Object result) {
        try {
            HttpServletRequest request = getCurrentHttpRequest();
            String ipAddress = (request != null) ? getClientIp(request) : "Unknown";
            String httpMethod = (request != null) ? request.getMethod() : "UNKNOWN";
            String uri = (request != null) ? request.getRequestURI() : "";

            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            String entityName = className.replace("Controller", "");

            // --- EXTRACT USER INFO ---
            // Priority: 1. Custom Headers, 2. Spring Security Principal, 3. Query Param, 4.
            // Anonymous
            String username = "Anonymous";
            String role = "Unknown";

            if (request != null) {
                // 1. Check custom headers (sent by frontend)
                String headerUser = request.getHeader("X-User-Name");
                String headerRole = request.getHeader("X-User-Role");

                if (headerUser != null && !headerUser.isEmpty()) {
                    username = headerUser;
                }
                if (headerRole != null && !headerRole.isEmpty()) {
                    role = headerRole;
                }

                // 2. Fallback to Spring Security Principal (if ever enabled)
                if ("Anonymous".equals(username) && request.getUserPrincipal() != null) {
                    username = request.getUserPrincipal().getName();
                }

                // 3. Fallback to query param
                if ("Anonymous".equals(username)) {
                    String paramUser = request.getParameter("username");
                    if (paramUser != null && !paramUser.isEmpty()) {
                        username = paramUser;
                    }
                }
            }

            // Generate human-friendly action and details
            String action = generateAction(httpMethod, methodName, entityName);
            String details = generateDetails(methodName, uri, entityName, result);

            activityLogService.logActivity(action, username, role, entityName, details, ipAddress);

        } catch (Exception e) {
            logger.error("Error logging modification activity", e);
        }
    }

    // --- Human-Friendly Action Generator ---
    private String generateAction(String httpMethod, String methodName, String entityName) {
        // Convert "approveAdmissionLetter" -> "APPROVE_ADMISSION_LETTER"
        String actionName = methodName.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        return actionName;
    }

    // --- Human-Friendly Details Generator ---
    private String generateDetails(String methodName, String uri, String entityName, Object result) {
        // Extract ID from URI if present (e.g., /api/students/80 -> 80)
        String id = extractIdFromUri(uri);

        // Convert camelCase to readable text: "approveAdmissionLetter" -> "Approve
        // admission letter"
        String readableAction = camelCaseToReadable(methodName);

        // Build a clean description
        StringBuilder details = new StringBuilder();
        details.append(readableAction);

        if (id != null && !id.isEmpty()) {
            details.append(" for ").append(entityName).append(" #").append(id);
        }

        // Append success/failure status
        if (result instanceof ResponseEntity) {
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            if (response.getStatusCode().is2xxSuccessful()) {
                details.append(" — Success");
            } else {
                details.append(" — Failed (").append(response.getStatusCode()).append(")");
            }
        }

        return details.toString();
    }

    private String extractIdFromUri(String uri) {
        if (uri == null || uri.isEmpty())
            return null;
        // Match the last numeric segment in the URI
        String[] parts = uri.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (parts[i].matches("\\d+")) {
                return parts[i];
            }
        }
        return null;
    }

    private String camelCaseToReadable(String camelCase) {
        // "approveAdmissionLetter" -> "Approve admission letter"
        String spaced = camelCase.replaceAll("([a-z])([A-Z])", "$1 $2");
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1).toLowerCase();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        // Check common proxy headers first
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                return ip.split(",")[0].trim();
            }
        }

        // Fallback to remote address
        String remoteAddr = request.getRemoteAddr();

        // Handle localhost cases (IPv6 and IPv4 loopback)
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1 (localhost)";
        }

        return remoteAddr;
    }
}
