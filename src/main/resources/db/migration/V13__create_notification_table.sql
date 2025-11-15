DROP TABLE IF EXISTS notifications;

-- Create notifications table
CREATE TABLE notifications (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                             receiver_id UUID NOT NULL,
                             title VARCHAR(255),
                             content TEXT,
                             type VARCHAR(50),

                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Optional: add index for faster lookup by user
CREATE INDEX idx_notifications_receiver_id ON notifications(receiver_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
