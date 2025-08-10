package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.ChatLieu;
import com.main.datn_sd31.repository.ChatLieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/chat-lieu")
@RequiredArgsConstructor
public class ChatLieuController {

    private final ChatLieuRepository chatLieuRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("chatLieus", chatLieuRepository.findAll());
        model.addAttribute("chatLieu", new ChatLieu()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/chat-lieu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        ChatLieu chatLieu = chatLieuRepository.findById(id).orElse(null);
        model.addAttribute("chatLieus", chatLieuRepository.findAll());
        model.addAttribute("chatLieu", chatLieu); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/chat-lieu";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute ChatLieu chatLieu) {
        if (chatLieu.getId() == null) {
            // THÊM MỚI
            chatLieu.setNgayTao(LocalDateTime.from(LocalDate.now()));
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            ChatLieu existing = chatLieuRepository.findById(chatLieu.getId()).orElse(null);
            if (existing != null) {
                chatLieu.setNgayTao(existing.getNgayTao());
            }
        }

        // Ngày sửa luôn được cập nhật
        chatLieu.setNgaySua(LocalDateTime.from(LocalDate.now()));
        chatLieuRepository.save(chatLieu);
        return "redirect:/admin/chat-lieu"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        chatLieuRepository.deleteById(id);
        return "redirect:/admin/chat-lieu";
    }
}