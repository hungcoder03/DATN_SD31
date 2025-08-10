package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dot-giam-gia")
@RequiredArgsConstructor
public class dotGiamGiaController {

    private final Dotgiamgiarepository dotGiamGiaRepository;

    //  Cập nhật trạng thái tự động theo thời gian
    private void capNhatTrangThaiTuDong() {
        List<DotGiamGia> list = dotGiamGiaRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (DotGiamGia dgg : list) {
            if (dgg.getNgayBatDau() == null || dgg.getNgayKetThuc() == null) continue;

            if (now.isBefore(dgg.getNgayBatDau())) {
                dgg.setTrangThai(0); // Chuẩn bị áp dụng
            } else if (now.isAfter(dgg.getNgayKetThuc())) {
                dgg.setTrangThai(2); // Ngừng hoạt động
            } else {
                dgg.setTrangThai(1); // Đang hoạt động
            }
        }
        dotGiamGiaRepository.saveAll(list);
    }

    @Autowired
    private Chitietsanphamrepository chiTietSanPhamRepo;

    @GetMapping("/{id}/san-pham")
    public String xemSanPhamApDung(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        DotGiamGia dot = dotGiamGiaRepository.findById(id).orElse(null);
        Pageable pageable = PageRequest.of(page, 5);
        Page<ChiTietSanPham> sanPhams = chiTietSanPhamRepo.findByDotGiamGia_Id(id, pageable);

        model.addAttribute("dot", dot);
//        model.addAttribute("dotGiamGia", new DotGiamGia());
        model.addAttribute("sanPhams", sanPhams);
        model.addAttribute("currentPage", page);

        return "admin/pages/dot-giam-gia/san-pham-ap-dung";
    }
    @GetMapping("/tim-kiem")
    public String index(
            @RequestParam(value = "ten", required = false) String ten,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "loai", required = false) String loai,
            Model model) {
        List<DotGiamGia> dotGiamGias = dotGiamGiaRepository.findAll();

        if (ten != null && !ten.trim().isEmpty()) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getTen() != null && d.getTen().toLowerCase().contains(ten.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (trangThai != null) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getTrangThai() == trangThai)
                    .collect(Collectors.toList());
        }

        if (loai != null && !loai.trim().isEmpty()) {
            dotGiamGias = dotGiamGias.stream()
                    .filter(d -> d.getLoai() != null && d.getLoai().equalsIgnoreCase(loai))
                    .collect(Collectors.toList());
        }

        model.addAttribute("dotGiamGias", dotGiamGias);
        model.addAttribute("dotGiamGia", new DotGiamGia()); // nếu bạn đang dùng form thêm
        return "admin/pages/dot-giam-gia/dot-giam-gia"; // tên template của bạn
    }

    //  Trang chính: hiển thị danh sách + form thêm/sửa
    @GetMapping
    public String hienthi(Model model) {
        capNhatTrangThaiTuDong();
        model.addAttribute("dotGiamGia", new DotGiamGia());
        model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
        return "admin/pages/dot-giam-gia/dot-giam-gia";
    }
//    @GetMapping("/admin/dot-giam-gia")
//    public String index(Model model,
//                        @RequestParam(defaultValue = "0") int page) {
//        int pageSize = 5; // 5 phần tử mỗi trang
//        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("ngayBatDau").descending());
//
//        Page<DotGiamGia> dotGiamGiaPage = dotGiamGiaRepository.findAll(pageable);
//
//        model.addAttribute("dotGiamGia", new DotGiamGia());
//        model.addAttribute("dotGiamGias", dotGiamGiaPage.getContent()); // danh sách hiện tại
//        model.addAttribute("totalPages", dotGiamGiaPage.getTotalPages());
//        model.addAttribute("currentPage", page);
//
//        return "admin/pages/dotGiamGia/dotGiamGia";
//    }

    //  Khi click sửa → load lại form với dữ liệu cũ
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        capNhatTrangThaiTuDong();
        DotGiamGia dotGiamGia = dotGiamGiaRepository.findById(id).orElse(new DotGiamGia());
        model.addAttribute("dotGiamGia", dotGiamGia);
        model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
        return "admin/pages/dot-giam-gia/dot-giam-gia";
    }

    //  Lưu (thêm hoặc cập nhật)
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("dotGiamGia") DotGiamGia dotGiamGia,
                       BindingResult result, RedirectAttributes redirectAttributes,
                       Model model) {

        // Kiểm tra ngày bắt đầu trước ngày kết thúc
        if (dotGiamGia.getNgayBatDau() != null && dotGiamGia.getNgayKetThuc() != null
                && dotGiamGia.getNgayBatDau().isAfter(dotGiamGia.getNgayKetThuc())) {
            result.rejectValue("ngayBatDau", null, "Ngày bắt đầu phải trước ngày kết thúc");
        }

        if (result.hasErrors()) {
            // Validation errors found
            model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
            return "admin/pages/dot-giam-gia/dot-giam-gia"; // Quay lại view nếu lỗi
        }
        if (dotGiamGia.getId() == null) {
            // THÊM MỚI
            dotGiamGia.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            DotGiamGia existing = dotGiamGiaRepository.findById(dotGiamGia.getId()).orElse(null);
            if (existing != null) {
                dotGiamGia.setNgayTao(existing.getNgayTao());
            }
        }


        // Ngày sửa luôn được cập nhật
        dotGiamGia.setNgaySua(LocalDateTime.now());
        int adminId = 1; //  ID mặc định cho admin

        if (dotGiamGia.getId() == null) {
            dotGiamGia.setNguoiTao(adminId);
        } else {
            dotGiamGia.setNguoiTao(adminId);
        }
        capNhatTrangThaiTuDong();
        boolean isNew = (dotGiamGia.getId() == null);
        dotGiamGiaRepository.save(dotGiamGia);
        if (isNew) {
            redirectAttributes.addFlashAttribute("success", "Thêm đợt giảm giá thành công!");
        } else {
            redirectAttributes.addFlashAttribute("success", "Cập nhật đợt giảm giá thành công!");
        }

        return "redirect:/admin/dot-giam-gia";
    }

    //  Xoá
//    @GetMapping("/delete/{id}")
//    public String delete(@PathVariable Integer id) {
//        dotGiamGiaRepository.deleteById(id);
//        return "redirect:/admin/dot-giam-gia";
//    }
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        Optional<DotGiamGia> optional = dotGiamGiaRepository.findById(id);
        if (optional.isPresent()) {
            DotGiamGia dot = optional.get();
            if (dot.getTrangThai() != 0) {
                attributes.addFlashAttribute("error", "Không thể xóa đợt giảm giá khi chuẩn bị hoạt động.");
                return "redirect:/admin/dot-giam-gia";
            }
            if (dot.getTrangThai() != 1) {
                attributes.addFlashAttribute("error", "Không thể xóa đợt giảm giá khi đang hoạt động.");
                return "redirect:/admin/dot-giam-gia";
            }

            dotGiamGiaRepository.deleteById(id);
            attributes.addFlashAttribute("success", "Xóa thành công!");
        } else {
            attributes.addFlashAttribute("error", "Không tìm thấy đợt giảm giá.");
        }
        return "redirect:/admin/dot-giam-gia";
    }
}