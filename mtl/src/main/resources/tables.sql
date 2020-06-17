CREATE TABLE IF NOT EXISTS accounts (
    no varchar NOT NULL,
    name varchar NOT NULL,
    type varchar NOT NULL,
    rateOfInterest decimal,
    dateOfOpen timestamp NOT NULL,
    dateOfClose timestamp,
    balance decimal NOT NULL
);
ALTER TABLE accounts ADD PRIMARY KEY (no);