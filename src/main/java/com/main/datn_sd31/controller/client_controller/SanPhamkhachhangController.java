package com.main.datn_sd31.controller.client_controller;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.entity.Size;
import com.main.datn_sd31.repository.Chatlieurepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class SanPhamkhachhangController {

    private final Sanphamservice sanPhamService;
    private final NhanVienRepository nhanvienRepo;
    private final Chatlieurepository chatLieuRepo;
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
    public String hienThiDanhSachSanPham(Model model) {
        List<SanPham> danhSach = sanPhamService.getAll();
        model.addAttribute("danhSachSanPham", danhSach);
        return "khachhang/dssanpham";
    }

    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSanPham(@PathVariable("id") Integer id, Model model) {
        List<ChiTietSanPham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);
        model.addAttribute("sanPham", sanPhamService.findbyid(id));
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh", hinhanhrepository.findByhinhanhid(id));

        // Gửi danh sách màu sắc duy nhất
        List<MauSac> dsMauSac = danhSachChiTiet.stream()
                .map(ChiTietSanPham::getMauSac)
                .filter(ms -> ms != null && ms.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(MauSac::getId, Function.identity(), (a, b) -> a),
                        map -> new ArrayList<>(map.values())
                ));
        model.addAttribute("dsMauSac", dsMauSac);
        model.addAttribute("mauSacCount", dsMauSac.size());

        // Gửi danh sách size duy nhất
        List<Size> dsSize = danhSachChiTiet.stream()
                .map(ChiTietSanPham::getSize)
                .filter(sz -> sz != null && sz.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Size::getId, Function.identity(), (a, b) -> a),
                        map -> new ArrayList<>(map.values())
                ));
        model.addAttribute("dsSize", dsSize);
        model.addAttribute("sizeCount", dsSize.size());

        // ✅ Gửi danh sách chi tiết với tồn kho - SỬA LỖI ở đây
        // ✅ Gửi danh sách chi tiết với tồn kho - đã fix null
        model.addAttribute("dsChiTietSanPham", danhSachChiTiet.stream()
                .filter(ct -> ct.getSize() != null && ct.getMauSac() != null &&
                        ct.getSize().getId() != null && ct.getMauSac().getId() != null)
                .map(ct -> {
                    Map<String, Object> chiTietMap = new HashMap<>();
                    chiTietMap.put("id", ct.getId());
                    chiTietMap.put("giaBan", ct.getGiaBan());
                    Map<String, Object> sizeMap = new HashMap<>();
                    sizeMap.put("id", ct.getSize().getId());
                    chiTietMap.put("size", sizeMap);

                    Map<String, Object> mauMap = new HashMap<>();
                    mauMap.put("id", ct.getMauSac().getId());
                    chiTietMap.put("mauSac", mauMap);

                    chiTietMap.put("soLuongTon", ct.getSoLuong());
                    return chiTietMap;
                })
                .collect(Collectors.toList()));

        return "khachhang/xemchitiet";
    }

}




