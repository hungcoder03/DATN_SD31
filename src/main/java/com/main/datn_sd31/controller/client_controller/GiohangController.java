package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.Giohangreposiroty;
import com.main.datn_sd31.repository.Hinhanhrepository;
import com.main.datn_sd31.repository.HoaDonChiTietRepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.repository.Mausacrepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.PhieuGiamGiaRepository;
import com.main.datn_sd31.repository.Sizerepository;
import com.main.datn_sd31.repository.Xuatxurepository;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

        // Xóa hết giỏ hàng cũ
        giohangreposiroty.deleteAll();

        // Đảm bảo Hibernate flush & clear để tránh stale state
        giohangreposiroty.flush();  // Nếu dùng JpaRepository có hỗ trợ flush
        // Nếu không có flush, inject EntityManager và gọi em.flush() + em.clear()
        KhachHang kh=khachhangrepository.find(1);
        // Lưu lại giỏ hàng đã gộp (chỉ lưu những phần tử mới, không lưu lại entity cũ bị stale)
        List<GioHangChiTiet> newList = new ArrayList<>();
        for (GioHangChiTiet item : gopMap.values()) {
            GioHangChiTiet newItem = new GioHangChiTiet();
            // Copy dữ liệu từ item sang newItem, đừng reuse entity cũ đã bị xóa
            newItem.setKhachHang(kh);
            newItem.setChiTietSp(item.getChiTietSp());
            newItem.setSoLuong(item.getSoLuong());
            newItem.setThanhTien(item.getThanhTien());
            newItem.setTrangThai(item.getTrangThai());
            // ... copy các trường khác nếu có
            newList.add(newItem);
        }
        giohangreposiroty.saveAll(newList);

        // Tính tổng tiền
        BigDecimal tongTien = BigDecimal.ZERO;
        for (GioHangChiTiet item : newList) {
            tongTien = tongTien.add(item.getThanhTien());
        }

        model.addAttribute("list", newList);
        model.addAttribute("tongTien", tongTien);

        return "/view/giohang/list";
    }



    @PostMapping("/them")
    public String xuLyThem( @RequestParam("sanPhamId") Integer sanphamId,
                            @RequestParam("sizeId") Integer sizeId,
                            @RequestParam("mauSacId") Integer mauSacId,
                            @RequestParam("soLuong") Integer soluong,
                            Model model) {

        // Tìm chi tiết sản phẩm theo sanPhamId + sizeId + mauSacId
        ChiTietSanPham chiTiet = chitietsanphamRepo.findBySanPhamIdAndSizeIdAndMauSacId(sanphamId, sizeId, mauSacId);
        GioHangChiTiet gh=new GioHangChiTiet();
        KhachHang kh= khachhangrepository.find(1);
        // Tạo đối tượng giỏ hàng mới (hoặc cập nhật nếu có)
        gh.setKhachHang(kh);
        gh.setChiTietSp(chiTiet);
        gh.setSoLuong(soluong);
        gh.setTrangThai(0);
        gh.setThanhTien(chiTiet.getGiaBan().multiply(BigDecimal.valueOf(soluong)));

        giohangreposiroty.save(gh);
        return "redirect:/gio-hang/hien_thi";
    }


    @GetMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Integer id) {
        giohangreposiroty.deleteById(id);
        return "redirect:/gio-hang/hien_thi";
    }
    @GetMapping("/xem/{id}")
    public String danhSachChiTiet(Model model, @PathVariable("id") Integer id) {
        // Lấy danh sách chi tiết sản phẩm dựa trên id sản phẩm
        List<ChiTietSanPham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);
        model.addAttribute("dsChiTietSanPham", danhSachChiTiet);

        // Lấy thông tin sản phẩm chính (nếu cần hiển thị tên hoặc info sản phẩm)
        SanPham sanPham = sanPhamService.findbyid(id);
        model.addAttribute("sanPham", sanPham);

        // Các danh sách phục vụ cho dropdown trong view
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh",hinhanhrepository.findByhinhanhid(id));
        model.addAttribute("dsMauSac", mausacrepository.findAll());
        model.addAttribute("dsLoaiThu", xuatxurepository.findAll());
        model.addAttribute("dsSize", sizerepository.findAll());

        return "/view/sanpham/xemchitiet";
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
    // API trả JSON dùng để lấy location (province, district, ward)
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



    // Trang thanh toán trả view (JSP/Thymeleaf)
    @GetMapping("/thanh-toan")
    public String hienThiTrangThanhToan(
            @RequestParam(value = "selectedId", required = false) List<Integer> selectedIds,
            @RequestParam(required = false) Integer provinceId,
            @RequestParam(required = false) Integer districtId,
            @RequestParam(required = false) String wardCode,  // cần thêm wardCode
            HttpServletRequest request,
            Model model) {

        if (selectedIds == null) {
            selectedIds = Collections.emptyList();
        }

        Integer id = 1;
        KhachHang kh = khachhangrepository.find(id);
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
    public String xacNhanThanhToan(
            @RequestParam("phuongThucThanhToan") String phuongThuc,
            @RequestParam("selectedId") List<Integer> selectedItemIds,
            @RequestParam Map<String, String> formData,
            HttpSession session,
            HttpServletRequest request,
            Model model) {

        // Địa chỉ
        String diaChiChiTiet = formData.get("diaChi");
        String fullAddress = diaChiChiTiet + ", " + formData.get("tenXa") + ", "
                + formData.get("tenHuyen") + ", " + formData.get("tenTinh");

        // Lấy sản phẩm đã chọn
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
        hoaDon.setTrangThai(true); // CHỜ XÁC NHẬN

        KhachHang kh = khachhangrepository.find(1); // demo
        NhanVien nv = nhanvienrepository.find(1);   // demo
        hoaDon.setKhachHang(kh);
        hoaDon.setNhanVien(nv);
        hoaDon.setNguoiTao(1);
        hoaDon.setNguoiSua(1);

        if (formData.containsKey("phieuGiamGia") && !formData.get("phieuGiamGia").isBlank()) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(formData.get("phieuGiamGia"));
            hoaDon.setPhieuGiamGia(phieu);
        }

        hoadonreposiroty.save(hoaDon);

        // Lịch sử hóa đơn
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setTrangThai(1);
        lichSu.setNguoiTao(hoaDon.getNguoiTao());
        lichSu.setNguoiSua(hoaDon.getNguoiSua());
        lichSu.setGhiChu("Tạo đơn hàng mới, chờ xác nhận");
        lichsuhoadonrepository.save(lichSu);

        // Tạo chi tiết hóa đơn (không trừ kho)
        for (GioHangChiTiet item : gioHangChiTiets) {
            ChiTietSanPham spct = item.getChiTietSp();
            int soLuongMua = item.getSoLuong();

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hoaDon);
            ct.setChiTietSanPham(spct);
            ct.setSoLuong(soLuongMua);
            ct.setGiaSauGiam(spct.getGiaBan());
            ct.setGiaGiam(tienGiam);
            hoadonCTreposiroty.save(ct);
        }

        giohangreposiroty.deleteAll(gioHangChiTiets); // Xóa giỏ hàng

        if ("chuyen_khoan".equals(phuongThuc)) {
            String orderInfo = "Thanh toan don hang #" + hoaDon.getId();
            long amount = hoaDon.getThanhTien().longValue();
            String vnpUrl = vnPayUtils.createVnpayUrl(amount, orderInfo, hoaDon.getMa(), request);
            return "redirect:" + vnpUrl;
        }

        model.addAttribute("maHoaDon", hoaDon.getMa());
        return "view/khachhang/thanhcong";
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


