-- ============================================
-- SCRIPT TAO TOAN BO DATABASE ECommerceDB
-- Chay 1 lan duy nhat (Ctrl+A -> Execute)
-- ============================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'ECommerceDB')
BEGIN
    ALTER DATABASE ECommerceDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE ECommerceDB;
END
GO

CREATE DATABASE ECommerceDB;
GO

USE ECommerceDB;
GO

-- ============================================
-- TAO BANG
-- ============================================

CREATE TABLE categories (
    categoryid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL
);
GO

CREATE TABLE users (
    userid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    is_active BIT,
    created_date DATETIME2(7)
);
GO

ALTER TABLE users ADD CONSTRAINT UQ_users_username UNIQUE (username);
ALTER TABLE users ADD CONSTRAINT UQ_users_email UNIQUE (email);
GO

CREATE TABLE products (
    productid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    price NUMERIC(18,2) NOT NULL,
    stock INT NOT NULL,
    categoryid INT NOT NULL,
    imageurl VARCHAR(255),
    created_date DATETIME2(7),
    updated_date DATETIME2(7),
    CONSTRAINT FK_products_categories FOREIGN KEY (categoryid) REFERENCES categories(categoryid)
);
GO

CREATE TABLE orders (
    orderid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    userid INT NOT NULL,
    total_amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    order_date DATETIME2(7),
    CONSTRAINT FK_orders_users FOREIGN KEY (userid) REFERENCES users(userid)
);
GO

CREATE TABLE order_details (
    order_detailid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    orderid INT NOT NULL,
    productid INT NOT NULL,
    quantity INT NOT NULL,
    price NUMERIC(18,2) NOT NULL,
    CONSTRAINT FK_orderdetails_orders FOREIGN KEY (orderid) REFERENCES orders(orderid),
    CONSTRAINT FK_orderdetails_products FOREIGN KEY (productid) REFERENCES products(productid)
);
GO

CREATE TABLE payments (
    paymentid INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    orderid INT NOT NULL,
    method VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    transaction_code VARCHAR(255),
    payment_date DATETIME2(7),
    CONSTRAINT FK_payments_orders FOREIGN KEY (orderid) REFERENCES orders(orderid),
    CONSTRAINT UQ_payments_orderid UNIQUE (orderid)
);
GO

-- ============================================
-- DU LIEU MAU: DANH MUC
-- ============================================

INSERT INTO categories (category_name) VALUES
(N'Dien thoai'),
(N'Laptop'),
(N'Phu kien'),
(N'Dong ho thong minh'),
(N'May tinh bang');
GO

-- ============================================
-- DU LIEU MAU: SAN PHAM (khong kem anh, tranh loi copy-paste)
-- ============================================

INSERT INTO products (name, description, price, stock, categoryid, imageurl, created_date) VALUES
(N'iPhone 15 Pro Max', N'Chip A17 Pro, camera 48MP, man hinh 6.7 inch', 34990000, 40, 1, NULL, GETDATE()),
(N'Samsung Galaxy S24 Ultra', N'But S Pen, camera 200MP, RAM 12GB', 29990000, 35, 1, NULL, GETDATE()),
(N'Xiaomi 14', N'Camera Leica, sac nhanh 90W', 15990000, 50, 1, NULL, GETDATE()),
(N'MacBook Pro 14 M3', N'Chip M3, RAM 16GB, SSD 512GB', 45990000, 15, 2, NULL, GETDATE()),
(N'Dell XPS 13', N'Intel Core i7, man hinh InfinityEdge', 28990000, 20, 2, NULL, GETDATE()),
(N'Asus ROG Zephyrus', N'Laptop gaming RTX 4060, RAM 16GB', 32990000, 12, 2, NULL, GETDATE()),
(N'AirPods Pro 2', N'Chong on chu dong, sac MagSafe', 5990000, 100, 3, NULL, GETDATE()),
(N'Ban phim co Logitech', N'Switch Blue, den RGB', 1490000, 80, 3, NULL, GETDATE()),
(N'Chuot Logitech MX Master 3', N'Chuot khong day cao cap', 2190000, 60, 3, NULL, GETDATE()),
(N'Sac nhanh Anker 65W', N'Sac GaN nho gon, da cong', 690000, 150, 3, NULL, GETDATE()),
(N'Apple Watch Series 9', N'GPS, do nhip tim, chong nuoc', 10990000, 45, 4, NULL, GETDATE()),
(N'Samsung Galaxy Watch 6', N'Theo doi suc khoe toan dien', 7990000, 40, 4, NULL, GETDATE()),
(N'iPad Air M2', N'Chip M2, man hinh 10.9 inch', 16990000, 25, 5, NULL, GETDATE()),
(N'Samsung Galaxy Tab S9', N'Man hinh AMOLED, kem but S Pen', 14990000, 22, 5, NULL, GETDATE());
GO

-- ============================================
-- TAI KHOAN ADMIN MAC DINH
-- Username: admin | Password: admin123
-- ============================================

INSERT INTO users (username, password, email, full_name, phone, role, is_active, created_date)
VALUES (
    'admin',
    '$2b$12$Mjqc4.9Bl1nv5yBltfhOdOQO7sZHWIPEK/Qj.T23XeU7fZH2sG356',
    'admin@shopvn.com',
    N'Quan tri vien',
    NULL,
    'ADMIN',
    1,
    GETDATE()
);
GO

-- ============================================
-- KIEM TRA KET QUA
-- ============================================

SELECT COUNT(*) AS SoLuongDanhMuc FROM categories;
SELECT COUNT(*) AS SoLuongSanPham FROM products;
SELECT COUNT(*) AS SoLuongUser FROM users;
