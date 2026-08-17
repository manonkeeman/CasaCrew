-- Een melding (schoonmaakster -> admin) kan optioneel een specifieke
-- student/kamer betreffen, zodat die student bij een statuswijziging via
-- WhatsApp op de hoogte gehouden kan worden. Blijft NULL voor meldingen
-- die niet aan één student gekoppeld zijn (bv. algemene voorraadtekorten).

ALTER TABLE public.supply_reports ADD COLUMN student_id BIGINT REFERENCES public.users(id) ON DELETE SET NULL;

CREATE INDEX idx_supply_reports_student ON public.supply_reports(student_id);
