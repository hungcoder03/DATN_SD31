package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final Mausacrepository mausacrepository;
    private final Sizerepository sizerepository;
    private final PhieuGiamGiaService phieuGiamGiaService;


//    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
//        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
//        if (carts == null) {
//            carts = new HashMap<>();
//            session.setAttribute("tatCaGio", carts);
//        }
//        return carts.computeIfAbsent(cartKey, k -> new ArrayList<>());
//    }
    @SuppressWarnings("unchecked")
    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> allCarts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (allCarts == null) {
            allCarts = new HashMap<>();
            session.setAttribute("tatCaGio", allCarts);
        }

        return allCarts.computeIfAbsent(cartKey, k -> new ArrayList<>());
    }

    private String findEmptyCartKey(Set<String> existingKeys) {
        for (int i = 1; i <= 5; i++) {
            String key = "gio-" + i;
            if (!existingKeys.contains(key)) {
                return key;
            }
        }
        return null; // Không còn giỏ trống
    }

    @GetMapping
    public String hienThiSanPham(
            @RequestParam(value = "idSanPham", required = false) Integer idSanPham,
            @RequestParam(value = "cartKey", defaultValue = "gio-1") String cartKey,
            Model model, HttpSession session) {

        // Lấy tất cả giỏ từ session
        Map<String, List<HoaDonChiTiet>> carts =
                (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts == null) {
            carts = new HashMap<>();
            session.setAttribute("tatCaGio", carts);
        }

        // Tìm cartKey trống để hiển thị cho nút "+ Giỏ mới"
        String nextCartKey = findEmptyCartKey(carts.keySet());
        model.addAttribute("nextCartKey", nextCartKey);

        // Danh sách sản phẩm & chi tiết sản phẩm
        List<SanPham> dsSanPham = sanphamrepository.findAll();
        List<ChiTietSanPham> dsChiTiet = idSanPham != null
                ? chiTietSanPhamRepository.findBySanPhamId(idSanPham)
                : new ArrayList<>();

        // Lấy giỏ hàng hiện tại
        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        // Tính tổng tiền
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy phí ship & giảm giá của giỏ hiện tại từ session (theo cartKey)
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen_" + cartKey))
                .orElse(BigDecimal.ZERO);
        BigDecimal giamGia = Optional.ofNullable((BigDecimal) session.getAttribute("giamGia_" + cartKey))
                .orElse(BigDecimal.ZERO);
        Object maGiamGia = session.getAttribute("maGiamGia_" + cartKey);

        // Nếu tổng tiền <= 0 thì reset riêng giỏ này
        if (tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            giamGia = BigDecimal.ZERO;
            phiShip = BigDecimal.ZERO;
            maGiamGia = null;

            session.setAttribute("giamGia_" + cartKey, giamGia);
            session.setAttribute("phiVanChuyen_" + cartKey, phiShip);
            session.removeAttribute("maGiamGia_" + cartKey);
        }

        // Tính tổng sau giảm
        BigDecimal tongSauGiam = tongTien.subtract(giamGia).add(phiShip);
        if (tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            tongSauGiam = BigDecimal.ZERO;
        }

        // Lấy danh sách phiếu giảm giá hợp lệ
        LocalDate today = LocalDate.now();
        List<PhieuGiamGia> dsPhieuGiamGia = phieugiamgiarepository.findAll().stream()
                .filter(phieu -> Boolean.TRUE.equals(phieu.getTrangThai()))
                .filter(phieu -> phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0)
                .filter(phieu -> (phieu.getNgayBatDau() == null || !today.isBefore(phieu.getNgayBatDau())))
                .filter(phieu -> (phieu.getNgayKetThuc() == null || !today.isAfter(phieu.getNgayKetThuc())))
                .collect(Collectors.toList());

        // Đẩy dữ liệu ra model
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsChiTietSanPham", dsChiTiet);
        model.addAttribute("sanPhamDaChon", idSanPham);
        model.addAttribute("gioHang", gio);
        model.addAttribute("tatCaGio", carts.keySet());
        model.addAttribute("cartKey", cartKey);
        model.addAttribute("tongTien", tongTien);

        model.addAttribute("giamGia", giamGia);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("maGiamGia", maGiamGia);
        model.addAttribute("tongTienSauGiam", tongSauGiam);

        model.addAttribute("dsPhieuGiamGia", dsPhieuGiamGia);

        return "admin/pages/banhang/banhang";
    }


    @PostMapping("/ap-dung-ma")
    public String apDungMa(@RequestParam("maGiamGia") String ma,
                           @RequestParam("cartKey") String cartKey,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        if (gio == null || gio.isEmpty()) {
            ThongBaoUtils.addError(redirectAttributes, "Giỏ hàng trống, không thể áp mã.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(ma.trim());

        if (phieu == null) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá không tồn tại.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        LocalDate today = LocalDate.now();

        // Kiểm tra điều kiện áp dụng
        if (!Boolean.TRUE.equals(phieu.getTrangThai())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá không khả dụng.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getNgayBatDau() != null && today.isBefore(phieu.getNgayBatDau())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá chưa bắt đầu.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getNgayKetThuc() != null && today.isAfter(phieu.getNgayKetThuc())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá đã hết hạn.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() <= 0) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá đã hết lượt sử dụng.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getDieuKien() != null && tongTien.compareTo(phieu.getDieuKien()) < 0) {
            ThongBaoUtils.addError(redirectAttributes, "Đơn hàng chưa đạt giá trị tối thiểu để áp mã.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        // Tính mức giảm
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (phieu.getLoaiPhieuGiamGia() == 2) { // giảm cố định
            tienGiam = phieu.getMucDo();
        } else if (phieu.getLoaiPhieuGiamGia() == 1) { // giảm %
            tienGiam = tongTien.multiply(phieu.getMucDo()).divide(BigDecimal.valueOf(100));
            if (phieu.getGiamToiDa() != null && phieu.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                tienGiam = tienGiam.min(phieu.getGiamToiDa());
            }
        }

        // Lưu theo cartKey
        session.setAttribute("giamGia_" + cartKey, tienGiam);
        session.setAttribute("maGiamGia_" + cartKey, ma.trim());

        // Trừ số lượng mã giảm giá
        if (phieu.getSoLuongTon() != null) {
            phieu.setSoLuongTon(phieu.getSoLuongTon() - 1);
            phieugiamgiarepository.save(phieu);
        }

        ThongBaoUtils.addSuccess(redirectAttributes, "Áp dụng mã giảm giá thành công!");
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
        if (carts != null) {
            carts.remove(cartKey);
            session.setAttribute("tatCaGio", carts); // Cập nhật lại session

            // Xóa thông tin liên quan
            session.removeAttribute("phiVanChuyen");
            session.removeAttribute("maGiamGia");

            // Nếu còn giỏ, redirect về giỏ đầu tiên
            if (!carts.isEmpty()) {
                String newCartKey = carts.keySet().iterator().next(); // giỏ đầu tiên còn lại
                return "redirect:/admin/ban-hang?cartKey=" + newCartKey;
            }
        }

        // Nếu không còn giỏ nào, tạo giỏ mới
        return "redirect:/admin/ban-hang?cartKey=gio-1";
    }

    @GetMapping("/tim-kiem-san-pham")
    @ResponseBody
    public List<Map<String, Object>> timKiemSanPham(@RequestParam("keyword") String keyword) {
        List<ChiTietSanPham> danhSach = chiTietSanPhamRepository.searchByKeywordSplit( keyword);

        List<Map<String, Object>> ketQua = new ArrayList<>();

        for (ChiTietSanPham ctsp : danhSach) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ctsp.getId()); // ID của ChiTietSanPham
            map.put("ma", ctsp.getSanPham().getMa());
            map.put("tenSanPham", ctsp.getSanPham().getTen());

            // Hình ảnh đại diện (lấy cái đầu tiên nếu có)
            String hinhAnh = ctsp.getSanPham().getHinhAnhs().stream()
                    .findFirst()
                    .map(h -> h.getUrl())
                    .orElse("/images/no-image.png");
            map.put("hinhAnh", hinhAnh);

            // Thông tin màu, size và tồn kho
            map.put("mauSac", ctsp.getMauSac().getTen());
            map.put("kichThuoc", ctsp.getSize().getTen());
            map.put("soLuong", ctsp.getSoLuong());
            map.put("giaBan", ctsp.getGiaBan());

            ketQua.add(map);
        }

        return ketQua;
    }

    @PostMapping("/cap-nhat-so-luong")
    public String capNhatSoLuong(@RequestParam("idChiTietSp") Integer id,
                                 @RequestParam("soLuong") Integer soLuong,
                                 @RequestParam("cartKey") String cartKey,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            redirectAttributes.addFlashAttribute("error", "❌ Không tìm thấy sản phẩm.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        if (soLuong > ctsp.getSoLuong()) {
            redirectAttributes.addFlashAttribute("error", "❌ Số lượng vượt quá tồn kho.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

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
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy sản phẩm");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        Optional<HoaDonChiTiet> existingItem = gio.stream()
                .filter(i -> i.getChiTietSanPham().getId().equals(id))
                .findFirst();

        int tongSoLuongMuonThem = soLuong + existingItem.map(HoaDonChiTiet::getSoLuong).orElse(0);
        if (tongSoLuongMuonThem > ctsp.getSoLuong()) {
            ThongBaoUtils.addError(redirectAttributes, "Sản phẩm không đủ tồn kho");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        if (existingItem.isPresent()) {
            existingItem.get().setSoLuong(tongSoLuongMuonThem);
        } else {
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
        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/them-gio-hang")
    public String themVaoGioBangJson(@RequestBody Map<String, Object> payload,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        Integer id = Integer.parseInt(payload.get("idChiTietSp").toString());
        Integer soLuong = Integer.parseInt(payload.get("soLuong").toString());
        String cartKey = payload.get("cartKey").toString();

        System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC ===");
        System.out.println("idChiTietSp: " + id);
        System.out.println("soLuong: " + soLuong);
        System.out.println("cartKey: " + cartKey);
        System.out.println("==========================");

        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy sản phẩm");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        Optional<HoaDonChiTiet> existingItem = gio.stream()
                .filter(i -> i.getChiTietSanPham().getId().equals(id))
                .findFirst();

        int tongSoLuongMuonThem = soLuong + existingItem.map(HoaDonChiTiet::getSoLuong).orElse(0);
        if (tongSoLuongMuonThem > ctsp.getSoLuong()) {
            ThongBaoUtils.addError(redirectAttributes, "Sản phẩm không đủ tồn kho");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        if (existingItem.isPresent()) {
            existingItem.get().setSoLuong(tongSoLuongMuonThem);
        } else {
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
        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/xoa-san-pham")
    public String xoaSanPhamTrongGio(@RequestParam("cartKey") String cartKey,
                                     @RequestParam("idChiTietSp") Integer idChiTietSp,
                                     HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.removeIf(item -> item.getChiTietSanPham().getId().equals(idChiTietSp));
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }


    @PostMapping("/thanh-toan")
    public String thanhToan(@RequestParam("cartKey") String cartKey,
                            @RequestParam(value = "soDienThoai", required = false) String sdt,
                            @RequestParam(value = "ten", required = false) String ten,
                            @RequestParam(value = "ghichu", required = false) String ghichu,
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
            ThongBaoUtils.addError(redirect, "Giỏ hàng trống!");
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
            ThongBaoUtils.addError(redirect, "Không tìm thấy nhân viên đang đăng nhập.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        hd.setNhanVien(nv);
        hd.setPhuongThuc(phuongThuc.equals("chuyen_khoan") ? "Chuyển khoản" : "Tiền mặt");
        System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC ===");
        System.out.println("dc: " + diaChiTinh);
        System.out.println("dc: " + diaChiHuyen);
        System.out.println("dc: " + diaChiXa);
        System.out.println("==========================");


        String diachi= diaChiTinh + "-" + diaChiHuyen + "-" + diaChiXa;

        hd.setDiaChi(diachi);
        hd.setTenNguoiNhan(ten);
        hd.setGhiChu(ghichu);
        hd.setSoDienThoai(sdt);
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setThanhTien(thanhTien);
        hd.setTrangThai(3);
        hd.setNgaySua(LocalDateTime.now());
        hd.setNguoiSua(1);
        hd.setNguoiTao(1);
        hd.setLoaihoadon("Trực tiếp");
        session.setAttribute("hoaDonTam", hd);
        session.setAttribute("gioTam", gio);
        session.setAttribute("cartKeyTam", cartKey);

        return hoanTatThanhToan(cartKey, gio, sdt,ten,ghichu, giagiam, tongTien, phiShip, phuongThuc,diachi,redirect, session);
    }

    private String hoanTatThanhToan(String cartKey, List<HoaDonChiTiet> gio,
                                    String sdt,String ten,String ghichu, BigDecimal giagiam, BigDecimal tongTien,
                                    BigDecimal phiShip, String phuongThuc,String diachi,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {

        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());

        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) nv = nhanVienRepository.findById(1).orElse(null); // fallback nếu cần
        hd.setNhanVien(nv);

        hd.setTrangThai(3);

        hd.setDiaChi(diachi);
        if(ten!=null) {
            hd.setTenNguoiNhan(ten);
        }else{
            hd.setTenNguoiNhan("trực tiếp");
        }
        hd.setGhiChu(ghichu);
        hd.setPhuongThuc(phuongThuc);
        hd.setSoDienThoai(sdt);
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setLoaihoadon("Trực tiếp");
        hd.setThanhTien(tongTien.subtract(giagiam).add(phiShip));
        hd.setNgaySua(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
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

        // ✅ Lưu lịch sử hóa đơn
        // Lấy từ session
        LichSuHoaDon lichSu1 = new LichSuHoaDon();
        LichSuHoaDon lichSu2 = new LichSuHoaDon();
        LichSuHoaDon lichSu3 = new LichSuHoaDon();
        lichSu1.setHoaDon(hd);
        lichSu1.setNgayTao(LocalDateTime.now());
        lichSu1.setNgaySua(LocalDateTime.now());
        lichSu1.setNguoiTao(nv.getId());
//        lichSu1.setNguoiSua(nv.getId());
        lichSu1.setGhiChu("Tạo hóa đơn: Chờ xác nhận");
        lichSu1.setTrangThai(1);

        lichSu2.setHoaDon(hd);
        lichSu2.setNgayTao(LocalDateTime.now());
        lichSu2.setNgaySua(LocalDateTime.now());
        lichSu2.setNguoiTao(nv.getId());
//        lichSu2.setNguoiSua(nv.getId());
        lichSu2.setGhiChu("Thay đổi trạng thái: Xác nhận");
        lichSu2.setTrangThai(2);

        lichSuHoaDonRepository.save(lichSu1);
        lichSuHoaDonRepository.save(lichSu2);

        if (diachi == null) {
            lichSu3.setHoaDon(hd);
            lichSu3.setNgayTao(LocalDateTime.now());
            lichSu3.setNgaySua(LocalDateTime.now());
            lichSu3.setNguoiTao(nv.getId());
//            lichSu3.setNguoiSua(nv.getId());
            lichSu3.setGhiChu("Thanh toán" + (phuongThuc.equals("chuyen_khoan") ? " bằng chuyển khoản" : " bằng tiền mặt"));
            lichSu3.setTrangThai(5);

            lichSuHoaDonRepository.save(lichSu3);
        }

        if (maGiamGia != null) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(maGiamGia);
            if (phieu != null) {
                hd.setPhieuGiamGia(phieu);

                // ✅ Trừ số lượng phiếu
                if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0) {
                    phieu.setSoLuongTon(phieu.getSoLuongTon() - 1);
                    phieugiamgiarepository.save(phieu);
                }
            }
        }

        // ✅ Dọn session
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts != null) carts.remove(cartKey);
        session.removeAttribute("phiVanChuyen");
        session.removeAttribute("giamGia");
        session.removeAttribute("maGiamGia");
        session.removeAttribute("hoaDonTam");
        session.removeAttribute("gioTam");

        ThongBaoUtils.addSuccess(redirectAttributes, "Thanh toán thành công");

        return "redirect:/admin/ban-hang";
    }
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
            @RequestParam("cartKey") String cartKey,
            HttpSession session) {

        int fromDistrictId = 3440; // Quận 1 mặc định
        int weight = 1000;

        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) {
            session.setAttribute("phiVanChuyen_" + cartKey, BigDecimal.ZERO);
            return ResponseEntity.ok(0);
        }

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);

        if (fee == null || fee < 0) {
            fee = 0; // tránh null hoặc âm
        }

        session.setAttribute("phiVanChuyen_" + cartKey, new BigDecimal(fee));

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