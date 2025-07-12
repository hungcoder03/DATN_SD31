package com.main.datn_sd31.service;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public interface HoaDonService {

    Pagination<HoaDonDTO> getAll(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate);

    List<HoaDonDTO> getAllHoaDon();

    Map<String, Long> getTrangThaiCount();

    Pagination<HoaDonDTO> getAllHoaDonByStatus(TrangThaiLichSuHoaDon status, int pageNo, int pageSize);

    Pagination<HoaDonDTO> searchByKeyword(String keyword, int pageNo, int pageSize);

    HoaDonDTO getHoaDonByMa(String ma);

}
