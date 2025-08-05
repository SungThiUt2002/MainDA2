package com.stu.account_service.Validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        return password.length() >= 8 &&
                password.matches(".*[a-z].*") &&     // ít nhất 1 chữ thường
                password.matches(".*[A-Z].*") &&     // ít nhất 1 chữ hoa
                password.matches(".*\\d.*") &&       // ít nhất 1 số
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");    // ít nhất 1 ký tự đặc biệt
    }
}

