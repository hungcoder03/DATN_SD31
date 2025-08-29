package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.repository.Danhmucrepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@Controller
@RequestMapping("/admin/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final Danhmucrepository danhMucRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("danhMuc", new DanhMuc()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/danh-muc";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        DanhMuc danhMuc = danhMucRepository.findById(id).orElse(null);
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("danhMuc", danhMuc); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/danh-muc";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute DanhMuc danhMuc) {
        if (danhMuc.getId() == null) {
            // THÊM MỚI
            danhMuc.setNgayTao(LocalDateTime.now());
            DanhMuc last = danhMucRepository.findTopByOrderByMaDesc();
            int nextNumber = 1;
            if (last != null && last.getMa() != null && last.getMa().startsWith("DM")) {
                try {
                    // Cắt phần số sau "TH"
                    nextNumber = Integer.parseInt(last.getMa().substring(2)) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            danhMuc.setMa(String.format("DM%03d", nextNumber));
        } else {
            DanhMuc existing = danhMucRepository.findById(danhMuc.getId()).orElse(null);
            if (existing != null) {
                danhMuc.setNgayTao(existing.getNgayTao());
                danhMuc.setMa(existing.getMa());
            }
        }

        // Ngày sửa luôn được cập nhật
        danhMuc.setNgaySua(LocalDateTime.now());
        danhMucRepository.save(danhMuc);
        return "redirect:/admin/danh-muc"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        danhMucRepository.deleteById(id);
        return "redirect:/admin/danh-muc";
    }
}