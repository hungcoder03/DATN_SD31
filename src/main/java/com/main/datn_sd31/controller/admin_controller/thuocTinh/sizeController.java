package com.main.datn_sd31.controller.admin_controller.thuocTinh;

import com.main.datn_sd31.entity.Size;
import com.main.datn_sd31.repository.thuocTinh.sizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/size")
public class sizeController {
    @Autowired
    sizeRepository sizeRepository;

    public sizeController(sizeRepository SizeRepository) {
        this.sizeRepository = SizeRepository;
    }

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("size", new Size()); // Form rỗng để thêm mới
        return "admin/pages/thuocTinh/size";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Size size = sizeRepository.findById(id).orElse(null);
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("size", size); // Truyền object để binding lại form
        return "admin/pages/thuocTinh/size";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute Size size) {
        if (size.getId() == null) {
            // THÊM MỚI
            size.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            Size existing = sizeRepository.findById(size.getId()).orElse(null);
            if (existing != null) {
                size.setNgayTao(existing.getNgayTao());
            }
        }

        // Ngày sửa luôn được cập nhật
        size.setNgaySua(LocalDateTime.now());
        sizeRepository.save(size);
        return "redirect:/admin/size"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        sizeRepository.deleteById(id);
        return "redirect:/admin/size";
    }
}
