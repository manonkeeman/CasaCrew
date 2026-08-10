-- organization_id toegevoegd als nullable op elke tabel (inclusief users
-- zelf, en de nieuwe password_reset_tokens-tabel) zodat elke rij zijn
-- eigen tenant-kolom draagt zonder join. Nullable in deze stap zodat de
-- backfill-migratie (V4) de kolom kan vullen voordat NOT NULL + FK
-- afgedwongen worden (V5).

ALTER TABLE public.announcements ADD COLUMN organization_id bigint;
ALTER TABLE public.cleaning_tasks ADD COLUMN organization_id bigint;
ALTER TABLE public.documents ADD COLUMN organization_id bigint;
ALTER TABLE public.email_templates ADD COLUMN organization_id bigint;
ALTER TABLE public.huisregels ADD COLUMN organization_id bigint;
ALTER TABLE public.invoices ADD COLUMN organization_id bigint;
ALTER TABLE public.password_reset_tokens ADD COLUMN organization_id bigint;
ALTER TABLE public.payments ADD COLUMN organization_id bigint;
ALTER TABLE public.rooms ADD COLUMN organization_id bigint;
ALTER TABLE public.shifts ADD COLUMN organization_id bigint;
ALTER TABLE public.supply_reports ADD COLUMN organization_id bigint;
ALTER TABLE public.task_photos ADD COLUMN organization_id bigint;
ALTER TABLE public.users ADD COLUMN organization_id bigint;
