CREATE DATABASE IF NOT EXISTS pinterest;
USE pinterest;

CREATE TABLE IF NOT EXISTS user_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    reset_otp VARCHAR(10) NULL,
    reset_otp_expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Boards table (includes `position` column used for ordering boards)
CREATE TABLE IF NOT EXISTS boards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC',
    position INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_boards_owner FOREIGN KEY (owner_id) REFERENCES user_accounts(id)
);

CREATE TABLE IF NOT EXISTS board_collaborations (
    board_id BIGINT NOT NULL,
    collaborator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (board_id, collaborator_id),
    CONSTRAINT fk_board_collab_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_board_collab_user FOREIGN KEY (collaborator_id) REFERENCES user_accounts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    board_id BIGINT,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    media_type ENUM('IMAGE', 'VIDEO') DEFAULT 'IMAGE',
    media_url LONGTEXT,
    source_url VARCHAR(500),
    attribution VARCHAR(255),
    keywords JSON,
    visibility ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC',
    status ENUM('DRAFT', 'PUBLISHED') DEFAULT 'DRAFT',
    save_count BIGINT DEFAULT 0,
    share_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pins_owner FOREIGN KEY (owner_id) REFERENCES user_accounts(id),
    CONSTRAINT fk_pins_board FOREIGN KEY (board_id) REFERENCES boards(id)
);

