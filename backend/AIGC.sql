create table knowledge_point
(
    id          bigint auto_increment
        primary key,
    subject_id  bigint                             not null,
    name        varchar(255)                       null,
    difficulty  int                                null,
    exam_weight int                                null,
    parent_id   bigint                             null comment '父级知识点',
    created_at  datetime default CURRENT_TIMESTAMP null
);

create table learning_behavior_log
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null,
    subject_id  bigint                             not null,
    action      varchar(255)                       null,
    target_type varchar(255)                       null,
    target_id   bigint                             null,
    duration    int                                null comment '秒',
    created_at  datetime default CURRENT_TIMESTAMP null
);

create table learning_path
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             not null,
    subject_id  bigint                             not null,
    path_period varchar(255)                       null,
    status      varchar(255)                       null,
    created_at  datetime default CURRENT_TIMESTAMP null
);

create table learning_path_item
(
    id                 bigint auto_increment
        primary key,
    path_id            bigint       not null,
    subject_id         bigint       not null,
    knowledge_point_id bigint       null,
    task_type          varchar(255) null,
    task_content       tinytext     null,
    status             varchar(255) null,
    order_no           int          null
);

create table learning_profile
(
    id              bigint auto_increment
        primary key,
    user_id         bigint                             not null,
    subject_id      bigint                             not null,
    current_level   int                                null,
    learning_speed  int                                null,
    preference      varchar(255)                       null,
    self_discipline int                                null,
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint UKdxg0ybygxjmffvick7j339dmy
        unique (user_id, subject_id),
    constraint user_id
        unique (user_id, subject_id)
);

create table learning_resource
(
    id                 bigint auto_increment
        primary key,
    subject_id         bigint                             not null,
    knowledge_point_id bigint                             not null,
    platform           varchar(255)                       null,
    title              varchar(255)                       null,
    resource_url       varchar(255)                       not null,
    resource_type      varchar(255)                       null,
    duration           int                                null comment '分钟',
    created_at         datetime default CURRENT_TIMESTAMP null
);

create table subject
(
    id           bigint auto_increment
        primary key,
    code         varchar(255)                       null,
    name         varchar(255)                       null,
    category     varchar(255)                       null,
    sub_category varchar(100)                       null comment '二级科目',
    description  varchar(255)                       null,
    status       int                                null,
    created_at   datetime default CURRENT_TIMESTAMP null,
    constraint code
        unique (code)
);

create table resource_match_profile
(
    id          bigint auto_increment
        primary key,
    resource_id bigint                             not null,
    subject_id  bigint                             not null,
    level_min   int                                null,
    level_max   int                                null,
    speed_min   int                                null,
    speed_max   int                                null,
    preference  varchar(255)                       null,
    created_at  datetime default CURRENT_TIMESTAMP null,
    constraint fk_resource_match_resource
        foreign key (resource_id) references learning_resource (id),
    constraint fk_resource_match_subject
        foreign key (subject_id) references subject (id)
);

create table user
(
    id             bigint auto_increment comment '用户ID'
        primary key,
    username       varchar(255)                       not null,
    email          varchar(255)                       not null,
    password       varchar(255)                       not null comment 'BCrypt加密密码',
    role           varchar(255)                       not null,
    status         int                                null,
    email_verified int                                null,
    last_login     datetime                           null comment '最后登录时间',
    created_at     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint email
        unique (email),
    constraint username
        unique (username)
)
    comment '用户表';

create table user_subject
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                             not null,
    subject_id    bigint                             not null,
    current_stage varchar(255)                       null,
    created_at    datetime default CURRENT_TIMESTAMP null,
    constraint UK43rtf5yan5qdn2cs5lt44eh3d
        unique (user_id, subject_id),
    constraint user_id
        unique (user_id, subject_id),
    constraint fk_user_subject_subject
        foreign key (subject_id) references subject (id),
    constraint fk_user_subject_user
        foreign key (user_id) references user (id)
);

-- =========================================
-- 为 learning_resource 表添加爬虫字段和AI匹配字段
-- =========================================

