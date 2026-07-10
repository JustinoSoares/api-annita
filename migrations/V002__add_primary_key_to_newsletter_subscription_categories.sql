-- Adicionar chave primária à tabela de associação de categorias da newsletter
-- A tabela existente (do @ManyToMany) tem apenas subscription_id e category_id
-- Precisamos adicionar a coluna id UUID sem perder dados existentes

ALTER TABLE tb_newsletter_subscription_categories ADD COLUMN IF NOT EXISTS id UUID DEFAULT gen_random_uuid();

UPDATE tb_newsletter_subscription_categories SET id = gen_random_uuid() WHERE id IS NULL;

ALTER TABLE tb_newsletter_subscription_categories ALTER COLUMN id SET NOT NULL;

ALTER TABLE tb_newsletter_subscription_categories DROP CONSTRAINT IF EXISTS tb_newsletter_subscription_categories_pkey;
ALTER TABLE tb_newsletter_subscription_categories ADD PRIMARY KEY (id);

CREATE INDEX IF NOT EXISTS idx_nsc_subscription_id ON tb_newsletter_subscription_categories(subscription_id);
CREATE INDEX IF NOT EXISTS idx_nsc_category_id ON tb_newsletter_subscription_categories(category_id);
