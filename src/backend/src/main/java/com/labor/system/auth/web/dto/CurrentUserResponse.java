package com.labor.system.auth.web.dto;

import java.util.List;

public record CurrentUserResponse(
    Long id, String username, String displayName, List<String> roles, List<String> permissions) {}

