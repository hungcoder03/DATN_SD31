# Chức Năng Quản Lý Nhân Viên

## Tổng quan
Chức năng quản lý nhân viên cho phép admin thêm, sửa, xóa và tìm kiếm thông tin nhân viên trong hệ thống.

## Luồng thực hiện

### 1. Truy cập trang quản lý nhân viên
- **URL**: `/admin/quanlytaikhoan/nhanvien`
- **Method**: GET
- **Controller**: `QuanLyTaiKhoan.listNhanVien()`

### 2. Thêm nhân viên mới

#### Bước 1: Nhấn nút "Thêm nhân viên"
- Mở modal form thêm nhân viên
- Tự động generate mã nhân viên mới (NV001, NV002, ...)

#### Bước 2: Điền thông tin
**Các trường bắt buộc:**
- Mã nhân viên (tự động generate, format: NV + 3-5 chữ số)
- Họ và tên (chỉ chữ cái và khoảng trắng)
- Ngày sinh (phải >= 18 tuổi)
- Giới tính (Nam/Nữ)
- Số điện thoại (10 chữ số)
- Email (phải kết thúc bằng @gmail.com)
- Mật khẩu
- Chức vụ (Nhân viên/Quản lý/Admin)
- Trạng thái (Hoạt động/Khoá)

**Các trường tùy chọn:**
- Số CMND/CCCD (12 chữ số)
- Ảnh đại diện (JPG, PNG, GIF, tối đa 5MB)

#### Bước 3: Validation
**Client-side validation:**
- Kiểm tra format dữ liệu
- Kiểm tra trùng lặp email, số điện thoại, CMND (real-time)

**Server-side validation:**
- Kiểm tra tất cả các ràng buộc dữ liệu
- Kiểm tra trùng lặp trong database
- Kiểm tra tuổi >= 18

#### Bước 4: Lưu dữ liệu
- **URL**: `/admin/quanlytaikhoan/nhanvien/save`
- **Method**: POST
- **Controller**: `QuanLyTaiKhoan.saveNhanVien()`

**Xử lý:**
1. Validation dữ liệu
2. Mã hóa mật khẩu (BCrypt)
3. Upload và lưu ảnh (nếu có)
4. Set ngày tham gia = ngày hiện tại
5. Lưu vào database
6. Redirect về danh sách với thông báo thành công

### 3. Xem chi tiết nhân viên
- **URL**: `/admin/quanlytaikhoan/nhanvien/detail?id={id}`
- **Method**: GET
- **Controller**: `QuanLyTaiKhoan.detail()`

### 4. Sửa thông tin nhân viên
- **URL**: `/admin/quanlytaikhoan/nhanvien/edit?id={id}`
- **Method**: GET (hiển thị form)
- **URL**: `/admin/quanlytaikhoan/nhanvien/update`
- **Method**: POST (xử lý cập nhật)
- **Controller**: `QuanLyTaiKhoan.editForm()` và `QuanLyTaiKhoan.updateNhanVien()`

### 5. Xóa nhân viên
- **URL**: `/admin/quanlytaikhoan/nhanvien/delete?id={id}`
- **Method**: GET
- **Controller**: `QuanLyTaiKhoan.deleteNhanVien()`

### 6. Tìm kiếm nhân viên
- **URL**: `/admin/quanlytaikhoan/nhanvien/search?search={keyword}`
- **Method**: GET
- **Controller**: `QuanLyTaiKhoan.searchNhanVien()`
- **Tìm kiếm theo**: Mã, tên, số điện thoại, email

## API Endpoints

### Validation APIs
- `GET /admin/quanlytaikhoan/nhanvien/generateMa` - Tạo mã nhân viên mới
- `GET /admin/quanlytaikhoan/nhanvien/checkEmail?email={email}` - Kiểm tra trùng email
- `GET /admin/quanlytaikhoan/nhanvien/checkSoDienThoai?soDienThoai={sdt}` - Kiểm tra trùng SĐT
- `GET /admin/quanlytaikhoan/nhanvien/checkChungMinhThu?chungMinhThu={cmnd}` - Kiểm tra trùng CMND

