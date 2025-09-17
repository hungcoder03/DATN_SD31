package com.main.datn_sd31.controller.admin_controller.thuocTinh;

import com.main.datn_sd31.entity.LoaiThu;
import com.main.datn_sd31.repository.thuocTinh.loaiThuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/loai-thu")
public class loaiThuController {
    @Autowired
    loaiThuRepository loaiThuRepository;
    public loaiThuController(loaiThuRepository LoaiThuRepository) {
        this.loaiThuRepository = LoaiThuRepository;
    }

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("loaiThus", loaiThuRepository.findAll());
        model.addAttribute("loaiThu", new LoaiThu()); // Form rỗng để thêm mới
        return "admin/pages/thuocTinh/loaiThu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        LoaiThu loaiThu = loaiThuRepository.findById(id).orElse(null);
        model.addAttribute("loaiThus", loaiThuRepository.findAll());
        model.addAttribute("loaiThu", loaiThu); // Truyền object để binding lại form
        return "admin/pages/thuocTinh/loaiThu";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute LoaiThu loaiThu) {
        if (loaiThu.getId() == null) {
            // THÊM MỚI
            loaiThu.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            LoaiThu existing = loaiThuRepository.findById(loaiThu.getId()).orElse(null);
            if (existing != null) {
                loaiThu.setNgayTao(existing.getNgayTao());
            }
        }

        // Ngày sửa luôn được cập nhật
        loaiThu.setNgaySua(LocalDateTime.now());
        loaiThuRepository.save(loaiThu);
        return "redirect:/admin/loai-thu"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        loaiThuRepository.deleteById(id);
        return "redirect:/admin/loai-thu";
    }
}
