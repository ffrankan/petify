-- Petify统一数据库初始化脚本
-- 合并所有业务服务的数据库表到单个数据库中

-- =====================================
-- 用户服务相关表
-- =====================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    real_name VARCHAR(100),
    avatar_url VARCHAR(255),
    status SMALLINT DEFAULT 1, -- 1:正常 0:禁用
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 用户认证信息表
CREATE TABLE IF NOT EXISTS user_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    auth_type VARCHAR(20) NOT NULL, -- password, wechat, qq等
    auth_key VARCHAR(100) NOT NULL, -- 认证标识
    auth_secret VARCHAR(255), -- 认证密钥
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================
-- 宠物服务相关表
-- =====================================

-- 宠物分类表
CREATE TABLE IF NOT EXISTS pet_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    sort_order INTEGER DEFAULT 0,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES pet_categories(id)
);

-- 宠物品种表
CREATE TABLE IF NOT EXISTS pet_breeds (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    characteristics TEXT,
    average_lifespan INTEGER,
    size_category VARCHAR(20), -- small, medium, large
    origin_country VARCHAR(50),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES pet_categories(id)
);

-- 宠物信息表
CREATE TABLE IF NOT EXISTS pets (
    id BIGSERIAL PRIMARY KEY,
    owner_id BIGINT NOT NULL, -- 关联用户表的用户ID
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL,
    breed_id BIGINT,
    gender VARCHAR(10), -- male, female, unknown
    birth_date DATE,
    weight DECIMAL(5,2),
    color VARCHAR(50),
    microchip_number VARCHAR(50),
    description TEXT,
    avatar_url VARCHAR(255),
    medical_notes TEXT,
    is_neutered BOOLEAN DEFAULT FALSE,
    is_vaccinated BOOLEAN DEFAULT FALSE,
    status SMALLINT DEFAULT 1, -- 1:健康 2:生病 3:已故
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES pet_categories(id),
    FOREIGN KEY (breed_id) REFERENCES pet_breeds(id)
);

