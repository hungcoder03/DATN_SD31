package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.MauSac;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.entity.Size;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.impl.Sanphamservice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/san-pham")
public class ListSanPhamController {
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
    private final SanPhamRepository sanPhamRepository;

    @GetMapping("/danh-sach")
    public String hienThiDanhSachSanPham(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="danhMucId", required=false) Integer danhMucId,
            @RequestParam(value="priceRange", required=false) String priceRange,
            @RequestParam(value="loaiThuId", required=false) Integer loaiThuId,

            Model model
    ) {
        // gọi đúng method mới
        List<SanPham> danhSachSanPham = sanPhamService.search(q, danhMucId, loaiThuId, priceRange);
        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // panel filter data
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());      // <-- thêm dòng này

        model.addAttribute("priceOptions", List.of(
                Map.of("value","0-100000","label","Dưới 100k"),
                Map.of("value","100000-300000","label","100k – 300k"),
                Map.of("value","300000-500000","label","300k – 500k"),
                Map.of("value","500000","label","Trên 500k")
        ));
        model.addAttribute("danhMucId", danhMucId);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("q", q);
        model.addAttribute("loaiThuId", loaiThuId);

        // 2. Lấy toàn bộ chi tiết để build các map giá / khuyến mãi…
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();

        // 2a. Giá gốc cao nhất mỗi sản phẩm
        Map<Integer, BigDecimal> giaGocMaxMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                                opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                        )
                ));

        // 2b. Giá bán thấp nhất (khuyến mãi) mỗi sản phẩm
        Map<Integer, BigDecimal> giaBanMinMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                        )
                ));

        // 2c. Phần trăm giảm cao nhất mỗi sản phẩm (nếu có)
        Map<Integer, Integer> phanTramGiamMap = chiTiets.stream()
                .filter(ct -> ct.getDotGiamGia() != null)
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(
                                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia()
                                )),
                                opt -> opt.map(ct -> ct.getDotGiamGia().getGiaTriDotGiamGia().intValue())
                                        .orElse(0)
                        )
                ));

        // 2d. Giá gốc thấp nhất mỗi sản phẩm
        Map<Integer, BigDecimal> giaGocMinMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                                opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                        )
                ));

        // 2e. Giá bán cao nhất mỗi sản phẩm
        Map<Integer, BigDecimal> giaBanMaxMap = chiTiets.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getSanPham().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                                opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                        )
                ));

        // 3. Đẩy lên model
        model.addAttribute("danhSachSanPham", danhSachSanPham);
        model.addAttribute("q", q);

        model.addAttribute("giaGocMaxMap", giaGocMaxMap);
        model.addAttribute("giaBanMinMap", giaBanMinMap);
        model.addAttribute("phanTramGiamMap", phanTramGiamMap);
        model.addAttribute("giaGocMinMap", giaGocMinMap);
        model.addAttribute("giaBanMaxMap", giaBanMaxMap);
        // Nếu còn map nào khác, cứ thêm tương tự:
        // model.addAttribute("giaKhuyenMaiMap", ...);

        return "khachhang/LISTSANPHAM";
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
    @GetMapping("/search")
    public String searchSanPham(
            @RequestParam("q") String q,
            Model model) {

        // Gọi service tìm theo tên chứa q
        List<SanPham> list = sanPhamRepository.search(q.trim());
        model.addAttribute("danhSachSanPham", list);

        // Để form binding (nếu template có dùng th:object)
        model.addAttribute("sanPham", new SanPham());

        // Giữ lại từ khoá và cờ search nếu bạn cần
        model.addAttribute("q", q);
        model.addAttribute("isSearch", true);

        return "khachhang/LISTSANPHAM";
    }
}
