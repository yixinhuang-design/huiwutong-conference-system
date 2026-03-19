package com.conference.auth.service;

import com.conference.auth.dto.LoginRequest;
import com.conference.auth.dto.LoginResponse;
import com.conference.auth.dto.UserInfo;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(String refreshToken);

    UserInfo getCurrentUser(String accessToken);
}
