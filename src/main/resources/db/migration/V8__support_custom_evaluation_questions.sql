alter table evaluations
    drop foreign key fk_evaluations_question;

alter table evaluations
    modify column question_id bigint null,
    add column custom_question text null after question_id;

alter table evaluations
    add constraint fk_evaluations_question foreign key (question_id) references questions(id);
