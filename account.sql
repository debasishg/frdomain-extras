CREATE TABLE IF NOT EXISTS accounts (
    no varchar NOT NULL,
    name varchar NOT NULL,
    rateOfInterest decimal,
    dateOfOpen timestamp NOT NULL,
    dateOfClose timestamp,
    balance decimal NOT NULL
);

ALTER TABLE accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (no);

