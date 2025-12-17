package com.cc.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OTPService {

    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> otpExpiry = new ConcurrentHashMap<>();
    private static final int OTP_VALIDITY_MINUTES = 5;

    public String generateOtp(String key) {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 6 digit OTP
        String otpString = String.valueOf(otp);

        otpStorage.put(key, otpString);
        otpExpiry.put(key, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_VALIDITY_MINUTES));

        return otpString;
    }

    public boolean validateOtp(String key, String otp) {
        if (!otpStorage.containsKey(key)) {
            return false;
        }

        Long expiryTime = otpExpiry.get(key);
        if (System.currentTimeMillis() > expiryTime) {
            otpStorage.remove(key);
            otpExpiry.remove(key);
            return false;
        }

        String storedOtp = otpStorage.get(key);
        if (storedOtp.equals(otp)) {
            otpStorage.remove(key);
            otpExpiry.remove(key);
            return true;
        }

        return false;
    }
}
