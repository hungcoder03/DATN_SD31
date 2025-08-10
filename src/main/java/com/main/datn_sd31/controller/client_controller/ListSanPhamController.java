package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.impl.DanhGiaService;
import com.main.datn_sd31.service.impl.Sanphamservice;
import com.main.datn_sd31.util.GetKhachHang;
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
    private final SanPhamRepository sanPhamRepository;
    private final DanhGiaService danhGiaService;

    private final GetKhachHang getKhachHang;

    @GetMapping("/danh-sach")
    public String hienThiDanhSachSanPham(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="danhMucId", required=false) Integer danhMucId,
            @RequestParam(value="priceRange", required=false) String priceRange,
            @RequestParam(value="loaiThuId", required=false) Integer loaiThuId,
            @RequestParam(value="chatLieuId", required=false) Integer chatLieuId,
            @RequestParam(value="kieuDangId", required=false) Integer kieuDangId,
            @RequestParam(value="xuatXuId", required=false) Integer xuatXuId,

            Model model
    ) {
        if (getKhachHang.getCurrentKhachHang() != null) {
            Integer currentId = getKhachHang.getCurrentKhachHang().getId();
            model.addAttribute("idKhachHang", currentId);
        }
        // gọi đúng method mới
        List<SanPham> danhSachSanPham = sanPhamService.search(q, danhMucId, loaiThuId, chatLieuId, kieuDangId, xuatXuId, priceRange);
        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // panel filter data
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());      // <-- thêm dòng này
        model.addAttribute("chatLieus", chatLieuRepo.findAll());      // <-- thêm dòng này
        model.addAttribute("kieuDangs", kieuDangRepo.findAll());      // <-- thêm dòng này
        model.addAttribute("xuatXus", xuatXuRepo.findAll());      // <-- thêm dòng này

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
        model.addAttribute("chatLieuId", chatLieuId);
        model.addAttribute("kieuDangId", kieuDangId);
        model.addAttribute("xuatXuId", xuatXuId);

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
        if (getKhachHang.getCurrentKhachHang() != null) {
            model.addAttribute("khachHangLogin", getKhachHang.getCurrentKhachHang());
        }

        return "khachhang/LISTSANPHAM";
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSanPham(
            @PathVariable("id") Integer id,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="star", required=false) Integer star,
            Model model
    ) {
        // --- 1. LOAD SẢN PHẨM, HÌNH, SIZE, MÀU ---
        SanPham sanPham = sanPhamService.findbyid(id);
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh", hinhanhrepository.findByhinhanhid(id));

        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findBySanPhamId(id);

        // Màu sắc duy nhất
        List<MauSac> dsMauSac = chiTiets.stream()
                .map(ChiTietSanPham::getMauSac)
                .filter(ms -> ms != null && ms.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(MauSac::getId, Function.identity(), (a,b)->a),
                        m -> new ArrayList<>(m.values())
                ));
        model.addAttribute("dsMauSac", dsMauSac);
        model.addAttribute("mauSacCount", dsMauSac.size());

        // Size duy nhất
        List<Size> dsSize = chiTiets.stream()
                .map(ChiTietSanPham::getSize)
                .filter(sz -> sz != null && sz.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Size::getId, Function.identity(), (a,b)->a),
                        m -> new ArrayList<>(m.values())
                ));
        model.addAttribute("dsSize", dsSize);
        model.addAttribute("sizeCount", dsSize.size());

        // Chi tiết với tồn kho (dùng cho JS update giá + kho)
        List<Map<String,Object>> dsChiTietMap = chiTiets.stream()
                .filter(ct -> ct.getSize()!=null && ct.getMauSac()!=null)
                .map(ct -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", ct.getId());
                    m.put("giaBan", ct.getGiaBan());
                    m.put("size",   Map.of("id", ct.getSize().getId()));
                    m.put("mauSac", Map.of("id", ct.getMauSac().getId()));
                    m.put("soLuongTon", ct.getSoLuong());
                    return m;
                }).toList();
        model.addAttribute("dsChiTietSanPham", dsChiTietMap);


        // --- 2. TÍNH TOÁN REVIEW ---
        // 2.1: Load toàn bộ review (mới nhất trước)
        List<DanhGia> allReviews = danhGiaService.layDanhGiaChoSanPham(id);
        model.addAttribute("totalCount", allReviews.size());

        // 2.2: Đếm theo sao
        Map<Integer, Long> countByStar = Map.of(
                5, allReviews.stream().filter(r -> r.getSoSao()==5).count(),
                4, allReviews.stream().filter(r -> r.getSoSao()==4).count(),
                3, allReviews.stream().filter(r -> r.getSoSao()==3).count(),
                2, allReviews.stream().filter(r -> r.getSoSao()==2).count(),
                1, allReviews.stream().filter(r -> r.getSoSao()==1).count()
        );
        model.addAttribute("countByStar", countByStar);

        // 2.3: Tính điểm trung bình và format
        double avg = danhGiaService.tinhDiemTrungBinh(id);
        // Tách phần nguyên và phần thập phân
        int base = (int) Math.floor(avg);
        double frac = avg - base;

        String avgRatingStr = String.format(Locale.FRANCE, "%.1f", avg);
        int fullStars;
        boolean halfStar;
        // Áp theo ngưỡng: <0.3 → không half, [0.3, 0.8) → half, ≥0.8 → làm tròn lên
        if (frac < 0.3) {
            fullStars = base;
            halfStar   = false;
        } else if (frac < 0.8) {
            fullStars = base;
            halfStar   = true;
        } else {
            fullStars = base + 1;
            halfStar   = false;
        }
        int emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

        model.addAttribute("avgRatingStr", avgRatingStr);
        model.addAttribute("fullStars",    fullStars);
        model.addAttribute("halfStar",     halfStar);
        model.addAttribute("emptyStars",   emptyStars);
        model.addAttribute("avgRatingStr", String.format(Locale.FRANCE, "%.1f", avg));

        // --- 3. LỌC THEO STAR VÀ PHÂN TRANG ---
        List<DanhGia> filtered = (star == null)
                ? allReviews
                : allReviews.stream()
                .filter(r -> r.getSoSao().equals(star))
                .toList();
        model.addAttribute("starFilter", star);

        int pageSize = 6;
        int total = filtered.size();
        int totalPages = (total + pageSize) / pageSize;
        int from = page * pageSize;
        int to   = Math.min(from + pageSize, total);
        List<DanhGia> pageReviews = (from < total)
                ? filtered.subList(from, to)
                : Collections.emptyList();

        model.addAttribute("reviews",      pageReviews);
        model.addAttribute("currentPage",  page);
        model.addAttribute("totalPages",   totalPages);

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