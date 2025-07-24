package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.util.GetNhanVien;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ban-hang")
@RequiredArgsConstructor
@Transactional
public class BanHangController {

    private final GetNhanVien getNhanVien;
    private final Chitietsanphamrepository chiTietSanPhamRepository;
    private final HoaDonRepository hoaDonRepository;
    private final Chitiethoadonrepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanphamrepository;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final GHNService ghnService;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;


    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts == null) {
            carts = new HashMap<>();
            session.setAttribute("tatCaGio", carts);
        }
        return carts.computeIfAbsent(cartKey, k -> new ArrayList<>());
    }

    @GetMapping
    public String hienThiSanPham(@RequestParam(value = "idSanPham", required = false) Integer idSanPham,
                                 @RequestParam(value = "cartKey", defaultValue = "gio-1") String cartKey,
                                 Model model, HttpSession session) {

        // Danh sách sản phẩm
        List<SanPham> dsSanPham = sanphamrepository.findAll();
        List<ChiTietSanPham> dsChiTiet = idSanPham != null
                ? chiTietSanPhamRepository.findBySanPhamId(idSanPham)
                : new ArrayList<>();

        // Lấy giỏ hàng theo cartKey
        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        // Tính tổng tiền
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy phí ship và tiền giảm từ session
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen")).orElse(BigDecimal.ZERO);
        BigDecimal giamGia = Optional.ofNullable((BigDecimal) session.getAttribute("giamGia")).orElse(BigDecimal.ZERO);

        // Tính tổng sau giảm
        BigDecimal tongSauGiam = tongTien.subtract(giamGia).add(phiShip);

        // Đẩy dữ liệu ra model
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsChiTietSanPham", dsChiTiet);
        model.addAttribute("sanPhamDaChon", idSanPham);
        model.addAttribute("gioHang", gio);
        model.addAttribute("tatCaGio", ((Map<String, ?>) session.getAttribute("tatCaGio")).keySet());
        model.addAttribute("cartKey", cartKey);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("giamGia", giamGia);
        model.addAttribute("tongTienSauGiam", tongSauGiam);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("dsPhieuGiamGia", phieugiamgiarepository.findAll());
        model.addAttribute("maGiamGia", session.getAttribute("maGiamGia"));
        model.addAttribute("giamGia", session.getAttribute("giamGia"));
        return "admin/pages/banhang/banhang";
    }


    @PostMapping("/ap-dung-ma")
    public String apDungMa(@RequestParam("maGiamGia") String ma,
                           @RequestParam("cartKey") String cartKey,
                           HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(ma.trim());
        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu != null && Boolean.TRUE.equals(phieu.getTrangThai())) {
            LocalDate today = LocalDate.now();
            if (phieu.getLoaiPhieuGiamGia() == 2) {
                tienGiam = phieu.getMucDo(); // giảm cố định
            } else if (phieu.getLoaiPhieuGiamGia() == 1) {
                tienGiam = tongTien.multiply(phieu.getMucDo()).divide(BigDecimal.valueOf(100));
                // Dùng giá trị giamToiDa từ DB nếu có
                if (phieu.getGiamToiDa() != null && phieu.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                    tienGiam = tienGiam.min(phieu.getGiamToiDa());
                }
            }

        }

        session.setAttribute("giamGia", tienGiam);
        session.setAttribute("maGiamGia", ma.trim());
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @GetMapping("/xoa-ma")
    public String xoaMa(@RequestParam("cartKey") String cartKey, HttpSession session) {
        session.removeAttribute("giamGia");
        session.removeAttribute("maGiamGia");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }


    @PostMapping("/xoa-gio")
    public String xoaGioHang(@RequestParam("cartKey") String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts != null) carts.remove(cartKey);
        session.removeAttribute("phiVanChuyen");
        session.removeAttribute("maGiamGia");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }
    @GetMapping("/tim-kiem-san-pham")
    @ResponseBody
    public List<Map<String, Object>> timKiemSanPham(@RequestParam("keyword") String keyword) {
        List<ChiTietSanPham> list = chiTietSanPhamRepository.findByTenSanPhamContainingIgnoreCase(keyword);
        return list.stream().map(ctsp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("idChiTietSp", ctsp.getId());
            map.put("tenSanPham", ctsp.getSanPham().getTen());
            map.put("mauSac", ctsp.getMauSac().getTen());
            map.put("size", ctsp.getSize().getTen());
            map.put("soluong", ctsp.getSoLuong());
            map.put("gia", ctsp.getGiaBan());
            return map;
        }).collect(Collectors.toList());
    }
    @PostMapping("/cap-nhat-so-luong")
    public String capNhatSoLuong(@RequestParam("idChiTietSp") Integer id,
                                 @RequestParam("soLuong") Integer soLuong,
                                 @RequestParam("cartKey") String cartKey,
                                 HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.stream()
                .filter(item -> item.getChiTietSanPham().getId().equals(id))
                .findFirst()
                .ifPresent(item -> item.setSoLuong(soLuong));

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }
    @GetMapping("/tim-kiem")
    public String timKiemSanPham(@RequestParam("keyword") String keyword,
                                 @RequestParam("cartKey") String cartKey,
                                 Model model) {
        ChiTietSanPham ketQua = chiTietSanPhamRepository.findByMaVach(keyword);
        model.addAttribute("ketQua", ketQua);
        model.addAttribute("ten",ketQua.getTenCt());
        model.addAttribute("mausac",ketQua.getMauSac().getTen());
        model.addAttribute("cartKey", cartKey);
        return "admin/banhang"; // trang bán hàng hiển thị luôn kết quả tìm
    }
    @GetMapping("/tim-kiem-theo-ma-vach")
    @ResponseBody
    public Map<String, Object> timKiemSanPhamJson(@RequestParam("maVach") String maVach) {
        Map<String, Object> result = new HashMap<>();
        ChiTietSanPham sp = chiTietSanPhamRepository.findByMaVach(maVach);
        if (sp != null) {
            result.put("tenSanPham", sp.getSanPham().getTen());
            result.put("mauSac", sp.getMauSac().getTen());
            result.put("size", sp.getSize().getTen());
            result.put("soluong", sp.getSoLuong());
            result.put("gia", sp.getGiaBan());
            result.put("idChiTietSp", sp.getId()); // Thêm ID nếu cần xử lý tiếp
        }
        return result;
    }

    @PostMapping("/them-gio")
    public String themVaoGio(@RequestParam("idChiTietSp") Integer id,
                             @RequestParam("soLuong") Integer soLuong,
                             @RequestParam("cartKey") String cartKey,
                             HttpSession session) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        gio.stream().filter(i -> i.getChiTietSanPham().getId().equals(id)).findFirst().ifPresentOrElse(
                item -> item.setSoLuong(item.getSoLuong() + soLuong),
                () -> {
                    HoaDonChiTiet ct = new HoaDonChiTiet();
                    ct.setChiTietSanPham(ctsp);
                    ct.setSoLuong(soLuong);
                    ct.setGiaGiam(BigDecimal.ZERO);
                    ct.setGiaSauGiam(ctsp.getGiaBan());
                    ct.setTrangThai(true);
                    ct.setNgayTao(LocalDateTime.now());
                    ct.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
                    ct.setTenCtsp(ctsp.getSanPham().getTen() + " - " + ctsp.getMauSac().getTen() + " / " + ctsp.getSize().getTen());
                    gio.add(ct);
                });

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/thanh-toan")
    public String thanhToan(@RequestParam("cartKey") String cartKey,
                            @RequestParam(value = "soDienThoai", required = false) String sdt,
                            @RequestParam(value = "giagiam", required = false) BigDecimal giagiam,
                            @RequestParam("phuongThucThanhToan") String phuongThuc,
                            @RequestParam("diaChiTinh") String diaChiTinh,
                            @RequestParam("diaChiHuyen") String diaChiHuyen,
                            @RequestParam("diaChiXa") String diaChiXa,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirect) {

        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        if (gio == null || gio.isEmpty()) {
            redirect.addFlashAttribute("error", "Giỏ hàng trống!");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        BigDecimal tongTien = gio.stream()
                .map(ct -> ct.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen")).orElse(BigDecimal.ZERO);
        giagiam = giagiam != null ? giagiam : BigDecimal.ZERO;
        BigDecimal thanhTien = tongTien.subtract(giagiam).add(phiShip);

        HoaDon hd = new HoaDon();
        String maHD = "HD" + System.currentTimeMillis();
        hd.setMa(maHD);
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTenNguoiNhan("Trực tiếp");
        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) {
            redirect.addFlashAttribute("error", "Không tìm thấy nhân viên đang đăng nhập.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        hd.setNhanVien(nv);
        hd.setPhuongThuc(phuongThuc.equals("chuyen_khoan") ? "Chuyển khoản" : "Tiền mặt");
        hd.setDiaChi(diaChiTinh+'-'+diaChiHuyen+'-'+diaChiXa);
        hd.setSoDienThoai(sdt);
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setThanhTien(thanhTien);
        hd.setTrangThai(3);
        hd.setNgaySua(LocalDateTime.now());
        hd.setNguoiSua(1);
        hd.setNguoiTao(1);


        session.setAttribute("hoaDonTam", hd);
        session.setAttribute("gioTam", gio);
        session.setAttribute("cartKeyTam", cartKey);

        String diachi=diaChiTinh+'-'+diaChiHuyen+'-'+diaChiXa;
        return hoanTatThanhToan(cartKey, gio, sdt, giagiam, tongTien, phiShip, "Tiền mặt", true,diachi, session);
    }
    private String hoanTatThanhToan(String cartKey, List<HoaDonChiTiet> gio,
                                    String sdt, BigDecimal giagiam, BigDecimal tongTien,
                                    BigDecimal phiShip, String phuongThuc, boolean trangThai,String diachi,
                                    HttpSession session) {

        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTenNguoiNhan("Trực tiếp");
        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) nv = nhanVienRepository.findById(1).orElse(null); // fallback nếu cần
        hd.setNhanVien(nv);

        hd.setTrangThai(3);
        hd.setDiaChi(diachi);
        hd.setPhuongThuc(phuongThuc);
        hd.setSoDienThoai(sdt);
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setThanhTien(tongTien.subtract(giagiam).add(phiShip));
        hd.setNgaySua(LocalDateTime.now());
        hd.setNguoiSua(1);
        hd.setNguoiTao(1);

        // 👉 Lưu mã giảm giá nếu có
        String maGiamGia = (String) session.getAttribute("maGiamGia");
        if (maGiamGia != null) {
            hd.setPhieuGiamGia(phieugiamgiarepository.findByMa(maGiamGia));
        }

        hoaDonRepository.save(hd);

        for (HoaDonChiTiet ct : gio) {
            ChiTietSanPham sp = chiTietSanPhamRepository.findWithDetailsById(ct.getChiTietSanPham().getId());
            ct.setChiTietSanPham(sp);
            ct.setHoaDon(hd);
            ct.setGiaGoc(sp.getGiaBan());
            ct.setTenCtsp(sp.getSanPham().getTen() + " - " + sp.getMauSac().getTen() + "/" + sp.getSize().getTen());
            hoaDonChiTietRepository.save(ct);

            sp.setSoLuong(sp.getSoLuong() - ct.getSoLuong());
            chiTietSanPhamRepository.save(sp);
        }

        // ✅ Lưu lịch sử hóa đơn// Lấy từ session
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hd);
        lichSu.setNgayTao(LocalDateTime.now());
        lichSu.setNgaySua(LocalDateTime.now());
        lichSu.setNguoiTao(nv.getId());
        lichSu.setNguoiSua(nv.getId());
        lichSu.setTrangThai(trangThai ? TrangThaiLichSuHoaDon.HOAN_THANH.getValue() : TrangThaiLichSuHoaDon.CHO_XAC_NHAN.getValue());
        lichSu.setGhiChu("Tạo hóa đơn và thanh toán" + (phuongThuc.equals("chuyen_khoan") ? " bằng chuyển khoản" : " bằng tiền mặt"));
        lichSuHoaDonRepository.save(lichSu);



        // ✅ Dọn session
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts != null) carts.remove(cartKey);
        session.removeAttribute("phiVanChuyen");
        session.removeAttribute("giamGia");
        session.removeAttribute("maGiamGia");
        session.removeAttribute("hoaDonTam");
        session.removeAttribute("gioTam");

        return "redirect:/admin/ban-hang";
    }
    //    @GetMapping("/vnpay/create-payment")
