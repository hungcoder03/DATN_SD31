package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class KhachHangService {


    public void sendOrderEmail(HoaDon hoaDon) {
        if (hoaDon.getKhachHang() == null || hoaDon.getKhachHang().getEmail() == null) return;

        String toEmail = hoaDon.getKhachHang().getEmail();
        String subject = "Xác nhận đơn hàng #" + hoaDon.getMa();
        String body = "Cảm ơn bạn đã đặt hàng!\n\n"
                + "Mã đơn hàng: " + hoaDon.getMa() + "\n"
                + "Tổng tiền: " + hoaDon.getThanhTien() + " VND\n"
                + "Phương thức: " + hoaDon.getPhuongThuc() + "\n"
                + "Ngày: " + hoaDon.getNgayThanhToan() + "\n\n"
                + "Chúng tôi sẽ xử lý đơn hàng trong thời gian sớm nhất.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
    }
}