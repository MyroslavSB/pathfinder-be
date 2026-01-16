package com.example.pathfinderbe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDto {
    private String firstName;
    private String lastName;
    private String email;
    private Long id;
}