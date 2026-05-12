CREATE TABLE tournaments (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    join_code VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    max_players INTEGER NOT NULL,
    bracket_format VARCHAR(50),
    format_override BOOLEAN NOT NULL DEFAULT FALSE,
    entry_fee DECIMAL(19, 2) NOT NULL DEFAULT 0.0,
    race_to_default INTEGER NOT NULL DEFAULT 3,
    enable_calcutta BOOLEAN NOT NULL DEFAULT FALSE,
    venue_id UUID,
    created_by_user_id UUID NOT NULL,
    date DATE,
    location VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_tournaments_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);
