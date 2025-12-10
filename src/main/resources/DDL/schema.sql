create table if not exists kakao_temp_token
(
    token       varchar(64)  not null
    primary key,
    created_at  datetime(6)  not null,
    expires_at  datetime(6)  not null,
    kakao_email varchar(100) null,
    kakao_name  varchar(50)  null,
    provider_id varchar(100) not null
    );

create table if not exists otp_rate_limit
(
    id           bigint auto_increment
    primary key,
    requested_at datetime(6) not null,
    user_id      bigint      not null
    );

create index idx_user_requested_at
    on otp_rate_limit (user_id, requested_at);

create table if not exists quiz_info
(
    quiz_id     bigint auto_increment
    primary key,
    created_at  datetime(6)  not null,
    updated_at  datetime(6)  not null,
    answer      varchar(255) not null,
    explanation text         not null,
    info        text         not null,
    question    text         not null,
    title       text         not null
    );

create table if not exists quiz_progress
(
    progress_id         bigint auto_increment
    primary key,
    created_at          datetime(6) not null,
    updated_at          datetime(6) not null,
    coupon              int         not null,
    course_completed    bit         not null,
    first_quiz_id_today int         null,
    monthly_reward      bit         not null,
    quiz_date           int         not null,
    request_completed   bit         not null,
    streak_days         int         not null,
    today_solved        int         not null,
    user_id             bigint      not null
    );

create table if not exists sample_entity
(
    id         bigint auto_increment
    primary key,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    price      varchar(64) not null
    );

create table if not exists user_service
(
    user_id            bigint auto_increment
    primary key,
    created_at         datetime(6)                       not null,
    updated_at         datetime(6)                       not null,
    birth_date         date                              not null,
    core_user_id       bigint                            null,
    email              varchar(100)                      not null,
    gender             tinyint                           not null,
    name               varchar(50)                       not null,
    night_push_enabled bit                               not null,
    password           varchar(255)                      not null,
    phone_number       varchar(20)                       not null,
    provider_id        varchar(255)                      null,
    push_enabled       bit                               not null,
    role               enum ('CHILD', 'ADMIN', 'PARENT') not null,
    simple_password    varchar(255)                      not null,
    constraint UKlv7sxdds0uxxy5r6bhtc9sm8g
    unique (email)
    );

create table if not exists account
(
    account_id bigint auto_increment
    primary key,
    created_at datetime(6)                                     not null,
    account_no varchar(50)                                     not null,
    type       enum ('ALLOWANCE', 'DEPOSIT', 'GOAL', 'INVEST') not null,
    user_id    bigint                                          not null,
    constraint FK6jagyrttankhabqnx3qrjth6u
    foreign key (user_id) references user_service (user_id)
    );

create table if not exists api_request_log
(
    request_id      bigint auto_increment
    primary key,
    created_at      datetime(6)                                                                                     not null,
    category        enum ('ACCOUNT', 'ALLOWANCE', 'AUTH', 'GOAL', 'INVESTMENTS', 'NOTIFICATION', 'PROFILE', 'QUIZ') not null,
    request_content text                                                                                            not null,
    response_code   int                                                                                             null,
    success         bit                                                                                             not null,
    user_id         bigint                                                                                          null,
    constraint FKjoty2jvwmppqkaivgqpha0mdx
    foreign key (user_id) references user_service (user_id)
    );

create table if not exists api_error_log
(
    error_id      bigint auto_increment
    primary key,
    created_at    datetime(6)  not null,
    error_code    varchar(100) null,
    error_detail  text         null,
    error_message text         null,
    request_id    bigint       not null,
    constraint FKnub6m5xnnn647lq22406v84ia
    foreign key (request_id) references api_request_log (request_id)
    );

create table if not exists auto_transfer
(
    auto_transfer_id         bigint auto_increment
    primary key,
    created_at               datetime(6)                         not null,
    updated_at               datetime(6)                         not null,
    frequency                enum ('DAILY', 'MONTHLY', 'WEEKLY') null,
    invest_bank_transfer_id  bigint                              null,
    primary_bank_transfer_id bigint                              not null,
    ratio                    int                                 null,
    transfer_amount          decimal(12, 2)                      not null,
    transfer_date            int                                 null,
    type                     enum ('ALLOWANCE', 'GOAL')          not null,
    account_id               bigint                              not null,
    user_id                  bigint                              not null,
    constraint FK3d6f5r32hmqy530lv05qukw5r
    foreign key (user_id) references user_service (user_id),
    constraint FKt5fx2lmi9bi4h18xm2k8beq0e
    foreign key (account_id) references account (account_id)
    );

