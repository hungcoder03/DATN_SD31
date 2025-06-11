package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Controller
@RequestMapping("/admin")
public class KhuyenMaiController {
    @Autowired
    PhieuGiamGiaRepository repository;



    @GetMapping("/hienThi")
    public String viewVoucher(Model model) {
        model.addAttribute("voucher", new PhieuGiamGia()); // Thêm dòng này
        model.addAttribute("listVoucher", repository.findAll());
        return "admin/pages/khuyen-mai";
    }
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("voucher") PhieuGiamGia voucher,
                       BindingResult result,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("listVoucher", repository.findAll());
            return "admin/pages/khuyen-mai"; // sửa lại tên view
        }
        repository.save(voucher);
        return "redirect:/admin/hienThi"; // sửa lại redirect
    }


    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {
        PhieuGiamGia phieu = repository.findById(id).orElse(null);
        model.addAttribute("voucher", phieu != null ? phieu : new PhieuGiamGia());
        model.addAttribute("listVoucher", repository.findAll());
        return "admin/pages/khuyen-mai";
    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        repository.deleteById(id);
        return "redirect:/voucher";
    }
}

