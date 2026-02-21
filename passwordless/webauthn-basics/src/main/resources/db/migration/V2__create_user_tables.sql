CREATE TABLE user_accounts
(
    id        UUID PRIMARY KEY,
    full_name VARCHAR(1024)        NOT NULL,
    email     VARCHAR(1024) UNIQUE NOT NULL
);

CREATE TABLE webauthn_user_credentials
(
    id              VARCHAR(2048) PRIMARY KEY,
    public_key_cose TEXT NOT NULL,
    user_id         UUID NOT NULL,
    type            TEXT NOT NULL
);

CREATE TABLE webauthn_registration_flow
(
    id                      UUID PRIMARY KEY,
    start_request           TEXT,
    start_response          TEXT,
    finish_request          TEXT,
    finish_response         TEXT,
    yubico_creation_options TEXT,
    yubico_reg_result       TEXT
);

CREATE TABLE webauthn_login_flow
(
    id               UUID PRIMARY KEY,
    start_request    TEXT,
    start_response   TEXT,
    assertion_request TEXT,
    assertion_result  TEXT,
    successful_login BOOLEAN
);