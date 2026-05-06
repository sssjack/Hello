create table if not exists questions (
    id bigint primary key auto_increment,
    exam_year int not null,
    exam_type varchar(32) not null,
    province varchar(64),
    source varchar(128),
    tags varchar(255),
    content text not null,
    enabled boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp on update current_timestamp,
    index idx_questions_filter (enabled, exam_year, exam_type),
    index idx_questions_type (exam_type)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;

create table if not exists evaluations (
    id bigint primary key auto_increment,
    question_id bigint not null,
    answer_text text not null,
    score int not null,
    level varchar(32) not null,
    result_json json not null,
    created_at timestamp not null default current_timestamp,
    index idx_evaluations_question (question_id),
    index idx_evaluations_score (score),
    constraint fk_evaluations_question foreign key (question_id) references questions(id)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_unicode_ci;
