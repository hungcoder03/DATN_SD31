package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        // modal ban đầu đóng
        model.addAttribute("showModal", false);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
    }
    @GetMapping("/nhanvien/add")
    public String themNhanVien(Model model) {
        model.addAttribute("nhanvien", new NhanVien());
        return "admin/pages/quan-ly-tai-khoan/Themnhanvien";
    }


    @PostMapping("/nhanvien/save")
    public String saveNhanVien(
            @Valid @ModelAttribute("nhanvien") NhanVien nhanVien,
            BindingResult result,
            @RequestParam("anhFile") MultipartFile anhFile,
            Model model
    ) throws IOException {
        // Ví dụ validate trùng mã
        List<NhanVien> existing = nhanVienRepository.findByMa(nhanVien.getMa());
        if (!existing.isEmpty() &&
                (nhanVien.getId() == null || !existing.get(0).getId().equals(nhanVien.getId()))) {
            result.rejectValue("ma", "error.nhanvien", "Mã nhân viên đã tồn tại");
        }
        if (!anhFile.isEmpty()) {
            String original = Path.of(anhFile.getOriginalFilename()).getFileName().toString();
            String fileName = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");
            Path uploadPath = Paths.get("C:/DATN_SD31/uploads/");
            Files.createDirectories(uploadPath);
            Files.copy(anhFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            nhanVien.setAnh("/uploads/" + fileName);
        } else {
            // gán avatar mặc định nếu không chọn file
            nhanVien.setAnh("/uploads/default-avatar.png");
        }
        if (result.hasErrors()) {
            // load lại table
            model.addAttribute("nhanvienList", nhanVienRepository.findAll());
            // báo cho template phải bật modal
            model.addAttribute("showModal", true);
            return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
        }

        // Nếu không lỗi → lưu bình thường
        nhanVien.setMatKhau(passwordEncoder.encode(nhanVien.getMatKhau()));
        nhanVien.setNgayThamGia(LocalDate.now());
        // … xử lý upload file …
        nhanVienRepository.save(nhanVien);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }

    @GetMapping("/nhanvien/detail")
    public String detail(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElseThrow();
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", true);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }

    @GetMapping("/nhanvien/edit")
    public String editForm(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElseThrow();
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", false);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }

    @PostMapping("/nhanvien/update")
    public String updateNhanVien(
            @Valid @ModelAttribute("nhanvien") NhanVien nv,
            BindingResult result,
            @RequestParam("anhFile") MultipartFile anhFile,
            Model model
    ) throws IOException {
        // 1) XỬ LÝ UPLOAD ẢNH (nếu có file mới)
        if (anhFile != null
                && !anhFile.isEmpty()
                && anhFile.getOriginalFilename() != null
                && !anhFile.getOriginalFilename().isBlank()) {

            String original = Path.of(anhFile.getOriginalFilename())
                    .getFileName().toString();
            String fileName = UUID.randomUUID()
                    + "_"
                    + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");

            Path uploadPath = Paths.get("C:/DATN_SD31/uploads/");
            Files.createDirectories(uploadPath);

            Files.copy(
                    anhFile.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            nv.setAnh("/uploads/" + fileName);
        }
        // nếu không upload, nv.getAnh() đã được Thymeleaf hidden-field *{anh} mang vào

        // 2) VALIDATE CUSTOM CHO CÁC FIELD KHÁC
        if (!nv.getMa().matches("^NV\\d{3,5}$")) {
            result.rejectValue("ma", "error.nv",
                    "Mã phải có dạng NV + tối đa 5 chữ số");
        }
        List<NhanVien> duplicates = nhanVienRepository.findByMa(nv.getMa());
        if (!duplicates.isEmpty()
                && !duplicates.get(0).getId().equals(nv.getId())) {
            result.rejectValue("ma", "error.nv", "Mã đã tồn tại");
        }
        // TODO: validate CMND, SĐT, email... theo nhu cầu

        // 3) KIỂM TRA LỖI NGOẠI TRỪ TRƯỜNG 'anh'
        boolean hasOtherErrors = result.getFieldErrors().stream()
                .anyMatch(e -> !"anh".equals(e.getField()));
        if (hasOtherErrors) {
            model.addAttribute("nhanvien", nv);
            model.addAttribute("readonly", false);
            return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
        }

        // 4) LƯU và CHUYỂN VỀ DANH SÁCH NHÂN VIÊN
        nhanVienRepository.save(nv);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
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
    public String saveKhachHang(
            @Valid @ModelAttribute("khachhang") KhachHang kh,
            BindingResult result,
            Model model) {

        // Kiểm tra mã bắt đầu bằng KH
        if (!kh.getMa().matches("^KH\\d{1,5}$")) {
            result.rejectValue("ma", "error.khachhang", "Mã phải có dạng KH + tối đa 5 chữ số");
        }

        // Kiểm tra mã đã tồn tại (trừ trường hợp đang cập nhật chính nó)
        List<KhachHang> existing = khachHangRepository.findByMa(kh.getMa());
        if (!existing.isEmpty() &&
                (kh.getId() == null || !existing.get(0).getId().equals(kh.getId()))) {
            result.rejectValue("ma", "error.khachhang", "Mã khách hàng đã tồn tại");
        }
        // Nếu có lỗi → giữ lại form và mở lại modal
        if (result.hasErrors()) {
            model.addAttribute("khachhangList", khachHangRepository.findAll());
            model.addAttribute("showModal", true);
            model.addAttribute("passwordVisible", false); // mặc định là ẩn sau lỗi
            return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
        }
        // Nếu là khách hàng mới (chưa có ID) thì gán ngày tham gia = hôm nay
        if (kh.getId() == null) {
            kh.setNgayThamGia(LocalDate.now());
        }

        // Nếu hợp lệ → lưu
        String rawPassword = kh.getMatKhau();
        kh.setMatKhau(passwordEncoder.encode(rawPassword));
        khachHangRepository.save(kh);

        return "redirect:/admin/quanlytaikhoan/khachhang";
    }



    @GetMapping("/khachhang/chitiet")
    public String chiTietKhachHang(@RequestParam Integer id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("khachhang", kh);
        model.addAttribute("readonly", true); // chỉ xem
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHangDetail";
    }

    @GetMapping("/khachhang/sua")
    public String suaKhachHang(@RequestParam Integer id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("khachhang", kh);
        model.addAttribute("readonly", false); // cho phép sửa
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHangDetail";
    }

    @PostMapping("/khachhang/update")
    public String updateKhachHang(@Valid @ModelAttribute("khachhang") KhachHang kh,
                                  BindingResult result,
                                  Model model) {
        // Validate mã
        if (kh.getMa() == null || !kh.getMa().matches("^KH\\d{1,5}$")) {
            result.rejectValue("ma", "error.khachhang", "Mã phải có dạng KH + chữ số ");
        }

        // Validate trùng mã
        List<KhachHang> existing = khachHangRepository.findByMa(kh.getMa());
        if (!existing.isEmpty() && !existing.get(0).getId().equals(kh.getId())) {
            result.rejectValue("ma", "error.khachhang", "Mã khách hàng đã tồn tại");
        }



        if (result.hasErrors()) {
            model.addAttribute("readonly", false);
            return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHangDetail";
        }

        // giữ nguyên mật khẩu
        KhachHang old = khachHangRepository.findById(kh.getId()).orElse(null);
        if (old != null) {
            kh.setMatKhau(old.getMatKhau());
            khachHangRepository.save(kh);
        }

        return "redirect:/admin/quanlytaikhoan/khachhang";
    }


    @GetMapping("/khachhang/delete")
    public String deleteKhachHang(@RequestParam Integer id) {
        khachHangRepository.deleteById(id);
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    @GetMapping("/khachhang/search")
    public String searchKhachHang(
            @RequestParam("search") String search,
            Model model) {

        List<KhachHang> list = khachHangRepository.search(search.trim());
        model.addAttribute("khachhangList", list);

        // ** phải có khachhang để form th:object binding **
        model.addAttribute("khachhang", new KhachHang());

        // giữ lại từ khóa và cờ tìm kiếm nếu bạn dùng
        model.addAttribute("search", search);
        model.addAttribute("isSearch", true);

        // nếu bạn dùng showModal / passwordVisible ở template thêm nữa:
        model.addAttribute("showModal", false);
        model.addAttribute("passwordVisible", false);

        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }








}