package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.entity.LoaiThu;
import com.main.datn_sd31.entity.KieuDang;
import com.main.datn_sd31.entity.XuatXu;
import com.main.datn_sd31.entity.ChatLieu;
import com.main.datn_sd31.service.AuthenticationService;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Loaithurepository;
import com.main.datn_sd31.repository.Kieudangrepository;
import com.main.datn_sd31.repository.Xuatxurepository;
import com.main.datn_sd31.repository.ChatLieuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

import com.main.datn_sd31.entity.NgonNgu;
import com.main.datn_sd31.service.DichVuBanDichService;
import java.util.Locale;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private Danhmucrepository danhMucRepository;

    @Autowired
    private DichVuBanDichService dichVuBanDichService;
    
    @Autowired
    private Loaithurepository loaithurepository;
    
    @Autowired
    private Kieudangrepository kieuDangRepository;
    
    @Autowired
    private Xuatxurepository xuatXuRepository;
    
    @Autowired
    private ChatLieuRepository chatLieuRepository;

    @ModelAttribute
    public void addKhachHangToSession(HttpSession session) {
        if (authenticationService.isCustomer() && session.getAttribute("khachHang") == null) {
            session.setAttribute("khachHang", authenticationService.getCurrentCustomer());
        }
    }
    
    @ModelAttribute("danhMucs")
    public List<DanhMuc> addDanhMucsToModel() {
        return danhMucRepository.findAll().stream()
                .filter(dm -> dm.getTrangThai() != null && dm.getTrangThai())
                .toList();
    }

    @ModelAttribute("danhSachNgonNgu")
    public List<NgonNgu> addDanhSachNgonNgu() {
        return dichVuBanDichService.layDanhSachNgonNguHoatDong();
    }

    @ModelAttribute("currentLocale")
    public String addCurrentLocale(Locale locale) {
        return locale != null ? locale.getLanguage() : "vi";
    }
    
    @ModelAttribute("tenDanhMucDaDich")
    public Map<Integer, String> addTenDanhMucDaDich(Locale locale) {
        String targetLang = locale != null ? locale.getLanguage() : "vi";
        List<DanhMuc> danhMucs = danhMucRepository.findAll().stream()
                .filter(dm -> dm.getTrangThai() != null && dm.getTrangThai())
                .toList();
        return dichVuBanDichService.dichTenDanhMuc(danhMucs, targetLang);
    }
    
    @ModelAttribute("tenLoaiThuDaDich") 
    public Map<Integer, String> addTenLoaiThuDaDich(Locale locale) {
        String targetLang = locale != null ? locale.getLanguage() : "vi";
        List<LoaiThu> loaiThus = loaithurepository.findAll();
        return dichVuBanDichService.dichTenLoaiThu(loaiThus, targetLang);
    }
    
    @ModelAttribute("tenKieuDangDaDich")
    public Map<Integer, String> addTenKieuDangDaDich(Locale locale) {
        String targetLang = locale != null ? locale.getLanguage() : "vi";
        List<KieuDang> kieuDangs = kieuDangRepository.findAll();
        return dichVuBanDichService.dichTenKieuDang(kieuDangs, targetLang);
    }
    
    @ModelAttribute("tenXuatXuDaDich")
    public Map<Integer, String> addTenXuatXuDaDich(Locale locale) {
        String targetLang = locale != null ? locale.getLanguage() : "vi";
        List<XuatXu> xuatXus = xuatXuRepository.findAll();
        return dichVuBanDichService.dichTenXuatXu(xuatXus, targetLang);
    }
    
    @ModelAttribute("tenChatLieuDaDich")
    public Map<Integer, String> addTenChatLieuDaDich(Locale locale) {
        String targetLang = locale != null ? locale.getLanguage() : "vi";
        List<ChatLieu> chatLieus = chatLieuRepository.findAll();
        return dichVuBanDichService.dichTenChatLieu(chatLieus, targetLang);
    }
}