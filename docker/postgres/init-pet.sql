-- 宠物服务数据库初始化脚本

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
    owner_id BIGINT NOT NULL, -- 关联用户服务的用户ID
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

-- 创建索引
CREATE INDEX idx_pets_owner_id ON pets(owner_id);
CREATE INDEX idx_pets_category_id ON pets(category_id);
CREATE INDEX idx_pets_breed_id ON pets(breed_id);
CREATE INDEX idx_pet_vaccinations_pet_id ON pet_vaccinations(pet_id);
CREATE INDEX idx_pet_medical_records_pet_id ON pet_medical_records(pet_id);
CREATE INDEX idx_pet_categories_parent_id ON pet_categories(parent_id);
CREATE INDEX idx_pet_breeds_category_id ON pet_breeds(category_id);

-- 插入初始数据
INSERT INTO pet_categories (name, description) VALUES 
('犬类', '各种犬类宠物'),
('猫类', '各种猫类宠物'),
('鸟类', '各种鸟类宠物'),
('鱼类', '各种观赏鱼类'),
('爬虫类', '各种爬虫类宠物'),
('小型哺乳动物', '兔子、仓鼠等小型宠物')
ON CONFLICT DO NOTHING;

INSERT INTO pet_breeds (category_id, name, description, size_category) VALUES 
((SELECT id FROM pet_categories WHERE name = '犬类'), '金毛寻回犬', '温顺友好的大型犬', 'large'),
((SELECT id FROM pet_categories WHERE name = '犬类'), '泰迪', '聪明活泼的小型犬', 'small'),
((SELECT id FROM pet_categories WHERE name = '犬类'), '哈士奇', '精力充沛的中大型犬', 'large'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '英国短毛猫', '温顺的短毛猫品种', 'medium'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '波斯猫', '优雅的长毛猫品种', 'medium'),
((SELECT id FROM pet_categories WHERE name = '猫类'), '暹罗猫', '活泼的短毛猫品种', 'medium')
ON CONFLICT DO NOTHING;