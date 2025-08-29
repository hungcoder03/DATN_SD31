//package com.main.datn_sd31.validator.impl;
//
//import com.main.datn_sd31.entity.PhieuGiamGia;
//import com.main.datn_sd31.validator.ValidDieuKienVsGiamToiDa;
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//
//public class DieuKienVsGiamToiDaValidator implements ConstraintValidator<ValidDieuKienVsGiamToiDa, PhieuGiamGia> {
//
//    @Override
//    public boolean isValid(PhieuGiamGia pg, ConstraintValidatorContext context) {
//        if (pg.getDieuKien() == null || pg.getGiamToiDa() == null) return true;
//        return pg.getDieuKien().compareTo(pg.getGiamToiDa()) <= 0;
//    }
//}
