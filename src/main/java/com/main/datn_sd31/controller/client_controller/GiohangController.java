package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.service.impl.Giohangservice;
import com.main.datn_sd31.service.impl.Sanphamservice;
import com.main.datn_sd31.util.VnPayUtils;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/gio-hang")
public class GiohangController {

    private final Sanphamservice sanPhamService;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;
    private final Xuatxurepository xuatxurepository;
    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Giohangreposiroty giohangreposiroty;
    private final Giohangservice giohangservice;
    private final KhachHangRepository khachhangrepository;
    private final HoaDonRepository hoadonreposiroty;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final NhanVienRepository nhanvienrepository;
    private final HoaDonChiTietRepository hoadonCTreposiroty;
    private final LichSuHoaDonRepository lichsuhoadonrepository;

    @Autowired
    private GHNService ghnService;
    @Autowired
    private VnPayUtils vnPayUtils;

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

        return "/view/giohang/list";
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

        giohangreposiroty.save(gh);
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

        model.addAttribute("danhSachPhieuGiamGia", phieugiamgiarepository.findAll());
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
        return "/view/giohang/thanh-toan";
    }

    @PostMapping("/thanh-toan/xac-nhan")
    public String xacNhanThanhToan(@RequestParam("phuongThucThanhToan") String phuongThuc,
                                   @RequestParam("selectedId") List<Integer> selectedItemIds,
                                   @RequestParam Map<String, String> formData,
                                   HttpSession session,
                                   HttpServletRequest request,
                                   Model model) {

        String diaChiChiTiet = formData.get("diaChi");
        String fullAddress = diaChiChiTiet + ", " + formData.get("tenXa") + ", " +
                formData.get("tenHuyen") + ", " + formData.get("tenTinh");

        List<GioHangChiTiet> gioHangChiTiets = giohangreposiroty.findAllById(selectedItemIds);
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
        hoaDon.setNgayMua(LocalDateTime.now());
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
        if ("chuyen_khoan".equals(phuongThuc)) {
            hoaDon.setTrangThai(true); // Chưa thanh toán vì đang chờ VNPay callback
        } else {
            hoaDon.setTrangThai(false); // Tiền mặt, được coi là sẽ thanh toán khi giao hàng
        }

        hoadonreposiroty.save(hoaDon);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setTrangThai(1);
        lichSu.setNguoiTao(hoaDon.getNguoiTao());
        lichSu.setNguoiSua(hoaDon.getNguoiSua());
        lichSu.setGhiChu("Tạo đơn hàng mới, chờ xác nhận");
        lichsuhoadonrepository.save(lichSu);

        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham spct = item.getChiTietSp();
            int soLuongMua = item.getSoLuong();

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hoaDon);
            ct.setChiTietSanPham(spct);
            ct.setSoLuong(soLuongMua);
            ct.setGiaGoc(spct.getGiaBan());
            ct.setGiaSauGiam(spct.getGiaBan().subtract(tienGiam));
            ct.setGiaGiam(tienGiam);

            // 👉 Gộp tên sản phẩm + chi tiết
            String tenSanPham = spct.getSanPham().getTen();
            String chiTiet = spct.getTenCt();
            ct.setTenCtsp(tenSanPham + " - " + chiTiet);

            hoadonCTreposiroty.save(ct);
        }


        giohangreposiroty.deleteAll(gioHangChiTiets);

        if ("chuyen_khoan".equals(phuongThuc)) {
            String orderInfo = "Thanh toan don hang #" + hoaDon.getId();
            long amount = hoaDon.getThanhTien().longValue();
            String vnpUrl = vnPayUtils.createVnpayUrl(amount, orderInfo, hoaDon.getMa(), request);
            return "redirect:" + vnpUrl;
        }
        model.addAttribute("maHoaDon", hoaDon.getMa());
        return "khachhang/thanhcong";
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
    @GetMapping("/thanh-toan/vnpay-return")
    public String ketQuaVnpay(@RequestParam Map<String, String> params, Model model) {
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String orderCode = params.get("vnp_TxnRef");

        boolean thanhCong = "00".equals(responseCode) && "00".equals(transactionStatus);

        HoaDon hoaDon = hoadonreposiroty.findByMa(orderCode);

        if (thanhCong) {
            hoaDon.setTrangThai(true); // ĐÃ THANH TOÁN
            hoaDon.setNgaySua(LocalDateTime.now());
            hoadonreposiroty.save(hoaDon);

            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setTrangThai(1);
            lichSu.setGhiChu("Đã thanh toán qua VNPAY");
            lichSu.setNguoiTao(1);
            lichSu.setNguoiSua(1);
            lichsuhoadonrepository.save(lichSu);

            model.addAttribute("message", "Thanh toán thành công!");
        } else {
            model.addAttribute("message", "Thanh toán thất bại hoặc bị hủy!");
        }

        return "view/khachhang/thanhcong";
    }

    @GetMapping("/phieu-giam-gia/tien-giam")
    @ResponseBody
    public ResponseEntity<BigDecimal> tinhTienGiam(
            @RequestParam("maPhieu") String maPhieu,
            @RequestParam("tongTien") BigDecimal tongTien) {
        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(maPhieu);
        if (phieu == null) return ResponseEntity.ok(BigDecimal.ZERO);

        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu.getLoaiPhieuGiamGia()==1) {
            tienGiam = tongTien.multiply(phieu.getMucDo())
                    .divide(BigDecimal.valueOf(100));
            if (tienGiam.compareTo(phieu.getGiamToiDa()) > 0) {
                tienGiam = phieu.getGiamToiDa();
            }
        } else{
            tienGiam = phieu.getMucDo();
        }

        return ResponseEntity.ok(tienGiam);
    }
}
