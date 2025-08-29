# 🎨 TEST CASES CHO LUỒNG THÊM MÀU SẮC MỚI

## 📋 Tổng quan luồng hoạt động

### **1. Cấu trúc Entity MauSac:**
```java
- id: Integer (Auto-generated)
- ma: String (Format: MS001, MS002,...)
- ten: String (Tên màu)
- maMau: String (Hex color code, optional)
- ngayTao: LocalDateTime
- ngaySua: LocalDateTime  
- trangThai: Boolean
```

### **2. Các endpoint chính:**
- `GET /admin/mau-sac` - Hiển thị trang với form rỗng + danh sách
- `GET /admin/mau-sac/edit/{id}` - Sửa màu (đổ dữ liệu vào form)
- `POST /admin/mau-sac/save` - Lưu (thêm mới hoặc cập nhật)
- `GET /admin/mau-sac/delete/{id}` - Xóa màu

---

## 🧪 TEST CASES CHI TIẾT

### **Test Case 1: Thêm màu mới thành công**
**Mô tả:** Thêm màu mới với dữ liệu hợp lệ

**Input:**
- Tên màu: "Xanh ngọc"
- Trạng thái: Hoạt động (true)

**Expected Output:**
- Mã tự động: MS013 (tăng từ MS012)
- Ngày tạo: Thời gian hiện tại
- Ngày sửa: Thời gian hiện tại
- Redirect về trang danh sách
- Màu mới xuất hiện trong danh sách

**Steps:**
1. Truy cập http://localhost:8080/admin/mau-sac
2. Điền "Xanh ngọc" vào ô Tên màu
3. Chọn "Hoạt động"
4. Nhấn "Thêm mới"
5. Kiểm tra kết quả

---

### **Test Case 2: Thêm màu mới với trạng thái không hoạt động**
**Mô tả:** Thêm màu mới với trạng thái false

**Input:**
- Tên màu: "Đỏ cam"
- Trạng thái: Không hoạt động (false)

**Expected Output:**
- Mã tự động: MS014
- Trạng thái: false
- Hiển thị badge "Không hoạt động" màu đỏ

---

### **Test Case 3: Sửa màu thành công**
**Mô tả:** Cập nhật thông tin màu hiện có

**Input:**
- Sửa màu MS001
- Tên mới: "Đỏ tươi"
- Trạng thái: Giữ nguyên (true)

**Expected Output:**
- Mã giữ nguyên: MS001
- Ngày tạo: Giữ nguyên
- Ngày sửa: Cập nhật thời gian hiện tại
- Tên mới: "Đỏ tươi"

**Steps:**
1. Nhấn "Sửa" bên cạnh MS001
2. Thay đổi tên thành "Đỏ tươi"
3. Nhấn "Cập nhật"
4. Kiểm tra kết quả

---

### **Test Case 4: Xóa màu thành công**
**Mô tả:** Xóa màu khỏi hệ thống

**Input:**
- Xóa màu MS012

**Expected Output:**
- Màu MS012 bị xóa khỏi database
- Không còn xuất hiện trong danh sách
- Mã MS013 vẫn giữ nguyên

**Steps:**
1. Nhấn "Xóa" bên cạnh MS012
2. Xác nhận trong popup
3. Kiểm tra màu đã bị xóa

---

### **Test Case 5: Validation - Tên màu trống**
**Mô tả:** Thử thêm màu với tên trống

**Input:**
- Tên màu: "" (trống)
- Trạng thái: Hoạt động

**Expected Output:**
- Form không submit
- Hiển thị validation error
- Yêu cầu nhập tên màu

---

### **Test Case 6: Validation - Tên màu quá dài**
**Mô tả:** Thử thêm màu với tên vượt quá 100 ký tự

**Input:**
- Tên màu: "Màu xanh lá cây rất đẹp và tươi mát..." (101 ký tự)
- Trạng thái: Hoạt động

