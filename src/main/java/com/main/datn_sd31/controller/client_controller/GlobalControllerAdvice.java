package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.service.AuthenticationService;
import com.main.datn_sd31.repository.Danhmucrepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private Danhmucrepository danhMucRepository;

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
}