CREATE TABLE IF NOT EXISTS pin_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pin_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    media_type ENUM('IMAGE', 'VIDEO') DEFAULT 'IMAGE',
    position INT DEFAULT 0,
    CONSTRAINT fk_pin_media_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pin_keywords (
    pin_id BIGINT NOT NULL,
    keyword VARCHAR(50) NOT NULL,
    PRIMARY KEY (pin_id, keyword),
    CONSTRAINT fk_pin_keywords_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pin_likes (
    pin_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (pin_id, user_id),
    CONSTRAINT fk_pin_likes_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE,
    CONSTRAINT fk_pin_likes_user FOREIGN KEY (user_id) REFERENCES user_accounts(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS followers (
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    CONSTRAINT fk_followers_follower FOREIGN KEY (follower_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_followers_following FOREIGN KEY (following_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT ck_no_self_follow CHECK (follower_id <> following_id)
);

CREATE TABLE IF NOT EXISTS invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inviter_id BIGINT NOT NULL,
    invitee_id BIGINT NOT NULL,
    type ENUM('BOARD_COLLABORATION', 'CONNECTION') NOT NULL,
    board_id BIGINT NULL,
    message TEXT,
    status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_invitations_inviter FOREIGN KEY (inviter_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_invitee FOREIGN KEY (invitee_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_invitations_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS business_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    category VARCHAR(100),
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS showcases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_profile_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    theme VARCHAR(100),
    cover_image_url VARCHAR(500),
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_showcases_business FOREIGN KEY (business_profile_id) REFERENCES business_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS showcase_pins (
    showcase_id BIGINT NOT NULL,
    pin_id BIGINT NOT NULL,
    position INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (showcase_id, pin_id),
    CONSTRAINT fk_showcase_pins_showcase FOREIGN KEY (showcase_id) REFERENCES showcases(id) ON DELETE CASCADE,
    CONSTRAINT fk_showcase_pins_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS business_followers (
    follower_id BIGINT NOT NULL,
    business_profile_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, business_profile_id),
    CONSTRAINT fk_business_followers_user FOREIGN KEY (follower_id) REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_business_followers_business FOREIGN KEY (business_profile_id) REFERENCES business_profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS advertising_campaigns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_profile_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    headline VARCHAR(200),
    summary TEXT,
    objective VARCHAR(100),
    status ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED') DEFAULT 'ACTIVE',
    curated_theme VARCHAR(120),
    hero_image_url VARCHAR(500),
    landing_page_url VARCHAR(500),
    audience_focus VARCHAR(200),
    daily_budget DECIMAL(12, 2),
    primary_metric VARCHAR(120),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_campaign_business FOREIGN KEY (business_profile_id) REFERENCES business_profiles(id)
);

CREATE TABLE IF NOT EXISTS sponsored_pins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pin_id BIGINT NOT NULL UNIQUE,
    business_profile_id BIGINT NOT NULL,
    campaign_id BIGINT NOT NULL,
    sponsored_label VARCHAR(80) DEFAULT 'Sponsored',
    cta_text VARCHAR(120),
    cta_url VARCHAR(500),
    priority INT DEFAULT 0,
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sponsored_pin_pin FOREIGN KEY (pin_id) REFERENCES pins(id) ON DELETE CASCADE,
    CONSTRAINT fk_sponsored_pin_business FOREIGN KEY (business_profile_id) REFERENCES business_profiles(id),
    CONSTRAINT fk_sponsored_pin_campaign FOREIGN KEY (campaign_id) REFERENCES advertising_campaigns(id)
);

CREATE TABLE IF NOT EXISTS sponsored_pin_keywords (
    sponsored_pin_id BIGINT NOT NULL,
    keyword VARCHAR(50) NOT NULL,
    PRIMARY KEY (sponsored_pin_id, keyword),
    CONSTRAINT fk_sp_keywords_pin FOREIGN KEY (sponsored_pin_id) REFERENCES sponsored_pins(id) ON DELETE CASCADE
);

-- Add avatar_url column (Hibernate's ddl-auto=update should handle this, but adding here for safety)
-- If column already exists, this will fail but Spring will continue with other statements
-- For fresh databases, this ensures the column exists before INSERT
ALTER TABLE user_accounts 
ADD COLUMN avatar_url VARCHAR(500) NULL 
AFTER password_hash;

-- Seed sample users (idempotent)
INSERT INTO user_accounts (email, username, full_name, phone_number, password_hash, avatar_url)
VALUES
    ('mark@meta.com','markzuckerberg','Mark Zuckerberg','1234567890','$2a$10$TWUMU8LYeQ0PEfAV0Dp6geMmgmrmf1AL6ImHrh29E8cAoe/HTbFYC','https://d2v5dzhdg4zhx3.cloudfront.net/web-assets/images/storypages/short/linkedin-profile-picture-maker/dummy_image/thumb/004.webp'),
    ('jane@pinterest.com','janedoe','Jane Doe','9876543210','$2a$10$TWUMU8LYeQ0PEfAV0Dp6geMmgmrmf1AL6ImHrh29E8cAoe/HTbFYC','https://img.freepik.com/premium-photo/captivating-black-white-linkedin-profile-picture-fitness-writerjournalist_983420-47941.jpg?w=2000'),
    ('elon@tesla.com','elonmusk','Elon Musk','5556667777','$2a$10$TWUMU8LYeQ0PEfAV0Dp6geMmgmrmf1AL6ImHrh29E8cAoe/HTbFYC','https://cdn.openart.ai/stable_diffusion/0f26305f30636c01ed2b3ab7ed4117079f7b0796_2000x2000.webp')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), avatar_url = VALUES(avatar_url);

-- Seed sample follower relationships
INSERT INTO followers (follower_id, following_id)
VALUES
    ((SELECT id FROM user_accounts WHERE username='markzuckerberg'),
     (SELECT id FROM user_accounts WHERE username='janedoe')),
    ((SELECT id FROM user_accounts WHERE username='janedoe'),
     (SELECT id FROM user_accounts WHERE username='elonmusk')),
    ((SELECT id FROM user_accounts WHERE username='elonmusk'),
     (SELECT id FROM user_accounts WHERE username='markzuckerberg'))
ON DUPLICATE KEY UPDATE created_at = created_at;

-- Seed sample boards for demo accounts
INSERT INTO boards (id, owner_id, name, description, visibility, position)
VALUES
    (101, (SELECT id FROM user_accounts WHERE username='markzuckerberg'),
     'Soft Minimal Interiors',
     'Layered neutrals, walnut shelving, and mindful Scandinavian styling.',
     'PUBLIC', 1),
    (102, (SELECT id FROM user_accounts WHERE username='janedoe'),
     'Mindful Morning Rituals',
     'Slow living recipes, wellness corners, and journaling spreads.',
     'PUBLIC', 2),
    (103, (SELECT id FROM user_accounts WHERE username='elonmusk'),
     'Color Journals',
     'Analog palettes and seasonal mood boards from studio projects.',
     'PUBLIC', 3)
ON DUPLICATE KEY UPDATE
    owner_id = VALUES(owner_id),
    name = VALUES(name),
    description = VALUES(description),
    visibility = VALUES(visibility),
    position = VALUES(position);

-- Seed sample pins tied to real user accounts
INSERT INTO pins (
    id, owner_id, board_id, title, description, media_type, media_url,
    source_url, attribution, visibility, status, save_count, share_count, like_count,
    published_at, created_at, updated_at)
VALUES
    (1001,
     (SELECT id FROM user_accounts WHERE username='markzuckerberg'),
     101,
     'Cozy Nordic Reading Corner',
     'Layered neutrals, walnut shelving, and warm task lighting from the design lab.',
     'IMAGE',
     'https://picsum.photos/id/1015/900/600',
     'https://example.com/pins/1001',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (1002,
     (SELECT id FROM user_accounts WHERE username='janedoe'),
     102,
     'Mindful Desk Essentials',
     'Analog clock, linen planner, and matte ceramic vessels for grounded work sessions.',
     'IMAGE',
     'https://picsum.photos/id/1016/900/600',
     'https://example.com/pins/1002',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
    0,
    0,
    0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (1003,
     (SELECT id FROM user_accounts WHERE username='elonmusk'),
     103,
     'Wildflower Color Study',
     'Analog palette inspired by alpine meadows and dusk gradients.',
     'IMAGE',
     'https://picsum.photos/id/1018/900/600',
     'https://example.com/pins/1003',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
    0,
    0,
    0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (2001,
     (SELECT id FROM user_accounts WHERE username='markzuckerberg'),
     NULL,
     'Nike Air Runner Lab',
     'Performance footwear developed with pro trainers and data-driven cushioning.',
     'IMAGE',
     'https://picsum.photos/id/1020/900/600',
     'https://www.nike.com/air-runner',
     'Creative by Nike Running',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (2002,
     (SELECT id FROM user_accounts WHERE username='janedoe'),
     NULL,
     'Apple Studio Display Workflow',
     'Clean workstations anchored by the latest Apple Silicon devices and Studio Display.',
     'IMAGE',
     'https://picsum.photos/id/1021/900/600',
     'https://www.apple.com/mac/',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (2003,
     (SELECT id FROM user_accounts WHERE username='elonmusk'),
     NULL,
     'Starbucks Cold Foam Moments',
     'Seasonal cold brew recipes shot on-location from favorite creative studios.',
     'IMAGE',
     'https://picsum.photos/id/1022/900/600',
     'https://www.starbucks.com/menu/product/2122686/iced',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (2004,
     (SELECT id FROM user_accounts WHERE username='janedoe'),
     NULL,
     'IKEA Calm Corners',
     'Modular shelving, layered textures, and multifunctional storage for studio lofts.',
     'IMAGE',
     'https://picsum.photos/id/1023/900/600',
     'https://www.ikea.com/us/en/rooms/living-room/',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    (2005,
     (SELECT id FROM user_accounts WHERE username='markzuckerberg'),
     NULL,
     'Sephora Radiant Skin Routine',
     'Skincare layering rituals from Sephora pros featuring vitamin C and ceramides.',
     'IMAGE',
     'https://picsum.photos/id/1024/900/600',
     'https://www.sephora.com/beauty/skincare-routine',
     'Stock photo',
     'PUBLIC',
     'PUBLISHED',
     0,
     0,
     0,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
    owner_id = VALUES(owner_id),
    board_id = VALUES(board_id),
    title = VALUES(title),
    description = VALUES(description),
    media_url = VALUES(media_url),
    source_url = VALUES(source_url),
    attribution = VALUES(attribution),
    visibility = VALUES(visibility),
    status = VALUES(status),
    save_count = VALUES(save_count),
    share_count = VALUES(share_count),
    like_count = VALUES(like_count);

-- Seed keywords for the demo pins
INSERT INTO pin_keywords (pin_id, keyword)
VALUES
    (1001, 'home'),
    (1001, 'decor'),
    (1002, 'wellness'),
    (1002, 'workspace'),
    (1003, 'color'),
    (1003, 'palette')
ON DUPLICATE KEY UPDATE keyword = VALUES(keyword);

-- Seed sample invitations
-- Create invitations for users (assuming user ID 4+ are regular users)
-- These invitations will be available for any logged-in user with ID > 3
INSERT INTO invitations (inviter_id, invitee_id, type, board_id, message, status)
VALUES
    -- Board Collaboration invitation from Mark Zuckerberg to first non-seed user
    ((SELECT id FROM user_accounts WHERE username='markzuckerberg' LIMIT 1),
     (SELECT id FROM user_accounts WHERE id > 3 ORDER BY id LIMIT 1),
     'BOARD_COLLABORATION',
     NULL,
     'Help refine the palette and typography direction for our February drop.',
     'PENDING'),
    -- Connection invitation from Jane Doe to first non-seed user
    ((SELECT id FROM user_accounts WHERE username='janedoe' LIMIT 1),
     (SELECT id FROM user_accounts WHERE id > 3 ORDER BY id LIMIT 1),
     'CONNECTION',
     NULL,
     'We love your mindful hosting series—let''s stay in touch!',
     'PENDING'),
    -- Board Collaboration invitation from Elon Musk to first non-seed user
    ((SELECT id FROM user_accounts WHERE username='elonmusk' LIMIT 1),
     (SELECT id FROM user_accounts WHERE id > 3 ORDER BY id LIMIT 1),
     'BOARD_COLLABORATION',
     NULL,
     'Would love your input on our Scandinavian design board!',
     'PENDING')
ON DUPLICATE KEY UPDATE message = VALUES(message);

-- Seed business profiles
INSERT INTO business_profiles (name, username, description, logo_url, website_url, category, verified)
VALUES
    ('Nike', 'nike', 'Just Do It. Official Nike account featuring the latest in sportswear, sneakers, and athletic gear.', 'https://logos-world.net/wp-content/uploads/2020/04/Nike-Logo.png', 'https://www.nike.com', 'Fashion & Apparel', TRUE),
    ('Apple', 'apple', 'Think different. Discover the latest Apple products, innovations, and design inspiration.', 'https://logos-world.net/wp-content/uploads/2020/04/Apple-Logo.png', 'https://www.apple.com', 'Technology', TRUE),
    ('Starbucks', 'starbucks', 'Inspiring and nurturing the human spirit—one person, one cup, and one neighborhood at a time.', 'https://logos-world.net/wp-content/uploads/2020/09/Starbucks-Logo.png', 'https://www.starbucks.com', 'Food & Beverage', TRUE),
    ('IKEA', 'ikea', 'Affordable home furnishings and solutions for every room in your home.', 'https://logos-world.net/wp-content/uploads/2020/11/IKEA-Logo.png', 'https://www.ikea.com', 'Home & Furniture', TRUE),
    ('Sephora', 'sephora', 'Beauty products, makeup, skincare, and fragrance from top brands worldwide.', 'https://logos-world.net/wp-content/uploads/2020/11/Sephora-Logo.png', 'https://www.sephora.com', 'Beauty & Cosmetics', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

INSERT INTO advertising_campaigns (
    id,
    business_profile_id,
    name,
    headline,
    summary,
    objective,
    status,
    curated_theme,
    hero_image_url,
    landing_page_url,
    audience_focus,
    daily_budget,
    primary_metric,
    start_date,
    end_date)
VALUES
    (6001,
     (SELECT id FROM business_profiles WHERE username='nike' LIMIT 1),
     'Nike Run Lab Launch',
     'Run the future in data-informed footwear',
     'Nike spotlights Air Runner innovations with pro athlete testing notes and recovery rituals.',
     'Traffic',
     'ACTIVE',
     'Outdoor Wellness',
     'https://picsum.photos/id/1025/1200/800',
     'https://www.nike.com/air-runner',
     'Urban runners and weekend wellness seekers',
     1500.00,
     'Click-through rate',
     '2024-02-01',
     '2024-04-30'),
    (6002,
     (SELECT id FROM business_profiles WHERE username='apple' LIMIT 1),
     'Apple Studio Workflow Stories',
     'Create bold ideas on Apple Silicon',
     'Creative agencies show how Studio Display, iPad Pro, and Final Cut streamline hybrid teams.',
     'Awareness',
     'ACTIVE',
     'Future of Work',
     'https://picsum.photos/id/1026/1200/800',
     'https://www.apple.com/mac/',
     'Design-forward knowledge workers upgrading their workstations',
     1750.00,
     'Video completion rate',
     '2024-01-15',
     '2024-05-31'),
    (6003,
     (SELECT id FROM business_profiles WHERE username='starbucks' LIMIT 1),
     'Starbucks Cold Foam Social',
     'Chill moments start with Cold Brew',
     'Limited micro-menu featuring creative cold brew builds filmed by Pinterest tastemakers.',
     'Engagement',
     'ACTIVE',
     'Seasonal Sips',
     'https://picsum.photos/id/1027/1200/800',
     'https://www.starbucks.com/menu/product/2122686/iced',
     'Gen Z coffee lovers and at-home baristas',
     900.00,
     'Saves per pin',
     '2023-11-01',
     '2024-02-29'),
    (6004,
     (SELECT id FROM business_profiles WHERE username='ikea' LIMIT 1),
     'IKEA Calm Corners',
     'Modular storage for mindful living',
     'IKEA curates space-saving systems and flexible shelving for creative apartments.',
     'Consideration',
     'ACTIVE',
     'Small Space Living',
     'https://picsum.photos/id/1028/1200/800',
     'https://www.ikea.com/us/en/rooms/living-room/',
     'Renters refreshing multi-use living rooms',
     1100.00,
     'Outbound clicks',
     '2024-03-01',
     '2024-06-30'),
    (6005,
     (SELECT id FROM business_profiles WHERE username='sephora' LIMIT 1),
     'Sephora Radiant Skin Stack',
     'Skin-improving makeup that glows',
     'Artists share AM-to-PM skincare stacks built from Sephora clean beauty favorites.',
     'Sales',
     'ACTIVE',
     'Glow Rituals',
     'https://picsum.photos/id/1029/1200/800',
     'https://www.sephora.com/beauty/skincare-routine',
     'Skinimalist shoppers and beauty minimalists',
     1325.00,
     'Add-to-cart rate',
     '2024-02-10',
     '2024-05-10')
ON DUPLICATE KEY UPDATE summary = VALUES(summary);

INSERT INTO sponsored_pins (
    id,
    pin_id,
    business_profile_id,
    campaign_id,
    sponsored_label,
    cta_text,
    cta_url,
    priority,
    featured)
VALUES
    (9001,
     2001,
     (SELECT id FROM business_profiles WHERE username='nike' LIMIT 1),
     6001,
     'Sponsored • Nike',
     'Shop the Air Runner drop',
     'https://www.nike.com/air-runner',
     1,
     TRUE),
    (9002,
     2002,
     (SELECT id FROM business_profiles WHERE username='apple' LIMIT 1),
     6002,
     'Sponsored • Apple',
     'Explore pro workstations',
     'https://www.apple.com/mac/',
     2,
     TRUE),
    (9003,
     2003,
     (SELECT id FROM business_profiles WHERE username='starbucks' LIMIT 1),
     6003,
     'Sponsored • Starbucks',
     'See the Cold Foam recipes',
     'https://www.starbucks.com/menu/product/2122686/iced',
     3,
     TRUE),
    (9004,
     2004,
     (SELECT id FROM business_profiles WHERE username='ikea' LIMIT 1),
     6004,
     'Sponsored • IKEA',
     'Design your calm corner',
     'https://www.ikea.com/us/en/rooms/living-room/',
     4,
     FALSE),
    (9005,
     2005,
     (SELECT id FROM business_profiles WHERE username='sephora' LIMIT 1),
     6005,
     'Sponsored • Sephora',
     'Build your glow routine',
     'https://www.sephora.com/beauty/skincare-routine',
     5,
     FALSE)
ON DUPLICATE KEY UPDATE cta_text = VALUES(cta_text);

INSERT INTO sponsored_pin_keywords (sponsored_pin_id, keyword)
VALUES
    (9001, 'running'),
    (9001, 'wellness'),
    (9001, 'training'),
    (9002, 'productivity'),
    (9002, 'home-office'),
    (9002, 'apple'),
    (9003, 'coffee'),
    (9003, 'seasonal'),
    (9003, 'recipes'),
    (9004, 'home-decor'),
    (9004, 'storage'),
    (9004, 'minimalist'),
    (9005, 'skincare'),
    (9005, 'beauty'),
    (9005, 'glow')
ON DUPLICATE KEY UPDATE keyword = VALUES(keyword);

-- Seed showcases for businesses
INSERT INTO showcases (business_profile_id, title, description, theme, cover_image_url, featured)
VALUES
    ((SELECT id FROM business_profiles WHERE username='nike' LIMIT 1),
     'New Arrivals: Spring Collection 2024',
     'Discover the latest styles and innovations in our spring collection',
     'New Arrivals',
     'https://picsum.photos/id/1030/800/600',
     TRUE),
    ((SELECT id FROM business_profiles WHERE username='nike' LIMIT 1),
     'Athletic Performance Gear',
     'Professional-grade equipment for serious athletes',
     'Performance',
     'https://picsum.photos/id/1031/800/600',
     FALSE),
    ((SELECT id FROM business_profiles WHERE username='apple' LIMIT 1),
     'Latest iPhone Collection',
     'Explore the newest iPhone models and accessories',
     'New Arrivals',
     'https://picsum.photos/id/1032/800/600',
     TRUE),
    ((SELECT id FROM business_profiles WHERE username='starbucks' LIMIT 1),
     'Holiday Drinks Menu',
     'Warm up with our seasonal favorites',
     'Seasonal Trends',
     'https://picsum.photos/id/1033/800/600',
     TRUE),
    ((SELECT id FROM business_profiles WHERE username='ikea' LIMIT 1),
     'Scandinavian Living Room Ideas',
     'Minimalist designs for modern homes',
     'Home Decor',
     'https://picsum.photos/id/1034/800/600',
     TRUE),
    ((SELECT id FROM business_profiles WHERE username='sephora' LIMIT 1),
     'Beauty Essentials for Every Skin Type',
     'Curated products for your skincare routine',
     'Featured Products',
     'https://picsum.photos/id/1035/800/600',
     TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Migration: Add reset password columns to existing user_accounts table
-- These columns are already in the CREATE TABLE above, but this ensures they exist in existing databases
-- Note: If columns already exist, you may see errors - that's fine, it means they're already there

-- Add reset_otp column (run this manually if you get errors on startup)
-- ALTER TABLE user_accounts ADD COLUMN reset_otp VARCHAR(10) NULL;

-- Add reset_otp_expires_at column (run this manually if you get errors on startup)
-- ALTER TABLE user_accounts ADD COLUMN reset_otp_expires_at TIMESTAMP NULL;

