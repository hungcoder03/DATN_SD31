package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hoa-don/detail1")
@RequiredArgsConstructor
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    private final HoaDonService hoaDonService;

    private final LichSuHoaDonService lichSuHoaDonService;

    @GetMapping("")
    public String detailHoaDon(
            @RequestParam(value = "ma-hoa-don") String maHoaDon,
            Model model
    ){
        model.addAttribute("lichSuList", lichSuHoaDonService.getLichSuHoaDonByHoaDon(maHoaDon));
        model.addAttribute("hoaDon", hoaDonService.getHoaDonByMa(maHoaDon));
        model.addAttribute("hdctList", hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon));
        model.addAttribute("maHoaDon", maHoaDon);
        return "admin/pages/hoa-don/hoa-don-detail";
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam(value = "ma-hoa-don") String maHoaDon,
            @RequestParam("trangThaiMoi") Integer trangThaiMoi,
            @RequestParam(value = "quayLui", required = false) Boolean quayLui,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        lichSuHoaDonService.capNhatTrangThai(maHoaDon, trangThaiMoi, ghiChu, quayLui != null && quayLui);
        redirectAttributes.addAttribute("ma-hoa-don", maHoaDon);
        return "redirect:/admin/hoa-don/detail1";
    }



}