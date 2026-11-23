IF OBJECT_ID('user_addresses', 'U') IS NULL
BEGIN
    CREATE TABLE user_addresses (
        address_id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        recipient_name NVARCHAR(100) NOT NULL,
        phone VARCHAR(20) NOT NULL,
        detail_address NVARCHAR(300) NOT NULL,
        address_label NVARCHAR(50) DEFAULT N'Nhà riêng',
        is_default BIT DEFAULT 0,
        created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT FK_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(userid) ON DELETE CASCADE
    );
END

-- Thêm địa chỉ mẫu cho hien (userid 8) nếu chưa có
IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = 8)
BEGIN
    INSERT INTO user_addresses (user_id, recipient_name, phone, detail_address, address_label, is_default)
    VALUES 
    (8, N'Trịnh Khắc Hiền', '0989061245', N'Số 125 Cầu Giấy, Phường Quan Hoa, Quận Cầu Giấy, Hà Nội', N'Nhà riêng', 1),
    (8, N'Trịnh Khắc Hiền', '0989061245', N'Tầng 8, Tòa nhà FPT Tower, Số 10 Phạm Văn Bạch, Cầu Giấy, Hà Nội', N'Văn phòng', 0);
END

-- Thêm địa chỉ mẫu cho nhi (userid 11) nếu chưa có
IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = 11)
BEGIN
    INSERT INTO user_addresses (user_id, recipient_name, phone, detail_address, address_label, is_default)
    VALUES 
    (11, N'Trịnh Thảo Nhi', '0358708903', N'284 Nguyễn Trãi, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh', N'Nhà riêng', 1),
    (11, N'Trịnh Thảo Nhi', '0358708903', N'Tòa nhà Bitexco Financial Tower, Số 2 Hải Triều, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', N'Văn phòng', 0);
END

-- Thêm địa chỉ mẫu cho Minh (userid 9) nếu chưa có
IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = 9)
BEGIN
    INSERT INTO user_addresses (user_id, recipient_name, phone, detail_address, address_label, is_default)
    VALUES 
    (9, N'Giang Minh', '0989061234', N'56 Quang Trung, Phường Thạch Thang, Quận Hải Châu, Đà Nẵng', N'Nhà riêng', 1);
END
