# ERD SPECIFICATION

> Tài liệu này mô tả đầy đủ ERD (Entity Relationship Diagram) để vẽ bằng dbdiagram.io, Draw.io, hoặc MySQL Workbench.
> Database: MySQL. 20 bảng. Schema sinh tự động bởi Hibernate `ddl-auto: update`.

---

## 1. Danh sách bảng và cột (DDL tham khảo)

```sql
-- ============================================================
-- MODULE: IDENTITY
-- ============================================================

CREATE TABLE users (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    email         VARCHAR(150)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    full_name     VARCHAR(100)  NOT NULL,
    phone         VARCHAR(20)   NULL,
    status        VARCHAR(20)   NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE roles (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ============================================================
-- MODULE: SHOP
-- ============================================================

CREATE TABLE shops (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    status      VARCHAR(20)  NOT NULL,           -- PENDING|ACTIVE|BANNED
    PRIMARY KEY (id),
    UNIQUE KEY uk_shops_user_id (user_id),
    UNIQUE KEY uk_shops_name (name)
    -- NOTE: user_id NOT enforced as FK (Loose Coupling pattern)
);

-- ============================================================
-- MODULE: PRODUCT
-- ============================================================

CREATE TABLE brands (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    logo_url    VARCHAR(255) NULL,
    description TEXT         NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_brands_name (name)
);

CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    parent_id   BIGINT       NULL,               -- Self-reference (NULL = root)
    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    shop_id        BIGINT         NOT NULL,       -- Loose coupling → shops.id
    name           VARCHAR(255)   NOT NULL,
    description    TEXT           NULL,
    price          DECIMAL(15,2)  NOT NULL,
    stock_quantity INT            NOT NULL,
    category_id    BIGINT         NOT NULL,
    brand_id       BIGINT         NULL,
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE', -- ACTIVE|OUT_OF_STOCK|HIDDEN
    PRIMARY KEY (id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id)    REFERENCES brands(id),
    INDEX idx_product_shop   (shop_id),
    INDEX idx_product_status (status)
);

CREATE TABLE product_variants (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    product_id     BIGINT         NOT NULL,
    sku            VARCHAR(100)   NOT NULL,
    price          DECIMAL(15,2)  NOT NULL,
    stock_quantity INT            NOT NULL,
    attributes     JSON           NULL,           -- {"color":"red","size":"L"}
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_variants_sku (sku),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE product_images (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    product_id   BIGINT       NOT NULL,
    image_url    VARCHAR(255) NOT NULL,
    public_id    VARCHAR(255) NULL,               -- Cloudinary publicId
    is_thumbnail TINYINT(1)   NULL DEFAULT 0,
    sort_order   INT          NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================
-- MODULE: CART
-- ============================================================

CREATE TABLE carts (
    id      BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,                      -- Loose coupling → users.id
    PRIMARY KEY (id),
    UNIQUE KEY uk_carts_user_id (user_id)
);

CREATE TABLE cart_items (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    cart_id            BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,            -- Loose coupling → product_variants.id
    quantity           INT    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
);

-- ============================================================
-- MODULE: ORDER
-- ============================================================

CREATE TABLE orders (
    id                    BIGINT         NOT NULL AUTO_INCREMENT,
    user_id               BIGINT         NOT NULL,  -- Loose → users.id
    shop_id               BIGINT         NOT NULL,  -- Loose → shops.id
    payment_group_id      VARCHAR(36)    NULL,       -- UUID group for multi-shop checkout
    status                VARCHAR(20)    NOT NULL,  -- PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED
    total_amount          DECIMAL(15,2)  NOT NULL,
    original_total_amount DECIMAL(15,2)  NOT NULL DEFAULT 0,
    discount_amount       DECIMAL(15,2)  NOT NULL DEFAULT 0,
    shipping_address      VARCHAR(500)   NOT NULL,
    payment_method        VARCHAR(20)    NOT NULL,  -- VNPAY|MOMO|COD
    applied_voucher_id    BIGINT         NULL,       -- Loose → vouchers.id
    applied_voucher_code  VARCHAR(50)    NULL,       -- Snapshot
    created_at            DATETIME       NOT NULL,
    updated_at            DATETIME       NULL,
    PRIMARY KEY (id),
    INDEX idx_order_user_status_created  (user_id, status, created_at),
    INDEX idx_order_user_created         (user_id, created_at),
    INDEX idx_order_shop_status_created  (shop_id, status, created_at),
    INDEX idx_order_shop_created         (shop_id, created_at),
    INDEX idx_order_payment_group        (payment_group_id)
);

CREATE TABLE order_items (
    id                 BIGINT         NOT NULL AUTO_INCREMENT,
    order_id           BIGINT         NOT NULL,
    product_variant_id BIGINT         NOT NULL,   -- Loose (Snapshot — no live FK dependency)
    quantity           INT            NOT NULL,
    unit_price         DECIMAL(15,2)  NOT NULL,   -- Snapshot: giá tại thời điểm đặt
    product_name       VARCHAR(255)   NOT NULL,   -- Snapshot: tên tại thời điểm đặt
    sub_total          DECIMAL(15,2)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- ============================================================
-- MODULE: PAYMENT
-- ============================================================

CREATE TABLE payments (
    id                     BIGINT         NOT NULL AUTO_INCREMENT,
    payment_group_id       VARCHAR(36)    NOT NULL, -- Links to orders.payment_group_id (no FK)
    amount                 DECIMAL(15,2)  NOT NULL,
    method                 VARCHAR(20)    NOT NULL, -- VNPAY|MOMO|COD
    status                 VARCHAR(20)    NOT NULL, -- PENDING|SUCCESS|FAILED|CANCELED
    transaction_id         VARCHAR(100)   NOT NULL, -- Internal: PAY_xxxxxx (UNIQUE)
    gateway_transaction_id VARCHAR(100)   NULL,      -- VNPay vnp_TxnRef
    raw_data               TEXT           NULL,      -- Full VNPay response (audit)
    created_at             DATETIME       NOT NULL,
    updated_at             DATETIME       NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_transaction_id (transaction_id),
    INDEX idx_payment_group_created (payment_group_id, created_at)
);

-- ============================================================
-- MODULE: VOUCHER
-- ============================================================

CREATE TABLE vouchers (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50)   NOT NULL,
    scope          VARCHAR(20)   NOT NULL,          -- SYSTEM|SHOP
    shop_id        BIGINT        NULL,               -- Loose → shops.id; NULL if scope=SYSTEM
    type           VARCHAR(20)   NOT NULL,           -- PERCENTAGE|FIXED_AMOUNT
    discount_value DECIMAL(15,2) NOT NULL,
    max_discount   DECIMAL(15,2) NULL,               -- Cap for PERCENTAGE
    min_order_value DECIMAL(15,2) NULL,
    start_date     DATETIME      NULL,
    end_date       DATETIME      NULL,
    usage_limit    INT           NULL,               -- NULL = unlimited
    used_count     INT           NOT NULL DEFAULT 0,
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vouchers_code (code)
);

-- ============================================================
-- MODULE: REVIEW
-- ============================================================

CREATE TABLE reviews (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    user_id          BIGINT        NOT NULL,  -- Loose → users.id
    product_id       BIGINT        NOT NULL,  -- Loose → products.id
    order_id         BIGINT        NOT NULL,  -- Loose → orders.id
    rating           INT           NOT NULL,  -- 1–5
    comment          VARCHAR(1000) NOT NULL,
    vendor_reply     TEXT          NULL,
    vendor_replied_at DATETIME     NULL,
    created_at       DATETIME      NOT NULL,
    updated_at       DATETIME      NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_order_product (user_id, order_id, product_id),
    INDEX idx_review_product_created (product_id, created_at)
);

CREATE TABLE review_images (
    review_id BIGINT       NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- ============================================================
-- MODULE: NOTIFICATION
-- ============================================================

CREATE TABLE notifications (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,  -- Loose → users.id (recipient)
    type         VARCHAR(30)  NOT NULL,  -- ORDER_CONFIRMED|ORDER_SHIPPED|...
    title        VARCHAR(255) NOT NULL,
    message      VARCHAR(500) NOT NULL,
    reference_id VARCHAR(50)  NULL,      -- orderId, reviewId...
    is_read      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notif_user_created (user_id, created_at)
);

-- ============================================================
-- MODULE: CHAT
-- ============================================================

CREATE TABLE chat_rooms (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    buyer_id   BIGINT   NOT NULL,  -- Loose → users.id
    shop_id    BIGINT   NOT NULL,  -- Loose → shops.id
    created_at DATETIME NOT NULL,
    updated_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_rooms_buyer_shop (buyer_id, shop_id)
);

CREATE TABLE chat_messages (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    room_id    BIGINT   NOT NULL,  -- Loose → chat_rooms.id
    sender_id  BIGINT   NOT NULL,  -- Loose → users.id
    content    TEXT     NOT NULL,
    is_read    TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_chat_room_created (room_id, created_at)
);
```

