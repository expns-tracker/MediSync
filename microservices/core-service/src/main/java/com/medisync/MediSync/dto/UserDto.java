package com.medisync.MediSync.dto;

import com.medisync.MediSync.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String role;
    private boolean isActive;

    public static UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .build();
    }
}
