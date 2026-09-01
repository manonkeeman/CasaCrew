-- Contractverloop-tracking voor de beheerder: einddatum van de
-- huurovereenkomst per student, zodat er tijdig overzicht is op
-- aflopende contracten.

ALTER TABLE public.users ADD COLUMN lease_end_date DATE;
