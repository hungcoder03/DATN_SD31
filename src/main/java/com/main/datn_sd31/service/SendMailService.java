package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.HoaDonChiTiet;
import com.main.datn_sd31.entity.GioHangChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SendMailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("noreply@dcfashions.com");

        mailSender.send(message);
    }

    public void sendOrderConfirmationMail(String customerEmail, HoaDon hoaDon, List<GioHangChiTiet> items) {
        try {
            String htmlContent = generateOrderConfirmationHtml(hoaDon, items);
            sendHtmlMail(customerEmail, "Xác nhận đơn hàng #" + hoaDon.getMa(), htmlContent);
        } catch (MessagingException e) {
            // Fallback to simple text email if HTML fails
            String textContent = generateOrderConfirmationText(hoaDon, items);
            sendSimpleMail(customerEmail, "Xác nhận đơn hàng #" + hoaDon.getMa(), textContent);
        }
    }

    private String generateOrderConfirmationHtml(HoaDon hoaDon, List<GioHangChiTiet> items) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang='vi'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Xác nhận đơn hàng</title>");
        html.append("<style>");
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        html.append(".email-container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 24px; font-weight: 600; }");
        html.append(".content { padding: 30px 20px; }");
        html.append(".order-info { background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; }");
        html.append(".order-info h3 { margin-top: 0; color: #333; border-bottom: 2px solid #667eea; padding-bottom: 10px; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }");
        html.append(".info-label { font-weight: 600; color: #555; }");
        html.append(".info-value { color: #333; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append(".items-table th { background-color: #667eea; color: white; padding: 12px; text-align: left; }");
        html.append(".items-table td { padding: 12px; border-bottom: 1px solid #eee; }");
        html.append(".items-table tr:nth-child(even) { background-color: #f9f9f9; }");
        html.append(".total-section { background-color: #e8f2ff; padding: 20px; border-radius: 8px; margin: 20px 0; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin: 8px 0; font-size: 16px; }");
        html.append(".total-final { font-weight: bold; font-size: 18px; color: #667eea; border-top: 2px solid #667eea; padding-top: 10px; margin-top: 10px; }");
        html.append(".footer { background-color: #333; color: white; padding: 20px; text-align: center; font-size: 14px; }");
        html.append(".status-badge { background-color: #28a745; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }");
        html.append("@media (max-width: 600px) { .info-row { flex-direction: column; } .info-label { margin-bottom: 5px; } }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='email-container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>🎉 Đặt hàng thành công!</h1>");
        html.append("<p>Cảm ơn bạn đã tin tương và mua sắm tại cửa hàng chúng tôi</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<p>Xin chào <strong>").append(hoaDon.getTenNguoiNhan()).append("</strong>,</p>");
        html.append("<p>Chúng tôi đã nhận được đơn hàng của bạn và đang xử lý. Dưới đây là thông tin chi tiết:</p>");

        // Order Info
        html.append("<div class='order-info'>");
        html.append("<h3>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'><span class='info-label'>Mã đơn hàng: </span><span class='info-value'><strong>").append(hoaDon.getMa()).append("</strong></span></div>");
        html.append("<div class='info-row'><span class='info-label'>Ngày đặt: </span><span class='info-value'>").append(hoaDon.getNgayTao().format(formatter)).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>Trạng thái: </span><span class='info-value'><span class='status-badge'>Chờ xác nhận</span></span></div>");
        html.append("<div class='info-row'><span class='info-label'>Phương thức thanh toán: </span><span class='info-value'>").append(hoaDon.getPhuongThuc().equals("tien_mat") ? "💵 Tiền mặt (COD)" : "🏦 Chuyển khoản").append("</span></div>");
        html.append("</div>");

        // Customer Info
        html.append("<div class='order-info'>");
        html.append("<h3>👤 Thông tin người nhận</h3>");
        html.append("<div class='info-row'><span class='info-label'>Họ tên: </span><span class='info-value'>").append(hoaDon.getTenNguoiNhan()).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>Số điện thoại: </span><span class='info-value'>").append(hoaDon.getSoDienThoai()).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>Email: </span><span class='info-value'>").append(hoaDon.getEmail()).append("</span></div>");
        html.append("<div class='info-row'><span class='info-label'>Địa chỉ giao hàng: </span><span class='info-value'>").append(hoaDon.getDiaChi()).append("</span></div>");
        if (hoaDon.getGhiChu() != null && !hoaDon.getGhiChu().isEmpty()) {
            html.append("<div class='info-row'><span class='info-label'>Ghi chú: </span><span class='info-value'>").append(hoaDon.getGhiChu()).append("</span></div>");
        }
        html.append("</div>");

        // Items
        html.append("<h3>🛍️ Sản phẩm đã đặt</h3>");
        html.append("<table class='items-table'>");
        html.append("<thead>");
        html.append("<tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (GioHangChiTiet item : items) {
            html.append("<tr>");
            html.append("<td>");
            html.append("<strong>").append(item.getChiTietSp().getSanPham().getTen()).append("</strong><br>");
            html.append("<small>Size: ").append(item.getChiTietSp().getSize().getTen()).append(" | Màu: ").append(item.getChiTietSp().getMauSac().getTen()).append("</small>");
            html.append("</td>");
            html.append("<td>").append(item.getSoLuong()).append("</td>");
            html.append("<td>").append(formatCurrency(item.getChiTietSp().getGiaBan())).append("</td>");
            html.append("<td><strong>").append(formatCurrency(item.getThanhTien())).append("</strong></td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Total Section
        html.append("<div class='total-section'>");
        html.append("<h3>💰 Tổng kết đơn hàng</h3>");
        html.append("<div class='total-row'><span>Tổng tiền hàng: </span><span>").append(formatCurrency(hoaDon.getGiaGoc())).append("</span></div>");
        if (hoaDon.getGiaGiamGia().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<div class='total-row'><span>Giảm giá:</span><span style='color: #dc3545;'>-").append(formatCurrency(hoaDon.getGiaGiamGia())).append("</span></div>");
        }
        html.append("<div class='total-row'><span>Phí vận chuyển: </span><span>").append(formatCurrency(hoaDon.getPhiVanChuyen())).append("</span></div>");
        html.append("<div class='total-row total-final'><span>Tổng thanh toán: </span><span>").append(formatCurrency(hoaDon.getThanhTien())).append("</span></div>");
        html.append("</div>");

        // Message
        html.append("<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 20px 0;'>");
        html.append("<p style='margin: 0; color: #856404;'><strong>📞 Lưu ý quan trọng:</strong></p>");
        html.append("<p style='margin: 5px 0 0 0; color: #856404;'>Chúng tôi sẽ liên hệ với bạn trong vòng 24h để xác nhận đơn hàng. Vui lòng giữ máy!</p>");
        html.append("</div>");

        html.append("<p>Cảm ơn bạn đã mua sắm tại cửa hàng chúng tôi! 💖</p>");
        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p><strong>D&C Fashions</strong></p>");
        html.append("<p>📧 Email: support@dcfashions.com | 📱 Hotline: 1900-8386</p>");
        html.append("<p>🌐 Website: www.dcfashions.com</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private String generateOrderConfirmationText(HoaDon hoaDon, List<GioHangChiTiet> items) {
        StringBuilder text = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        text.append("🎉 ĐẶT HÀNG THÀNH CÔNG!\n");
        text.append("==========================================\n\n");
        text.append("Xin chào ").append(hoaDon.getTenNguoiNhan()).append(",\n\n");
        text.append("Cảm ơn bạn đã đặt hàng tại cửa hàng chúng tôi!\n\n");

        text.append("📋 THÔNG TIN ĐƠN HÀNG:\n");
        text.append("- Mã đơn hàng: ").append(hoaDon.getMa()).append("\n");
        text.append("- Ngày đặt: ").append(hoaDon.getNgayTao().format(formatter)).append("\n");
        text.append("- Trạng thái: Chờ xác nhận\n");
        text.append("- Phương thức thanh toán: ").append(hoaDon.getPhuongThuc().equals("tien_mat") ? "Tiền mặt (COD)" : "Chuyển khoản").append("\n\n");

        text.append("👤 THÔNG TIN NGƯỜI NHẬN:\n");
        text.append("- Họ tên: ").append(hoaDon.getTenNguoiNhan()).append("\n");
        text.append("- Số điện thoại: ").append(hoaDon.getSoDienThoai()).append("\n");
        text.append("- Địa chỉ: ").append(hoaDon.getDiaChi()).append("\n\n");

        text.append("🛍️ SẢN PHẨM ĐÃ ĐẶT:\n");
        for (GioHangChiTiet item : items) {
            text.append("- ").append(item.getChiTietSp().getSanPham().getTen());
            text.append(" (Size: ").append(item.getChiTietSp().getSize().getTen());
            text.append(", Màu: ").append(item.getChiTietSp().getMauSac().getTen()).append(")");
            text.append(" - SL: ").append(item.getSoLuong());
            text.append(" - Thành tiền: ").append(formatCurrency(item.getThanhTien())).append("\n");
        }

        text.append("\n💰 TỔNG KẾT:\n");
        text.append("- Tổng tiền hàng: ").append(formatCurrency(hoaDon.getGiaGoc())).append("\n");
        if (hoaDon.getGiaGiamGia().compareTo(BigDecimal.ZERO) > 0) {
            text.append("- Giảm giá: -").append(formatCurrency(hoaDon.getGiaGiamGia())).append("\n");
        }
        text.append("- Phí vận chuyển: ").append(formatCurrency(hoaDon.getPhiVanChuyen())).append("\n");
        text.append("- TỔNG THANH TOÁN: ").append(formatCurrency(hoaDon.getThanhTien())).append("\n\n");

        text.append("📞 Chúng tôi sẽ liên hệ với bạn trong vòng 24h để xác nhận đơn hàng.\n");
        text.append("Cảm ơn bạn đã tin tưởng và mua sắm tại cửa hàng! 💖\n\n");
        text.append("==========================================\n");
        text.append("📧 Email: support@dcfashions.com\n");
        text.append("📱 Hotline: 1900-8386");

        return text.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0₫";
        return String.format("%,d₫", amount.longValue());
    }

    public void sendSimpleOrderNotification(String customerEmail, String customerName, String orderCode) {
        try {
            String htmlContent = generateSimpleNotificationHtml(customerName, orderCode);
            sendHtmlMail(customerEmail, "Cảm ơn bạn đã đặt hàng - " + orderCode, htmlContent);
        } catch (MessagingException e) {
            // Fallback to text email
            String textContent = String.format(
                    "Xin chào %s,\n\n" +
                            "Cảm ơn bạn đã đặt hàng tại cửa hàng chúng tôi!\n" +
                            "Mã đơn hàng của bạn là: %s\n\n" +
                            "Chúng tôi sẽ liên hệ với bạn sớm nhất để xác nhận đơn hàng.\n\n" +
                            "Trân trọng,\n" +
                            "D&C Fahions", customerName, orderCode);
            sendSimpleMail(customerEmail, "Cảm ơn bạn đã đặt hàng - " + orderCode, textContent);
        }
    }

    private String generateSimpleNotificationHtml(String customerName, String orderCode) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                "<div style='background: #667eea; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>" +
                "<h1 style='margin: 0; font-size: 24px;'>Cảm ơn bạn đã đặt hàng!</h1>" +
                "</div>" +
                "<div style='background: white; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 10px 10px;'>" +
                "<p>Xin chào <strong>" + customerName + "</strong>,</p>" +
                "<p>Chúng tôi đã nhận được đơn hàng của bạn với mã: <strong style='color: #667eea;'>" + orderCode + "</strong></p>" +
                "<div style='background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                "<p style='margin: 0; color: #856404;'><strong>Chúng tôi sẽ liên hệ với bạn trong vòng 24h để xác nhận đơn hàng.</strong></p>" +
                "</div>" +
                "<p>Cảm ơn bạn đã tin tưởng và mua sắm tại cửa hàng!</p>" +
                "<hr style='margin: 30px 0; border: none; height: 1px; background: #e0e0e0;'>" +
                "<p style='font-size: 14px; color: #666; text-align: center; margin: 0;'>" +
                "Email: support@dcfashions.com | Hotline: 1900-8386<br>" +
                "Website: www.dcfashions.com" +
                "</p>" +
                "</div>" +
                "</body></html>";
    }
}