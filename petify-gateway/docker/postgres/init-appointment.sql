-- 预约服务数据库初始化脚本

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
    user_id BIGINT NOT NULL, -- 关联用户服务的用户ID
    pet_id BIGINT NOT NULL, -- 关联宠物服务的宠物ID
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
    FOREIGN KEY (provider_id) REFERENCES service_providers(id),
    FOREIGN KEY (staff_id) REFERENCES provider_staff(id)
);

-- 创建索引
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

-- 插入初始数据
INSERT INTO service_items (name, description, category, duration_minutes, price) VALUES 
('基础体检', '宠物基础健康检查', 'medical', 30, 150.00),
('疫苗接种', '宠物疫苗注射服务', 'medical', 15, 80.00),
('绝育手术', '宠物绝育手术', 'medical', 120, 800.00),
('基础洗护', '宠物基础清洁护理', 'grooming', 60, 100.00),
('造型美容', '宠物造型设计', 'grooming', 90, 200.00),
('基础训练', '宠物基础行为训练', 'training', 60, 150.00),
('寄养服务', '宠物日托寄养', 'boarding', 480, 80.00)
ON CONFLICT DO NOTHING;

INSERT INTO service_providers (name, type, address, phone, description) VALUES 
('爱宠动物医院', 'hospital', '北京市朝阳区xxx街道123号', '010-12345678', '专业的宠物医疗服务'),
('美宠造型店', 'grooming_salon', '北京市海淀区xxx路456号', '010-87654321', '专业的宠物美容造型'),
('宠物训练中心', 'training_center', '北京市丰台区xxx大街789号', '010-11111111', '专业的宠物行为训练')
ON CONFLICT DO NOTHING;

-- 为每个服务提供者添加员工
INSERT INTO provider_staff (provider_id, name, title, specialization) VALUES 
((SELECT id FROM service_providers WHERE name = '爱宠动物医院'), '张医生', '主治医师', '小动物内科'),
((SELECT id FROM service_providers WHERE name = '爱宠动物医院'), '李医生', '副主任医师', '小动物外科'),
((SELECT id FROM service_providers WHERE name = '美宠造型店'), '王师傅', '高级美容师', '犬类造型'),
((SELECT id FROM service_providers WHERE name = '美宠造型店'), '赵师傅', '美容师', '猫类护理'),
((SELECT id FROM service_providers WHERE name = '宠物训练中心'), '陈教练', '资深训练师', '行为矫正'),
((SELECT id FROM service_providers WHERE name = '宠物训练中心'), '刘教练', '训练师', '基础训练')
ON CONFLICT DO NOTHING;