package com.lostfound.service;

import com.lostfound.dto.LoginDto;
import com.lostfound.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);

    String register(RegisterDto registerDto);
}
