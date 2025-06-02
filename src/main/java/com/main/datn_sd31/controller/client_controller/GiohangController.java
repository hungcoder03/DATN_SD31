package com.example.demo.controler;

import com.example.demo.DTO.Sanphamform;
import com.example.demo.entity.Chitietsanpham;
import com.example.demo.entity.Giohang;
import com.example.demo.entity.Hinhanh;
import com.example.demo.entity.Khachhang;
import com.example.demo.entity.Sanpham;
import com.example.demo.repository.Chatlieurepository;
import com.example.demo.repository.Chitietsanphamrepository;
import com.example.demo.repository.Danhmucrepository;
import com.example.demo.repository.Giohangreposiroty;
import com.example.demo.repository.Hinhanhrepository;
import com.example.demo.repository.Khachhangrepository;
import com.example.demo.repository.Kieudangrepository;
import com.example.demo.repository.Mausacrepository;
import com.example.demo.repository.Nhanvienrepository;
import com.example.demo.repository.Sizerepository;
import com.example.demo.repository.Thuonghieurepository;
import com.example.demo.repository.Xuatxurepository;
import com.example.demo.service.Sanphamservice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
    private final Khachhangrepository khachhangrepository;


    @GetMapping("/hien_thi")
    public String hienthi(Model model) {
        model.addAttribute("list", giohangreposiroty.findAll());
        return "/view/giohang/list";
    }
    @PostMapping("/them")
    public String xuLyThem( @RequestParam("sanPhamId") Integer sanphamId,
                            @RequestParam("sizeId") Integer sizeId,
                            @RequestParam("mauSacId") Integer mauSacId,
                            @RequestParam("soLuong") Integer soluong,
                            Model model) {

        // Tìm chi tiết sản phẩm theo sanPhamId + sizeId + mauSacId
        Chitietsanpham chiTiet = chitietsanphamRepo.findBySanPhamIdAndSizeIdAndMauSacId(sanphamId, sizeId, mauSacId);
        Giohang gh=new Giohang();
        Khachhang kh= khachhangrepository.find(1);
        // Tạo đối tượng giỏ hàng mới (hoặc cập nhật nếu có)
        gh.setKhachhang(kh);
        gh.setChiTietSanPham(chiTiet);
        gh.setSoluong(soluong);
        gh.setTrangthai(0);
        gh.setThanhtien(chiTiet.getGiaBan()*soluong);

        giohangreposiroty.save(gh);
        return "redirect:/gio-hang/hien_thi";
    }


    @GetMapping("/xoa/{id}")
    public String xoa(@PathVariable("id") Integer id) {
        sanPhamService.delete(id);
        return "redirect:/admin/san-pham/hien_thi";
    }
    @GetMapping("/xem/{id}")
    public String danhSachChiTiet(Model model, @PathVariable("id") Integer id) {
        // Lấy danh sách chi tiết sản phẩm dựa trên id sản phẩm
        List<Chitietsanpham> danhSachChiTiet = chitietsanphamRepo.findBySanPhamId(id);
        model.addAttribute("dsChiTietSanPham", danhSachChiTiet);

        // Lấy thông tin sản phẩm chính (nếu cần hiển thị tên hoặc info sản phẩm)
        Sanpham sanPham = sanPhamService.findbyid(id);
        model.addAttribute("sanPham", sanPham);

        // Các danh sách phục vụ cho dropdown trong view
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh",hinhanhrepository.findByhinhanhid(id));
        model.addAttribute("dsMauSac", mausacrepository.findAll());
        model.addAttribute("dsLoaiThu", xuatxurepository.findAll());
        model.addAttribute("dsSize", sizerepository.findAll());

        return "/view/sanpham/xemchitiet";
    }

}


