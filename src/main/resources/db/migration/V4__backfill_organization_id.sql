-- Backfill: alle bestaande data hoort bij precies één organisatie
-- ("CasaCrew", de enige tenant op het moment van deze migratie).
-- Nieuwe organisaties die hierna zelfregistreren krijgen hun eigen rij en
-- worden nooit door deze migratie geraakt (die draait immers maar één keer).

INSERT INTO public.organizations (name, slug, created_at)
SELECT 'CasaCrew', 'casacrew', now()
WHERE NOT EXISTS (SELECT 1 FROM public.organizations WHERE slug = 'casacrew');

UPDATE public.announcements SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.cleaning_tasks SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.documents SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.email_templates SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.huisregels SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.invoices SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.password_reset_tokens SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.payments SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.rooms SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.shifts SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.supply_reports SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.task_photos SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
UPDATE public.users SET organization_id = (SELECT id FROM public.organizations WHERE slug = 'casacrew') WHERE organization_id IS NULL;
