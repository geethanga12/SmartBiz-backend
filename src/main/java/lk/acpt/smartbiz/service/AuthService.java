package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.AuthLoginRequest;
import lk.acpt.smartbiz.dto.AuthRegisterRequest;
import lk.acpt.smartbiz.dto.AuthResponse;

public interface AuthService {
    String register(AuthRegisterRequest request);
    AuthResponse login(AuthLoginRequest request);
}
