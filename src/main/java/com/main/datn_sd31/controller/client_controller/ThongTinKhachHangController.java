package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.service.KhachHangService;
import com.main.datn_sd31.service.impl.GHNService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tai-khoan")
@RequiredArgsConstructor
public class ThongTinKhachHangController {

    private final KhachHangService khachHangService;
    private final GHNService ghnService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/thong-tin")
    public String hienThiThongTinKhachHang(Model model,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        KhachHang khachHang = khachHangService.findByEmail(email);
        model.addAttribute("khachHang", khachHang);
        return "client/pages/profile/profile";
    }

    /**
     * Cập nhật tên khách hàng
     */
    @PostMapping("/cap-nhat-ten")
    public String capNhatTen(@RequestParam("ten") String ten,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validate tên
            if (ten == null || ten.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên không được để trống!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (ten.trim().length() < 2 || ten.trim().length() > 100) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên phải từ 2-100 ký tự!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);
            khachHang.setTen(ten.trim());
            khachHangService.save(khachHang);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật email
     */
    @PostMapping("/cap-nhat-email")
    public String capNhatEmail(@RequestParam("email") String emailMoi,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate email
            if (emailMoi == null || emailMoi.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email không được để trống!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (!emailMoi.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email không đúng định dạng!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String emailCu = userDetails.getUsername();

            // Kiểm tra email mới có trùng với email cũ không
            if (emailCu.equals(emailMoi.trim())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email mới phải khác email hiện tại!");
                return "redirect:/tai-khoan/thong-tin";
            }

            // Kiểm tra email đã tồn tại
            if (khachHangService.emailDaTonTai(emailMoi.trim())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email này đã được sử dụng!");
                return "redirect:/tai-khoan/thong-tin";
            }

            KhachHang kh = khachHangService.findByEmail(emailCu);
            khachHangService.capNhatEmail(kh.getId(), emailMoi.trim());

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật email thành công! Vui lòng đăng nhập lại.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật số điện thoại
     */
    @PostMapping("/cap-nhat-sdt")
    public String capNhatSoDienThoai(@RequestParam("soDienThoai") String soDienThoaiMoi,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Validate số điện thoại
            if (soDienThoaiMoi == null || soDienThoaiMoi.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không được để trống!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (!soDienThoaiMoi.matches("^(0[3|5|7|8|9])[0-9]{8}$")) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không đúng định dạng!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);

            // Kiểm tra số điện thoại đã tồn tại (trừ của chính user hiện tại)
            if (khachHangService.soDienThoaiDaTonTai(soDienThoaiMoi.trim()) &&
                    !soDienThoaiMoi.trim().equals(khachHang.getSoDienThoai())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại này đã được sử dụng!");
                return "redirect:/tai-khoan/thong-tin";
            }

            khachHangService.capNhatSoDienThoai(khachHang.getId(), soDienThoaiMoi.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số điện thoại thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật giới tính
     */
    @PostMapping("/cap-nhat-gioi-tinh")
    public String capNhatGioiTinh(@RequestParam("gioiTinh") Boolean gioiTinh,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);
            khachHangService.capNhatGioiTinh(khachHang.getId(), gioiTinh);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giới tính thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật ngày sinh
     */
    @PostMapping("/cap-nhat-ngay-sinh")
    public String capNhatNgaySinh(@RequestParam("ngaySinh") LocalDate ngaySinh,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Validate ngày sinh
            if (ngaySinh == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ngày sinh!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (ngaySinh.isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày sinh không thể là ngày trong tương lai!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (ngaySinh.isBefore(LocalDate.now().minusYears(120))) {
                redirectAttributes.addFlashAttribute("errorMessage", "Ngày sinh không hợp lệ!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);
            khachHangService.capNhatNgaySinh(khachHang.getId(), ngaySinh);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ngày sinh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật mật khẩu
     */
    @PostMapping("/cap-nhat-mat-khau")
    public String capNhatMatKhau(@RequestParam("matKhauCu") String matKhauCu,
                                 @RequestParam("matKhauMoi") String matKhauMoi,
                                 @RequestParam("xacNhanMatKhau") String xacNhanMatKhau,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (matKhauCu == null || matKhauCu.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mật khẩu cũ!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mật khẩu mới!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (matKhauMoi.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (!matKhauMoi.equals(xacNhanMatKhau)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);

            // Kiểm tra mật khẩu cũ
            if (!passwordEncoder.matches(matKhauCu, khachHang.getMatKhau())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không đúng!");
                return "redirect:/tai-khoan/thong-tin";
            }

            // Kiểm tra mật khẩu mới khác mật khẩu cũ
            if (passwordEncoder.matches(matKhauMoi, khachHang.getMatKhau())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải khác mật khẩu cũ!");
                return "redirect:/tai-khoan/thong-tin";
            }

            khachHangService.capNhatMatKhau(khachHang.getId(), matKhauMoi);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    /**
     * Cập nhật địa chỉ khách hàng từ lựa chọn tỉnh/huyện/xã
     */
    @PostMapping("/cap-nhat-dia-chi")
    public String capNhatDiaChi(@RequestParam("tinh") String tinh,
                                @RequestParam("huyen") String huyen,
                                @RequestParam("xa") String xa,
                                @RequestParam("chiTiet") String chiTiet,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            // Validate
            if (chiTiet == null || chiTiet.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập địa chỉ chi tiết!");
                return "redirect:/tai-khoan/thong-tin";
            }

            if (tinh.isEmpty() || huyen.isEmpty() || xa.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn đầy đủ tỉnh/huyện/xã!");
                return "redirect:/tai-khoan/thong-tin";
            }

            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);

            String diaChiGop = chiTiet.trim() + ", " + xa + ", " + huyen + ", " + tinh;
            khachHang.setDiaChi(diaChiGop);

            khachHangService.save(khachHang);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    @PostMapping("/xoa-dia-chi")
    public String xoaDiaChi(@AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        try {
            String email = userDetails.getUsername();
            KhachHang khachHang = khachHangService.findByEmail(email);

            khachHang.setDiaChi(null);
            khachHangService.save(khachHang);

            redirectAttributes.addFlashAttribute("successMessage", "Xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/tai-khoan/thong-tin";
    }

    // API endpoints cho địa chỉ
    @GetMapping("/dia-chi/tinh")
    @ResponseBody
    public List<Map<String, Object>> getTinh() {
        return ghnService.getProvinces();
    }

    @GetMapping("/dia-chi/huyen")
    @ResponseBody
    public List<Map<String, Object>> getHuyen(@RequestParam("provinceId") int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/dia-chi/xa")
    @ResponseBody
    public List<Map<String, Object>> getXa(@RequestParam("districtId") int districtId) {
        return ghnService.getWards(districtId);
    }
}