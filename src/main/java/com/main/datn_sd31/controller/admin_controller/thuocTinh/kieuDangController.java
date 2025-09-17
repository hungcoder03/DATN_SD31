package com.main.datn_sd31.controller.admin_controller.thuocTinh;

import com.main.datn_sd31.entity.KieuDang;
import com.main.datn_sd31.repository.thuocTinh.kieuDangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Controller
@RequestMapping("/admin/kieu-dang")
public class kieuDangController {
    @Autowired
    kieuDangRepository kieuDangRepository;

    public kieuDangController(kieuDangRepository KieuDangRepository) {
        this.kieuDangRepository = KieuDangRepository;
    }

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("kieuDangs", kieuDangRepository.findAll());
        model.addAttribute("kieuDang", new KieuDang()); // Form rỗng để thêm mới
        return "admin/pages/thuocTinh/kieuDang";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        KieuDang kieuDang = kieuDangRepository.findById(id).orElse(null);
        model.addAttribute("kieuDangs", kieuDangRepository.findAll());
        model.addAttribute("kieuDang", kieuDang); // Truyền object để binding lại form
        return "admin/pages/thuocTinh/kieuDang";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute KieuDang kieuDang) {
        if (kieuDang.getId() == null) {
            // THÊM MỚI
            kieuDang.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            KieuDang existing = kieuDangRepository.findById(kieuDang.getId()).orElse(null);
            if (existing != null) {
                kieuDang.setNgayTao(existing.getNgayTao());
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
