-- Gedeelde agenda: huisfeesten, afspraken (monteur, keuring, etc.) die
-- voor het hele huis relevant zijn. Elke rol kan een event toevoegen; een
-- event is verwijderbaar door de maker of door de beheerder.

CREATE TABLE public.calendar_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    event_date DATE NOT NULL,
    event_time TIME,
    created_by_id BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_calendar_events_organization ON public.calendar_events(organization_id);
CREATE INDEX idx_calendar_events_date ON public.calendar_events(event_date);
