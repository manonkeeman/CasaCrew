-- Elke org-gescoped query filtert straks op organization_id (vaak samen
-- met een andere kolom), dus die kolom verdient een index op alle 13
-- tabellen.

CREATE INDEX idx_announcements_organization ON public.announcements USING btree (organization_id);
CREATE INDEX idx_cleaning_tasks_organization ON public.cleaning_tasks USING btree (organization_id);
CREATE INDEX idx_documents_organization ON public.documents USING btree (organization_id);
CREATE INDEX idx_email_templates_organization ON public.email_templates USING btree (organization_id);
CREATE INDEX idx_huisregels_organization ON public.huisregels USING btree (organization_id);
CREATE INDEX idx_invoices_organization ON public.invoices USING btree (organization_id);
CREATE INDEX idx_password_reset_tokens_organization ON public.password_reset_tokens USING btree (organization_id);
CREATE INDEX idx_payments_organization ON public.payments USING btree (organization_id);
CREATE INDEX idx_rooms_organization ON public.rooms USING btree (organization_id);
CREATE INDEX idx_shifts_organization ON public.shifts USING btree (organization_id);
CREATE INDEX idx_supply_reports_organization ON public.supply_reports USING btree (organization_id);
CREATE INDEX idx_task_photos_organization ON public.task_photos USING btree (organization_id);
CREATE INDEX idx_users_organization ON public.users USING btree (organization_id);
