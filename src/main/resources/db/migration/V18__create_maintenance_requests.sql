-- Onderhoudsmeldingen: studenten en schoonmakers melden kapotte/defecte
-- dingen in het pand aan de beheerder. Los van klachten (die gaan over
-- gedrag van mensen) en los van voorraadmeldingen (die gaan over
-- schoonmaakmiddelen op).

CREATE TABLE public.maintenance_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    reported_by_id BIGINT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    location VARCHAR(100),
    urgency VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    admin_note VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT maintenance_requests_urgency_check CHECK (urgency IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT maintenance_requests_status_check CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED'))
);

CREATE INDEX idx_maintenance_requests_organization ON public.maintenance_requests(organization_id);
CREATE INDEX idx_maintenance_requests_reported_by ON public.maintenance_requests(reported_by_id);