**Expected Output:**
- Form không submit
- Hiển thị validation error
- Yêu cầu tên ngắn hơn

---

### **Test Case 7: Responsive Design**
**Mô tả:** Kiểm tra giao diện trên các thiết bị khác nhau

**Test trên:**
- Desktop (1920x1080)
- Tablet (768x1024)
- Mobile (375x667)

**Expected Output:**
- Form responsive, không bị vỡ layout
- Table có thể scroll ngang trên mobile
- Buttons và inputs dễ sử dụng trên touch

---

### **Test Case 8: Animation và UI Effects**
**Mô tả:** Kiểm tra các hiệu ứng giao diện

**Expected Output:**
- Form có animation fade-in
- Table có animation slide-in
- Hover effects trên buttons và links
- Focus states trên input fields
- Smooth transitions

---

## 🗄️ DATA TEST SAMPLE

### **Dữ liệu có sẵn sau khi chạy SQL:**
```sql
MS001 - Đỏ (#FF0000) - Hoạt động
MS002 - Xanh dương (#0000FF) - Hoạt động  
MS003 - Xanh lá (#00FF00) - Hoạt động
MS004 - Vàng (#FFFF00) - Hoạt động
MS005 - Đen (#000000) - Hoạt động
MS006 - Trắng (#FFFFFF) - Hoạt động
MS007 - Cam (#FFA500) - Hoạt động
MS008 - Tím (#800080) - Hoạt động
MS009 - Hồng (#FFC0CB) - Hoạt động
MS010 - Nâu (#A52A2A) - Hoạt động
MS011 - Xám (#808080) - Không hoạt động
MS012 - Bạc (#C0C0C0) - Không hoạt động
```

### **Dữ liệu để test thêm:**
```
MS013 - Xanh ngọc (#00FF7F) - Hoạt động
MS014 - Đỏ cam (#FF4500) - Không hoạt động
MS015 - Vàng kim (#FFD700) - Hoạt động
```

---

## 🔍 KIỂM TRA LOGIC BUSINESS

### **1. Tự động tạo mã:**
- Format: MS + 3 chữ số (MS001, MS002,...)
- Tăng dần từ mã lớn nhất hiện có
- Xử lý trường hợp không có dữ liệu (bắt đầu từ MS001)

### **2. Quản lý thời gian:**
- Ngày tạo: Chỉ set 1 lần khi thêm mới
- Ngày sửa: Luôn cập nhật khi save
- Format: LocalDateTime

### **3. Trạng thái:**
- true: Hoạt động (hiển thị badge xanh)
- false: Không hoạt động (hiển thị badge đỏ)

### **4. Validation:**
- Tên màu: Required, max 100 ký tự
- Trạng thái: Required
- Mã: Auto-generated, không được sửa

---

## 🚀 CÁCH CHẠY TEST

### **1. Chuẩn bị:**
```bash
# Chạy SQL để tạo data test
mysql -u username -p database_name < test_mau_sac_data.sql
```

### **2. Khởi động ứng dụng:**
```bash
# Chạy Spring Boot app
mvn spring-boot:run
```

### **3. Truy cập:**
```
http://localhost:8080/admin/mau-sac
```

### **4. Thực hiện test cases:**
- Làm theo từng test case ở trên
- Ghi lại kết quả thực tế
- So sánh với expected output

---

## 📝 BUGS CÓ THỂ GẶP

### **1. Validation Issues:**
- Form submit được khi tên trống
- Không validate độ dài tên màu

### **2. UI Issues:**
- Responsive không hoạt động trên mobile
- Animation không mượt
- Hover effects không hoạt động

### **3. Business Logic Issues:**
- Mã không tự động tăng đúng
- Ngày tạo bị thay đổi khi sửa
- Trạng thái không được lưu đúng

### **4. Database Issues:**
- Lỗi khi xóa màu đang được sử dụng
- Duplicate mã màu
- Lỗi encoding tiếng Việt 