@ModelAttribute("danhMucs")
public List<DanhMuc> addDanhMucsToModel() {
    return danhMucRepository.findAll().stream()
            .filter(dm -> dm.getTrangThai() != null && dm.getTrangThai())
            .toList();
} 