ALTER TABLE learning_resource 
ADD COLUMN author varchar(255) NULL COMMENT '作者' AFTER title,
ADD COLUMN views varchar(100) NULL COMMENT '观看次数' AFTER author,
ADD COLUMN review_count int NULL COMMENT '评论数' AFTER views,
ADD COLUMN publish_time varchar(100) NULL COMMENT '发布时间' AFTER review_count,
ADD COLUMN topic varchar(500) NULL COMMENT '话题' AFTER publish_time,
ADD COLUMN class_hours int NULL COMMENT '课时' AFTER topic,
ADD COLUMN image_url varchar(500) NULL COMMENT '封面图片URL' AFTER class_hours,
ADD COLUMN ai_match_confidence varchar(20) NULL COMMENT 'AI匹配置信度(high/medium/low)' AFTER image_url,
ADD COLUMN ai_match_info json NULL COMMENT 'AI匹配详细信息' AFTER ai_match_confidence,
ADD COLUMN tags varchar(1000) NULL COMMENT '标签，逗号分隔' AFTER ai_match_info,
ADD COLUMN description text NULL COMMENT '资源描述' AFTER tags,
MODIFY COLUMN knowledge_point_id bigint NULL COMMENT '知识点ID，允许为空' AFTER subject_id;

-- 添加索引
ALTER TABLE learning_resource ADD INDEX idx_platform (platform);
ALTER TABLE learning_resource ADD INDEX idx_subject_kp (subject_id, knowledge_point_id);
ALTER TABLE learning_resource ADD INDEX idx_created (created_at);

-- =========================================
-- 为 learning_profile 表添加 LQAI 字段
-- =========================================
ALTER TABLE learning_profile 
ADD COLUMN LQAI varchar(255) NULL COMMENT 'LQAI字段' AFTER self_discipline;

ALTER TABLE learning_profile 
ADD COLUMN LQAI_code varchar(255) NULL COMMENT 'LQAI_code字段' AFTER LQAI;

ALTER TABLE learning_profile 
ADD COLUMN profile varchar(255) NULL COMMENT 'profile字段' AFTER LQAI_code;

-- =========================================
-- 科目评估问卷表
-- 用于存放每个科目对应的评估提问，支持多科目、多题型
-- =========================================

