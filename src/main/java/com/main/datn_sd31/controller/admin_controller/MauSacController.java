package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.repository.Mausacrepository;
import com.main.datn_sd31.util.ColorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/mau-sac")
@RequiredArgsConstructor
public class MauSacController {

    private final Mausacrepository mauSacRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("mauSacs", mauSacRepository.findAll());
        model.addAttribute("mauSac", new MauSac()); // Form rỗng để thêm mới
        model.addAttribute("availableColors", ColorUtil.getGroupedColors()); // Danh sách màu có sẵn
        return "admin/pages/thuoc-tinh/mau-sac";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        MauSac mauSac = mauSacRepository.findById(id).orElse(null);
        model.addAttribute("mauSacs", mauSacRepository.findAll());
        model.addAttribute("mauSac", mauSac); // Truyền object để binding lại form
        model.addAttribute("availableColors", ColorUtil.getGroupedColors()); // Danh sách màu có sẵn
        return "admin/pages/thuoc-tinh/mau-sac";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute MauSac mauSac) {
        if (mauSac.getId() == null) {
            // THÊM MỚI
            mauSac.setNgayTao(LocalDateTime.now());
            MauSac last = mauSacRepository.findTopByOrderByMaDesc();
            int nextNumber = 1;

            if (last != null && last.getMa() != null && last.getMa().startsWith("MS")) {
                try {
                    nextNumber = Integer.parseInt(last.getMa().substring(2)) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            // Format mã theo dạng TH001, TH002...
            mauSac.setMa(String.format("MS%03d", nextNumber));
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            MauSac existing = mauSacRepository.findById(mauSac.getId()).orElse(null);
            if (existing != null) {
                mauSac.setNgayTao(existing.getNgayTao());
                mauSac.setMa(existing.getMa()); // Giữ nguyên mã khi sửa

            }
        }

        // Ngày sửa luôn được cập nhật
        mauSac.setNgaySua(LocalDateTime.now());

        mauSacRepository.save(mauSac);
        return "redirect:/admin/mau-sac"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        mauSacRepository.deleteById(id);
        return "redirect:/admin/mau-sac";
    }
}