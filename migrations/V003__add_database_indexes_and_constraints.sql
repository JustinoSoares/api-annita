-- ============================================================
-- Migration V003: Add missing indexes and FK constraints
-- ============================================================

-- -------------------------------------------------------
-- tb_events
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_events_category_id ON tb_events(category_id);
CREATE INDEX IF NOT EXISTS idx_tb_events_created_by ON tb_events(created_by);
CREATE INDEX IF NOT EXISTS idx_tb_events_status ON tb_events(status);
CREATE INDEX IF NOT EXISTS idx_tb_events_created_at ON tb_events(created_at);
CREATE INDEX IF NOT EXISTS idx_tb_events_created_by_status ON tb_events(created_by, status);

-- -------------------------------------------------------
-- tb_users
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_users_created_at ON tb_users(created_at);
CREATE INDEX IF NOT EXISTS idx_tb_users_is_active ON tb_users(is_active) WHERE is_active = TRUE;

-- -------------------------------------------------------
-- tb_notifications
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_notifications_user_id ON tb_notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_tb_notifications_event_id ON tb_notifications(event_id);
CREATE INDEX IF NOT EXISTS idx_tb_notifications_user_id_is_read ON tb_notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_tb_notifications_user_id_created_at ON tb_notifications(user_id, created_at DESC);

-- -------------------------------------------------------
-- tb_event_votes
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_event_votes_event_id ON tb_event_votes(event_id);
CREATE INDEX IF NOT EXISTS idx_tb_event_votes_event_id_type ON tb_event_votes(event_id, type);
CREATE INDEX IF NOT EXISTS idx_tb_event_votes_user_id_event_id ON tb_event_votes(user_id, event_id);

-- -------------------------------------------------------
-- tb_reports
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_reports_event_id ON tb_reports(event_id);
CREATE INDEX IF NOT EXISTS idx_tb_reports_reported_by ON tb_reports(reported_by);

-- -------------------------------------------------------
-- tb_categories
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_categories_group_name ON tb_categories(group_name);
CREATE INDEX IF NOT EXISTS idx_tb_categories_created_at ON tb_categories(created_at);

-- -------------------------------------------------------
-- tb_newsletter_subscriptions
-- -------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tb_newsletter_subs_created_at ON tb_newsletter_subscriptions(created_at);