CREATE TABLE subject_assessment_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    subject_id BIGINT NOT NULL COMMENT '所属科目ID，关联subject表',
    question_code VARCHAR(50) NOT NULL COMMENT '题目编号，如：Q1、Q2，用于标识顺序',
    question_text TEXT NOT NULL COMMENT '题目文本内容（题干）',
    question_type ENUM('single_choice', 'multi_choice', 'text_input') DEFAULT 'single_choice' COMMENT '题型：单选/多选/文本输入',
    mapping_field ENUM('current_level', 'learning_speed', 'preference', 'self_discipline', 'open_ended') NULL COMMENT '映射到learning_profile的哪个字段',
    options_json JSON NULL COMMENT '选项列表，JSON格式：[{"code":"A","text":"选项内容","score":1}]，开放性问题可为空',
    default_order INT DEFAULT 0 COMMENT '题目显示顺序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引
    INDEX idx_subject_id (subject_id),
    INDEX idx_question_code (question_code),
    INDEX idx_mapping_field (mapping_field),
    INDEX idx_is_active (is_active),
    INDEX idx_default_order (default_order),
    
    -- 外键约束
    CONSTRAINT fk_assessment_subject FOREIGN KEY (subject_id) REFERENCES subject(id) ON DELETE CASCADE,
    
    -- 同一个科目下题目编号唯一
    UNIQUE KEY uk_subject_question_code (subject_id, question_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科目评估问卷表';

-- 第1题：水平/速度 - 函数极限概念（映射到 current_level）
INSERT INTO subject_assessment_question (subject_id, question_code, question_text, question_type, mapping_field, options_json, default_order) VALUES
(1, 'Q1', '关于函数极限的概念，以下哪种描述最符合你当前的状态？', 'single_choice', 'current_level',
JSON_ARRAY(
    JSON_OBJECT('code', 'A', 'text', '完全没接触过，不知道极限是什么。', 'score', 1),
    JSON_OBJECT('code', 'B', 'text', '听说过lim符号，知道它代表无限接近，但不懂具体计算。', 'score', 2),
    JSON_OBJECT('code', 'C', 'text', '会用代入法算简单的极限，但遇到0/0型或涉及无穷小量比较时会卡壳。', 'score', 3),
    JSON_OBJECT('code', 'D', 'text', '熟练掌握洛必达法则、泰勒公式展开来求极限，但对ε-δ语言的严格证明感到头疼。', 'score', 4),
    JSON_OBJECT('code', 'E', 'text', '对ε-δ定义非常清晰，能轻松处理各类复杂极限证明与计算。', 'score', 5)
), 1);

-- 第2题：水平/速度 - 积分识别（映射到 learning_speed）
INSERT INTO subject_assessment_question (subject_id, question_code, question_text, question_type, mapping_field, options_json, default_order) VALUES
(1, 'Q2', '请观察以下微积分表达式：∫x·cos(x)dx。面对这道题，你的第一反应是？', 'single_choice', 'learning_speed',
'[
    {"code":"A","text":"这是天书，完全看不懂符号含义。","score":1},
    {"code":"B","text":"知道这是求积分，但不知道从何下手。","score":2},
    {"code":"C","text":"记得好像有个分部积分法，公式大概是∫udv=uv-∫vdu，试着套一下。","score":3},
    {"code":"D","text":"一眼看出需要用分部积分法，能迅速写出u=x, dv=cosx dx并得出结果x·sinx+cosx+C。","score":4},
    {"code":"E","text":"不仅秒解，还能立刻联想到如果是定积分∫₀^π x·cosx dx该如何利用对称性或周期性简化运算。","score":5}
]', 2);

-- 第3题：总体偏好（映射到 preference）
INSERT INTO subject_assessment_question (subject_id, question_code, question_text, question_type, mapping_field, options_json, default_order) VALUES
(1, 'Q3', '假设你要攻克"多元函数微分学"这一章，你希望平台首先为你呈现的学习材料是？', 'single_choice', 'preference',
'[
    {"code":"A","text":"视频演示：通过动态3D图像展示曲面、切平面和等高线，配合老师讲解。","score":1},
    {"code":"B","text":"文本+图解：一份结构清晰的图文笔记，包含核心公式推导和典型例题解析。","score":2},
    {"code":"C","text":"音频/播客：像听故事一样，用通俗的语言把概念逻辑讲清楚，方便我通勤时磨耳朵。","score":3},
    {"code":"D","text":"交互式练习：直接给我几个简单的填空题，让我在做题中自己摸索规律。","score":4}
]', 3);

-- 第4题：自律程度（映射到 self_discipline）
INSERT INTO subject_assessment_question (subject_id, question_code, question_text, question_type, mapping_field, options_json, default_order) VALUES
(1, 'Q4', '今天是周末，你原本计划花2小时复习高数，但朋友突然约你出去玩。此时你会？', 'single_choice', 'self_discipline',
'[
    {"code":"A","text":"毫不犹豫放下书本，高数太难了，正好休息。","score":1},
    {"code":"B","text":"去玩，但心里会有点愧疚，回来后再说（大概率不补了）。","score":2},
    {"code":"C","text":"跟朋友商量晚一点去，或者缩短游玩时间，争取完成至少1小时的学习任务。","score":3},
    {"code":"D","text":"婉拒朋友，雷打不动地完成今天的2小时学习计划，玩也要等学完再安心玩。","score":4}
]', 4);

-- 第5题：开放式问题（映射到 open_ended，不存入learning_profile）
INSERT INTO subject_assessment_question (subject_id, question_code, question_text, question_type, mapping_field, options_json, default_order) VALUES
(1, 'Q5', '请用一句话（10-20字以内）描述你目前学习高数最大的痛点或目标。（例如："公式记不住"、"想快速通过期末考"、"基础太差听不懂"等）', 'text_input', 'open_ended', NULL, 5);