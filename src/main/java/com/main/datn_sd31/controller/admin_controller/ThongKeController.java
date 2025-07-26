package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.service.ThongKeService;
import com.main.datn_sd31.util.ThongBaoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongkeService;

    public static LocalDate[] getDateRange(String type) {
        LocalDate today = LocalDate.now();
        LocalDate start, end;

        switch (type.toLowerCase()) {
            case "ngay":
                start = today;
                end = today;
                break;

            case "tuan":
                // Start: Thứ 2 đầu tuần | End: Chủ nhật
                DayOfWeek dow = today.getDayOfWeek();
                start = today.minusDays(dow.getValue() - 1); // MONDAY = 1
                end = today.plusDays(7 - dow.getValue());
                break;

            case "thang":
                start = today.withDayOfMonth(1);
                end = today.withDayOfMonth(today.lengthOfMonth());
                break;

            case "nam":
                start = today.withDayOfYear(1);
                end = today.withDayOfYear(today.lengthOfYear());
                break;

            case "custom":
                // Trả về null hoặc today - today để tránh lỗi
                return new LocalDate[]{today, today}; // hoặc return null nếu bạn muốn controller xử lý

            default:
                throw new IllegalArgumentException("Loại thống kê không hợp lệ: " + type);
        }

        return new LocalDate[]{start, end};
    }

    @GetMapping("")
    public String thongKe(
            Model model,
            @RequestParam(value = "typeCalendar", defaultValue = "ngay") String type,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        LocalDate start;
        LocalDate end;

        if ("custom".equalsIgnoreCase(type)) {
            if (startDate == null || endDate == null) {
                ThongBaoUtils.addError(redirectAttributes, "Phải chọn đầy đủ ngày bắt đầu và ngày kết thúc");
            }
            start = startDate;
            end = endDate;
        } else {
            LocalDate[] range = getDateRange(type); // loại bỏ nguy cơ gọi với custom
            start = range[0];
            end = range[1];
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);

        model.addAttribute("doanhThu", thongkeService.getDoanhThu(startDateTime, endDateTime, 3));
        model.addAttribute("donThanhCong", thongkeService.countDonHang(startDateTime, endDateTime, 3));
        model.addAttribute("donHuy", thongkeService.countDonHang(startDateTime, endDateTime, 5));
        model.addAttribute("donTra", thongkeService.countDonHang(startDateTime, endDateTime, 4));
        model.addAttribute("tongSanPham", thongkeService.getTongSanPham(startDateTime, endDateTime));

        model.addAttribute("labelThoiGian", switch (type) {
            case "tuan" -> "trong tuần này";
            case "thang" -> "trong tháng này";
            case "nam" -> "trong năm nay";
            case "custom" -> "từ " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến " + end.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            default -> "hôm nay";
        });

        model.addAttribute("typeCalendar", type);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);

        model.addAttribute("sanPhamThongKeList", thongkeService.getThongKeSanPham(startDateTime, endDateTime));

        return "admin/pages/dashboard";
    }



}