---

## 2. Quan hệ ERD (Notation: Crow's Foot)

### 2.1. Identity Module

```
users ||--o{ user_roles : "has"
roles ||--o{ user_roles : "assigned to"
users ||--o| shops      : "owns (user_id UNIQUE)"
```

### 2.2. Product Module

```
categories  ||--o{ categories   : "parent → children (self-ref)"
categories  ||--o{ products     : "categorizes"
brands      |o--o{ products     : "brands (nullable)"
products    ||--o{ product_variants : "has variants"
products    ||--o{ product_images   : "has images"
```

### 2.3. Shop → Product (Cross-module Loose Coupling)

```
shops ~~o{ products : "shop_id (loose FK, no enforced constraint)"
```

### 2.4. Cart Module

```
users  ~~|| carts      : "user_id (loose FK, UNIQUE)"
carts  ||--o{ cart_items : "contains"
product_variants ~~o{ cart_items : "product_variant_id (loose FK)"
```

### 2.5. Order Module

```
users             ~~o{ orders      : "user_id (loose FK)"
shops             ~~o{ orders      : "shop_id (loose FK)"
orders            ||--o{ order_items : "contains (cascade)"
product_variants  ~~o{ order_items  : "product_variant_id (snapshot loose FK)"
```

### 2.6. Payment — Order (UUID Group Link)

