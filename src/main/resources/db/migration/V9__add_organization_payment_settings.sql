-- Betaalgegevens waren platformbreed hardcoded (bunq.me-username in config,
-- IBAN + tenaamstelling letterlijk in job-code) in plaats van per organisatie.
-- Elke organisatie krijgt nu haar eigen instelbare betaalidentiteit.

ALTER TABLE public.organizations ADD COLUMN bunq_me_username varchar(80);
ALTER TABLE public.organizations ADD COLUMN iban varchar(34);
ALTER TABLE public.organizations ADD COLUMN account_holder_name varchar(120);

-- Backfill: behoud het huidige (tot nu toe hardcoded) gedrag voor de
-- bestaande live tenant, zodat dit deploy geen functionele wijziging
-- geeft voor de huidige klant.
UPDATE public.organizations
SET bunq_me_username = 'MaximStaal',
    iban = 'NL94INGB0660851083',
    account_holder_name = 'M. Staal'
WHERE slug = 'casacrew';
