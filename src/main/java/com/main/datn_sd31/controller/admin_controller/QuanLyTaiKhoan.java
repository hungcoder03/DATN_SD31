package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/quanlytaikhoan")
public class QuanLyTaiKhoan {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final String uploadDir = "E:/DATN/DATN_SD31/uploads/";

    // ==== NHÂN VIÊN ====

    @GetMapping("/nhanvien")
    public String listNhanVien(Model model) {
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        model.addAttribute("nhanvien", new NhanVien());
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
    }
    @GetMapping("/nhanvien/add")
    public String themNhanVien(Model model) {
        model.addAttribute("nhanvien", new NhanVien());
        return "admin/pages/quan-ly-tai-khoan/Themnhanvien"; // hoặc đổi tên theo file trên
    }

    @PostMapping("/nhanvien/save")
    public String saveNhanVien(
            @ModelAttribute("nhanvien") NhanVien nhanVien,
            @RequestParam("anhFile") MultipartFile anhFile
    ) throws IOException {

        String rawPassword = nhanVien.getMatKhau();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        nhanVien.setMatKhau(encodedPassword);

        String uploadDir = "E:/DATN/DATN_SD31/uploads/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        if (Files.notExists(uploadPath)) Files.createDirectories(uploadPath);

        if (!anhFile.isEmpty()) {
            String original = Path.of(anhFile.getOriginalFilename()).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");
            try (InputStream is = anhFile.getInputStream()) {
                Files.copy(is, uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
            nhanVien.setAnh("/uploads/" + fileName);
        }
        nhanVien.setNgayThamGia(LocalDateTime.now());

        nhanVienRepository.save(nhanVien);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }
    @GetMapping("/nhanvien/detail")
    public String detailNhanVien(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", true); // để form detail disable
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }

    @GetMapping("/nhanvien/edit")
    public String editNhanVien(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", false); // để form có thể sửa
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }




    @GetMapping("/nhanvien/delete")
    public String deleteNhanVien(@RequestParam Integer id) {
        nhanVienRepository.deleteById(id);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }

    @GetMapping("/nhanvien/search")
    public String searchNhanVien(Model model, @RequestParam("search") String search) {
        model.addAttribute("nhanvienList",nhanVienRepository.search(search));
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
    }

    // ==== KHÁCH HÀNG ====

    @GetMapping("/khachhang")
    public String listKhachHang(Model model) {
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        model.addAttribute("khachhang", new KhachHang());
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }

    @PostMapping("/khachhang/save")
    public String saveKhachHang(@ModelAttribute("khachhang") KhachHang kh) {
        String rawPassword = kh.getMatKhau();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        kh.setMatKhau(encodedPassword);
        khachHangRepository.save(kh);

        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    @GetMapping("/khachhang/chitiet")
    public String chiTietKhachHang(@RequestParam Integer id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("khachhang", kh);      // BẮT BUỘC
        model.addAttribute("readonly", true);     // để hiển thị readonly
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHangDetail";
    }

    @GetMapping("/khachhang/sua")
    public String suaKhachHang(@RequestParam Integer id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("khachhang", kh);      // BẮT BUỘC
        model.addAttribute("readonly", false);    // cho phép chỉnh sửa
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHangDetail";
    }
    @GetMapping("/khachhang/delete")
    public String deleteKhachHang(@RequestParam Integer id) {
        khachHangRepository.deleteById(id);
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    @GetMapping("/khachhang/search")
    public String searchKhachHang(@RequestParam Integer id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("khachhangList", kh != null ? List.of(kh) : List.of());
        model.addAttribute("khachhang", kh != null ? kh : new KhachHang());
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }



}
