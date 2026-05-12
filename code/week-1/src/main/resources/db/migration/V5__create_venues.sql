CREATE TABLE venues (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    logo_url VARCHAR(255),
    menu_url VARCHAR(255),
    specials_text TEXT
);

ALTER TABLE tournaments ADD CONSTRAINT fk_tournaments_venue FOREIGN KEY (venue_id) REFERENCES venues(id);
