package com.main.datn_sd31.validator;

import com.main.datn_sd31.validator.impl.MucDoGiamGiaValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MucDoGiamGiaValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMucDoGiamGia {
    String message() default "Mức giảm không hợp lệ với loại phiếu giảm giá";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}