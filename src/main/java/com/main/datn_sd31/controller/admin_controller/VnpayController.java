package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.controller.client_controller.GiohangController;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.entity.GioHangChiTiet;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.Giohangreposiroty;
import com.main.datn_sd31.repository.HoaDonChiTietRepository;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.service.impl.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class VnpayController {

    @Autowired
    private VnpayService vnpayService;
    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private Giohangreposiroty giohangreposiroty;

    @Autowired
    private GiohangController giohangController;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

    @GetMapping("/thanh-toan-vnpay")
    public void thanhToan(@RequestParam("maHoaDon") String maHoaDon,
                          @RequestParam(name = "ids", required = false) String ids,
                          HttpServletResponse response,
                          HttpServletRequest request) throws IOException {
        HoaDon hoaDon = hoaDonRepository.findByMa(maHoaDon);
        if (hoaDon == null) {
            response.sendRedirect("/loi");
            return;
        }
        long amount = hoaDon.getThanhTien().longValue();
//        long amount = 50000L; // 50.000 VNĐ
        String orderInfo = "Thanh toan test VNPay";
        String paymentUrl = vnpayService.createPaymentUrl(request, amount, orderInfo,maHoaDon,ids);
        response.sendRedirect(paymentUrl);
    }

    @Transactional
    @GetMapping("/vnpay-payment-return")
    public String vnpayReturn(HttpServletRequest request,
                              Model model,
                              @RequestParam(name = "ids", required = false) String ids,
                              HttpSession session) {

        String responseCode = request.getParameter("vnp_ResponseCode");
        String maHoaDon = request.getParameter("vnp_TxnRef");
        String transactionStatus = request.getParameter("vnp_TransactionStatus");
        // Processing payment for order: " + maHoaDon
        HoaDon hoaDon = hoaDonRepository.findByMa(maHoaDon);

        if (hoaDon == null) {
            model.addAttribute("error", "Hóa đơn không tồn tại.");
            return "khachhang/thanhcong";
        }
        List<GioHangChiTiet> gioHangChiTiets = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            gioHangChiTiets = giohangreposiroty.findAllById(idList);
        }
        BigDecimal tienGiam = hoaDon.getGiaGiamGia() != null ? hoaDon.getGiaGiamGia() : BigDecimal.ZERO;
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {

            hoaDonRepository.capNhatTrangThaiHoaDon(3,maHoaDon);
            // Lưu lịch sử
            LichSuHoaDon lichSu = new LichSuHoaDon();
            lichSu.setHoaDon(hoaDon);
            lichSu.setTrangThai(1);
            lichSu.setNguoiTao(hoaDon.getNguoiTao());
            lichSu.setNguoiSua(hoaDon.getNguoiSua());
            lichSu.setGhiChu("Đặt hàng thành công");
            lichSu.setNgayTao(LocalDateTime.now());
            lichSuHoaDonRepository.save(lichSu);

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

                hoaDonChiTietRepository.save(hdct);

            }
            giohangreposiroty.deleteAll(gioHangChiTiets);

            model.addAttribute("ma", maHoaDon);
            model.addAttribute("message", "Thanh toán VNPay thành công!");
            return "/khachhang/thanhcong";
        } else {
            hoaDon.setTrangThai(5);
            hoaDonRepository.save(hoaDon);
            model.addAttribute("message", "Thanh toán thất bại hoặc bị hủy.");
        }

        return "/khachhang/thanhcong";
    }
}