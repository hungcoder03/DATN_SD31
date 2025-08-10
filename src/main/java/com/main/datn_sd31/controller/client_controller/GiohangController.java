package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.service.impl.Giohangservice;
import com.main.datn_sd31.service.impl.Sanphamservice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/gio-hang")
public class GiohangController {

    private final Sanphamservice sanPhamService;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;
    private final PhieuGiamGiaService phieuGiamGiaService;
    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Giohangreposiroty giohangreposiroty;
    private final Giohangservice giohangservice;
    private final KhachHangRepository khachhangrepository;
    private final HoaDonRepository hoadonreposiroty;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final NhanVienRepository nhanvienrepository;
    private final HoaDonChiTietRepository hoadonCTreposiroty;

    @Autowired
    private GHNService ghnService;

    private KhachHang getCurrentKhachHang() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return khachhangrepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
    }


    @Transactional
    @GetMapping("/hien_thi")
    public String hienthi(Model model) {
        List<GioHangChiTiet> giohangList = giohangreposiroty.findAll();

        Map<String, GioHangChiTiet> gopMap = new LinkedHashMap<>();
        for (GioHangChiTiet item : giohangList) {
            String key = item.getChiTietSp().getId() + "_" +
                    item.getChiTietSp().getSize().getId() + "_" +
                    item.getChiTietSp().getMauSac().getId();

            if (gopMap.containsKey(key)) {
                GioHangChiTiet daCo = gopMap.get(key);
                daCo.setSoLuong(daCo.getSoLuong() + item.getSoLuong());
                daCo.setThanhTien(daCo.getThanhTien().add(item.getThanhTien()));
            } else {
                gopMap.put(key, item);
            }
        }

        giohangreposiroty.deleteAll();
        giohangreposiroty.flush();
        KhachHang kh = getCurrentKhachHang();
        List<GioHangChiTiet> newList = new ArrayList<>();
        for (GioHangChiTiet item : gopMap.values()) {
            GioHangChiTiet newItem = new GioHangChiTiet();
            newItem.setKhachHang(kh);
            newItem.setChiTietSp(item.getChiTietSp());
            newItem.setSoLuong(item.getSoLuong());
            newItem.setThanhTien(item.getThanhTien());
            newItem.setTrangThai(item.getTrangThai());
            newList.add(newItem);
        }
        giohangreposiroty.saveAll(newList);

        BigDecimal tongTien = BigDecimal.ZERO;
        for (GioHangChiTiet item : newList) {
            tongTien = tongTien.add(item.getThanhTien());
        }


        model.addAttribute("list", newList);
        model.addAttribute("tongTien", tongTien);

        System.out.println("▶ Đã vào controller: /gio-hang/hien_thi");
        return "view/giohang/list";
    }

    @PostMapping("/them")
    public String xuLyThem(@RequestParam("sanPhamId") Integer sanphamId,
                           @RequestParam("sizeId") Integer sizeId,
                           @RequestParam("mauSacId") Integer mauSacId,
                           @RequestParam("soLuong") Integer soluong,
                           Model model) {

        ChiTietSanPham chiTiet = chitietsanphamRepo.findBySanPhamIdAndSizeIdAndMauSacId(sanphamId, sizeId, mauSacId);
        GioHangChiTiet gh = new GioHangChiTiet();
        KhachHang kh = getCurrentKhachHang();
        gh.setKhachHang(kh);
        gh.setChiTietSp(chiTiet);
        gh.setSoLuong(soluong);
        gh.setTrangThai(0);
        gh.setThanhTien(chiTiet.getGiaBan().multiply(BigDecimal.valueOf(soluong)));

        if(chiTiet.getSoLuong()<soluong){
            model.addAttribute("messa", "Số lượng không đủ");
        }
        giohangreposiroty.save(gh);
        return "redirect:/gio-hang/hien_thi";
    }

    @GetMapping("/xoa/{id}")
    public String xoaSanPhamKhoiGio(@PathVariable("id") Integer id,
                                    RedirectAttributes redirectAttributes) {
        try {
            if (giohangreposiroty.existsById(id)) {
                giohangreposiroty.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại trong giỏ hàng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thất bại: " + e.getMessage());
        }
        return "redirect:/gio-hang/hien_thi";
    }
    @GetMapping("/cap-nhat/{id}")
    public String capNhatSoLuong(
            @PathVariable("id") Integer id,
            @RequestParam("action") String action,
            @RequestParam(value = "newSoluong", required = false) Integer newSoluong,
            @RequestParam(value = "soluong", required = false) Integer soluong
    ) {
        Optional<GioHangChiTiet> optionalItem = giohangreposiroty.findById(id);
        if (!optionalItem.isPresent()) {
            return "redirect:/gio-hang/hien_thi";
        }
        GioHangChiTiet item = optionalItem.get();

        int soLuongMoi;
        if (newSoluong != null && newSoluong > 0 && !"increase".equals(action) && !"decrease".equals(action)) {
            // Nếu nhập tay (không phải bấm + hoặc -)
            soLuongMoi = newSoluong;
        } else if ("increase".equals(action)) {
            soLuongMoi = item.getSoLuong() + 1;
        } else if ("decrease".equals(action)) {
            soLuongMoi = Math.max(item.getSoLuong() - 1, 1);
        } else {
            soLuongMoi = item.getSoLuong();
        }

        item.setSoLuong(soLuongMoi);
        BigDecimal giaBan = item.getChiTietSp().getGiaBan();
        item.setThanhTien(giaBan.multiply(BigDecimal.valueOf(soLuongMoi)));

        giohangreposiroty.save(item);

        return "redirect:/gio-hang/hien_thi";
    }

    @GetMapping("/thanh-toan")
    public String hienThiTrangThanhToan(
            @RequestParam(value = "selectedId", required = false) List<Integer> selectedIds,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) String wardCode,
            HttpServletRequest request,
            Model model) {

        if (selectedIds == null) {
            selectedIds = Collections.emptyList();
        }

        KhachHang kh = getCurrentKhachHang();
        List<GioHangChiTiet> selectedItems = giohangservice.findByIds(selectedIds);
        BigDecimal tongTien = selectedItems.stream()
                .map(item -> item.getChiTietSp().getGiaBan()
                        .multiply(BigDecimal.valueOf(item.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now();

        List<PhieuGiamGia> dsPhieuHopLe = phieugiamgiarepository.findAll().stream()
                // Lọc trạng thái true
                .filter(p -> Boolean.TRUE.equals(p.getTrangThai()))
                // Lọc ngày hiệu lực: ngày hiện tại >= ngày bắt đầu (nếu có) và <= ngày kết thúc (nếu có)
                .filter(p -> {
                    boolean afterStart = p.getNgayBatDau() == null || !today.isBefore(p.getNgayBatDau());
                    boolean beforeEnd = p.getNgayKetThuc() == null || !today.isAfter(p.getNgayKetThuc());
                    return afterStart && beforeEnd;
                })
                .collect(Collectors.toList());

        model.addAttribute("danhSachPhieuGiamGia", dsPhieuHopLe);
        model.addAttribute("selectedItems", selectedItems);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("khachHang", kh);

        List<Map<String, Object>> provinces = ghnService.getProvinces();
        model.addAttribute("provinces", provinces);

        if (provinceId != null) {
            List<Map<String, Object>> districts = ghnService.getDistricts(provinceId);
            model.addAttribute("districts", districts);
        }

        if (districtId != null) {
            List<Map<String, Object>> wards = ghnService.getWards(districtId);
            model.addAttribute("wards", wards);
        }
        List<PhieuGiamGia> danhSachPhieuGiamGia = phieugiamgiarepository.findAll();
        model.addAttribute("danhSachPhieuGiamGia", danhSachPhieuGiamGia);

// Xử lý chọn mã giảm tốt nhất
        String selectedVoucherCode = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (PhieuGiamGia phieu : danhSachPhieuGiamGia) {
            BigDecimal discount = phieuGiamGiaService.tinhTienGiam(phieu.getMa(), tongTien); // <-- Gọi đến service bạn đang dùng
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                selectedVoucherCode = phieu.getMa();
            }
        }

        model.addAttribute("selectedVoucherCode", selectedVoucherCode);
        return "/view/giohang/thanh-toan";
    }

    @PostMapping("/thanh-toan/xac-nhan")
    @Transactional
    public String xacNhanThanhToan(@RequestParam("phuongThucThanhToan") String phuongThuc,
                                   @RequestParam("selectedId") List<Integer> selectedItemIds,
                                   @RequestParam Map<String, String> formData,
                                   HttpSession session,
                                   HttpServletRequest request,
                                   RedirectAttributes redirect,
                                   Model model) {

        String diaChiChiTiet = formData.get("diaChi");
        String fullAddress = diaChiChiTiet + ", " + formData.get("tenXa") + ", " +
                formData.get("tenHuyen") + ", " + formData.get("tenTinh");

        KhachHang khachHang = (KhachHang) session.getAttribute("khachHang");
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        List<GioHangChiTiet> gioHangChiTiets = giohangreposiroty.findAllById(selectedItemIds);
        // 4. Lọc sản phẩm khách đã chọn từ giỏ hàng của chính họ
        BigDecimal tongTienGoc = BigDecimal.ZERO;
        for (GioHangChiTiet item : gioHangChiTiets) {
            BigDecimal gia = item.getChiTietSp().getGiaBan();
            tongTienGoc = tongTienGoc.add(gia.multiply(BigDecimal.valueOf(item.getSoLuong())));
        }

        BigDecimal tienGiam = new BigDecimal(formData.getOrDefault("tienGiam", "0"));
        BigDecimal phiVanChuyen = new BigDecimal(formData.getOrDefault("tienVanChuyen", "0"));
        BigDecimal thanhTien = tongTienGoc.subtract(tienGiam).add(phiVanChuyen);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMa("HD" + System.currentTimeMillis());
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setNgayThanhToan(LocalDateTime.now());
        hoaDon.setNgaySua(LocalDateTime.now());
        hoaDon.setPhuongThuc(phuongThuc);
        hoaDon.setLoaihoadon(formData.get("loaiHoaDon"));
        hoaDon.setTenNguoiNhan(formData.get("tenNguoiNhan"));
        hoaDon.setSoDienThoai(formData.get("soDienThoai"));
        hoaDon.setEmail(formData.get("email"));
        hoaDon.setGhiChu(formData.get("ghiChu"));
        hoaDon.setDiaChi(fullAddress);
        hoaDon.setGiaGoc(tongTienGoc);
        hoaDon.setGiaGiamGia(tienGiam);
        hoaDon.setPhiVanChuyen(phiVanChuyen);
        hoaDon.setThanhTien(thanhTien);

        KhachHang kh = getCurrentKhachHang();
        NhanVien nv = nhanvienrepository.find(1);
        hoaDon.setKhachHang(kh);
        hoaDon.setNhanVien(nv);
        hoaDon.setNguoiTao(1);
        hoaDon.setNguoiSua(1);

        if (formData.containsKey("phieuGiamGia") && !formData.get("phieuGiamGia").isBlank()) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(formData.get("phieuGiamGia"));
            hoaDon.setPhieuGiamGia(phieu);
        }
        hoaDon.setTrangThai(1);
        hoadonreposiroty.save(hoaDon);
        if (phuongThuc.equalsIgnoreCase("tien_mat")) {
            xuLySauKhiDatHang(hoaDon, gioHangChiTiets, tienGiam,2);
            model.addAttribute("ma",  hoaDon.getMa());
            model.addAttribute("message", "Đặt hàng tiền mặt thành công!");
        }
        if ("chuyen_khoan".equalsIgnoreCase(phuongThuc)) {
            String ids = gioHangChiTiets.stream()
                    .map(ct -> String.valueOf(ct.getId()))
                    .collect(Collectors.joining(","));

            return "redirect:/thanh-toan-vnpay?maHoaDon=" + hoaDon.getMa()+ "&ids=" + ids;
        }
        model.addAttribute("maHoaDon", hoaDon.getMa());
        return "khachhang/thanhcong";
    }
    public void xuLySauKhiDatHang(HoaDon hoaDon, List<GioHangChiTiet> gioHangChiTiets, BigDecimal tienGiam, int trangThai) {
        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham ctsp = item.getChiTietSp();
            int soLuong = item.getSoLuong();

            HoaDonChiTiet hdct = new HoaDonChiTiet();
            hdct.setHoaDon(hoaDon);
            hdct.setChiTietSanPham(ctsp);
            hdct.setSoLuong(soLuong);
            hdct.setGiaGoc(ctsp.getGiaBan());
            hdct.setGiaGiam(tienGiam);
            hdct.setGiaSauGiam(ctsp.getGiaBan().subtract(tienGiam));
            hdct.setTenCtsp(ctsp.getSanPham().getTen() + " - " + ctsp.getTenCt());

            hoadonCTreposiroty.save(hdct);
        }

        if (hoaDon.getPhieuGiamGia() != null) {
            PhieuGiamGia phieu = hoaDon.getPhieuGiamGia();
            if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0) {
                phieu.setSoLuongTon(phieu.getSoLuongTon() - 1);
                phieugiamgiarepository.save(phieu);
            }
        }

        // Xóa khỏi giỏ hàng
        System.out.println("id san pham gio hang"+gioHangChiTiets);
        giohangreposiroty.deleteAll(gioHangChiTiets);
        System.out.println("hoa don id"+hoaDon.getId());
        HoaDon hd = hoadonreposiroty.findById(hoaDon.getId()).orElse(null);

        hd.setTrangThai(trangThai);
        hoadonreposiroty.save(hd);

    }


    @GetMapping("/thanh-toan/location")
    @ResponseBody
    public List<Map<String, Object>> getLocation(
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId) {
        if (provinceId != null) {
            return ghnService.getDistricts(provinceId);
        }
        if (districtId != null) {
            return ghnService.getWards(districtId);
        }
        return List.of();
    }
    @GetMapping("/thanh-toan/shipping-fee")
    @ResponseBody
    public ResponseEntity<?> getShippingFee(@RequestParam("districtId") int districtId,
                                            @RequestParam("wardCode") String wardCode) {
        int fromDistrictId = 3440;
        int weight = 500;

        System.out.println("Request shipping fee - toDistrictId: " + districtId + ", toWardCode: " + wardCode);

        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, districtId);
        System.out.println("Available services: " + services);

        if (services.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy dịch vụ vận chuyển phù hợp.");
        }

        int serviceId = (Integer) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, districtId, wardCode, weight, serviceId);

        if (fee == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi tính phí vận chuyển hoặc phí bằng 0.");
        }

        return ResponseEntity.ok(fee);
    }
    @GetMapping("/phieu-giam-gia/tien-giam")
    @ResponseBody
    public ResponseEntity<BigDecimal> tinhTienGiam(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam("tongTien") BigDecimal tongTien) {
        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(maPhieu);
        if (phieu == null) return ResponseEntity.ok(BigDecimal.ZERO);

        LocalDate today = LocalDate.now();

        // Kiểm tra ngày hiệu lực
        if (phieu.getNgayBatDau() != null && today.isBefore(phieu.getNgayBatDau())) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
        if (phieu.getNgayKetThuc() != null && today.isAfter(phieu.getNgayKetThuc())) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }

        // Kiểm tra tổng tiền tối thiểu áp dụng giảm giá
        if (phieu.getDieuKien() != null && tongTien.compareTo(phieu.getDieuKien()) < 0) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }

        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu.getLoaiPhieuGiamGia() == 1) {
            tienGiam = tongTien.multiply(phieu.getMucDo())
                    .divide(BigDecimal.valueOf(100));
            if (tienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                tienGiam = phieu.getGiamToiDa();
            }
        } else {
            tienGiam = phieu.getMucDo();
        }

        return ResponseEntity.ok(tienGiam);
    }

}
