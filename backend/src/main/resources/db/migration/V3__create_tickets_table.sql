CREATE TABLE tickets (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    number        VARCHAR(10)  NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'WAITING',
    priority      VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    counter_id    BIGINT       NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at     TIMESTAMP    NULL,
    completed_at  TIMESTAMP    NULL,

    CONSTRAINT uq_tickets_number UNIQUE (number),
    CONSTRAINT chk_tickets_status CHECK (status IN ('WAITING', 'CALLED', 'IN_SERVICE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('NORMAL', 'PRIORITY')),
    CONSTRAINT fk_tickets_counter FOREIGN KEY (counter_id) REFERENCES counters(id)
);

CREATE INDEX idx_tickets_status_priority ON tickets(status, priority, created_at);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);

