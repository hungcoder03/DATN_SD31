package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/quanlytaikhoan")
public class QuanLyTaiKhoan {

    // Allow matKhau field binding for new employee creation
    @InitBinder("nhanvien")
    public void initBinder(WebDataBinder binder) {
        // Remove matKhau from disallowed fields to allow password binding for new employees
        // binder.setDisallowedFields("matKhau"); // Commented out to allow password binding
    }

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${file.upload-dir:src/main/resources/static/uploads}")
    private String uploadDir;

    // ==== NHÂN VIÊN ====
    @GetMapping("/nhanvien")
    public String listNhanVien(Model model) {
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        
        // Tạo object nhân viên mới cho modal
        NhanVien nhanVien = new NhanVien();
        String generatedMa = generateMaNhanVien();
        nhanVien.setMa(generatedMa);
        nhanVien.setGioiTinh(true); // Nam mặc định
        nhanVien.setTrangThai(true); // Hoạt động mặc định
        nhanVien.setChucVu("Nhân viên"); // Chức vụ mặc định
        model.addAttribute("nhanvien", nhanVien);
        
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
    }

    // Hiển thị trang thêm nhân viên mới
    @GetMapping("/nhanvien/them")
    public String showThemNhanVien(Model model) {
        // Tạo object nhân viên mới
        NhanVien nhanVien = new NhanVien();
        String generatedMa = generateMaNhanVien();
        nhanVien.setMa(generatedMa);
        nhanVien.setGioiTinh(true); // Nam mặc định
        nhanVien.setTrangThai(true); // Hoạt động mặc định
        nhanVien.setChucVu("Nhân viên"); // Chức vụ mặc định
        model.addAttribute("nhanvien", nhanVien);
        
        return "admin/pages/quan-ly-tai-khoan/ThemNhanVien";
    }

    // Generate mã nhân viên tự động
    @GetMapping("/nhanvien/generateMa")
    @ResponseBody
    public String generateMa() {
        return generateMaNhanVien();
    }

