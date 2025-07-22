package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import org.springframework.validation.FieldError;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/quanlytaikhoan")
public class QuanLyTaiKhoan {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private final String uploadDir = "C:/DATN_SD31/uploads/";

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
    @GetMapping("/nhanvien/checkMa")
    @ResponseBody
    public boolean checkMa(@RequestParam("ma") String ma) {
        return nhanVienRepository.existsByMa(ma);
    }

    @PostMapping("/nhanvien/save")
    public String saveNhanVien(
            @Valid
            @ModelAttribute("nhanvien") NhanVien nhanVien,
            BindingResult result,
            Model model,
            @RequestParam("anhFile") MultipartFile anhFile,
            RedirectAttributes ra 
    ) throws IOException {
        if (nhanVienRepository.existsByMa(nhanVien.getMa())) {
            model.addAttribute("maDuplicateError", "Mã NV đã tồn tại!");
            model.addAttribute("nhanvien", nhanVien);
            return "admin/pages/quan-ly-tai-khoan/Themnhanvien";
        }
     
        String rawPassword = nhanVien.getMatKhau();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        nhanVien.setMatKhau(encodedPassword);

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
        nhanVien.setNgayThamGia(LocalDate.now());
        ra.addFlashAttribute("added", true);     // ← đánh dấu thêm thành công

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

        // Lấy danh sách mã nhân viên (trừ mã của chính nhân viên đang sửa)
        List<String> maNhanVienList = nhanVienRepository.findAll()
                .stream()
                .map(NhanVien::getMa)
                .filter(ma -> !ma.equals(nv.getMa()))
                .collect(Collectors.toList());
        model.addAttribute("maNhanVienList", maNhanVienList);

        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }

    @PostMapping("/nhanvien/update")
    public String updateNhanVien(
            @Valid @ModelAttribute("nhanvien") NhanVien nv,
            BindingResult result,
            @RequestParam("anhFile") MultipartFile anhFile,
            Model model,
            RedirectAttributes ra 
    ) throws IOException {
        // Lấy bản ghi cũ để giữ lại các giá trị không thay đổi
        NhanVien old = nhanVienRepository.findById(nv.getId()).orElseThrow();

        // Xử lý ảnh:
        if (anhFile != null && !anhFile.isEmpty()
                && anhFile.getOriginalFilename() != null
                && !anhFile.getOriginalFilename().isBlank()) {

            // Validate định dạng file ảnh
            String originalFilename = anhFile.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!fileExtension.matches("jpg|jpeg|png|gif")) {
                result.rejectValue("anh", "error.nv", "Chỉ chấp nhận file ảnh có định dạng: JPG, JPEG, PNG, GIF");
            } else if (anhFile.getSize() > 5 * 1024 * 1024) { // 5MB
                result.rejectValue("anh", "error.nv", "Kích thước file không được vượt quá 5MB");
            } else {
                String original = Path.of(originalFilename).getFileName().toString();
                String fileName = UUID.randomUUID() + "_"
                        + original.replaceAll("[^a-zA-Z0-9.\\-]", "_");
                Path uploadPath = Paths.get(uploadDir);
                Files.createDirectories(uploadPath);
                Files.copy(
                        anhFile.getInputStream(),
                        uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING
                );
                nv.setAnh("/uploads/" + fileName);
            }
        } else {
            // Nếu không chọn file mới, giữ ảnh cũ lấy từ DB
            nv.setAnh(old.getAnh());
        }

        // Validate mã nhân viên
        if (!nv.getMa().matches("^NV\\d{3,5}$")) {
            result.rejectValue("ma", "error.nv",
                    "Mã phải có dạng NV + tối đa 5 chữ số");
        }

        // Validate trùng mã
        List<NhanVien> duplicates = nhanVienRepository.findByMa(nv.getMa());
        if (!duplicates.isEmpty() && !duplicates.get(0).getId().equals(nv.getId())) {
            result.rejectValue("ma", "error.nv", "Mã đã tồn tại");
        }



        // Nếu có lỗi validation, trả về form với lỗi
        if (result.hasErrors()) {
            model.addAttribute("nhanvien", nv);
            model.addAttribute("readonly", false);
            return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
        }

        // // Giữ nguyên mật khẩu cũ
        // nv.setMatKhau(old.getMatKhau());

        // Giữ nguyên ngày tham gia
        nv.setNgayThamGia(old.getNgayThamGia());

        // Cập nhật người sửa và ngày sửa
        nv.setNguoiSua(old.getId()); // Giả sử người sửa là chính nhân viên đó
        nv.setNgaySua(LocalDateTime.now());

        // Lưu thông tin cập nhật
        ra.addFlashAttribute("updated", true);

        nhanVienRepository.save(nv);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }


    @GetMapping("/nhanvien/delete")
    public String deleteNhanVien(@RequestParam Integer id, RedirectAttributes ra) {
        nhanVienRepository.deleteById(id);
        ra.addFlashAttribute("deleted", true);

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
            Model model,
            RedirectAttributes ra) {

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
        kh.setMatKhau(passwordEncoder.encode(kh.getMatKhau()));
        khachHangRepository.save(kh);
        ra.addFlashAttribute("added", true);     // ← đánh dấu thêm thành công

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
                                  Model model,
                                  RedirectAttributes ra) {
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
        // KhachHang old = khachHangRepository.findById(kh.getId()).orElse(null);
        // if (old != null) {
        //     kh.setMatKhau(old.getMatKhau());
        //     khachHangRepository.save(kh);
        // }
        ra.addFlashAttribute("updated", true);   // ← đánh dấu cập nhật thành công
        khachHangRepository.save(kh);
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }


    @GetMapping("/khachhang/delete")
    public String deleteKhachHang(@RequestParam Integer id,RedirectAttributes ra) {
        khachHangRepository.deleteById(id);
        ra.addFlashAttribute("deleted", true);

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