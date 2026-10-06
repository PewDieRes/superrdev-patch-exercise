CREATE TABLE IF NOT EXISTS tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255)  NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(20)   NOT NULL,
    priority    VARCHAR(10)   DEFAULT 'MEDIUM',
    archived    BOOLEAN       DEFAULT FALSE,
    assignee    VARCHAR(100),
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Lets the search skip archived rows and narrow by status without a full table scan.
-- The LIKE '%term%' match itself still can't use an index.
CREATE INDEX IF NOT EXISTS idx_tasks_active ON tasks (archived, status, created_at DESC, id DESC);
