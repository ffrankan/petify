package com.petify.pet.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    
    private Long userId;
    private String username;
    private String roles;
    
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}