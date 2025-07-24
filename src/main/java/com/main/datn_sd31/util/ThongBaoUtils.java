package com.main.datn_sd31.util;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//Thông báo chung cho các Controller
public class ThongBaoUtils {
    public static void addSuccess(RedirectAttributes redirectAttributes, String msg) {
        redirectAttributes.addFlashAttribute("success", msg);
    }

    public static void addError(RedirectAttributes redirectAttributes, String msg) {
        redirectAttributes.addFlashAttribute("error", msg);
    }
}
