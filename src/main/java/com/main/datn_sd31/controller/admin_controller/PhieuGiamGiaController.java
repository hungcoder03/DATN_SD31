package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/phieu-giam-gia")
@RequiredArgsConstructor
public class PhieuGiamGiaController {

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
        model.addAttribute("page", "admin/PhieuGiamGia/index");

        return "admin/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("page", "admin/PhieuGiamGia/create");
        model.addAttribute("phieuGiamGia", new PhieuGiamGia());
        return "admin/index";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia phieuGiamGia,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("page", "admin/PhieuGiamGia/create");
            return "admin/index";
        }

        phieuGiamGia.setNgayTao(LocalDate.now());
        phieuGiamGia.setNgaySua(LocalDate.now());
        phieuGiamGiaService.save(phieuGiamGia);
        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        PhieuGiamGia entity = phieuGiamGiaService.findById(id);
        if (entity == null) {
            return "redirect:/admin/phieu-giam-gia";
        }

        model.addAttribute("phieuGiamGia", entity);
        model.addAttribute("page", "admin/PhieuGiamGia/edit");
        return "admin/index";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia pg,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("page", "admin/PhieuGiamGia/form");
            return "admin/index";
        }

        pg.setNgaySua(LocalDate.now());
        phieuGiamGiaService.save(pg);
        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        phieuGiamGiaService.delete(id);
        return "redirect:/admin/phieu-giam-gia";
    }
}
