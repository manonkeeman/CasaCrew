-- Afvalschema: vaste afvalcategorieën per organisatie (restafval, GFT+E,
-- PMD, papier) waarvan de admin de eigen ophaaldag/frequentie invult --
-- zelfde patroon als emergency_contacts (V13): vaste labels, aanpasbare
-- waarde. De exacte ophaaldagen zijn adresafhankelijk (RMN/gemeente), dus
-- worden bewust NIET met verzonnen data geseed. Plus een organisatie-brede
-- policy over het opruimen van statiegeldflessen/-blikjes.

ALTER TABLE public.organizations ADD COLUMN deposit_return_policy TEXT;

CREATE TABLE public.waste_schedule_entries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    waste_type VARCHAR(100) NOT NULL,
    schedule_info VARCHAR(255),
    order_index INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_waste_schedule_organization ON public.waste_schedule_entries(organization_id);
