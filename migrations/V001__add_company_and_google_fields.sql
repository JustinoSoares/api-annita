-- Adicionar campos de empresa à tabela de utilizadores
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS company_name VARCHAR(200);
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS company_nif VARCHAR(20);
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS company_phone VARCHAR(20);
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS company_address VARCHAR(300);
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS company_website VARCHAR(200);

-- Adicionar campo de Google ID à tabela de utilizadores
ALTER TABLE tb_users ADD COLUMN IF NOT EXISTS google_id VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS idx_tb_users_google_id ON tb_users(google_id) WHERE google_id IS NOT NULL;