## Cấu trúc Files

### Backend
```
src/main/java/com/main/datn_sd31/
├── controller/admin_controller/
│   └── QuanLyTaiKhoan.java          # Controller chính
├── entity/
│   └── NhanVien.java                # Entity nhân viên
├── repository/
│   └── NhanVienRepository.java      # Repository với các query
├── service/
│   ├── NhanVienService.java         # Service interface
│   └── impl/
│       └── NhanVienServiceImpl.java # Service implementation
└── config/
    ├── SecurityConfig.java          # Cấu hình bảo mật
    └── WebConfig.java               # Cấu hình web resources
```

### Frontend
```
src/main/resources/templates/admin/pages/quan-ly-tai-khoan/
├── QuanLyNhanVien.html              # Trang danh sách + modal thêm
└── QuanLyNhanVienDetail.html        # Trang chi tiết/sửa
```

### Static Resources
```
src/main/resources/static/
├── uploads/                         # Thư mục lưu ảnh upload
└── images/
    └── default-avatar.png           # Ảnh mặc định
```

## Cấu hình

### application.properties
```properties
# File upload configuration
file.upload-dir=src/main/resources/static/uploads/
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Validation Rules

### Mã nhân viên
- Format: `^NV[0-9]{1,5}$`
- Ví dụ: NV001, NV123, NV12345

### Tên
- Format: `^[a-zA-ZÀ-ỹ\\s]+$`
- Chỉ chữ cái và khoảng trắng

### Số điện thoại
- Format: `^\\d{10}$`
- Đúng 10 chữ số

### Email
- Format: `^[A-Za-z0-9._%+-]+@gmail\\.com$`
- Phải kết thúc bằng @gmail.com

### CMND/CCCD
- Format: `^\\d{12}$|^$`
- 12 chữ số hoặc để trống

### Tuổi
- Phải >= 18 tuổi

## Tính năng

### ✅ Đã triển khai
- [x] Hiển thị danh sách nhân viên
- [x] Thêm nhân viên mới với modal
- [x] Tự động generate mã nhân viên
- [x] Validation client-side và server-side
- [x] Upload ảnh đại diện
- [x] Mã hóa mật khẩu
- [x] Xem chi tiết nhân viên
- [x] Sửa thông tin nhân viên
- [x] Xóa nhân viên
- [x] Tìm kiếm nhân viên
- [x] Kiểm tra trùng lặp real-time

### 🔄 Có thể mở rộng
- [ ] Phân quyền chi tiết theo chức vụ
- [ ] Lịch sử thay đổi thông tin
- [ ] Export danh sách nhân viên
- [ ] Import nhân viên từ Excel
- [ ] Gửi email thông báo khi tạo tài khoản
- [ ] Đổi mật khẩu riêng biệt
- [ ] Quản lý ca làm việc

## Cách sử dụng

1. **Khởi động ứng dụng**
2. **Đăng nhập admin** tại `/admin/dang-nhap`
3. **Truy cập** `/admin/quanlytaikhoan/nhanvien`
4. **Nhấn "Thêm nhân viên"** để mở modal
5. **Điền thông tin** và nhấn "Lưu nhân viên"
6. **Kiểm tra** nhân viên đã được thêm vào danh sách

## Troubleshooting

### Lỗi upload ảnh
- Kiểm tra thư mục `uploads` có quyền ghi
- Kiểm tra kích thước file <= 5MB
- Kiểm tra định dạng file (JPG, PNG, GIF)

### Lỗi validation
- Kiểm tra format dữ liệu đầu vào
- Kiểm tra trùng lặp email, SĐT, CMND
- Kiểm tra tuổi >= 18

### Lỗi 404
- Kiểm tra cấu hình routing
- Kiểm tra SecurityConfig cho phép truy cập
- Kiểm tra template path 