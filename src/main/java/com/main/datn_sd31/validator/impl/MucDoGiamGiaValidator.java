package com.main.datn_sd31.validator.impl;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.validator.ValidMucDoGiamGia;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class MucDoGiamGiaValidator implements ConstraintValidator<ValidMucDoGiamGia, PhieuGiamGia> {

//    @Override
    @Override
    public boolean isValid(PhieuGiamGia phieu, ConstraintValidatorContext context) {
        if (phieu.getLoaiPhieuGiamGia() == null || phieu.getMucDo() == null) {
            return true; // để @NotNull xử lý riêng
        }

        BigDecimal mucDo = phieu.getMucDo();

        // Tắt thông báo mặc định
        context.disableDefaultConstraintViolation();

        if (phieu.getLoaiPhieuGiamGia() == 1) { // %
            if (mucDo.compareTo(BigDecimal.ZERO) < 0 || mucDo.compareTo(BigDecimal.valueOf(100)) > 0) {
                context.buildConstraintViolationWithTemplate("Mức giảm (%) phải từ 0 đến 100")
                        .addPropertyNode("mucDo")
                        .addConstraintViolation();
                return false;
            }
        } else if (phieu.getLoaiPhieuGiamGia() == 2) { // VNĐ
            if (mucDo.compareTo(BigDecimal.ZERO) < 0 || mucDo.compareTo(BigDecimal.valueOf(500000)) > 0) {
                context.buildConstraintViolationWithTemplate("Mức giảm (VNĐ) phải từ 0 đến 500.000đ")
                        .addPropertyNode("mucDo")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }

}
