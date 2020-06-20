CREATE TYPE accountType AS ENUM ('checking', 'savings')

CREATE TABLE IF NOT EXISTS accounts (
    no varchar NOT NULL,
    name varchar NOT NULL,
    type accountType NOT NULL,
    rateOfInterest decimal,
    dateOfOpen timestamp NOT NULL,
    dateOfClose timestamp,
    balance decimal NOT NULL
);
ALTER TABLE accounts ADD PRIMARY KEY (no);