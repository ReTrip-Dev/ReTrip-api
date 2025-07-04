CREATE TABLE IF NOT EXISTS members
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    email       VARCHAR(255),
    kakao_id    VARCHAR(255),
    nickname    VARCHAR(255),
    password    VARCHAR(255),
    user_id     VARCHAR(255),
    login_type  ENUM ('APPLE','GOOGLE','KAKAO','NAVER','NORMAL'),
    PRIMARY KEY (id),
    UNIQUE KEY UK_email (email),
    UNIQUE KEY UK_kakao_id (kakao_id),
    UNIQUE KEY UK_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS retrip_reports
(
    id          BIGINT NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    image_url   VARCHAR(255),
    member_id   VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS retrips
(
    id                  BIGINT NOT NULL AUTO_INCREMENT,
    image_count         INT,
    main_location_lat   FLOAT(53),
    main_location_lng   FLOAT(53),
    total_distance      FLOAT(53),
    created_at          DATETIME(6),
    end_date            DATETIME(6),
    member_id           BIGINT,
    modified_at         DATETIME(6),
    start_date          DATETIME(6),
    country_code        VARCHAR(255),
    favorite_photo_spot VARCHAR(255),
    favorite_subjects   VARCHAR(255),
    hashtag             VARCHAR(255),
    keywords            TEXT,
    mbti                VARCHAR(255),
    summary_line        VARCHAR(255),
    main_time_slot      ENUM ('AFTERNOON','DAWN','MORNING','NIGHT'),
    egen_teto_type      VARCHAR(255),
    egen_teto_subtype   VARCHAR(255),
    egen_teto_hashtag   VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT FK_retrips_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS recommendation_places
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    created_at  DATETIME(6),
    modified_at DATETIME(6),
    retrip_id   BIGINT,
    description TEXT         NOT NULL,
    emoji       VARCHAR(255) NOT NULL,
    place       VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_recommendation_places_retrip FOREIGN KEY (retrip_id) REFERENCES retrips (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;