create table if not exists card_info
(
    card_id    bigint auto_increment
    primary key,
    created_at datetime(6)  not null,
    cvc        varchar(3)   not null,
    expired_at varchar(5)   not null,
    name       varchar(100) not null,
    number     varchar(20)  not null,
    password   varchar(255) not null,
    account_id bigint       not null,
    constraint FKeti938xbifp369vyv6oasvi6t
    foreign key (account_id) references account (account_id)
    );

create table if not exists goal_savings
(
    goal_id        bigint auto_increment
    primary key,
    created_at     datetime(6)                                                       not null,
    updated_at     datetime(6)                                                       not null,
    monthly_amount decimal(12, 2)                                                    not null,
    name           varchar(100)                                                      not null,
    pay_day        int                                                               not null,
    status         enum ('CANCELLED', 'COMPLETED', 'ONGOING', 'PENDING', 'REJECTED') not null,
    target_amount  decimal(12, 2)                                                    not null,
    account_id     bigint                                                            null,
    user_id        bigint                                                            not null,
    constraint UKhe3e9kp5f8qfqbc0e3oy45x9q
    unique (account_id),
    constraint FK2q47n7ne16x5xpmm3kl56juvw
    foreign key (user_id) references user_service (user_id),
    constraint FKchv6h31nqucyi625dsltjvx6q
    foreign key (account_id) references account (account_id)
    );

create table if not exists notification
(
    notification_id bigint auto_increment
    primary key,
    created_at      datetime(6)                                  not null,
    content         text                                         null,
    is_read         bit                                          not null,
    title           varchar(100)                                 not null,
    type            enum ('ALLOWANCE', 'GOAL', 'QUIZ', 'SYSTEM') not null,
    user_id         bigint                                       not null,
    constraint FK5cky08wb6wqabcndjxgyox0d8
    foreign key (user_id) references user_service (user_id)
    );

create table if not exists summary_report
(
    report_id          bigint auto_increment
    primary key,
    created_at         datetime(6)    not null,
    month              int            not null,
    year               int            not null,
    prev_total_expense decimal(12, 2) null,
    total_expense      decimal(12, 2) not null,
    user_id            bigint         not null,
    constraint FKeqvx2vekwyr2lhdloe5e7y7hv
    foreign key (user_id) references user_service (user_id)
    );

create table if not exists detail_report
(
    detail_report_id bigint auto_increment
    primary key,
    created_at       datetime(6)                                                             not null,
    amount           decimal(12, 2)                                                          not null,
    category         enum ('EDU', 'ENT', 'ETC', 'FOOD', 'SHOPPING', 'TRANSFER', 'TRANSPORT') not null,
    percent          decimal(5, 2)                                                           not null,
    report_id        bigint                                                                  not null,
    constraint FKh9hf4r8jysx4kk04megcbo2v7
    foreign key (report_id) references summary_report (report_id)
    );

create table if not exists feedback
(
    feedback_id bigint auto_increment
    primary key,
    created_at  datetime(6) not null,
    updated_at  datetime(6) not null,
    message     text        not null,
    report_id   bigint      not null,
    writer_id   bigint      not null,
    constraint FKo2oudaticgyok5tii1sic4w41
    foreign key (writer_id) references user_service (user_id),
    constraint FKume1a9a9kx3fbpkmqwnhd4w9
    foreign key (report_id) references summary_report (report_id)
    );

create table if not exists user_relationship
(
    relationship_id bigint auto_increment
    primary key,
    created_at      datetime(6) not null,
    family_otp      varchar(10) not null,
    child_id        bigint      null,
    parent_id       bigint      not null,
    constraint FK2a1spjn8pd43s97iiil1nilgb
    foreign key (child_id) references user_service (user_id),
    constraint FKaxctcbkv0u9eni2q4cfkbvyqb
    foreign key (parent_id) references user_service (user_id)
    );

