package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.PhieuGiamGia;
import com.main.datn_sd31.dto.phieu_giam_gia.PhieuGiamGiaDto;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/phieu-giam-gia")
@RequiredArgsConstructor
public class PhieuGiamGiaController {
    private final PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping
    public String index(Model model) {
        List<PhieuGiamGiaDto> list = phieuGiamGiaService.findAll();
        model.addAttribute("page", "admin/PhieuGiamGia/index");
        model.addAttribute("listData", list);
        model.addAttribute("phieuGiamGia", new PhieuGiamGia());
        return "admin/index";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> create(
            @Valid @ModelAttribute("phieuGiamGia") PhieuGiamGia dto,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errorMessage
            ));
        }

        dto.setNgayTao(LocalDate.now());
        phieuGiamGiaService.save(dto);

        return ResponseEntity.ok(Map.of(
                "success", true
        ));
    }


    @GetMapping("/get/{id}")
    @ResponseBody
    public ResponseEntity<PhieuGiamGiaDto> getPhieuGiamGia(@PathVariable Integer id) {
        return ResponseEntity.ok(phieuGiamGiaService.findDtoById(id));
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> update(@ModelAttribute("phieuGiamGia") @Valid PhieuGiamGia pg,
                                    BindingResult result) {
        if (result.hasErrors()) {
            String errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", errorMessages
            ));
        }

        try {
            pg.setNgaySua(LocalDate.now());
            phieuGiamGiaService.save(pg);
            return ResponseEntity.ok(Map.of(
                    "success", true
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        phieuGiamGiaService.delete(id);
        return "redirect:/admin/phieu-giam-gia";
    }
}
