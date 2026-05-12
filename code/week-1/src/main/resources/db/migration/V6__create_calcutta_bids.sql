CREATE TABLE calcutta_bids (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL,
    player_id UUID NOT NULL,
    bidder_name VARCHAR(255) NOT NULL,
    bid_amount DECIMAL(19, 2) NOT NULL,
    CONSTRAINT fk_calcutta_bids_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id),
    CONSTRAINT fk_calcutta_bids_player FOREIGN KEY (player_id) REFERENCES player_registrations(id)
);
