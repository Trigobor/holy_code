package com.example.bank_app.mapper;

import com.example.bank_app.DTO.UserDTO;
import com.example.bank_app.entity.User;

public class UserMapper {
    public static UserDTO fromEntityToUserDTO(User user) {
        return new UserDTO(user.getUsername(), user.getPassword(), user.getRole());
    }
}