//    public ResponseEntity<String> createPayment(HttpServletRequest request) {
//        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
//        String vnp_OrderInfo = "Thanh toan don hang test";
//        String orderType = "other";
//        String vnp_Amount = String.valueOf(5000000); // 50,000đ * 100
//        String vnp_Locale = "vn";
//        String vnp_BankCode = "NCB";
//        String vnp_IpAddr = request.getRemoteAddr();
//
//        String vnp_TmnCode = "HWOXLM32";
//        String secretKey = "8CPTJKZ7WW953XQYYJDDNSDW0HL22E8Z";
//        String vnp_ReturnUrl = "http://localhost:8080/vnpay-payment-return";
//        String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", "2.1.0");
//        vnp_Params.put("vnp_Command", "pay");
//        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", vnp_Amount);
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
//        vnp_Params.put("vnp_OrderType", orderType);
//        vnp_Params.put("vnp_Locale", vnp_Locale);
//        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//        vnp_Params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        for (String fieldName : fieldNames) {
//            String value = vnp_Params.get(fieldName);
//            if (hashData.length() > 0) {
//                hashData.append('&');
//                query.append('&');
//            }
//            hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
//            query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
//        }
//
//        String vnp_SecureHash = vnpayService.hmacSHA512(secretKey, hashData.toString());
//        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
//        String paymentUrl = vnp_Url + "?" + query.toString();
//
//        return ResponseEntity.ok(paymentUrl);
//    }
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

    @GetMapping("/phi-ship")
    @ResponseBody
    public ResponseEntity<Integer> getPhiShip(
            @RequestParam("toDistrictId") int toDistrictId,
            @RequestParam("wardCode") String toWardCode,
            HttpSession session) {
        int fromDistrictId = 3440; // Mặc định Quận 1
        int weight = 1000;
        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) return ResponseEntity.ok(0);

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);
        session.setAttribute("phiVanChuyen", new BigDecimal(fee));
        return ResponseEntity.ok(fee);
    }
    @PostMapping("/luu-dia-chi")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> luuDiaChiVaPhiShip(
            @RequestParam("cartKey") String cartKey,
            @RequestParam("tinh") String tinh,
            @RequestParam("huyen") String huyen,
            @RequestParam("xa") String xa,
            @RequestParam("toDistrictId") int toDistrictId,
            @RequestParam("wardCode") String toWardCode,
            HttpSession session) {

        int fromDistrictId = 3440;
        int weight = 1000;
        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) return ResponseEntity.ok(Map.of("phiShip", 0));

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);

        // Lưu vào session
        session.setAttribute("phiVanChuyen", new BigDecimal(fee));
        session.setAttribute("diaChiTinh", tinh);
        session.setAttribute("diaChiHuyen", huyen);
        session.setAttribute("diaChiXa", xa);

        return ResponseEntity.ok(Map.of("phiShip", fee));
    }

}