package com.main.datn_sd31.controller.client_controller;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.entity.Size;
import com.main.datn_sd31.repository.ChatLieuRepository;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.Loaithurepository;
import com.main.datn_sd31.repository.Mausacrepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.Sizerepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import com.main.datn_sd31.service.impl.Sanphamservice;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class SanPhamkhachhangController {

    private final Sanphamservice sanPhamService;
    private final NhanVienRepository nhanvienRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final Danhmucrepository danhMucRepo;
    private final Thuonghieurepository thuongHieuRepo;
    private final Xuatxurepository xuatXuRepo;
    private final Kieudangrepository kieuDangRepo;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;
    private final Xuatxurepository xuatxurepository;
    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Loaithurepository loaithurepository;
    private final Dotgiamgiarepository dotgiamgiarepository;

    @GetMapping("/danh-sach")
    public String hienThiDanhSachSanPham(Model model, HttpSession session) {
        List<SanPham> danhSachSanPham = sanPhamService.getAllActive();
        // Chỉ lấy chi tiết của sản phẩm đang hoạt động
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll().stream()
            .filter(ct -> ct.getSanPham() != null && ct.getSanPham().getTrangThai())
            .collect(Collectors.toList());

        Map<Integer, BigDecimal> giaGocMap = chiTiets.stream()
                .collect(Collectors.groupingBy(ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                        )
                ));

        Map<Integer, BigDecimal> giaKhuyenMaiMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                optionalMin -> {
                                    BigDecimal min = optionalMin.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO);
                                    BigDecimal max = giaGocMap.getOrDefault(
                                            optionalMin.map(ChiTietSanPham::getSanPham)
                                                    .map(SanPham::getId)
                                                    .orElse(-1),
                                            BigDecimal.ZERO
                                    );
                                    return min.compareTo(max) < 0 ? min : null;
                                }
                        )
                ));
// Sau khi đã load danhSachSanPham và danhSachChiTietSp
        Map<Integer, Integer> phanTramGiamMap = chiTiets.stream()
                .filter(ct -> ct.getDotGiamGia() != null)
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(
                                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia())),
                                opt -> opt
                                        .map(ct -> ct.getDotGiamGia().getGiaTriDotGiamGia().intValue())
                                        .orElse(0)
                        )
                ));
        Map<Integer, BigDecimal> giaGocThapNhatMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                                opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                        )
                ));
        Map<Integer, BigDecimal> giaBanMaxMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                        )
                ));

        Map<Integer, BigDecimal> giaBanMinMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                        )
                ));

        Map<Integer, BigDecimal> giaGocMinMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                                opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                        )
                ));
        Map<Integer, BigDecimal> giaGocMaxMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                                opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                        )
                ));
        model.addAttribute("giaGocMaxMap", giaGocMaxMap);
        model.addAttribute("giaBanMaxMap", giaBanMaxMap);
        model.addAttribute("giaBanMinMap", giaBanMinMap);
        model.addAttribute("giaGocMinMap", giaGocMinMap);
        // Set thuộc tính dotGiamGia cho mỗi sản phẩm
        Map<Integer, DotGiamGia> dotGiamGiaMap = chiTiets.stream()
            .filter(ct -> ct.getDotGiamGia() != null)
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(
                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia())),
                    opt -> opt.map(ChiTietSanPham::getDotGiamGia).orElse(null)
                )
            ));

        // Set dotGiamGia cho mỗi sản phẩm
        danhSachSanPham.forEach(sanPham -> {
            sanPham.setDotGiamGia(dotGiamGiaMap.get(sanPham.getId()));
        });

        model.addAttribute("phanTramGiamMap", phanTramGiamMap);
        model.addAttribute("danhSachSanPham", danhSachSanPham);
        model.addAttribute("giagoc", giaGocThapNhatMap);
        model.addAttribute("phanTramGiamMap", phanTramGiamMap);
        model.addAttribute("danhSachSanPham", danhSachSanPham);
        model.addAttribute("giaGocMap", giaGocMap);
        model.addAttribute("giaKhuyenMaiMap", giaKhuyenMaiMap);

        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        model.addAttribute("khachHangLogin", khachHang);
        return "client/pages/product/search";
    }



    // Endpoint này đã được di chuyển sang ListSanPhamController để tránh conflict
    // @GetMapping("/chi-tiet/{id}") - REMOVED

}