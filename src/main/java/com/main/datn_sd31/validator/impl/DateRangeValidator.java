package com.main.datn_sd31.validator.impl;

import com.main.datn_sd31.validator.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.main.datn_sd31.entity.PhieuGiamGia;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, PhieuGiamGia> {

    @Override
    public boolean isValid(PhieuGiamGia value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.getNgayBatDau() == null || value.getNgayKetThuc() == null) return true;
        return !value.getNgayBatDau().isAfter(value.getNgayKetThuc());
    }
}