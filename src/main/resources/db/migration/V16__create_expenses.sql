-- Eenvoudige kosten/uitgaven-tracking voor de beheerder (onderhoud,
-- schoonmaakmiddelen, reparaties, etc.), los van de facturen aan
-- studenten. Geen dubbel boekhouden of BTW-afhandeling -- dat blijft bij
-- een echt boekhoudpakket; deze tabel is bedoeld om snel bij te houden
-- wat er is uitgegeven en dat als CSV te exporteren voor de boekhouder.

CREATE TABLE public.expenses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    category VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    created_by_id BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT expenses_category_check CHECK (category IN
        ('ONDERHOUD', 'SCHOONMAAK', 'REPARATIE', 'INVENTARIS', 'NUTSVOORZIENINGEN', 'OVERIG')),
    CONSTRAINT expenses_amount_check CHECK (amount >= 0)
);

CREATE INDEX idx_expenses_organization ON public.expenses(organization_id);
CREATE INDEX idx_expenses_date ON public.expenses(expense_date);
