-- =============================================================================
-- FLAREHUB E-COMMERCE DATABASE EXPORT (SQL SERVER)
-- Hệ thống thương mại điện tử Điện Máy & Smart Home FlareHub
-- Ngày xuất bản: 2026-11-22 20:03:46
-- Bản quyền: © 2026 FlareHub Retail Platform
-- =============================================================================
-- THÔNG TIN TÀI KHOẢN ĐĂNG NHẬP MẪU:
-- 1. Quản trị viên (ADMIN):
--    - Username: admin
--    - Password: admin123
-- 2. Khách hàng (CUSTOMER) - Tất cả mật khẩu đều là: 123456
--    - Username: hien   | Họ tên: Trịnh Khắc Hiền (Hà Nội)
--    - Username: nhi    | Họ tên: Trịnh Thảo Nhi (TP.HCM)
--    - Username: hoang  | Họ tên: Hoàng Văn Nam (TP.HCM)
--    - Username: linh   | Họ tên: Vũ Thùy Linh (Hà Nội)
--    - Username: Minh   | Họ tên: Giang Minh (Đà Nẵng)
-- =============================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'ECommerceDB')
BEGIN
    CREATE DATABASE [ECommerceDB] COLLATE Vietnamese_CI_AS;
END
GO

USE [ECommerceDB];
GO

-- =============================================================================
-- 1. XÓA CÁC BẢNG CŨ (NẾU ĐÃ TỒN TẠI)
-- =============================================================================
IF OBJECT_ID(N'dbo.[admin_action_logs]', N'U') IS NOT NULL DROP TABLE dbo.[admin_action_logs];
IF OBJECT_ID(N'dbo.[wishlists]', N'U') IS NOT NULL DROP TABLE dbo.[wishlists];
IF OBJECT_ID(N'dbo.[product_reviews]', N'U') IS NOT NULL DROP TABLE dbo.[product_reviews];
IF OBJECT_ID(N'dbo.[payments]', N'U') IS NOT NULL DROP TABLE dbo.[payments];
IF OBJECT_ID(N'dbo.[order_status_history]', N'U') IS NOT NULL DROP TABLE dbo.[order_status_history];
IF OBJECT_ID(N'dbo.[order_details]', N'U') IS NOT NULL DROP TABLE dbo.[order_details];
IF OBJECT_ID(N'dbo.[orders]', N'U') IS NOT NULL DROP TABLE dbo.[orders];
IF OBJECT_ID(N'dbo.[product_images]', N'U') IS NOT NULL DROP TABLE dbo.[product_images];
IF OBJECT_ID(N'dbo.[product_variants]', N'U') IS NOT NULL DROP TABLE dbo.[product_variants];
IF OBJECT_ID(N'dbo.[products]', N'U') IS NOT NULL DROP TABLE dbo.[products];
IF OBJECT_ID(N'dbo.[user_addresses]', N'U') IS NOT NULL DROP TABLE dbo.[user_addresses];
IF OBJECT_ID(N'dbo.[store_settings]', N'U') IS NOT NULL DROP TABLE dbo.[store_settings];
IF OBJECT_ID(N'dbo.[vouchers]', N'U') IS NOT NULL DROP TABLE dbo.[vouchers];
IF OBJECT_ID(N'dbo.[users]', N'U') IS NOT NULL DROP TABLE dbo.[users];
IF OBJECT_ID(N'dbo.[categories]', N'U') IS NOT NULL DROP TABLE dbo.[categories];
GO

-- =============================================================================
-- 2. KHỞI TẠO CẤU TRÚC BẢNG (DDL)
-- =============================================================================
-- Bảng Danh mục ngành hàng
CREATE TABLE dbo.categories (
    categoryid INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL
);

-- Bảng Người dùng
CREATE TABLE dbo.users (
    userid INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER',
    is_active BIT NOT NULL DEFAULT 1,
    created_date DATETIME2 DEFAULT GETDATE(),
    deletion_reason NVARCHAR(255),
    deletion_requested BIT DEFAULT 0,
    deletion_requested_date DATETIME2,
    email_verified BIT DEFAULT 1,
    email_verify_code VARCHAR(255),
    email_verify_expiry DATETIME2,
    phone_verified BIT DEFAULT 1,
    phone_verify_code VARCHAR(255),
    phone_verify_expiry DATETIME2,
    lock_reason NVARCHAR(255),
    locked_until DATETIME2
);

-- Bảng Mã giảm giá (Vouchers)
CREATE TABLE dbo.vouchers (
    voucherid INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    discount_type VARCHAR(255) NOT NULL,
    discount_value NUMERIC(18,2) NOT NULL,
    min_order_amount NUMERIC(18,2),
    max_discount NUMERIC(18,2),
    quantity INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    start_date DATETIME2,
    end_date DATETIME2,
    active BIT NOT NULL DEFAULT 1
);

-- Bảng Cài đặt Cửa hàng & Bản đồ Google Maps
CREATE TABLE dbo.store_settings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    store_name NVARCHAR(255) NOT NULL,
    address NVARCHAR(500) NOT NULL,
    phone NVARCHAR(100),
    email VARCHAR(100),
    opening_hours NVARCHAR(200),
    map_embed_url NVARCHAR(MAX),
    updated_at DATETIME2
);

-- Bảng Sổ địa chỉ người dùng
CREATE TABLE dbo.user_addresses (
    address_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    recipient_name NVARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    detail_address NVARCHAR(300) NOT NULL,
    address_label NVARCHAR(50),
    is_default BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_user_addresses_users FOREIGN KEY (user_id) REFERENCES dbo.users(userid) ON DELETE CASCADE
);

