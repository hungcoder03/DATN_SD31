package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/phieu-giam-gia")
@RequiredArgsConstructor
public class PhieuGiamGiaController {

    private final HoaDonService hoaDonService;

    private final PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Model model) {

        List<PhieuGiamGia> list;

        if (startDate == null && endDate == null && (status == null || status.isEmpty())) {
            list = phieuGiamGiaService.findAll();
        } else {
            list = phieuGiamGiaService.findByFilter(startDate, endDate, status);
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("listData", list);

        return "admin/pages/phieu-giam-gia/phieu-giam-gia";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("phieuGiamGia", new PhieuGiamGia());
        return "admin/pages/phieu-giam-gia/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia phieuGiamGia,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Thêm thất bại");
            return "admin/pages/phieu-giam-gia/create";
        }

        if (phieuGiamGia.getLoaiPhieuGiamGia() != 1) {
            phieuGiamGia.setGiamToiDa(phieuGiamGia.getMucDo());
        }

        phieuGiamGia.setNgayTao(LocalDate.now());
        phieuGiamGia.setNgaySua(LocalDate.now());
        phieuGiamGiaService.save(phieuGiamGia);
        ThongBaoUtils.addSuccess(redirectAttributes, "Thêm thành công");
        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable("id") Integer id,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        PhieuGiamGia entity = phieuGiamGiaService.findById(id);
        if (entity == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy phiếu giảm giá này");
            return "redirect:/admin/phieu-giam-gia";
        }

        model.addAttribute("phieuGiamGia", entity);
        return "admin/pages/phieu-giam-gia/edit";
    }

    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia pg,
             BindingResult result,
             Model model,
             RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Cập nhật thất bại");
            model.addAttribute("phieuGiamGia", pg);
            return "admin/pages/phieu-giam-gia/edit";
        }

        pg.setNgaySua(LocalDate.now());
        phieuGiamGiaService.save(pg);
        ThongBaoUtils.addSuccess(redirectAttributes, "Cập nhật thành công");
        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes
    ) {
        PhieuGiamGia entity = phieuGiamGiaService.findById(id);

        if (entity == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy phiếu giảm giá này");
            return "redirect:/admin/phieu-giam-gia";
        }

        if (hoaDonService.existsByPhieuGiamGia(entity)) {
            ThongBaoUtils.addError(redirectAttributes, "Không thể xóa Mã giảm giá này");
            return "redirect:/admin/phieu-giam-gia";
        }

        phieuGiamGiaService.delete(id);
        ThongBaoUtils.addSuccess(redirectAttributes, "Xóa thành công");
        return "redirect:/admin/phieu-giam-gia";
    }
}