package com.main.datn_sd31.controller.admin_controller;

import com.lowagie.text.Font;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.Chitietsanphamrepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import com.main.datn_sd31.util.ThongBaoUtils;
import com.main.datn_sd31.util.ThymleafHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/hoa-don/detail")
@RequiredArgsConstructor
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    private final HoaDonService hoaDonService;

    private final Chitietsanphamrepository chitietsanphamrepository;

    private final LichSuHoaDonService lichSuHoaDonService;

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private final NhanVienRepository nhanVienRepository;

    //Lấy thông tin nhân viên
    private NhanVien getCurrentNhanVien() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return nhanVienRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với email: " + email));
    }

    @GetMapping("")
    public String detailHoaDon(
            @RequestParam(value = "maHoaDon") String maHoaDon,
            Model model
    ){
        model.addAttribute("lichSuList", lichSuHoaDonService.getLichSuHoaDonByHoaDon(maHoaDon));
        model.addAttribute("lichSuHoaDonList", lichSuHoaDonService.getLichSuHoaDonDTOByHoaDon(maHoaDon));

        var hoaDon = hoaDonService.getHoaDonByMa(maHoaDon);
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("lichSuMoiNhat", lichSuHoaDonService.getLichSuHoaDonDTODescByMaHoaDon(maHoaDon).get(0));
//        model.addAttribute("hoaDon", hoaDonService.getHoaDonByMa(maHoaDon));
        model.addAttribute("hdctList", hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon));
        model.addAttribute("maHoaDon", maHoaDon);

        var trangThaiHopLe = lichSuHoaDonService.getTrangThaiTiepTheoHopLe(hoaDon.getTrangThaiLichSuHoaDon(), hoaDon);
        model.addAttribute("trangThaiHopLe", trangThaiHopLe);
        return "admin/pages/hoa-don/hoa-don-detail";
    }

    @PostMapping("/cap-nhat-ghi-chu")
    public String capNhatGhiChu(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "ghiChuHoaDon", required = false) String ghiChuHoaDon,
            RedirectAttributes redirectAttributes
    ) {
        if (ghiChuHoaDon == null) {
            return "redirect:/admin/hoa-don/detail";
        }
        hoaDonService.capNhatGhiChuHoaDon(maHoaDon, ghiChuHoaDon);
        redirectAttributes.addFlashAttribute("success", "Cập nhật ghi chú thành công.");
        redirectAttributes.addAttribute("maHoaDon", maHoaDon);
        return "redirect:/admin/hoa-don/detail";
    }

    @PostMapping("/cap-nhat-trang-thai")
    public String capNhatTrangThai(
            @RequestParam("maHoaDon") String maHoaDon,
            @RequestParam(value = "trangThaiMoi", required = false) Integer trangThaiMoi,
            @RequestParam(value = "lyDoGiaoKhongThanhCong", required = false) Integer lyDoGiaoKhongThanhCong,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        var ketQua = lichSuHoaDonService.xuLyCapNhatTrangThai(
                maHoaDon,
                trangThaiMoi,
                lyDoGiaoKhongThanhCong,
                ghiChu,
                getCurrentNhanVien()
        );

        if (ketQua.thanhCong()) {
            ThongBaoUtils.addSuccess(redirectAttributes, ketQua.message());
        } else {
            ThongBaoUtils.addError(redirectAttributes, ketQua.message());
        }

        redirectAttributes.addAttribute("maHoaDon", maHoaDon);

        return "redirect:/admin/hoa-don/detail";
    }

    @GetMapping("/{maHoaDon}/pdf")
    public void xuatHoaDonPDF(@PathVariable("maHoaDon") String maHoaDon,
                              HttpServletResponse response) throws Exception {
        HoaDonDTO hoaDon = hoaDonService.getHoaDonByMa(maHoaDon);
        List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=hoa-don-" + maHoaDon + ".pdf");

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Font cơ bản hỗ trợ tiếng Việt
        BaseFont baseFont = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font normalFont = new Font(baseFont, 12);
        Font boldFont = new Font(baseFont, 14, Font.BOLD);

        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG", boldFont));
        document.add(new Paragraph("Mã hóa đơn: " + hoaDon.getMa(), normalFont));
        document.add(new Paragraph("Khách hàng: " + hoaDon.getTenKH(), normalFont));
        document.add(new Paragraph("Email: " + hoaDon.getEmail(), normalFont));
        document.add(new Paragraph("Ngày tạo: " + hoaDon.getNgayTao(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{1f, 3f, 2f, 1f, 2f});

        // Tiêu đề bảng
        String[] headers = {"STT", "Tên sản phẩm", "Đơn giá", "SL", "Tổng"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
            table.addCell(cell);
        }

        int stt = 1;
        for (HoaDonChiTietDTO ct : chiTietList) {
            table.addCell(new Phrase(String.valueOf(stt++), normalFont));
            table.addCell(new Phrase(ct.getTenCTSP(), normalFont));
            table.addCell(new Phrase(String.valueOf(ct.getGiaSauGiam()), normalFont));
            table.addCell(new Phrase(String.valueOf(ct.getSoLuong()), normalFont));
            table.addCell(new Phrase(String.valueOf(ct.getTongTien()), normalFont));
        }

        document.add(table);
        document.add(new Paragraph(" ", normalFont));

        document.add(new Paragraph("Tổng tiền: " + hoaDon.getThanhTien(), boldFont));
        document.close();
    }

    @PostMapping("/api/cap-nhat-so-luong")
    @ResponseBody
    public ResponseEntity<Map<String, String>> apiCapNhatSoLuong(@RequestBody Map<String, Object> payload) {
        Integer id = Integer.valueOf(payload.get("id").toString());
        Integer soLuong = Integer.valueOf(payload.get("soLuong").toString());

        HoaDonChiTietDTO hdct = hoaDonChiTietService.capNhatSoLuong(id, soLuong);
        BigDecimal tongTien = hdct.getGiaSauGiam().multiply(BigDecimal.valueOf(soLuong));

        String tongTienFormatted = ThymleafHelper.formatCurrency(tongTien); // bạn đã có hàm này rồi

        Map<String, String> response = new HashMap<>();
        response.put("tongTienFormatted", tongTienFormatted);

        return ResponseEntity.ok(response);
    }



}