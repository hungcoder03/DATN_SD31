package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KieuDang;
import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.repository.Kieudangrepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@Controller
@RequestMapping("/admin/kieu-dang")
@RequiredArgsConstructor
public class KieuDangController {

    private final Kieudangrepository kieuDangRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("kieuDangs", kieuDangRepository.findAll());
        model.addAttribute("kieuDang", new KieuDang()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/kieu-dang";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        KieuDang kieuDang = kieuDangRepository.findById(id).orElse(null);
        model.addAttribute("kieuDangs", kieuDangRepository.findAll());
        model.addAttribute("kieuDang", kieuDang); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/kieu-dang";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute KieuDang kieuDang) {
        if (kieuDang.getId() == null) {
            // THÊM MỚI
            kieuDang.setNgayTao(LocalDateTime.now());
            KieuDang last = kieuDangRepository.findTopByOrderByMaDesc();
            int nextNumber = 1;
            if (last != null && last.getMa() != null && last.getMa().startsWith("KD")) {
                try {
                    // Cắt phần số sau "TH"
                    nextNumber = Integer.parseInt(last.getMa().substring(2)) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            // Format mã theo dạng TH001, TH002...
            kieuDang.setMa(String.format("KD%03d", nextNumber));

        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            KieuDang existing = kieuDangRepository.findById(kieuDang.getId()).orElse(null);
            if (existing != null) {
                kieuDang.setNgayTao(existing.getNgayTao());
                kieuDang.setMa(existing.getMa()); // Giữ nguyên mã khi sửa

            }
        }

        // Ngày sửa luôn được cập nhật
        kieuDang.setNgaySua(LocalDateTime.now());
        kieuDangRepository.save(kieuDang);
        return "redirect:/admin/kieu-dang"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        kieuDangRepository.deleteById(id);
        return "redirect:/admin/kieu-dang";
    }
}