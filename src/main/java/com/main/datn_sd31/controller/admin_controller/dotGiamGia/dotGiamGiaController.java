package com.main.datn_sd31.controller.admin_controller.dotGiamGia;

import com.main.datn_sd31.entity.ChatLieu;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.repository.dotGiamGia.DotGiamGiaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/dot-giam-gia")
public class dotGiamGiaController {

    @Autowired
    DotGiamGiaRepository dotGiamGiaRepository;

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

    //  Trang chính: hiển thị danh sách + form thêm/sửa
    @GetMapping
    public String hienthi(Model model) {
        capNhatTrangThaiTuDong();
        model.addAttribute("dotGiamGia", new DotGiamGia());
        model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
        return "admin/pages/dotGiamGia/dotGiamGia";
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
        return "admin/pages/dotGiamGia/dotGiamGia";
    }

    //  Lưu (thêm hoặc cập nhật)
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("dotGiamGia") DotGiamGia dotGiamGia,
                       BindingResult result,
                       Model model) {

        // Kiểm tra ngày bắt đầu trước ngày kết thúc
        if (dotGiamGia.getNgayBatDau() != null && dotGiamGia.getNgayKetThuc() != null
                && dotGiamGia.getNgayBatDau().isAfter(dotGiamGia.getNgayKetThuc())) {
            result.rejectValue("ngayBatDau", null, "Ngày bắt đầu phải trước ngày kết thúc");
        }

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.toString()));
            model.addAttribute("dotGiamGias", dotGiamGiaRepository.findAll());
            return "admin/pages/dotGiamGia/dotGiamGia"; // Quay lại view nếu lỗi
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
        int adminId = 1; // 👈 ID mặc định cho admin

        if (dotGiamGia.getId() == null) {
            dotGiamGia.setNguoiTao(adminId);
        } else {
            dotGiamGia.setNguoiSua(adminId);
        }
        capNhatTrangThaiTuDong();
        dotGiamGiaRepository.save(dotGiamGia);

        return "redirect:/admin/dot-giam-gia";
    }

    //  Xoá
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        dotGiamGiaRepository.deleteById(id);
        return "redirect:/admin/dot-giam-gia";
    }
}
