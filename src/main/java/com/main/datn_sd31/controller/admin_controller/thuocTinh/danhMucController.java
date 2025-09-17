package com.main.datn_sd31.controller.admin_controller.thuocTinh;

import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.repository.thuocTinh.danhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Controller
@RequestMapping("/admin/danh-muc")
public class danhMucController {
    @Autowired
    danhMucRepository danhMucRepository;

    public danhMucController(danhMucRepository DanhMucRepository) {
        this.danhMucRepository = DanhMucRepository;
    }

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("danhMuc", new DanhMuc()); // Form rỗng để thêm mới
        return "admin/pages/thuocTinh/danhMuc";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        DanhMuc danhMuc = danhMucRepository.findById(id).orElse(null);
        model.addAttribute("danhMucs", danhMucRepository.findAll());
        model.addAttribute("danhMuc", danhMuc); // Truyền object để binding lại form
        return "admin/pages/thuocTinh/danhMuc";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute DanhMuc danhMuc) {
        if (danhMuc.getId() == null) {
            // THÊM MỚI
            danhMuc.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            DanhMuc existing = danhMucRepository.findById(danhMuc.getId()).orElse(null);
            if (existing != null) {
                danhMuc.setNgayTao(existing.getNgayTao());
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