```
payments }o~~o{ orders : "payment_group_id = payment_group_id (no FK, UUID match)"
```

*Đây là quan hệ N:N thông qua UUID string, không phải FK truyền thống.*

### 2.7. Voucher

```
shops    ~~o{ vouchers : "shop_id (loose FK); NULL = SYSTEM scope"
vouchers ~~o{ orders   : "applied_voucher_id (loose FK, nullable snapshot)"
```

### 2.8. Review

```
users    ~~o{ reviews       : "user_id (loose FK)"
products ~~o{ reviews       : "product_id (loose FK)"
orders   ~~o{ reviews       : "order_id (loose FK)"
reviews  ||--o{ review_images : "has images (ElementCollection)"
```

### 2.9. Notification

```
users ~~o{ notifications : "user_id (loose FK)"
```

### 2.10. Chat

```
users (buyer) ~~o{ chat_rooms    : "buyer_id (loose FK)"
shops         ~~o{ chat_rooms    : "shop_id (loose FK)"
chat_rooms    ~~o{ chat_messages : "room_id (loose FK)"
users         ~~o{ chat_messages : "sender_id (loose FK)"
```

---

## 3. ERD dbdiagram.io Format

Copy đoạn code dưới vào [dbdiagram.io](https://dbdiagram.io) để tạo ERD tự động:

```dbml
// ── IDENTITY ──
Table users {
  id            bigint     [pk, increment]
  email         varchar(150) [unique, not null]
  password_hash varchar(255) [not null]
  full_name     varchar(100) [not null]
  phone         varchar(20)
  status        varchar(20) [not null]
}

Table roles {
  id   bigint      [pk, increment]
  name varchar(50) [unique, not null]
}

Table user_roles {
  user_id bigint [not null, ref: > users.id]
  role_id bigint [not null, ref: > roles.id]
  indexes {
    (user_id, role_id) [pk]
  }
}

// ── SHOP ──
Table shops {
  id          bigint      [pk, increment]
  user_id     bigint      [unique, not null, note: 'Loose coupling to users.id']
  name        varchar(100) [unique, not null]
  description text
  status      varchar(20) [not null, note: 'PENDING|ACTIVE|BANNED']
}

// ── PRODUCT ──
Table brands {
  id          bigint      [pk, increment]
  name        varchar(100) [unique, not null]
  logo_url    varchar(255)
  description text
}

Table categories {
  id          bigint      [pk, increment]
  name        varchar(100) [unique, not null]
  description text
  parent_id   bigint      [ref: > categories.id, note: 'NULL = root category']
}

Table products {
  id             bigint        [pk, increment]
  shop_id        bigint        [not null, note: 'Loose → shops.id']
  name           varchar(255)  [not null]
  description    text
  price          decimal(15,2) [not null]
  stock_quantity int           [not null]
  category_id    bigint        [not null, ref: > categories.id]
  brand_id       bigint        [ref: > brands.id]
  status         varchar(20)   [not null, default: 'ACTIVE']

  indexes {
    shop_id  [name: 'idx_product_shop']
    status   [name: 'idx_product_status']
  }
}

Table product_variants {
  id             bigint        [pk, increment]
  product_id     bigint        [not null, ref: > products.id]
  sku            varchar(100)  [unique, not null]
  price          decimal(15,2) [not null]
  stock_quantity int           [not null]
  attributes     json          [note: '{"color":"red","size":"L"}']
  status         varchar(20)   [not null, default: 'ACTIVE']
}

Table product_images {
  id           bigint       [pk, increment]
  product_id   bigint       [not null, ref: > products.id]
  image_url    varchar(255) [not null]
  public_id    varchar(255) [note: 'Cloudinary publicId']
  is_thumbnail tinyint(1)   [default: 0]
  sort_order   int          [default: 0]
}

// ── CART ──
Table carts {
  id      bigint [pk, increment]
  user_id bigint [unique, not null, note: 'Loose → users.id']
}

Table cart_items {
  id                 bigint [pk, increment]
  cart_id            bigint [not null, ref: > carts.id]
  product_variant_id bigint [not null, note: 'Loose → product_variants.id']
  quantity           int    [not null]
}

// ── ORDER ──
Table orders {
  id                    bigint        [pk, increment]
  user_id               bigint        [not null, note: 'Loose → users.id']
  shop_id               bigint        [not null, note: 'Loose → shops.id']
  payment_group_id      varchar(36)   [note: 'UUID for multi-shop checkout']
  status                varchar(20)   [not null, note: 'PENDING|CONFIRMED|SHIPPED|DELIVERED|CANCELLED']
  total_amount          decimal(15,2) [not null]
  original_total_amount decimal(15,2) [not null, default: 0]
  discount_amount       decimal(15,2) [not null, default: 0]
  shipping_address      varchar(500)  [not null]
  payment_method        varchar(20)   [not null]
  applied_voucher_id    bigint        [note: 'Loose → vouchers.id; snapshot']
  applied_voucher_code  varchar(50)
  created_at            datetime      [not null]
  updated_at            datetime

  indexes {
    (user_id, status, created_at)  [name: 'idx_order_user_status_created']
    (user_id, created_at)          [name: 'idx_order_user_created']
    (shop_id, status, created_at)  [name: 'idx_order_shop_status_created']
    (shop_id, created_at)          [name: 'idx_order_shop_created']
    payment_group_id               [name: 'idx_order_payment_group']
  }
}

Table order_items {
  id                 bigint        [pk, increment]
  order_id           bigint        [not null, ref: > orders.id]
  product_variant_id bigint        [not null, note: 'Snapshot loose ref to product_variants.id']
  quantity           int           [not null]
  unit_price         decimal(15,2) [not null, note: 'SNAPSHOT: price at time of order']
  product_name       varchar(255)  [not null, note: 'SNAPSHOT: name at time of order']
  sub_total          decimal(15,2) [not null]
}

// ── PAYMENT ──
Table payments {
  id                     bigint        [pk, increment]
  payment_group_id       varchar(36)   [not null, note: 'UUID → matches orders.payment_group_id']
  amount                 decimal(15,2) [not null]
  method                 varchar(20)   [not null, note: 'VNPAY|MOMO|COD']
  status                 varchar(20)   [not null, note: 'PENDING|SUCCESS|FAILED|CANCELED']
  transaction_id         varchar(100)  [unique, not null, note: 'Internal: PAY_xxxxxxxx']
  gateway_transaction_id varchar(100)  [note: 'VNPay vnp_TxnRef']
  raw_data               text          [note: 'Full VNPay response for audit']
  created_at             datetime      [not null]
  updated_at             datetime

  indexes {
    (payment_group_id, created_at) [name: 'idx_payment_group_created']
  }
}

// ── VOUCHER ──
Table vouchers {
  id              bigint        [pk, increment]
  code            varchar(50)   [unique, not null]
  scope           varchar(20)   [not null, note: 'SYSTEM|SHOP']
  shop_id         bigint        [note: 'Loose → shops.id; NULL if scope=SYSTEM']
  type            varchar(20)   [not null, note: 'PERCENTAGE|FIXED_AMOUNT']
  discount_value  decimal(15,2) [not null]
  max_discount    decimal(15,2)
  min_order_value decimal(15,2)
  start_date      datetime
  end_date        datetime
  usage_limit     int           [note: 'NULL = unlimited']
  used_count      int           [not null, default: 0]
  created_at      datetime      [not null]
  updated_at      datetime
}

// ── REVIEW ──
Table reviews {
  id                bigint        [pk, increment]
  user_id           bigint        [not null, note: 'Loose → users.id']
  product_id        bigint        [not null, note: 'Loose → products.id']
  order_id          bigint        [not null, note: 'Loose → orders.id']
  rating            int           [not null, note: '1-5']
  comment           varchar(1000) [not null]
  vendor_reply      text
  vendor_replied_at datetime
  created_at        datetime      [not null]
  updated_at        datetime

  indexes {
    (user_id, order_id, product_id) [unique, name: 'uk_user_order_product']
    (product_id, created_at)        [name: 'idx_review_product_created']
  }
}

Table review_images {
  review_id bigint       [not null, ref: > reviews.id]
  image_url varchar(255) [not null]
}

// ── NOTIFICATION ──
Table notifications {
  id           bigint       [pk, increment]
  user_id      bigint       [not null, note: 'Loose → users.id (recipient)']
  type         varchar(30)  [not null, note: 'ORDER_CONFIRMED|ORDER_SHIPPED|ORDER_DELIVERED|NEW_ORDER|NEW_REVIEW|SYSTEM']
  title        varchar(255) [not null]
  message      varchar(500) [not null]
  reference_id varchar(50)
  is_read      tinyint(1)   [not null, default: 0]
  created_at   datetime     [not null]

  indexes {
    (user_id, created_at) [name: 'idx_notif_user_created']
  }
}

// ── CHAT ──
Table chat_rooms {
  id         bigint   [pk, increment]
  buyer_id   bigint   [not null, note: 'Loose → users.id']
  shop_id    bigint   [not null, note: 'Loose → shops.id']
  created_at datetime [not null]
  updated_at datetime

  indexes {
    (buyer_id, shop_id) [unique, name: 'uk_chat_rooms_buyer_shop']
  }
}

Table chat_messages {
  id         bigint   [pk, increment]
  room_id    bigint   [not null, note: 'Loose → chat_rooms.id']
  sender_id  bigint   [not null, note: 'Loose → users.id']
  content    text     [not null]
  is_read    tinyint(1) [not null, default: 0]
  created_at datetime [not null]

  indexes {
    (room_id, created_at) [name: 'idx_chat_room_created']
  }
}

// ── RELATIONSHIPS (Enforced) ──
Ref: user_roles.user_id > users.id
Ref: user_roles.role_id > roles.id
Ref: categories.parent_id > categories.id
Ref: products.category_id > categories.id
Ref: products.brand_id > brands.id
Ref: product_variants.product_id > products.id
Ref: product_images.product_id > products.id
Ref: cart_items.cart_id > carts.id
Ref: order_items.order_id > orders.id
Ref: review_images.review_id > reviews.id
```

---

## 4. Ghi chú quan trọng về thiết kế

### 4.1. Loose Coupling Pattern (cross-module)
Các cột sau đây là FK về mặt nghiệp vụ nhưng **KHÔNG** được khai báo `FOREIGN KEY` trong DB:

| Cột | Bảng | Tham chiếu |
|---|---|---|
| `shop_id` | `products` | `shops.id` |
| `user_id` | `shops`, `carts`, `orders`, `reviews`, `notifications`, `chat_rooms`, `chat_messages` | `users.id` |
| `shop_id` | `orders`, `vouchers`, `chat_rooms` | `shops.id` |
| `product_variant_id` | `cart_items`, `order_items` | `product_variants.id` |
| `product_id` | `reviews` | `products.id` |
| `order_id` | `reviews` | `orders.id` |
| `room_id` | `chat_messages` | `chat_rooms.id` |
| `sender_id` | `chat_messages` | `users.id` |

Đây là thiết kế chủ đích theo kiến trúc **Modular Monolith** — mỗi module tự quản lý dữ liệu của mình, tránh coupling giữa module qua DB layer.

### 4.2. Payment — Order UUID Group
```
payments.payment_group_id ←→ orders.payment_group_id
```
Không có FK. Liên kết qua UUID string. Cho phép 1 Payment ứng với N Orders từ nhiều gian hàng.

### 4.3. Snapshot Pattern trong order_items
`unit_price` và `product_name` là bản sao tại thời điểm đặt hàng. Đây là pattern quan trọng — không thể JOIN với `product_variants` để lấy giá vì Vendor có thể thay đổi giá sau khi đơn đã đặt.

### 4.4. Kiểu dữ liệu tiền tệ
Tất cả giá trị tiền tệ dùng `DECIMAL(15, 2)` — không bao giờ dùng `FLOAT` hay `DOUBLE` để tránh sai số dấu phẩy động trong tài chính.

### 4.5. JSON Column
`product_variants.attributes` dùng kiểu `JSON` của MySQL — hỗ trợ query và index theo key JSON. Linh hoạt cho phép biến thể có các thuộc tính khác nhau mà không cần schema cứng.