-- 宠物疫苗记录表
CREATE TABLE IF NOT EXISTS pet_vaccinations (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    vaccine_name VARCHAR(100) NOT NULL,
    vaccination_date DATE NOT NULL,
    next_due_date DATE,
    veterinarian VARCHAR(100),
    clinic_name VARCHAR(100),
    batch_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

-- 宠物医疗记录表
CREATE TABLE IF NOT EXISTS pet_medical_records (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    record_type VARCHAR(50) NOT NULL, -- checkup, treatment, surgery, emergency
    record_date DATE NOT NULL,
    veterinarian VARCHAR(100),
    clinic_name VARCHAR(100),
    diagnosis TEXT,
    treatment TEXT,
    medications TEXT,
    cost DECIMAL(10,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

-- =====================================
-- 预约服务相关表
-- =====================================

-- 服务项目表
CREATE TABLE IF NOT EXISTS service_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- medical, grooming, training, boarding
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 服务提供者表
CREATE TABLE IF NOT EXISTS service_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- clinic, hospital, grooming_salon, training_center
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    description TEXT,
    operating_hours JSONB,
    rating DECIMAL(3,2) DEFAULT 0.00,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 服务提供者员工表
CREATE TABLE IF NOT EXISTS provider_staff (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    title VARCHAR(100), -- 职位
    specialization TEXT, -- 专业领域
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar_url VARCHAR(255),
    working_hours JSONB,
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES service_providers(id)
);

-- 预约时间段表
CREATE TABLE IF NOT EXISTS appointment_slots (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL,
    staff_id BIGINT,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    max_appointments INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES service_providers(id),
    FOREIGN KEY (staff_id) REFERENCES provider_staff(id)
);

-- 预约表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    appointment_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL, -- 关联用户表的用户ID
    pet_id BIGINT NOT NULL, -- 关联宠物表的宠物ID
    provider_id BIGINT NOT NULL,
    staff_id BIGINT,
    service_item_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, confirmed, in_progress, completed, cancelled
    notes TEXT,
    total_price DECIMAL(10,2),
    payment_status VARCHAR(20) DEFAULT 'unpaid', -- unpaid, paid, refunded
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES service_providers(id),
    FOREIGN KEY (staff_id) REFERENCES provider_staff(id),
    FOREIGN KEY (service_item_id) REFERENCES service_items(id),
    FOREIGN KEY (slot_id) REFERENCES appointment_slots(id)
);

-- 预约状态变更记录表
CREATE TABLE IF NOT EXISTS appointment_status_history (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    change_reason TEXT,
    changed_by BIGINT, -- 操作人员ID
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE
);

-- 服务评价表
CREATE TABLE IF NOT EXISTS service_reviews (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    provider_id BIGINT NOT NULL,
    staff_id BIGINT,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    images JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES service_providers(id),
    FOREIGN KEY (staff_id) REFERENCES provider_staff(id)
);

-- =====================================
-- 创建索引
-- =====================================

-- 用户服务索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_auth_user_id ON user_auth(user_id);
CREATE INDEX idx_user_auth_key ON user_auth(auth_key);

-- 宠物服务索引
CREATE INDEX idx_pets_owner_id ON pets(owner_id);
CREATE INDEX idx_pets_category_id ON pets(category_id);
CREATE INDEX idx_pets_breed_id ON pets(breed_id);
CREATE INDEX idx_pet_vaccinations_pet_id ON pet_vaccinations(pet_id);
CREATE INDEX idx_pet_medical_records_pet_id ON pet_medical_records(pet_id);
CREATE INDEX idx_pet_categories_parent_id ON pet_categories(parent_id);
CREATE INDEX idx_pet_breeds_category_id ON pet_breeds(category_id);

-- 预约服务索引
CREATE INDEX idx_appointments_user_id ON appointments(user_id);
CREATE INDEX idx_appointments_pet_id ON appointments(pet_id);
CREATE INDEX idx_appointments_provider_id ON appointments(provider_id);
CREATE INDEX idx_appointments_staff_id ON appointments(staff_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointment_slots_provider_id ON appointment_slots(provider_id);
CREATE INDEX idx_appointment_slots_date ON appointment_slots(slot_date);
CREATE INDEX idx_provider_staff_provider_id ON provider_staff(provider_id);
CREATE INDEX idx_service_reviews_provider_id ON service_reviews(provider_id);

-- =====================================
-- 插入初始数据
-- =====================================

-- 用户初始数据
INSERT INTO users (username, password, email, real_name, status) 
VALUES ('admin', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', 'admin@petify.com', '系统管理员', 1)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_name) 
SELECT id, 'ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- 宠物分类初始数据
INSERT INTO pet_categories (name, description) VALUES 
('犬类', '各种犬类宠物'),
('猫类', '各种猫类宠物'),
('鸟类', '各种鸟类宠物'),
('鱼类', '各种观赏鱼类'),
('爬虫类', '各种爬虫类宠物'),
('小型哺乳动物', '兔子、仓鼠等小型宠物')
ON CONFLICT DO NOTHING;

-- 宠物品种初始数据
INSERT INTO pet_breeds (category_id, name, description, size_category) VALUES 
((SELECT id FROM pet_categories WHERE name = '犬类'), '金毛寻回犬', '温顺友好的大型犬', 'large'),
((SELECT id FROM pet_categories WHERE name = '犬类'), '泰迪', '聪明活泼的小型犬', 'small'),
((SELECT id FROM pet_categories WHERE name = '犬类'), '哈士奇', '精力充沛的中大型犬', 'large'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '英国短毛猫', '温顺的短毛猫品种', 'medium'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '波斯猫', '优雅的长毛猫品种', 'medium'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '暹罗猫', '活泼的短毛猫品种', 'medium')
ON CONFLICT DO NOTHING;

-- 服务项目初始数据
INSERT INTO service_items (name, description, category, duration_minutes, price) VALUES 
('基础体检', '宠物基础健康检查', 'medical', 30, 150.00),
('疫苗接种', '宠物疫苗注射服务', 'medical', 15, 80.00),
('绝育手术', '宠物绝育手术', 'medical', 120, 800.00),
('基础洗护', '宠物基础清洁护理', 'grooming', 60, 100.00),
('造型美容', '宠物造型设计', 'grooming', 90, 200.00),
('基础训练', '宠物基础行为训练', 'training', 60, 150.00),
('寄养服务', '宠物日托寄养', 'boarding', 480, 80.00)
ON CONFLICT DO NOTHING;

-- 服务提供者初始数据
INSERT INTO service_providers (name, type, address, phone, description) VALUES 
('爱宠动物医院', 'hospital', '北京市朝阳区xxx街道123号', '010-12345678', '专业的宠物医疗服务'),
('美宠造型店', 'grooming_salon', '北京市海淀区xxx路456号', '010-87654321', '专业的宠物美容造型'),
('宠物训练中心', 'training_center', '北京市丰台区xxx大街789号', '010-11111111', '专业的宠物行为训练')
ON CONFLICT DO NOTHING;

-- 服务提供者员工初始数据
INSERT INTO provider_staff (provider_id, name, title, specialization) VALUES 
((SELECT id FROM service_providers WHERE name = '爱宠动物医院'), '张医生', '主治医师', '小动物内科'),
((SELECT id FROM service_providers WHERE name = '爱宠动物医院'), '李医生', '副主任医师', '小动物外科'),
((SELECT id FROM service_providers WHERE name = '美宠造型店'), '王师傅', '高级美容师', '犬类造型'),
((SELECT id FROM service_providers WHERE name = '美宠造型店'), '赵师傅', '美容师', '猫类护理'),
((SELECT id FROM service_providers WHERE name = '宠物训练中心'), '陈教练', '资深训练师', '行为矫正'),
((SELECT id FROM service_providers WHERE name = '宠物训练中心'), '刘教练', '训练师', '基础训练')
ON CONFLICT DO NOTHING;