-- Bảng Sản phẩm
CREATE TABLE dbo.products (
    productid INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    price NUMERIC(18,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    description NVARCHAR(MAX),
    imageurl VARCHAR(255),
    categoryid INT,
    created_date DATETIME2 DEFAULT GETDATE(),
    updated_date DATETIME2,
    CONSTRAINT FK_products_categories FOREIGN KEY (categoryid) REFERENCES dbo.categories(categoryid)
);

-- Bảng Biến thể sản phẩm
CREATE TABLE dbo.product_variants (
    variantid INT IDENTITY(1,1) PRIMARY KEY,
    productid INT NOT NULL,
    sku VARCHAR(255) NOT NULL,
    variant_name NVARCHAR(100) NOT NULL,
    attribute_size NVARCHAR(50),
    attribute_color NVARCHAR(50),
    color_hex VARCHAR(20),
    price NUMERIC(18,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    is_default BIT DEFAULT 0,
    CONSTRAINT FK_product_variants_products FOREIGN KEY (productid) REFERENCES dbo.products(productid) ON DELETE CASCADE
);

-- Bảng Thư viện ảnh sản phẩm
CREATE TABLE dbo.product_images (
    imageid INT IDENTITY(1,1) PRIMARY KEY,
    productid INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    display_order INT DEFAULT 0,
    is_primary BIT DEFAULT 0,
    CONSTRAINT FK_product_images_products FOREIGN KEY (productid) REFERENCES dbo.products(productid) ON DELETE CASCADE
);

-- Bảng Đơn hàng
CREATE TABLE dbo.orders (
    orderid INT IDENTITY(1,1) PRIMARY KEY,
    userid INT,
    total_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    shipping_address NVARCHAR(500),
    order_date DATETIME2 DEFAULT GETDATE(),
    voucherid INT,
    discount_amount NUMERIC(18,2) DEFAULT 0,
    CONSTRAINT FK_orders_users FOREIGN KEY (userid) REFERENCES dbo.users(userid),
    CONSTRAINT FK_orders_vouchers FOREIGN KEY (voucherid) REFERENCES dbo.vouchers(voucherid)
);

-- Bảng Chi tiết đơn hàng
CREATE TABLE dbo.order_details (
    order_detailid INT IDENTITY(1,1) PRIMARY KEY,
    orderid INT NOT NULL,
    productid INT NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(18,2) NOT NULL,
    variantid INT,
    variant_info NVARCHAR(100),
    CONSTRAINT FK_order_details_orders FOREIGN KEY (orderid) REFERENCES dbo.orders(orderid) ON DELETE CASCADE,
    CONSTRAINT FK_order_details_products FOREIGN KEY (productid) REFERENCES dbo.products(productid)
);

-- Bảng Lịch sử trạng thái đơn hàng
CREATE TABLE dbo.order_status_history (
    history_id INT IDENTITY(1,1) PRIMARY KEY,
    orderid INT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    updated_by NVARCHAR(100),
    action_title NVARCHAR(100),
    note NVARCHAR(500),
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_order_status_history_orders FOREIGN KEY (orderid) REFERENCES dbo.orders(orderid) ON DELETE CASCADE
);

-- Bảng Giao dịch thanh toán
CREATE TABLE dbo.payments (
    paymentid INT IDENTITY(1,1) PRIMARY KEY,
    orderid INT NOT NULL UNIQUE,
    method VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    transaction_code VARCHAR(255),
    payment_date DATETIME2,
    CONSTRAINT FK_payments_orders FOREIGN KEY (orderid) REFERENCES dbo.orders(orderid) ON DELETE CASCADE
);

-- Bảng Đánh giá & Bình luận sản phẩm
CREATE TABLE dbo.product_reviews (
    reviewid INT IDENTITY(1,1) PRIMARY KEY,
    userid INT NOT NULL,
    productid INT NOT NULL,
    rating INT NOT NULL,
    comment NVARCHAR(1000),
    created_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_date DATETIME2,
    CONSTRAINT UQ_reviews_user_product UNIQUE (userid, productid),
    CONSTRAINT FK_reviews_users FOREIGN KEY (userid) REFERENCES dbo.users(userid) ON DELETE CASCADE,
    CONSTRAINT FK_reviews_products FOREIGN KEY (productid) REFERENCES dbo.products(productid) ON DELETE CASCADE
);

-- Bảng Danh sách yêu thích (Wishlist)
CREATE TABLE dbo.wishlists (
    wishlistid INT IDENTITY(1,1) PRIMARY KEY,
    userid INT NOT NULL,
    productid INT NOT NULL,
    created_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_wishlists_users FOREIGN KEY (userid) REFERENCES dbo.users(userid) ON DELETE CASCADE,
    CONSTRAINT FK_wishlists_products FOREIGN KEY (productid) REFERENCES dbo.products(productid) ON DELETE CASCADE
);

-- Bảng Nhật ký thao tác quản trị viên
CREATE TABLE dbo.admin_action_logs (
    logid INT IDENTITY(1,1) PRIMARY KEY,
    admin_username VARCHAR(255),
    target_userid INT,
    target_username VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    detail NVARCHAR(500),
    created_date DATETIME2 DEFAULT GETDATE()
);
GO

-- =============================================================================
-- 3. DỮ LIỆU MẪU CHUẨN HOÁ (DML)
-- =============================================================================

-- -------------------------------------------------------------
-- Dữ liệu bảng: categories (5 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[categories] ON;
GO
INSERT INTO dbo.[categories] ([categoryid], [category_name]) VALUES
(1, N'Tivi & Thiết bị nghe nhìn'),
(2, N'Tủ lạnh & Tủ đông'),
(3, N'Máy giặt & Máy sấy'),
(4, N'Điều hòa & Làm mát'),
(5, N'Robot & Thiết bị thông minh');
GO
SET IDENTITY_INSERT dbo.[categories] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: users (6 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[users] ON;
GO
INSERT INTO dbo.[users] ([userid], [created_date], [email], [full_name], [is_active], [password], [phone], [role], [username], [deletion_reason], [deletion_requested], [deletion_requested_date], [email_verified], [email_verify_code], [email_verify_expiry], [phone_verified], [phone_verify_code], [phone_verify_expiry], [lock_reason], [locked_until]) VALUES
(7, '2026-08-22 19:35:18.160', N'ductktp00905@gmail.com', N'Trịnh Khắc Đức', 1, N'$2a$10$jRkg9RpdTIgjUQNReGAXOOl.k/U8XLSqcXL0JHohgmV50Qk65gLve', N'0927353086', N'ADMIN', N'admin', NULL, 0, NULL, 0, N'129603', '2026-11-09 11:23:48.192', 1, NULL, NULL, NULL, NULL),
(8, '2026-08-30 20:08:41.284', N'hien@gmail.com', N'Trịnh Khắc Hiền', 1, N'$2a$10$gpwJL8dTTZNog4oGVKRBuOobzBuu4CYhJz4wM8E1h/YmVnV58wVYC', N'0989061245', N'CUSTOMER', N'hien', NULL, 0, NULL, 0, N'443518', '2026-10-24 22:02:18.906', 1, NULL, NULL, NULL, NULL),
(9, '2026-09-26 20:16:39.917', N'minh@gmail.com', N'Giang Minh', 0, N'$2a$10$aQbiGa2Dv6T2JtAAHs90C.HvgJ8J7IjpK8XpASbP43p0Wi0XpmFoe', N'0989061234', N'CUSTOMER', N'Minh', NULL, 0, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(11, '2026-10-29 19:59:49.546', N'ttnhi1904@gmail.com', N'Trịnh Thảo Nhi', 1, N'$2a$10$95CYI89DNKUr7xEyuDJ2QeXPcNz48QZrpydkotUy.NcnLtL7w89S.', N'0358708903', N'CUSTOMER', N'nhi', NULL, 0, NULL, 0, NULL, NULL, 0, NULL, NULL, NULL, NULL),
(12, '2026-11-02 19:57:57.803', N'hoangnam@gmail.com', N'Hoàng Văn Nam', 1, N'/YmVnV58wVYC', N'0912345678', N'CUSTOMER', N'hoang', NULL, NULL, NULL, 1, NULL, NULL, 1, NULL, NULL, NULL, NULL),
(13, '2026-11-07 19:57:57.803', N'thuylinh.vu@gmail.com', N'Vũ Thùy Linh', 1, N'/YmVnV58wVYC', N'0987654321', N'CUSTOMER', N'linh', NULL, NULL, NULL, 1, NULL, NULL, 1, NULL, NULL, NULL, NULL);
GO
SET IDENTITY_INSERT dbo.[users] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: vouchers (5 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[vouchers] ON;
GO
INSERT INTO dbo.[vouchers] ([voucherid], [active], [code], [discount_type], [discount_value], [end_date], [max_discount], [min_order_amount], [quantity], [start_date], [used_count]) VALUES
(1, 1, N'GIAM10', N'PERCENT', 10.00, '2027-04-29 19:57:02.643', 2000000.00, 500000.00, 100, '2026-10-29 19:57:02.643', 2),
(2, 1, N'SALE200K', N'AMOUNT', 200000.00, '2027-04-29 19:57:02.717', NULL, 2000000.00, 50, '2026-10-29 19:57:02.717', 0),
(3, 1, N'DIENMAY500K', N'AMOUNT', 500000.00, '2027-05-22 19:57:57.803', 500000.00, 5000000.00, 100, '2026-10-22 19:57:57.803', 3),
(4, 1, N'FREESHIP', N'AMOUNT', 50000.00, '2027-05-22 19:57:57.803', 50000.00, 500000.00, 200, '2026-10-22 19:57:57.803', 15),
(5, 1, N'FLAREVIP', N'PERCENT', 15.00, '2027-05-22 19:57:57.803', 3000000.00, 10000000.00, 50, '2026-10-22 19:57:57.803', 1);
GO
SET IDENTITY_INSERT dbo.[vouchers] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: store_settings (1 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[store_settings] ON;
GO
INSERT INTO dbo.[store_settings] ([id], [address], [email], [map_embed_url], [opening_hours], [phone], [store_name], [updated_at]) VALUES
(1, N'Số 134 Nguyễn Thái Học, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'cskh@flarehub.vn', N'', N'08:00 - 21:30 (Cả Thứ 7, CN & Ngày lễ)', N'1800.2097 - 098.123.4567', N'FlareHub Flagship Store — TP. Hồ Chí Minh', '2026-11-22 16:18:35.249');
GO
SET IDENTITY_INSERT dbo.[store_settings] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: user_addresses (8 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[user_addresses] ON;
GO
INSERT INTO dbo.[user_addresses] ([address_id], [user_id], [recipient_name], [phone], [detail_address], [address_label], [is_default], [created_at]) VALUES
(1, 8, N'Trịnh Khắc Hiền', N'0989061245', N'Số 125 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội', N'Nhà riêng', 1, '2026-11-22 15:48:21.353'),
(2, 8, N'Trịnh Khắc Hiền', N'0989061245', N'Tầng 8, Tòa nhà FPT Tower, Số 10 Phạm Văn Bạch, Cầu Giấy, Hà Nội', N'Văn phòng', 0, '2026-11-22 15:48:21.353'),
(3, 11, N'Trịnh Thảo Nhi', N'0358708903', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'Nhà riêng', 1, '2026-11-22 15:48:21.356'),
(4, 11, N'Trịnh Thảo Nhi', N'0358708903', N'Tòa nhà Bitexco Financial Tower, Số 2 Hải Triều, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'Văn phòng', 0, '2026-11-22 15:48:21.356'),
(5, 9, N'Giang Minh', N'0989061234', N'56 Quang Trung, Phường Thạch Thang, Quận Hải Châu, Đà Nẵng', N'Nhà riêng', 1, '2026-11-22 15:48:21.356'),
(6, 12, N'Hoàng Văn Nam', N'0912345678', N'Số 45 Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'Văn phòng', 1, '2026-11-22 19:57:57.803'),
(7, 12, N'Hoàng Văn Nam', N'0912345678', N'Chung cư Vinhomes Central Park, 208 Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM', N'Nhà riêng', 0, '2026-11-22 19:57:57.803'),
(8, 13, N'Vũ Thùy Linh', N'0987654321', N'Số 88 Phố Huế, Phường Ngô Thì Nhậm, Quận Hai Bà Trưng, Hà Nội', N'Nhà riêng', 1, '2026-11-22 19:57:57.803');
GO
SET IDENTITY_INSERT dbo.[user_addresses] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: products (15 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[products] ON;
GO
INSERT INTO dbo.[products] ([productid], [created_date], [description], [imageurl], [name], [price], [stock], [updated_date], [categoryid]) VALUES
(1, '2026-11-22 14:56:12.990', N'Độ phân giải 4K sắc nét gấp 4 lần Full HD, công nghệ Dynamic Crystal Color tái tạo 1 tỷ sắc màu thuần khiết, bộ xử lý Crystal 4K nâng cấp hình ảnh thông minh. Hệ điều hành Tizen OS giao diện mượt mà, hỗ trợ trợ lý ảo giọng nói tiếng Việt Bixby.', N'/uploads/products/tv-samsung-crystal.svg', N'Smart Tivi Samsung Crystal UHD 4K 2024', 8490000.00, 45, '2026-11-22 14:56:12.990', 1),
(2, '2026-11-22 14:56:12.990', N'Bộ xử lý 4K X-Reality PRO nâng chuẩn mọi nội dung, chuyển động mượt mà Motionflow XR 200, âm thanh Dolby Audio sống động. Tích hợp Google TV với kho ứng dụng phong phú YouTube, Netflix, VieON cùng remote tìm kiếm giọng nói rảnh tay.', N'/uploads/products/tv-sony-bravia.svg', N'Google Tivi Sony 4K Bravia X75K', 10990000.00, 35, '2026-11-22 14:56:12.990', 1),
(3, '2026-11-22 14:56:12.990', N'Công nghệ Dolby Atmos không dây đầu tiên trên thế giới, âm thanh vòm đỉnh cao Q-Symphony đồng bộ hoàn hảo với loa TV Samsung, công suất 360W bùng nổ cùng loa trầm Subwoofer mạnh mẽ cho trải nghiệm rạp chiếu phim tại gia.', N'/uploads/products/soundbar-samsung.svg', N'Loa Thanh Soundbar Samsung Chuẩn 3.1.2 Kênh', 4890000.00, 30, '2026-11-22 14:56:12.990', 1),
(4, '2026-11-22 14:56:12.990', N'Dung tích cực lớn 519L cho gia đình từ 5-7 thành viên, công nghệ Smart Inverter siêu tiết kiệm điện năng vận hành êm ái, hệ thống làm lạnh đa chiều Multi Air Flow bảo quản thực phẩm luôn tươi ngon đồng đều.', N'/uploads/products/fridge-lg-sidebyside.svg', N'Tủ Lạnh Side By Side Inverter LG 519 Lít', 13990000.00, 25, '2026-11-22 14:56:12.990', 2),
(5, '2026-11-22 14:56:12.990', N'Ngăn đông mềm thế hệ mới Prime Fresh+ chuẩn -3°C nấu ngay không cần rã đông, công nghệ Inverter kết hợp cảm biến Econavi tiết kiệm 40% điện năng, công nghệ kháng khuẩn Ag Clean tiêu diệt 99.9% vi khuẩn và mùi hôi.', N'/uploads/products/fridge-panasonic.svg', N'Tủ Lạnh Inverter Panasonic 322 Lít Kháng Khuẩn Ag Clean', 9790000.00, 20, '2026-11-22 14:56:12.990', 2),
(6, '2026-11-22 14:56:12.990', N'Công nghệ trí tuệ nhân tạo AI DD tự động nhận diện độ mềm sợi vải để chọn chu trình giặt tối ưu nhất, công nghệ giặt hơi nước Steam diệt khuẩn 99.9% và bảo vệ tối đa cấu trúc sợi vải.', N'/uploads/products/washing-machine-lg.svg', N'Máy Giặt Cửa Ngang LG AI DD Inverter', 8690000.00, 30, '2026-11-22 14:56:12.990', 3),
(7, '2026-11-22 14:56:12.990', N'Giải pháp 2 trong 1 hoàn hảo: Giặt 10kg, sấy khô 7kg tiện lợi cho mùa mưa nồm ẩm. Công nghệ UltraMix hòa tan hoàn toàn bột giặt, công nghệ cảm biến SensorWash tự động điều chỉnh thời gian và lượng nước.', N'/uploads/products/washing-machine-electrolux.svg', N'Máy Giặt Sấy Electrolux UltimateCare Inverter', 14290000.00, 18, '2026-11-22 14:56:12.990', 3),
(8, '2026-11-22 14:56:12.990', N'Máy lạnh Daikin thế hệ mới sử dụng môi chất lạnh R-32 thân thiện môi trường, luồng gió Coanda tỏa đều khắp phòng không phả trực tiếp vào người, phin lọc Enzyme Blue diệt khuẩn và khử mùi hiệu quả.', N'/uploads/products/ac-daikin-inverter.svg', N'Điều Hòa Daikin Inverter 1 Chiều Tiết Kiệm Điện', 9290000.00, 40, '2026-11-22 14:56:12.990', 4),
(9, '2026-11-22 14:56:12.990', N'Công nghệ lọc khí Nanoe-X thế hệ 2 ức chế hiệu quả vi khuẩn, virus và các chất gây dị ứng suốt 24 giờ. Chế độ ECO tích hợp trí tuệ nhân tạo AI giúp cân bằng nhiệt độ tối ưu và tiết kiệm điện.', N'/uploads/products/ac-panasonic-nanoe.svg', N'Điều Hòa Panasonic Inverter Nanoe-X Lọc Bụi Mịn', 10490000.00, 30, '2026-11-22 14:56:12.990', 4),
(10, '2026-11-22 14:56:12.990', N'Robot cao cấp tự động gom rác 60 ngày, tự giặt giẻ và sấy khô bằng khí nóng chống nấm mốc. Lực hút siêu mạnh 5300Pa, camera AI RGB tránh vật cản chính xác và định vị Laser LDS vẽ bản đồ 3D ngôi nhà.', N'/uploads/products/robot-dreame-l10s.svg', N'Robot Hút Bụi Lau Nhà Dreame L10s Ultra Tự Giặt Giẻ', 12990000.00, 22, '2026-11-22 14:56:12.990', 5),
(11, '2026-11-22 14:56:12.990', N'Hệ thống lau xoay kép 200 vòng/phút đánh bay mọi vết bẩn cứng đầu, tự nâng khăn lau 7mm khi lên thảm, lực hút 5500Pa Extreme, trạm sạc đa năng tự động nạp nước sạch và giặt giẻ.', N'/uploads/products/robot-roborock-qrevo.svg', N'Robot Hút Bụi Lau Nhà Roborock Q Revo Trạm Đa Năng', 13590000.00, 20, '2026-11-22 14:56:12.990', 5),
(12, '2026-11-22 14:56:12.990', N'CADR lên tới 500m³/h phù hợp cho phòng khách và căn hộ diện tích từ 35-60m², màng lọc HEPA hiệu suất cao lọc 99.97% hạt bụi mịn PM2.5, phấn hoa và mùi khó chịu. Màn hình OLED cảm ứng hiện đại.', N'/uploads/products/airpurifier-xiaomi-pro4.svg', N'Máy Lọc Không Khí Xiaomi Smart Air Purifier 4 Pro', 1890000.00, 35, '2026-11-22 14:56:12.990', 5),
(13, '2026-11-22 14:56:12.990', N'Công nghệ Rapid Air độc quyền giảm 90% lượng chất béo trong thực phẩm, dung tích lớn 7.2L chứa vừa gà nguyên con, điều khiển cảm ứng điện tử một chạm với 16 chế độ nấu cài sẵn.', N'/uploads/products/airfryer-philips.svg', N'Nồi Chiên Không Dầu Điện Tử Philips 7.2 Lít', 2790000.00, 25, '2026-11-22 14:56:12.990', 5),
(14, '2026-11-22 14:56:12.990', N'Công nghệ nhận diện gương mặt 3D Face ID tiên tiến chống giả mạo bằng ảnh/video, cảm biến vân tay FPC Thụy Điển nhận diện trong 0.5s, kết nối Wi-Fi quản lý lịch sử mở cửa qua điện thoại thông minh.', N'/uploads/products/smartlock-kaadas.svg', N'Khóa Cửa Thông Minh Nhận Diện Khuôn Mặt Kaadas K20-F', 7890000.00, 18, '2026-11-22 14:56:12.990', 5),
(15, '2026-11-22 14:56:12.990', N'Độ phân giải 2K+ siêu nét, khả năng xoay 360 độ bao quát toàn cảnh không điểm mù, ghi hình có màu ban đêm với đèn spotlight trợ sáng, cảnh báo còi hú và đèn chớp khi phát hiện người lạ.', N'/uploads/products/camera-ezviz-c8w.svg', N'Camera An Ninh Xoay 360 Ngoài Trời Ezviz C8W 4MP 2K+', 1390000.00, 40, '2026-11-22 14:56:12.990', 5);
GO
SET IDENTITY_INSERT dbo.[products] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: product_variants (35 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[product_variants] ON;
GO
INSERT INTO dbo.[product_variants] ([variantid], [productid], [sku], [variant_name], [attribute_size], [attribute_color], [color_hex], [price], [stock], [image_url], [is_default]) VALUES
(1, 1, N'SAM-TV-43', N'43 inch - Đen Titan', N'43 inch', N'Đen Titan', N'#1e293b', 8490000.00, 20, N'/uploads/products/tv-samsung-crystal.svg', 0),
(2, 1, N'SAM-TV-55', N'55 inch - Đen Titan', N'55 inch', N'Đen Titan', N'#1e293b', 11990000.00, 15, N'/uploads/products/tv-samsung-crystal.svg', 1),
(3, 1, N'SAM-TV-65', N'65 inch - Đen Titan', N'65 inch', N'Đen Titan', N'#1e293b', 15490000.00, 10, N'/uploads/products/tv-samsung-crystal.svg', 0),
(4, 2, N'SNY-TV-50', N'50 inch - Đen Carbon', N'50 inch', N'Đen Carbon', N'#111827', 10990000.00, 15, N'/uploads/products/tv-sony-bravia.svg', 0),
(5, 2, N'SNY-TV-65', N'65 inch - Đen Carbon', N'65 inch', N'Đen Carbon', N'#111827', 16890000.00, 12, N'/uploads/products/tv-sony-bravia.svg', 1),
(6, 2, N'SNY-TV-75', N'75 inch - Đen Carbon', N'75 inch', N'Đen Carbon', N'#111827', 24990000.00, 8, N'/uploads/products/tv-sony-bravia.svg', 0),
(7, 3, N'SND-SAM-BLK', N'Tiêu chuẩn - Đen Huyền Bí', N'Tiêu chuẩn', N'Đen Huyền Bí', N'#0f172a', 4890000.00, 20, N'/uploads/products/soundbar-samsung.svg', 1),
(8, 3, N'SND-SAM-SLV', N'Tiêu chuẩn - Bạc Titan', N'Tiêu chuẩn', N'Bạc Titan', N'#94a3b8', 5190000.00, 10, N'/uploads/products/soundbar-samsung.svg', 0),
(9, 4, N'FR-LG-SLV', N'519 Lít - Bạc Thép', N'519 Lít', N'Bạc Thép', N'#94a3b8', 13990000.00, 15, N'/uploads/products/fridge-lg-sidebyside.svg', 1),
(10, 4, N'FR-LG-BLK', N'519 Lít - Đen Gương', N'519 Lít', N'Đen Gương', N'#1e293b', 14990000.00, 10, N'/uploads/products/fridge-lg-sidebyside.svg', 0),
(11, 5, N'FR-PAN-SLV', N'322 Lít - Ánh Kim Bạc', N'322 Lít', N'Ánh Kim Bạc', N'#cbd5e1', 9790000.00, 12, N'/uploads/products/fridge-panasonic.svg', 1),
(12, 5, N'FR-PAN-BLK', N'322 Lít - Đen Ánh Kim', N'322 Lít', N'Đen Ánh Kim', N'#0f172a', 10290000.00, 8, N'/uploads/products/fridge-panasonic.svg', 0),
(13, 6, N'WM-LG-9W', N'9.0 Kg - Trắng Sứ', N'9.0 Kg', N'Trắng Sứ', N'#ffffff', 8690000.00, 12, N'/uploads/products/washing-machine-lg.svg', 1),
(14, 6, N'WM-LG-9G', N'9.0 Kg - Xám Khói', N'9.0 Kg', N'Xám Khói', N'#475569', 8990000.00, 10, N'/uploads/products/washing-machine-lg.svg', 0),
(15, 6, N'WM-LG-10G', N'10.5 Kg - Xám Khói', N'10.5 Kg', N'Xám Khói', N'#475569', 10990000.00, 8, N'/uploads/products/washing-machine-lg.svg', 0),
(16, 7, N'WM-ELX-W', N'Giặt 10kg/Sấy 7kg - Trắng', N'Giặt 10kg - Sấy 7kg', N'Trắng Tinh Khôi', N'#ffffff', 14290000.00, 10, N'/uploads/products/washing-machine-electrolux.svg', 1),
(17, 7, N'WM-ELX-G', N'Giặt 10kg/Sấy 7kg - Xám', N'Giặt 10kg - Sấy 7kg', N'Xám Titan', N'#334155', 15190000.00, 8, N'/uploads/products/washing-machine-electrolux.svg', 0),
(18, 8, N'AC-DKN-10', N'1.0 HP (9000 BTU) - Trắng', N'1.0 HP (9000 BTU)', N'Trắng Ngọc', N'#f8fafc', 9290000.00, 18, N'/uploads/products/ac-daikin-inverter.svg', 1),
(19, 8, N'AC-DKN-15', N'1.5 HP (12000 BTU) - Trắng', N'1.5 HP (12000 BTU)', N'Trắng Ngọc', N'#f8fafc', 11490000.00, 14, N'/uploads/products/ac-daikin-inverter.svg', 0),
(20, 8, N'AC-DKN-20', N'2.0 HP (18000 BTU) - Trắng', N'2.0 HP (18000 BTU)', N'Trắng Ngọc', N'#f8fafc', 17990000.00, 8, N'/uploads/products/ac-daikin-inverter.svg', 0),
(21, 9, N'AC-PAN-10', N'1.0 HP (9000 BTU) - Trắng', N'1.0 HP (9000 BTU)', N'Trắng Sứ', N'#ffffff', 10490000.00, 18, N'/uploads/products/ac-panasonic-nanoe.svg', 1),
(22, 9, N'AC-PAN-15', N'1.5 HP (12000 BTU) - Trắng', N'1.5 HP (12000 BTU)', N'Trắng Sứ', N'#ffffff', 12890000.00, 12, N'/uploads/products/ac-panasonic-nanoe.svg', 0),
(23, 10, N'RB-DRM-WHT', N'Tiêu Chuẩn - Trắng Ceramic', N'Tiêu Chuẩn', N'Trắng Ceramic', N'#ffffff', 12990000.00, 14, N'/uploads/products/robot-dreame-l10s.svg', 1),
(24, 10, N'RB-DRM-BLK', N'Bản Đặc Biệt - Đen Mờ', N'Bản Đặc Biệt', N'Đen Mờ', N'#1e293b', 13490000.00, 8, N'/uploads/products/robot-dreame-l10s.svg', 0),
(25, 11, N'RB-ROB-WHT', N'Trạm Đa Năng - Trắng Tinh Khôi', N'Trạm Đa Năng', N'Trắng Tinh Khôi', N'#ffffff', 13590000.00, 12, N'/uploads/products/robot-roborock-qrevo.svg', 1),
(26, 11, N'RB-ROB-BLK', N'Trạm Đa Năng - Đen Carbon', N'Trạm Đa Năng', N'Đen Carbon', N'#111827', 13990000.00, 8, N'/uploads/products/robot-roborock-qrevo.svg', 0),
(27, 12, N'AP-MI-PRO', N'Bản Pro (35-60m²) - Trắng', N'Bản Pro (35-60m²)', N'Trắng Tinh Khôi', N'#ffffff', 3990000.00, 20, N'/uploads/products/airpurifier-xiaomi-pro4.svg', 1),
(28, 12, N'AP-MI-CMP', N'Bản Compact (16-27m²) - Trắng', N'Bản Compact (16-27m²)', N'Trắng Tinh Khôi', N'#ffffff', 1890000.00, 15, N'/uploads/products/airpurifier-xiaomi-pro4.svg', 0),
(29, 13, N'AF-PHI-72', N'7.2 Lít - Đen Viền Đồng', N'7.2 Lít', N'Đen Viền Đồng', N'#262626', 3490000.00, 15, N'/uploads/products/airfryer-philips.svg', 1),
(30, 13, N'AF-PHI-62', N'6.2 Lít - Đen Viền Bạc', N'6.2 Lít', N'Đen Viền Bạc', N'#333333', 2790000.00, 10, N'/uploads/products/airfryer-philips.svg', 0),
(31, 14, N'LK-KAA-BLK', N'Mở Khóa 3D Face - Đen Nhám Titan', N'Mở Khóa 3D Face', N'Đen Nhám Titan', N'#1e1e1e', 7890000.00, 10, N'/uploads/products/smartlock-kaadas.svg', 1),
(32, 14, N'LK-KAA-COP', N'Mở Khóa 3D Face - Đồng Đỏ Cổ Điển', N'Mở Khóa 3D Face', N'Đồng Đỏ Cổ Điển', N'#843b2b', 8290000.00, 8, N'/uploads/products/smartlock-kaadas.svg', 0),
(33, 15, N'CAM-EZV-32', N'Kèm Thẻ 32GB - Trắng Sứ', N'Kèm Thẻ 32GB', N'Trắng Sứ', N'#ffffff', 1390000.00, 15, N'/uploads/products/camera-ezviz-c8w.svg', 1),
(34, 15, N'CAM-EZV-64', N'Kèm Thẻ 64GB - Trắng Sứ', N'Kèm Thẻ 64GB', N'Trắng Sứ', N'#ffffff', 1590000.00, 15, N'/uploads/products/camera-ezviz-c8w.svg', 0),
(35, 15, N'CAM-EZV-128', N'Kèm Thẻ 128GB - Trắng Sứ', N'Kèm Thẻ 128GB', N'Trắng Sứ', N'#ffffff', 1850000.00, 10, N'/uploads/products/camera-ezviz-c8w.svg', 0);
GO
SET IDENTITY_INSERT dbo.[product_variants] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: product_images (22 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[product_images] ON;
GO
INSERT INTO dbo.[product_images] ([imageid], [productid], [image_url], [display_order], [is_primary]) VALUES
(1, 1, N'/uploads/products/tv-samsung-crystal.svg', 1, 1),
(2, 1, N'/uploads/products/tv-samsung-crystal-side.svg', 2, 0),
(3, 1, N'/uploads/products/tv-samsung-crystal-room.svg', 3, 0),
(4, 2, N'/uploads/products/tv-sony-bravia.svg', 1, 1),
(5, 2, N'/uploads/products/tv-sony-bravia-angle.svg', 2, 0),
(6, 3, N'/uploads/products/soundbar-samsung.svg', 1, 1),
(7, 4, N'/uploads/products/fridge-lg-sidebyside.svg', 1, 1),
(8, 4, N'/uploads/products/fridge-lg-open.svg', 2, 0),
(9, 5, N'/uploads/products/fridge-panasonic.svg', 1, 1),
(10, 6, N'/uploads/products/washing-machine-lg.svg', 1, 1),
(11, 6, N'/uploads/products/washing-machine-lg-open.svg', 2, 0),
(12, 7, N'/uploads/products/washing-machine-electrolux.svg', 1, 1),
(13, 8, N'/uploads/products/ac-daikin-inverter.svg', 1, 1),
(14, 8, N'/uploads/products/ac-daikin-remote.svg', 2, 0),
(15, 9, N'/uploads/products/ac-panasonic-nanoe.svg', 1, 1),
(16, 10, N'/uploads/products/robot-dreame-l10s.svg', 1, 1),
(17, 10, N'/uploads/products/robot-dreame-station.svg', 2, 0),
(18, 11, N'/uploads/products/robot-roborock-qrevo.svg', 1, 1),
(19, 12, N'/uploads/products/airpurifier-xiaomi-pro4.svg', 1, 1),
(20, 13, N'/uploads/products/airfryer-philips.svg', 1, 1),
(21, 14, N'/uploads/products/smartlock-kaadas.svg', 1, 1),
(22, 15, N'/uploads/products/camera-ezviz-c8w.svg', 1, 1);
GO
SET IDENTITY_INSERT dbo.[product_images] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: orders (12 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[orders] ON;
GO
INSERT INTO dbo.[orders] ([orderid], [order_date], [shipping_address], [status], [total_amount], [userid], [discount_amount], [voucherid]) VALUES
(101, '2026-11-22 14:44:59.676', N'Số 125 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội', N'PENDING', 15490000.00, 8, 0.00, NULL),
(102, '2026-11-22 14:59:59.676', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'SHIPPING', 12990000.00, 11, 0.00, NULL),
(103, '2026-11-22 10:09:59.676', N'56 Quang Trung, Phường Thạch Thang, Quận Hải Châu, Đà Nẵng', N'SHIPPING', 8690000.00, 9, 0.00, NULL),
(104, '2026-11-21 15:09:59.676', N'Số 125 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội', N'COMPLETED', 3490000.00, 8, 0.00, NULL),
(105, '2026-11-20 15:09:59.676', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'COMPLETED', 9290000.00, 11, 0.00, NULL),
(106, '2026-11-22 19:58:30.820', N'Số 45 Lê Duẩn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'PENDING', 10990000.00, 12, 0.00, NULL),
(107, '2026-11-20 19:58:30.820', N'Số 88 Phố Huế, Phường Ngô Thì Nhậm, Quận Hai Bà Trưng, Hà Nội', N'COMPLETED', 13990000.00, 13, 0.00, NULL),
(108, '2026-11-19 19:58:30.820', N'Số 125 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội', N'COMPLETED', 12990000.00, 8, 0.00, NULL),
(109, '2026-11-18 19:58:30.820', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'COMPLETED', 8690000.00, 11, 0.00, NULL),
(110, '2026-11-17 19:58:30.820', N'Chung cư Vinhomes Central Park, 208 Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM', N'COMPLETED', 9290000.00, 12, 0.00, NULL),
(111, '2026-11-20 19:58:30.820', N'Số 88 Phố Huế, Phường Ngô Thì Nhậm, Quận Hai Bà Trưng, Hà Nội', N'CANCELLED', 4680000.00, 13, 0.00, NULL),
(112, '2026-11-18 19:58:30.820', N'56 Quang Trung, Phường Thạch Thang, Quận Hải Châu, Đà Nẵng', N'CANCELLED', 7890000.00, 9, 0.00, NULL);
GO
SET IDENTITY_INSERT dbo.[orders] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: order_details (13 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[order_details] ON;
GO
INSERT INTO dbo.[order_details] ([order_detailid], [price], [quantity], [orderid], [productid], [variantid], [variant_info]) VALUES
(38, 15490000.00, 1, 101, 1, 3, N'65 inch - Đen Titan'),
(39, 12990000.00, 1, 102, 10, 23, N'Tiêu Chuẩn - Trắng Ceramic'),
(40, 8690000.00, 1, 103, 6, 13, N'9.0 Kg - Trắng Sứ'),
(41, 3490000.00, 1, 104, 13, 29, N'7.2 Lít - Đen Viền Đồng'),
(42, 9290000.00, 1, 105, 8, 18, N'1.0 HP (9000 BTU) - Trắng Ngọc'),
(43, 10990000.00, 1, 106, 2, 4, N'50 inch - Đen Carbon'),
(44, 13990000.00, 1, 107, 4, 9, N'519 Lít - Bạc Thép'),
(45, 12990000.00, 1, 108, 10, 23, N'Bản Quốc Tế - Trắng Tinh Khôi'),
(46, 8690000.00, 1, 109, 6, 14, N'9 Kg - Trắng Tinh Khiết'),
(47, 9290000.00, 1, 110, 8, 19, N'1.0 HP (9000 BTU) - Trắng Sứ'),
(48, 1890000.00, 1, 111, 12, 27, N'Bản Quốc Tế - Trắng Tinh Khôi'),
(49, 2790000.00, 1, 111, 13, 29, N'7.2 Lít - Đen Bóng'),
(50, 7890000.00, 1, 112, 14, 31, N'Nhận diện khuôn mặt Face 3D - Đồng Cổ');
GO
SET IDENTITY_INSERT dbo.[order_details] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: order_status_history (19 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[order_status_history] ON;
GO
INSERT INTO dbo.[order_status_history] ([history_id], [orderid], [from_status], [to_status], [updated_by], [action_title], [note], [created_at]) VALUES
(1, 101, NULL, N'PENDING', N'Trịnh Khắc Hiền', N'Khách đặt hàng thành công', N'Đơn hàng mới được tạo trực tuyến. Hình thức thanh toán: Tiền mặt khi nhận hàng (COD)', '2026-11-22 15:03:40.153'),
(2, 102, NULL, N'PENDING', N'Trịnh Thảo Nhi', N'Khách đặt hàng thành công', N'Đơn hàng mới được tạo trực tuyến. Hình thức thanh toán: Chuyển khoản online VNPay', '2026-11-22 15:18:40.153'),
(3, 103, NULL, N'PENDING', N'Giang Minh', N'Khách đặt hàng thành công', N'Đơn hàng được khởi tạo qua website.', '2026-11-22 10:28:40.153'),
(4, 103, N'PENDING', N'SHIPPING', N'admin', N'Bàn giao vận chuyển & Bắt đầu giao hàng', N'Kỹ thuật viên đã nhận thiết bị tại kho Đà Nẵng và đang liên hệ giao hàng.', '2026-11-22 11:28:40.153'),
(5, 104, NULL, N'PENDING', N'Trịnh Khắc Hiền', N'Khách đặt hàng thành công', N'Đơn hàng thanh toán VNPay', '2026-11-21 15:28:40.153'),
(6, 104, N'PENDING', N'SHIPPING', N'admin', N'Bàn giao vận chuyển', N'Hàng đã xuất kho và bàn giao cho nhân viên giao vận.', '2026-11-21 19:28:40.153'),
(7, 104, N'SHIPPING', N'COMPLETED', N'admin', N'Giao hàng thành công & Hoàn tất đơn', N'Khách hàng đã nhận nồi chiên không dầu Philips, kiểm tra máy hoạt động tốt.', '2026-11-21 21:28:40.153'),
(8, 105, NULL, N'PENDING', N'Trịnh Thảo Nhi', N'Khách đặt hàng thành công', N'Đơn hàng mới tạo', '2026-11-20 15:28:40.153'),
(9, 105, N'PENDING', N'SHIPPING', N'admin', N'Bàn giao vận chuyển & Khảo sát lắp đặt', N'Kỹ thuật viên mang theo ống đồng và vật tư lắp đặt điều hòa.', '2026-11-21 15:28:40.153'),
(10, 105, N'SHIPPING', N'COMPLETED', N'admin', N'Lắp đặt hoàn tất & Thu tiền COD', N'Đã hoàn tất hút chân không, kiểm tra độ lạnh máy Daikin và thu đủ tiền mặt.', '2026-11-22 03:28:40.153'),
(11, 102, N'PENDING', N'PENDING', N'admin', N'Ghi chú tiến độ', N'Đã gọi điện xác nhận, khách nhận hàng trước 17h', '2026-11-22 15:31:28.004'),
(12, 102, N'PENDING', N'SHIPPING', N'admin', N'Bàn giao vận chuyển & Bắt đầu giao hàng', N'Admin đã xác nhận đơn hàng, hoàn tất đóng gói và bàn giao kỹ thuật viên đi giao tận nơi.', '2026-11-22 15:31:38.085'),
(13, 106, NULL, N'PENDING', N'Khách hàng', N'Đặt hàng thành công', N'Khách hàng đặt hàng trực tuyến phương thức thanh toán COD khi nhận hàng.', '2026-11-22 19:58:30.823'),
(14, 107, NULL, N'PENDING', N'Khách hàng', N'Đặt hàng thành công', N'Thanh toán qua cổng VNPay thành công.', '2026-11-20 19:58:30.826'),
(15, 107, N'PENDING', N'SHIPPING', N'admin', N'Xác nhận và giao vận chuyển', N'Đã xuất kho FlareHub Hà Nội, bàn giao đơn vị vận chuyển Viettel Post.', '2026-11-20 21:58:30.826'),
(16, 107, N'SHIPPING', N'COMPLETED', N'admin', N'Giao hàng thành công', N'Khách hàng đã nhận tủ lạnh và kỹ thuật viên hoàn tất lắp đặt bàn giao.', '2026-11-20 23:58:30.826'),
(17, 108, N'SHIPPING', N'COMPLETED', N'admin', N'Giao hàng thành công', N'Giao robot Dreame thành công, khách đã kích hoạt bảo hành điện tử.', '2026-11-19 19:58:30.826'),
(18, 111, N'PENDING', N'CANCELLED', N'Khách hàng', N'Hủy đơn hàng', N'Khách đổi ý muốn đặt combo khác cao cấp hơn.', '2026-11-20 19:58:30.826'),
(19, 112, N'PENDING', N'CANCELLED', N'Khách hàng', N'Hủy đơn hàng', N'Khách hàng chọn nhầm địa chỉ nhận hàng.', '2026-11-18 19:58:30.826');
GO
SET IDENTITY_INSERT dbo.[order_status_history] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: payments (12 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[payments] ON;
GO
INSERT INTO dbo.[payments] ([paymentid], [method], [payment_date], [status], [transaction_code], [orderid]) VALUES
(23, N'CASH', NULL, N'PENDING', NULL, 101),
(24, N'VNPAY', NULL, N'PENDING', NULL, 102),
(25, N'CASH', NULL, N'PENDING', NULL, 103),
(26, N'VNPAY', '2026-11-21 15:13:04.713', N'SUCCESS', N'VNPAY20261121104', 104),
(27, N'CASH', '2026-11-20 15:13:04.713', N'SUCCESS', N'CASH_RECEIPT_105', 105),
(28, N'COD', NULL, N'PENDING', NULL, 106),
(29, N'VNPAY', '2026-11-20 19:58:30.823', N'COMPLETED', N'VNP14829371', 107),
(30, N'VNPAY', '2026-11-19 19:58:30.823', N'COMPLETED', N'VNP14829382', 108),
(31, N'COD', '2026-11-18 19:58:30.823', N'COMPLETED', N'COD_PAID_009', 109),
(32, N'VNPAY', '2026-11-17 19:58:30.823', N'COMPLETED', N'VNP14829405', 110),
(33, N'COD', NULL, N'CANCELLED', NULL, 111),
(34, N'COD', NULL, N'CANCELLED', NULL, 112);
GO
SET IDENTITY_INSERT dbo.[payments] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: product_reviews (14 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[product_reviews] ON;
GO
INSERT INTO dbo.[product_reviews] ([reviewid], [comment], [created_date], [rating], [productid], [userid], [updated_date]) VALUES
(5, N'Tivi Samsung 4K màn hình cực nét, xem bóng đá với phim chiếu rạp rất đã mắt. Kỹ thuật viên FlareHub hỗ trợ treo tường nhiệt tình và cẩn thận!', '2026-11-17 19:57:57.803', 5, 1, 8, NULL),
(6, N'Thiết kế viền siêu mỏng sang trọng, màu sắc rực rỡ, giao diện Tizen OS mượt mà dễ dùng cho cả người lớn tuổi trong nhà.', '2026-11-19 19:57:57.803', 5, 1, 11, NULL),
(7, N'Google Tivi Sony chất âm đỉnh cao, hình ảnh chiều sâu tốt đặc trưng của dòng Bravia. Mua đợt trợ giá thu cũ đổi mới tiết kiệm được 2 triệu.', '2026-11-18 19:57:57.803', 5, 2, 12, NULL),
(8, N'Loa Soundbar Samsung âm bass uy lực, kết nối bluetooth với tivi nhanh chóng chỉ 1 chạm, nghe nhạc rất êm.', '2026-11-20 19:57:57.803', 4, 3, 13, NULL),
(9, N'Tủ lạnh LG Side by Side dung tích lớn để đồ cả tuần thoải mái, chạy cực kỳ êm không hề có tiếng ồn. Công nghệ làm lạnh LinearCooling rau củ để 5 ngày vẫn tươi xanh.', '2026-11-16 19:57:57.803', 5, 4, 8, NULL),
(10, N'Giao hàng đúng hẹn trong 2 tiếng, nhân viên khiêng lên tận tầng 2 lắp đặt hướng dẫn chu đáo. Đáng tiền!', '2026-11-21 19:57:57.803', 5, 4, 9, NULL),
(11, N'Máy giặt LG AI DD thông minh tự cân lượng đồ để chỉnh chu trình giặt, quần áo giặt xong thơm tho và không bị nhăn rách sợi vải.', '2026-11-18 19:57:57.803', 5, 6, 11, NULL),
(12, N'Chế độ giặt hơi nước diệt khuẩn rất tốt cho đồ em bé. Máy êm ru lúc vắt tốc độ cao 1400 vòng.', '2026-11-19 19:57:57.803', 4, 6, 12, NULL),
(13, N'Điều hòa Daikin làm lạnh sâu và dễ chịu, bật cả đêm không bị khô họng, hóa đơn tiền điện tháng vừa rồi giảm rõ rệt.', '2026-11-17 19:57:57.803', 5, 8, 13, NULL),
(14, N'Robot Dreame L10s Ultra đúng là chân ái giải phóng sức lao động! Tự động giặt giẻ sấy khô nước nóng, lau nhà sạch bóng không tì vết.', '2026-11-20 19:57:57.803', 5, 10, 8, NULL),
(15, N'Bản đồ laser quét cực chuẩn, né dây điện và đồ chơi rất thông minh. Khuyên mọi người nên đầu tư.', '2026-11-21 19:57:57.803', 5, 10, 11, NULL),
(16, N'Roborock Q Revo lau xoay kép đánh bay vết bẩn cà phê khô, trạm sạc đa năng rất tiện lợi.', '2026-11-19 19:57:57.803', 4, 11, 9, NULL),
(17, N'Máy lọc không khí Xiaomi chạy êm, cảm biến bụi mịn PM2.5 nhạy, không khí phòng ngủ thoáng đãng hẳn.', '2026-11-20 19:57:57.803', 5, 12, 12, NULL),
(18, N'Nồi chiên Philips 7.2L nướng được cả con gà lớn, giòn ngoài mềm trong không cần dầu mỡ, cả nhà ai cũng thích.', '2026-11-21 19:57:57.803', 5, 13, 13, NULL);
GO
SET IDENTITY_INSERT dbo.[product_reviews] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: wishlists (7 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[wishlists] ON;
GO
INSERT INTO dbo.[wishlists] ([wishlistid], [created_date], [productid], [userid]) VALUES
(18, '2026-11-22 19:57:57.803', 2, 8),
(19, '2026-11-22 19:57:57.803', 10, 8),
(20, '2026-11-22 19:57:57.803', 4, 11),
(21, '2026-11-22 19:57:57.803', 8, 11),
(22, '2026-11-22 19:57:57.803', 1, 9),
(23, '2026-11-22 19:57:57.803', 14, 12),
(24, '2026-11-22 19:57:57.803', 6, 13);
GO
SET IDENTITY_INSERT dbo.[wishlists] OFF;
GO

-- -------------------------------------------------------------
-- Dữ liệu bảng: admin_action_logs (3 dòng)
-- -------------------------------------------------------------
SET IDENTITY_INSERT dbo.[admin_action_logs] ON;
GO
INSERT INTO dbo.[admin_action_logs] ([logid], [action], [admin_username], [created_date], [detail], [target_userid], [target_username]) VALUES
(1, N'Khóa tài kho?n', N'admin', '2026-10-25 12:24:27.480', N'v?nh vi?n', 9, N'Minh'),
(2, N'M? khóa', N'admin', '2026-10-25 19:51:30.091', N'M? khóa th? công', 9, N'Minh'),
(3, N'Khóa tài kho?n', N'admin', '2026-10-25 19:52:32.164', N'v?nh vi?n', 9, N'Minh');
GO
SET IDENTITY_INSERT dbo.[admin_action_logs] OFF;
GO

