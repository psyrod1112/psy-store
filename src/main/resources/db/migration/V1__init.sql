-- 회원
CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,   -- 앱에서 소문자로 정규화 후 저장
    password_hash  VARCHAR(100) NOT NULL,
    role           VARCHAR(20)  NOT NULL DEFAULT 'USER'
                   CHECK (role IN ('USER', 'ADMIN')),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 앱 (상품)
CREATE TABLE apps (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slug            VARCHAR(100) NOT NULL UNIQUE,   -- URL용 식별자: /apps/agar-io
    title           VARCHAR(200) NOT NULL,
    description     TEXT         NOT NULL,
    price_krw       INTEGER      NOT NULL CHECK (price_krw > 0),
    screenshot_key  VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 앱 릴리스 (버전별 다운로드 파일)
CREATE TABLE app_releases (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app_id           BIGINT       NOT NULL REFERENCES apps (id),
    version          VARCHAR(50)  NOT NULL,
    file_key         VARCHAR(500) NOT NULL,
    file_size_bytes  BIGINT       NOT NULL CHECK (file_size_bytes > 0),
    sha256           CHAR(64)     NOT NULL,         -- 다운로드 무결성 검증용
    released_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (app_id, version)
);

-- 주문 (이력)
CREATE TABLE orders (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_no    VARCHAR(64)  NOT NULL UNIQUE,       -- 외부 노출용, 토스 orderId
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    app_id      BIGINT       NOT NULL REFERENCES apps (id),
    amount_krw  INTEGER      NOT NULL CHECK (amount_krw > 0),   -- 가격 스냅샷
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELED')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_user_id ON orders (user_id);

-- 결제 승인 기록
CREATE TABLE payments (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id      BIGINT       NOT NULL UNIQUE REFERENCES orders (id),  -- 주문당 승인 1회
    payment_key   VARCHAR(200) NOT NULL UNIQUE,     -- 토스 paymentKey
    method        VARCHAR(50),
    amount_krw    INTEGER      NOT NULL,
    approved_at   TIMESTAMPTZ  NOT NULL,
    raw_response  JSONB        NOT NULL,            -- 토스 응답 원문 (감사/분쟁 대응용)
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 소유권
CREATE TABLE entitlements (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    app_id      BIGINT      NOT NULL REFERENCES apps (id),
    source      VARCHAR(20) NOT NULL DEFAULT 'PURCHASE'
                CHECK (source IN ('PURCHASE', 'GRANT')),
    order_id    BIGINT      REFERENCES orders (id),  -- GRANT(관리자 지급)이면 NULL
    granted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, app_id)                          -- 같은 앱을 두 번 소유할 수 없음
);