-- Huis-adres en huurregels (bedrag, wanneer facturen ontstaan, wanneer ze
-- vervallen) waren nergens instelbaar -- adres bestond niet, het bedrag was
-- één globale app.rent.amount-property en de dag van de maand stond vast
-- (1e van de maand aanmaken, altijd +7 dagen vervaldatum) in job-code.
-- Elke organisatie krijgt nu haar eigen instelbare huisprofiel en huurregels.

ALTER TABLE public.organizations ADD COLUMN address varchar(255);
ALTER TABLE public.organizations ADD COLUMN default_rent_amount numeric(10,2);
ALTER TABLE public.organizations ADD COLUMN rent_invoice_day_of_month integer;
ALTER TABLE public.organizations ADD COLUMN rent_due_day_of_month integer;

-- Backfill: behoud het huidige (tot nu toe hardcoded) gedrag voor de
-- bestaande live tenant.
UPDATE public.organizations
SET rent_invoice_day_of_month = 1,
    rent_due_day_of_month = 8
WHERE slug = 'casacrew';
