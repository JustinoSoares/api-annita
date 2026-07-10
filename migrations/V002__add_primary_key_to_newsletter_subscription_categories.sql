-- Adicionar chave primária à tabela de associação de categorias da newsletter
-- Remove a tabela antiga (sem PK) e recria com PK própria

DROP TABLE IF EXISTS tb_newsletter_subscription_categories;

CREATE TABLE tb_newsletter_subscription_categories (
    id UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES tb_newsletter_subscriptions(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES tb_categories(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_nsc_subscription_id ON tb_newsletter_subscription_categories(subscription_id);
CREATE INDEX IF NOT EXISTS idx_nsc_category_id ON tb_newsletter_subscription_categories(category_id);
