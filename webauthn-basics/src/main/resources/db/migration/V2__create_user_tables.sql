CREATE TABLE user_accounts(
   id UUID  PRIMARY KEY,
   full_name VARCHAR(1024) NOT NULL,
   email VARCHAR(1024) UNIQUE NOT NULL
);

CREATE TABLE user_webauthn_credentials(
    id UUID PRIMARY KEY
);

CREATE TABLE webauthn_registration_flow(
    id UUID PRIMARY KEY,
    start_request TEXT,
    start_response TEXT,
    finish_request TEXT,
    finish_response TEXT,
    yubico_creation_options TEXT,
    yubico_reg_result TEXT
)