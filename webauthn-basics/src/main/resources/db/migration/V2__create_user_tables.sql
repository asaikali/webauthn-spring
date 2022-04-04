CREATE TABLE user_accounts(
   id UUID  PRIMARY KEY,
   full_name VARCHAR(1024) NOT NULL,
   email VARCHAR(1024) UNIQUE NOT NULL
);

CREATE TABLE user_webauthn_credentials(
    id UUID PRIMARY KEY
);
