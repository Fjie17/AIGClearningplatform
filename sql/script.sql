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
    LQAI            varchar(255)                       null comment 'LQAI字段',
    LQAI_code       varchar(255)                       null comment 'LQAI_code字段',
    profile         varchar(255)                       null comment 'profile字段',
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint UKdxg0ybygxjmffvick7j339dmy
        unique (user_id, subject_id),
    constraint user_id
        unique (user_id, subject_id)
);

create table learning_resource
(
    id                  bigint auto_increment
        primary key,
    subject_id          bigint                             not null,
    knowledge_point_id  bigint                             null comment '知识点ID，允许为空',
    platform            varchar(255)                       null,
    title               varchar(255)                       null,
    author              varchar(255)                       null comment '作者',
    views               varchar(255)                       null,
    review_count        int                                null comment '评论数',
    publish_time        varchar(255)                       null,
    topic               varchar(255)                       null,
    class_hours         int                                null comment '课时',
    image_url           varchar(255)                       null,
    ai_match_confidence varchar(255)                       null,
    ai_match_info       json                               null comment 'AI匹配详细信息',
    tags                varchar(255)                       null,
    description         text                               null comment '资源描述',
    resource_url        varchar(255)                       not null,
    resource_type       varchar(255)                       null,
    duration            int                                null comment '分钟',
    created_at          datetime default CURRENT_TIMESTAMP null
);

create index idx_created
    on learning_resource (created_at);

create index idx_platform
    on learning_resource (platform);

create index idx_subject_kp
    on learning_resource (subject_id, knowledge_point_id);

create table subject
(
    id           bigint auto_increment
        primary key,
    code         varchar(255)                       null,
    name         varchar(255)                       null,
    category     varchar(255)                       null,
    sub_category varchar(255)                       null,
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

create table subject_assessment_question
(
    id            bigint auto_increment comment '题目ID'
        primary key,
    subject_id    bigint                                                                                  not null comment '所属科目ID，关联subject表',
    question_code varchar(50)                                                                             not null comment '题目编号，如：Q1、Q2，用于标识顺序',
    question_text text                                                                                    not null comment '题目文本内容（题干）',
    question_type enum ('single_choice', 'multi_choice', 'text_input') default 'single_choice'            null comment '题型：单选/多选/文本输入',
    mapping_field enum ('current_level', 'learning_speed', 'preference', 'self_discipline', 'open_ended') null comment '映射到learning_profile的哪个字段',
    options_json  json                                                                                    null comment '选项列表，JSON格式：[{"code":"A","text":"选项内容","score":1}]，开放性问题可为空',
    default_order int                                                  default 0                          null comment '题目显示顺序',
    is_active     tinyint                                              default 1                          null comment '是否启用：0-禁用，1-启用',
    created_at    datetime                                             default CURRENT_TIMESTAMP          null comment '创建时间',
    updated_at    datetime                                             default CURRENT_TIMESTAMP          null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_subject_question_code
        unique (subject_id, question_code),
    constraint fk_assessment_subject
        foreign key (subject_id) references subject (id)
            on delete cascade
)
    comment '科目评估问卷表' collate = utf8mb4_unicode_ci;

create index idx_default_order
    on subject_assessment_question (default_order);

create index idx_is_active
    on subject_assessment_question (is_active);

create index idx_mapping_field
    on subject_assessment_question (mapping_field);

create index idx_question_code
    on subject_assessment_question (question_code);

create index idx_subject_id
    on subject_assessment_question (subject_id);

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


