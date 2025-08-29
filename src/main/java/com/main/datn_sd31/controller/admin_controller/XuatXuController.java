package com.main.datn_sd31.controller.admin_controller;


import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.entity.XuatXu;
import com.main.datn_sd31.repository.Xuatxurepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/xuat-xu")
@RequiredArgsConstructor
public class XuatXuController {

    private final Xuatxurepository xuatXuRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("xuatXus", xuatXuRepository.findAll());
        model.addAttribute("xuatXu", new XuatXu()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/xuat-xu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        XuatXu xuatXu = xuatXuRepository.findById(id).orElse(null);
        model.addAttribute("xuatXus", xuatXuRepository.findAll());
        model.addAttribute("xuatXu", xuatXu); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/xuat-xu";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute XuatXu xuatXu) {
        if (xuatXu.getId() == null) {
            // THÊM MỚI
            xuatXu.setNgayTao(LocalDateTime.now());
            XuatXu last = xuatXuRepository.findTopByOrderByMaDesc();
            int nextNumber = 1;

            if (last != null && last.getMa() != null && last.getMa().startsWith("XX")) {
                try {
                    // Cắt phần số sau "TH"
                    nextNumber = Integer.parseInt(last.getMa().substring(2)) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            // Format mã theo dạng TH001, TH002...
            xuatXu.setMa(String.format("XX%03d", nextNumber));

        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            XuatXu existing = xuatXuRepository.findById(xuatXu.getId()).orElse(null);
            if (existing != null) {
                xuatXu.setNgayTao(existing.getNgayTao());
                xuatXu.setMa(existing.getMa()); // Giữ nguyên mã khi sửa

            }
        }

        // Ngày sửa luôn được cập nhật
        xuatXu.setNgaySua(LocalDateTime.now());

        xuatXuRepository.save(xuatXu);
        return "redirect:/admin/xuat-xu"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        xuatXuRepository.deleteById(id);
        return "redirect:/admin/xuat-xu";
    }
}