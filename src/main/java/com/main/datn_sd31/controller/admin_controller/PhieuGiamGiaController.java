package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/admin/phieu-giam-gia")
@RequiredArgsConstructor
public class PhieuGiamGiaController {

    private final HoaDonService hoaDonService;
    private final PhieuGiamGiaService phieuGiamGiaService;
    private final GetNhanVien get_nhan_vien;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;

    // Custom validation method
    private void validatePhieuGiamGia(PhieuGiamGia phieuGiamGia, BindingResult bindingResult) {
        LocalDate today = LocalDate.now();

        // Kiểm tra ngày bắt đầu không được nhỏ hơn hôm nay
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayBatDau().isBefore(today)) {
            bindingResult.rejectValue("ngayBatDau", "error.ngayBatDau", "Ngày bắt đầu không được nhỏ hơn ngày hiện tại");
        }

        // Kiểm tra khoảng cách tối thiểu 1 ngày giữa ngày bắt đầu và kết thúc
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayKetThuc() != null) {
            if (!phieuGiamGia.getNgayKetThuc().isAfter(phieuGiamGia.getNgayBatDau())) {
                bindingResult.rejectValue("ngayKetThuc", "error.ngayKetThuc", "Ngày kết thúc phải sau ngày bắt đầu ít nhất 1 ngày");
            }
        }

        // Kiểm tra logic mức giảm theo loại phiếu
        if (phieuGiamGia.getLoaiPhieuGiamGia() != null && phieuGiamGia.getMucDo() != null) {
            if (phieuGiamGia.getLoaiPhieuGiamGia() == 1) { // Phần trăm
                if (phieuGiamGia.getMucDo().compareTo(BigDecimal.ONE) < 0 ||
                        phieuGiamGia.getMucDo().compareTo(BigDecimal.valueOf(100)) > 0) {
                    bindingResult.rejectValue("mucDo", "error.mucDo", "Mức giảm phần trăm phải từ 1% đến 100%");
                }

                // Kiểm tra giảm tối đa với phần trăm
                if (phieuGiamGia.getGiamToiDa() != null && phieuGiamGia.getDieuKien() != null) {
                    BigDecimal maxPossibleDiscount = phieuGiamGia.getDieuKien()
                            .multiply(phieuGiamGia.getMucDo())
                            .divide(BigDecimal.valueOf(100));
                    if (phieuGiamGia.getGiamToiDa().compareTo(maxPossibleDiscount) > 0) {
                        bindingResult.rejectValue("giamToiDa", "error.giamToiDa",
                                "Giảm tối đa không được lớn hơn mức giảm tính từ điều kiện");
                    }
                }
            } else if (phieuGiamGia.getLoaiPhieuGiamGia() == 2) { // Tiền mặt
                if (phieuGiamGia.getMucDo().compareTo(BigDecimal.ZERO) <= 0) {
                    bindingResult.rejectValue("mucDo", "error.mucDo", "Mức giảm tiền mặt phải lớn hơn 0");
                }

                // Đối với tiền mặt, mức giảm không được lớn hơn điều kiện
                if (phieuGiamGia.getDieuKien() != null &&
                        phieuGiamGia.getMucDo().compareTo(phieuGiamGia.getDieuKien()) > 0) {
                    bindingResult.rejectValue("mucDo", "error.mucDo",
                            "Mức giảm tiền mặt không được lớn hơn điều kiện áp dụng");
                }
            }
        }

        // Kiểm tra số lượng hợp lý
        if (phieuGiamGia.getSoLuongTon() != null && phieuGiamGia.getSoLuongTon() < 0) {
            bindingResult.rejectValue("soLuongTon", "error.soLuongTon", "Số lượng không được âm");
        }
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Model model) {

        List<PhieuGiamGia> list;

        if (startDate == null && endDate == null && (status == null || status.isEmpty())) {
            list = phieuGiamGiaService.findAll();
        } else {
            list = phieuGiamGiaService.findByFilter(startDate, endDate, status);
        }

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);
        model.addAttribute("listData", list);

        return "admin/pages/phieu-giam-gia/phieu-giam-gia";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();
        // Set default values
        phieuGiamGia.setLoaiPhieuGiamGia(1); // Default to percentage
        phieuGiamGia.setTrangThai(true);
        phieuGiamGia.setNgayBatDau(LocalDate.now());
        phieuGiamGia.setNgayKetThuc(LocalDate.now().plusDays(30)); // Default 30 days

        model.addAttribute("phieuGiamGia", phieuGiamGia);
        return "admin/pages/phieu-giam-gia/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia phieuGiamGia,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Custom validation
        validatePhieuGiamGia(phieuGiamGia, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Thêm thất bại, vui lòng kiểm tra lại thông tin");
            return "admin/pages/phieu-giam-gia/create";
        }

        try {
            // Tự sinh mã PGG nếu để trống với thread-safe approach
            if (phieuGiamGia.getMa() == null || phieuGiamGia.getMa().trim().isEmpty()) {
                String generated = generateUniqueCode();
                if (generated == null) {
                    model.addAttribute("error", "Không thể tạo mã tự động, vui lòng nhập mã thủ công");
                    return "admin/pages/phieu-giam-gia/create";
                }
                phieuGiamGia.setMa(generated);
            } else if (phieuGiamGiaRepository.existsByMa(phieuGiamGia.getMa().trim())) {
                model.addAttribute("error", "Mã phiếu giảm giá đã tồn tại");
                return "admin/pages/phieu-giam-gia/create";
            }

            // Set giảm tối đa cho loại tiền mặt
            if (phieuGiamGia.getLoaiPhieuGiamGia() == 2) {
                phieuGiamGia.setGiamToiDa(phieuGiamGia.getMucDo());
            }

            phieuGiamGia.setNgayTao(LocalDate.now());
            phieuGiamGia.setNgaySua(LocalDate.now());

            phieuGiamGiaService.save(phieuGiamGia, get_nhan_vien.getCurrentNhanVien());
            ThongBaoUtils.addSuccess(redirectAttributes, "Thêm phiếu giảm giá thành công");

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "admin/pages/phieu-giam-gia/create";
        }

        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable("id") Integer id,
            Model model,
            RedirectAttributes redirectAttributes) {

        PhieuGiamGia entity = phieuGiamGiaService.findById(id);
        if (entity == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy phiếu giảm giá này");
            return "redirect:/admin/phieu-giam-gia";
        }

        model.addAttribute("phieuGiamGia", entity);
        return "admin/pages/phieu-giam-gia/edit";
    }

    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia pg,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Custom validation
        validatePhieuGiamGia(pg, result);

        // Kiểm tra trạng thái hợp lý
        LocalDate today = LocalDate.now();
        if (pg.getTrangThai() && pg.getNgayKetThuc() != null && today.isAfter(pg.getNgayKetThuc())) {
            result.rejectValue("trangThai", "error.trangThai",
                    "Không thể kích hoạt phiếu giảm giá đã hết hạn");
        }

        if (result.hasErrors()) {
            model.addAttribute("error", "Cập nhật thất bại, vui lòng kiểm tra lại thông tin");
            model.addAttribute("phieuGiamGia", pg);
            return "admin/pages/phieu-giam-gia/edit";
        }

        try {
            pg.setNgaySua(LocalDate.now());

            // Logic tự động set trạng thái dựa trên thời gian
            if (pg.getNgayKetThuc() != null && today.isAfter(pg.getNgayKetThuc())) {
                pg.setTrangThai(false);
            } else if (pg.getNgayBatDau() != null && today.isBefore(pg.getNgayBatDau())) {
                pg.setTrangThai(false);
            }
            // Trong khoảng hợp lệ thì giữ theo user selection

            phieuGiamGiaService.save(pg, get_nhan_vien.getCurrentNhanVien());
            ThongBaoUtils.addSuccess(redirectAttributes, "Cập nhật phiếu giảm giá thành công");

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("phieuGiamGia", pg);
            return "admin/pages/phieu-giam-gia/edit";
        }

        return "redirect:/admin/phieu-giam-gia";
    }

    @GetMapping("/delete/{id}")
    public String delete(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes) {

        PhieuGiamGia entity = phieuGiamGiaService.findById(id);

        if (entity == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy phiếu giảm giá này");
            return "redirect:/admin/phieu-giam-gia";
        }

        if (hoaDonService.existsByPhieuGiamGia(entity)) {
            ThongBaoUtils.addError(redirectAttributes, "Không thể xóa phiếu giảm giá đã được sử dụng");
            return "redirect:/admin/phieu-giam-gia";
        }

        try {
            phieuGiamGiaService.delete(id);
            ThongBaoUtils.addSuccess(redirectAttributes, "Xóa phiếu giảm giá thành công");
        } catch (Exception e) {
            ThongBaoUtils.addError(redirectAttributes, "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }

        return "redirect:/admin/phieu-giam-gia";
    }

    // Thread-safe method to generate unique code
    private synchronized String generateUniqueCode() {
        for (int attempt = 0; attempt < 50; attempt++) {
            String code = "PGG" + String.format("%06d", (int) (Math.random() * 1_000_000));
            if (!phieuGiamGiaRepository.existsByMa(code)) {
                return code;
            }
        }
        return null; // Failed to generate unique code after 50 attempts
    }
}