    // Method helper để generate mã nhân viên
    private String generateMaNhanVien() {
        String prefix = "NV";
        List<NhanVien> allNhanVien = nhanVienRepository.findAll();
        
        int maxNumber = 0;
        for (NhanVien nv : allNhanVien) {
            String ma = nv.getMa();
            if (ma != null && ma.startsWith(prefix)) {
                try {
                    int number = Integer.parseInt(ma.substring(prefix.length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
        
        return prefix + String.format("%03d", maxNumber + 1);
    }

    @PostMapping("/nhanvien/save")
    public String saveNhanVien(
            @Valid @ModelAttribute("nhanvien") NhanVien nhanVien,
            BindingResult result,
            @RequestParam(value = "anhFile", required = false) MultipartFile anhFile,
            Model model,
            RedirectAttributes ra) {

        // Validate input data

        // Validation
        validateNhanVien(nhanVien, result);

        if (result.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            result.getAllErrors().forEach(error -> {
                errorMessage.append("• ").append(error.getDefaultMessage()).append("\n");
            });
            
            model.addAttribute("validationError", errorMessage.toString().trim());
            model.addAttribute("nhanvien", nhanVien);
            return "admin/pages/quan-ly-tai-khoan/ThemNhanVien";
        }

        // Xử lý ảnh
        if (anhFile != null && !anhFile.isEmpty()) {
            try {
                String fileName = saveUploadedFile(anhFile);
                nhanVien.setAnh(fileName);
            } catch (IOException e) {
                String fileError = getFileUploadErrorMessage(e.getMessage());
                model.addAttribute("saveError", fileError);
                model.addAttribute("nhanvien", nhanVien);
                return "admin/pages/quan-ly-tai-khoan/ThemNhanVien";
            }
        }

        try {
            // Kiểm tra và mã hóa mật khẩu
            if (nhanVien.getMatKhau() == null || nhanVien.getMatKhau().trim().isEmpty()) {
                model.addAttribute("saveError", "Mật khẩu không được để trống");
                model.addAttribute("nhanvien", nhanVien);
                return "admin/pages/quan-ly-tai-khoan/ThemNhanVien";
            }
            
            String encodedPassword = passwordEncoder.encode(nhanVien.getMatKhau());
            nhanVien.setMatKhau(encodedPassword);

            // Set thông tin audit
            nhanVien.setNgayThamGia(LocalDate.now());
            nhanVien.setNgayTao(LocalDateTime.now());

            nhanVienRepository.save(nhanVien);
            ra.addFlashAttribute("added", true);

            return "redirect:/admin/quanlytaikhoan/nhanvien";
        } catch (Exception e) {
            String userFriendlyError = getUserFriendlyErrorMessage(e.getMessage());
            model.addAttribute("saveError", userFriendlyError);
            model.addAttribute("nhanvien", nhanVien);
            return "admin/pages/quan-ly-tai-khoan/ThemNhanVien";
        }
    }

    // Xử lý lỗi upload file
    private String getFileUploadErrorMessage(String error) {
        if (error == null) {
            return "Lỗi khi tải lên file ảnh";
        }
        
        String lowerError = error.toLowerCase();
        
        if (lowerError.contains("file too large") || lowerError.contains("size")) {
            return "File ảnh quá lớn, vui lòng chọn file nhỏ hơn 5MB";
        } else if (lowerError.contains("invalid") || lowerError.contains("format")) {
            return "Định dạng file không hợp lệ, chỉ chấp nhận JPG, PNG, GIF";
        } else if (lowerError.contains("permission") || lowerError.contains("access")) {
            return "Không có quyền ghi file, vui lòng liên hệ quản trị viên";
        } else if (lowerError.contains("space") || lowerError.contains("disk")) {
            return "Không đủ dung lượng lưu trữ, vui lòng thử lại sau";
        }
        
        return "Lỗi khi tải lên file ảnh, vui lòng thử lại";
    }

    // Chuyển đổi lỗi kỹ thuật thành thông báo thân thiện
    private String getUserFriendlyErrorMessage(String technicalError) {
        if (technicalError == null) {
            return "Có lỗi xảy ra khi lưu thông tin nhân viên";
        }
        
        String lowerError = technicalError.toLowerCase();
        
        // Lỗi NULL constraint
        if (lowerError.contains("cannot insert the value null") || lowerError.contains("not allow nulls")) {
            if (lowerError.contains("anh")) {
                return "Vui lòng chọn ảnh đại diện";
            } else if (lowerError.contains("chuc_vu")) {
                return "Vui lòng chọn chức vụ";
            } else if (lowerError.contains("email")) {
                return "Email không được để trống";
            } else if (lowerError.contains("ten")) {
                return "Tên không được để trống";
            } else if (lowerError.contains("so_dien_thoai")) {
                return "Số điện thoại không được để trống";
            } else if (lowerError.contains("mat_khau")) {
                return "Mật khẩu không được để trống";
            } else if (lowerError.contains("ngay_sinh")) {
                return "Ngày sinh không được để trống";
            }
            return "Vui lòng điền đầy đủ thông tin bắt buộc";
        }
        
        // Lỗi duplicate key
        if (lowerError.contains("duplicate") || lowerError.contains("unique")) {
            if (lowerError.contains("email")) {
                return "Email này đã được sử dụng bởi nhân viên khác";
            } else if (lowerError.contains("so_dien_thoai")) {
                return "Số điện thoại này đã được sử dụng bởi nhân viên khác";
            } else if (lowerError.contains("chung_minh_thu")) {
                return "Số CMND này đã được sử dụng bởi nhân viên khác";
            } else if (lowerError.contains("ma")) {
                return "Mã nhân viên này đã tồn tại";
            }
            return "Thông tin này đã được sử dụng bởi nhân viên khác";
        }
        
        // Lỗi constraint
        if (lowerError.contains("constraint")) {
            return "Dữ liệu không hợp lệ, vui lòng kiểm tra lại thông tin";
        }
        
        // Lỗi connection
        if (lowerError.contains("connection") || lowerError.contains("timeout")) {
            return "Lỗi kết nối cơ sở dữ liệu, vui lòng thử lại";
        }
        
        // Lỗi file
        if (lowerError.contains("file") || lowerError.contains("upload")) {
            return "Lỗi khi tải lên file ảnh, vui lòng thử lại";
        }
        
        // Lỗi khác
        return "Có lỗi xảy ra khi lưu thông tin nhân viên, vui lòng thử lại";
    }

    // Validation cho nhân viên
    private void validateNhanVien(NhanVien nhanVien, BindingResult result) {
        // Kiểm tra mật khẩu (bắt buộc cho nhân viên mới)
        if (nhanVien.getMatKhau() == null || nhanVien.getMatKhau().trim().isEmpty()) {
            result.rejectValue("matKhau", "required", "Mật khẩu không được để trống");
        } else if (nhanVien.getMatKhau().length() < 6) {
            result.rejectValue("matKhau", "invalid", "Mật khẩu phải có ít nhất 6 ký tự");
        }
        
        // Kiểm tra mã nhân viên
        if (nhanVien.getMa() == null || !nhanVien.getMa().matches("^NV\\d{3,5}$")) {
            result.rejectValue("ma", "invalid", "Mã phải có dạng NV + 3-5 chữ số");
        }
        
        // Kiểm tra trùng mã (chỉ khi không phải là thêm mới)
        if (nhanVien.getId() == null && nhanVienRepository.existsByMa(nhanVien.getMa())) {
            result.rejectValue("ma", "duplicate", "Mã nhân viên đã tồn tại");
        }
        
        // Kiểm tra trùng email
        if (nhanVienRepository.existsByEmail(nhanVien.getEmail())) {
            result.rejectValue("email", "duplicate", "Email đã được sử dụng");
        }
        
        // Kiểm tra trùng số điện thoại
        if (nhanVienRepository.existsBySoDienThoai(nhanVien.getSoDienThoai())) {
            result.rejectValue("soDienThoai", "duplicate", "Số điện thoại đã được sử dụng");
        }
        
        // Kiểm tra trùng CMND
        if (nhanVien.getChungMinhThu() != null && !nhanVien.getChungMinhThu().trim().isEmpty()) {
            if (nhanVienRepository.existsByChungMinhThu(nhanVien.getChungMinhThu())) {
                result.rejectValue("chungMinhThu", "duplicate", "Số CMND đã được sử dụng");
            }
        }
        
        // Kiểm tra tuổi (>= 18)
        if (nhanVien.getNgaySinh() != null) {
            LocalDate now = LocalDate.now();
            int age = Period.between(nhanVien.getNgaySinh(), now).getYears();
            if (age < 18) {
                result.rejectValue("ngaySinh", "invalid", "Nhân viên phải từ 18 tuổi trở lên");
            }
        }
    }

    // Kiểm tra trùng lặp email
    @GetMapping("/nhanvien/checkEmail")
    @ResponseBody
    public boolean checkEmail(@RequestParam("email") String email) {
        return nhanVienRepository.existsByEmail(email);
    }

    // Kiểm tra trùng lặp số điện thoại
    @GetMapping("/nhanvien/checkSdt")
    @ResponseBody
    public boolean checkSdt(@RequestParam("sdt") String sdt) {
        return nhanVienRepository.existsBySoDienThoai(sdt);
    }

    // Kiểm tra trùng lặp CMND
    @GetMapping("/nhanvien/checkCmnd")
    @ResponseBody
    public boolean checkCmnd(@RequestParam("cmnd") String chungMinhThu) {
        return nhanVienRepository.existsByChungMinhThu(chungMinhThu);
    }

    // Lưu file upload
    private String saveUploadedFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lưu file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    // Xem chi tiết nhân viên
    @GetMapping("/nhanvien/detail")
    public String detail(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElseThrow();
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", true);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }
    
    // API endpoint để lấy thông tin nhân viên dạng JSON cho modal
    @GetMapping("/nhanvien/api/detail/{id}")
    @ResponseBody
    public NhanVien getEmployeeDetail(@PathVariable Integer id) {
        return nhanVienRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
    }

    // Form sửa nhân viên
    @GetMapping("/nhanvien/edit")
    public String editForm(@RequestParam Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElseThrow();
        // matKhau field sẽ không được bind từ form (xem @InitBinder)
        model.addAttribute("nhanvien", nv);
        model.addAttribute("readonly", false);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
    }

    // Cập nhật nhân viên
    @PostMapping("/nhanvien/update")
    public String updateNhanVien(
            @ModelAttribute("nhanvien") NhanVien nv,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            BindingResult result,
            @RequestParam(value = "anhFile", required = false) MultipartFile anhFile,
            Model model,
            RedirectAttributes ra) {

        // Update employee validation and processing

        NhanVien old = nhanVienRepository.findById(nv.getId()).orElseThrow();

        // Validation cho update
        validateNhanVienUpdate(nv, result);

        // Xử lý validation mật khẩu mới
        boolean isChangingPassword = newPassword != null && !newPassword.trim().isEmpty();
        
        if (isChangingPassword) {
            if (newPassword.length() < 6) {
                result.rejectValue("matKhau", "invalid", "Mật khẩu phải có ít nhất 6 ký tự");
            }
            
            if (confirmPassword == null || confirmPassword.trim().isEmpty() || !newPassword.equals(confirmPassword)) {
                result.rejectValue("matKhau", "mismatch", "Mật khẩu xác nhận không khớp");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("nhanvien", nv);
            model.addAttribute("readonly", false);
            return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
        }

        // Xử lý ảnh
        if (anhFile != null && !anhFile.isEmpty()) {
            try {
                String fileName = saveUploadedFile(anhFile);
                nv.setAnh(fileName);
            } catch (IOException e) {
                model.addAttribute("nhanvien", nv);
                model.addAttribute("readonly", false);
                model.addAttribute("saveError", "Lỗi khi lưu file ảnh: " + e.getMessage());
                return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVienDetail";
            }
        } else {
            nv.setAnh(old.getAnh());
        }

        // Xử lý mật khẩu
        nv.setMatKhau(old.getMatKhau());
        
        if (isChangingPassword) {
            String encodedPassword = passwordEncoder.encode(newPassword);
            nv.setMatKhau(encodedPassword);
        }

        // Giữ nguyên các thông tin audit
        nv.setNgayThamGia(old.getNgayThamGia());
        nv.setNgayTao(old.getNgayTao());
        nv.setNguoiTao(old.getNguoiTao());
        nv.setNgaySua(LocalDateTime.now());

        nhanVienRepository.save(nv);
        ra.addFlashAttribute("updated", true);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }

    // Validation cho update
    private void validateNhanVienUpdate(NhanVien nhanVien, BindingResult result) {
        // Kiểm tra trùng mã (loại trừ chính nó)
        if (nhanVienRepository.existsByMaAndIdNot(nhanVien.getMa(), nhanVien.getId())) {
            result.rejectValue("ma", "duplicate", "Mã nhân viên đã tồn tại");
        }
        
        // Kiểm tra trùng email (loại trừ chính nó)
        if (nhanVienRepository.existsByEmailAndIdNot(nhanVien.getEmail(), nhanVien.getId())) {
            result.rejectValue("email", "duplicate", "Email đã được sử dụng");
        }
        
        // Kiểm tra trùng số điện thoại (loại trừ chính nó)
        if (nhanVienRepository.existsBySoDienThoaiAndIdNot(nhanVien.getSoDienThoai(), nhanVien.getId())) {
            result.rejectValue("soDienThoai", "duplicate", "Số điện thoại đã được sử dụng");
        }
        
        // Kiểm tra trùng CMND (loại trừ chính nó)
        if (nhanVien.getChungMinhThu() != null && !nhanVien.getChungMinhThu().trim().isEmpty()) {
            if (nhanVienRepository.existsByChungMinhThuAndIdNot(nhanVien.getChungMinhThu(), nhanVien.getId())) {
                result.rejectValue("chungMinhThu", "duplicate", "Số CMND đã được sử dụng");
            }
        }
        
        // Kiểm tra tuổi (>= 18)
        if (nhanVien.getNgaySinh() != null) {
            LocalDate now = LocalDate.now();
            int age = Period.between(nhanVien.getNgaySinh(), now).getYears();
            if (age < 18) {
                result.rejectValue("ngaySinh", "invalid", "Nhân viên phải từ 18 tuổi trở lên");
            }
        }
    }

    // Xóa nhân viên
    @GetMapping("/nhanvien/delete")
    public String deleteNhanVien(@RequestParam Integer id, RedirectAttributes ra) {
        nhanVienRepository.deleteById(id);
        ra.addFlashAttribute("deleted", true);
        return "redirect:/admin/quanlytaikhoan/nhanvien";
    }

    // Tìm kiếm nhân viên
    @GetMapping("/nhanvien/search")
    public String searchNhanVien(Model model, @RequestParam("search") String search) {
        List<NhanVien> searchResults = nhanVienRepository.search(search);
        model.addAttribute("nhanvienList", searchResults);
        model.addAttribute("nhanvien", new NhanVien());
        model.addAttribute("searchKeyword", search);
        return "admin/pages/quan-ly-tai-khoan/QuanLyNhanVien";
    }

    // ==== KHÁCH HÀNG ====

    @GetMapping("/khachhang")
    public String listKhachHang(Model model) {
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        
        // Tạo object khách hàng mới cho modal
        KhachHang khachHang = new KhachHang();
        khachHang.setMa(generateMaKhachHang());
        khachHang.setGioiTinh(true); // Nam mặc định
        khachHang.setTrangThai(true); // Hoạt động mặc định
        model.addAttribute("khachhang", khachHang);
        
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }

    // Form thêm khách hàng
    @GetMapping("/khachhang/add")
    public String addKhachHangForm(Model model) {
        KhachHang khachHang = new KhachHang();
        // Tạo mã khách hàng tự động
        String ma = generateMaKhachHang();
        khachHang.setMa(ma);
        khachHang.setGioiTinh(true); // Nam mặc định
        khachHang.setTrangThai(true); // Hoạt động mặc định
        
        model.addAttribute("khachhang", khachHang);
        return "admin/pages/quan-ly-tai-khoan/ThemKhachHang";
    }

    // Generate mã khách hàng tự động
    private String generateMaKhachHang() {
        String prefix = "KH";
        List<KhachHang> allKhachHang = khachHangRepository.findAll();
        
        int maxNumber = 0;
        for (KhachHang kh : allKhachHang) {
            String ma = kh.getMa();
            if (ma != null && ma.startsWith(prefix)) {
                try {
                    int number = Integer.parseInt(ma.substring(prefix.length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }
        
        return prefix + String.format("%03d", maxNumber + 1);
    }

    // Tìm kiếm khách hàng
    @GetMapping("/khachhang/search")
    public String searchKhachHang(@RequestParam("search") String search, Model model) {
        List<KhachHang> khachhangList;
        if (search == null || search.trim().isEmpty()) {
            khachhangList = khachHangRepository.findAll();
        } else {
            khachhangList = khachHangRepository.search(search.trim());
        }
        
        model.addAttribute("khachhangList", khachhangList);
        model.addAttribute("searchKeyword", search);
        
        // Thêm khách hàng mới cho modal (nếu cần)
        KhachHang khachHang = new KhachHang();
        khachHang.setMa(generateMaKhachHang());
        khachHang.setGioiTinh(true);
        khachHang.setTrangThai(true);
        model.addAttribute("khachhang", khachHang);
        
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }

    @PostMapping("/khachhang/save")
    public String saveKhachHang(
            @Valid @ModelAttribute("khachhang") KhachHang khachHang,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        // Validation
        validateKhachHang(khachHang, result);

        if (result.hasErrors()) {
            model.addAttribute("khachhang", khachHang);
            model.addAttribute("validationError", getValidationErrorMessage(result));
            return "admin/pages/quan-ly-tai-khoan/ThemKhachHang";
        }

        // Mã hóa mật khẩu nếu có
        if (khachHang.getMatKhau() != null && !khachHang.getMatKhau().trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(khachHang.getMatKhau());
            khachHang.setMatKhau(encodedPassword);
        }

        // Set thông tin audit
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setNgayThamGia(LocalDateTime.now());
        khachHang.setNguoiTao(1); // TODO: Lấy từ session user hiện tại

        khachHangRepository.save(khachHang);
        ra.addFlashAttribute("added", true);
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    // Form sửa khách hàng
    @GetMapping("/khachhang/sua")
    public String editKhachHangForm(@RequestParam("id") Integer id, Model model) {
        KhachHang khachHang = khachHangRepository.findById(id).orElse(null);
        if (khachHang == null) {
            return "redirect:/admin/quanlytaikhoan/khachhang";
        }
        
        model.addAttribute("khachhang", khachHang);
        model.addAttribute("isEdit", true);
        
        return "admin/pages/quan-ly-tai-khoan/SuaKhachHang";
    }

    // Cập nhật khách hàng
    @PostMapping("/khachhang/update")
    public String updateKhachHang(
            @Valid @ModelAttribute("khachhang") KhachHang khachHang,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        // Validation
        validateKhachHang(khachHang, result);

        if (result.hasErrors()) {
            model.addAttribute("khachhang", khachHang);
            model.addAttribute("isEdit", true);
            model.addAttribute("validationError", getValidationErrorMessage(result));
            return "admin/pages/quan-ly-tai-khoan/SuaKhachHang";
        }

        // Lấy khách hàng hiện tại từ DB
        KhachHang existingKhachHang = khachHangRepository.findById(khachHang.getId()).orElse(null);
        if (existingKhachHang == null) {
            ra.addFlashAttribute("error", "Không tìm thấy khách hàng để cập nhật");
            return "redirect:/admin/quanlytaikhoan/khachhang";
        }

        // Cập nhật thông tin (giữ nguyên một số field)
        existingKhachHang.setTen(khachHang.getTen());
        existingKhachHang.setEmail(khachHang.getEmail());
        existingKhachHang.setSoDienThoai(khachHang.getSoDienThoai());
        existingKhachHang.setNgaySinh(khachHang.getNgaySinh());
        existingKhachHang.setGioiTinh(khachHang.getGioiTinh());
        existingKhachHang.setDiaChi(khachHang.getDiaChi());
        existingKhachHang.setTrangThai(khachHang.getTrangThai());

        // Cập nhật mật khẩu nếu có
        if (khachHang.getMatKhau() != null && !khachHang.getMatKhau().trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(khachHang.getMatKhau());
            existingKhachHang.setMatKhau(encodedPassword);
        }

        // Cập nhật thời gian
        existingKhachHang.setNgaySua(LocalDateTime.now());
        existingKhachHang.setNguoiSua(1); // TODO: Lấy từ session user hiện tại

        khachHangRepository.save(existingKhachHang);
        ra.addFlashAttribute("updated", true);
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    // Xóa khách hàng
    @GetMapping("/khachhang/delete")
    public String deleteKhachHang(@RequestParam("id") Integer id, RedirectAttributes ra) {
        try {
            KhachHang khachHang = khachHangRepository.findById(id).orElse(null);
            if (khachHang == null) {
                ra.addFlashAttribute("error", "Không tìm thấy khách hàng để xóa");
                return "redirect:/admin/quanlytaikhoan/khachhang";
            }
            
            khachHangRepository.delete(khachHang);
            ra.addFlashAttribute("deleted", true);
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa khách hàng. Có thể khách hàng đang có dữ liệu liên quan.");
        }
        
        return "redirect:/admin/quanlytaikhoan/khachhang";
    }

    // Validation cho khách hàng
    private void validateKhachHang(KhachHang khachHang, BindingResult result) {
        // Kiểm tra mã khách hàng
        if (khachHang.getMa() == null || !khachHang.getMa().matches("^KH\\d{3,5}$")) {
            result.rejectValue("ma", "invalid", "Mã phải có dạng KH + 3-5 chữ số");
        }
        
        // Kiểm tra trùng mã
        if (khachHang.getId() == null) {
            List<KhachHang> existing = khachHangRepository.findByMa(khachHang.getMa());
            if (!existing.isEmpty()) {
                result.rejectValue("ma", "duplicate", "Mã khách hàng đã tồn tại");
            }
        }
        
        // Kiểm tra tên (bắt buộc)
        if (khachHang.getTen() == null || khachHang.getTen().trim().isEmpty()) {
            result.rejectValue("ten", "required", "Tên khách hàng không được để trống");
        } else if (khachHang.getTen().trim().length() < 2) {
            result.rejectValue("ten", "invalid", "Tên phải có ít nhất 2 ký tự");
        }
        
        // Kiểm tra email (tùy chọn nhưng phải đúng format nếu có)
        if (khachHang.getEmail() != null && !khachHang.getEmail().trim().isEmpty()) {
            // Validate email format với regex chặt chẽ hơn
            if (!khachHang.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                result.rejectValue("email", "invalid", "Email không đúng định dạng");
            } else {
                // Kiểm tra trùng email (cho cả thêm mới và cập nhật)
                Optional<KhachHang> existingEmail = khachHangRepository.findByEmail(khachHang.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(khachHang.getId())) {
                    result.rejectValue("email", "duplicate", "Email đã được sử dụng bởi khách hàng khác");
                }
            }
        }
        
        // Kiểm tra số điện thoại (tùy chọn nhưng phải đúng format nếu có)
        if (khachHang.getSoDienThoai() != null && !khachHang.getSoDienThoai().trim().isEmpty()) {
            String sdt = khachHang.getSoDienThoai().trim();
            
            // Validate định dạng số điện thoại Việt Nam
            if (!sdt.matches("^(0[3|5|7|8|9])[0-9]{8}$")) {
                result.rejectValue("soDienThoai", "invalid", "Số điện thoại phải có định dạng: 0X XXXXXXXX (X là số từ 3,5,7,8,9)");
            } else {
                // Kiểm tra trùng số điện thoại (cho cả thêm mới và cập nhật)
                Optional<KhachHang> existingSdt = khachHangRepository.findBySoDienThoai(sdt);
                if (existingSdt.isPresent() && !existingSdt.get().getId().equals(khachHang.getId())) {
                    result.rejectValue("soDienThoai", "duplicate", "Số điện thoại đã được sử dụng bởi khách hàng khác");
                }
            }
        }
        
        // Kiểm tra tuổi (nếu có ngày sinh)
        if (khachHang.getNgaySinh() != null) {
            LocalDate now = LocalDate.now();
            int age = Period.between(khachHang.getNgaySinh(), now).getYears();
            if (age < 16) {
                result.rejectValue("ngaySinh", "invalid", "Khách hàng phải từ 16 tuổi trở lên");
            }
        }
        
        // Kiểm tra mật khẩu (tùy chọn nhưng phải đủ mạnh nếu có)
        if (khachHang.getMatKhau() != null && !khachHang.getMatKhau().trim().isEmpty()) {
            if (khachHang.getMatKhau().length() < 6) {
                result.rejectValue("matKhau", "invalid", "Mật khẩu phải có ít nhất 6 ký tự");
            }
        }
    }

    // API endpoint để lấy thông tin khách hàng dạng JSON cho modal
    @GetMapping("/khachhang/api/detail/{id}")
    @ResponseBody
    public KhachHang getCustomerDetail(@PathVariable Integer id) {
        return khachHangRepository.findById(id).orElseThrow(() -> 
            new RuntimeException("Không tìm thấy khách hàng với ID: " + id));
    }

    @GetMapping("/khachhang/chitiet")
    public String chiTietKhachHang(@RequestParam("id") Integer id, Model model) {
        KhachHang khachHang = khachHangRepository.findById(id).orElse(null);
        if (khachHang == null) {
            return "redirect:/admin/quanlytaikhoan/khachhang";
        }
        model.addAttribute("khachhang", khachHang);
        return "admin/pages/quan-ly-tai-khoan/QuanLyKhachHang";
    }

    // Tạo thông báo lỗi validation
    private String getValidationErrorMessage(BindingResult result) {
        StringBuilder sb = new StringBuilder();
        result.getAllErrors().forEach(error -> {
            sb.append("• ").append(error.getDefaultMessage()).append("\n");
        });
        return sb.toString();
    }
}