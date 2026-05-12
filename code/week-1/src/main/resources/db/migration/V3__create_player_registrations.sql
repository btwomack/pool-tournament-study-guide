CREATE TABLE player_registrations (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    player_name VARCHAR(255) NOT NULL,
    contact_info VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    paid_flag BOOLEAN NOT NULL DEFAULT FALSE,
    payment_intent_id VARCHAR(255),
    seed_number INTEGER,
    losses_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_player_registrations_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id)
);
