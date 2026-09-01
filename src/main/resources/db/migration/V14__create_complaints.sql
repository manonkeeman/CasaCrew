-- Klachten: student -> beheerder (algemeen, geen specifieke target) en
-- beheerder -> student (target = die student). Plus een organisatie-brede,
-- door de admin aanpasbare klachtenpolicy.

ALTER TABLE public.organizations ADD COLUMN complaints_policy TEXT;

CREATE TABLE public.complaints (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    target_id BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    direction VARCHAR(20) NOT NULL,
    subject VARCHAR(150) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    response VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    CONSTRAINT complaints_direction_check CHECK (direction IN ('STUDENT_TO_ADMIN', 'ADMIN_TO_STUDENT')),
    CONSTRAINT complaints_status_check CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED'))
);

CREATE INDEX idx_complaints_organization ON public.complaints(organization_id);
CREATE INDEX idx_complaints_author ON public.complaints(author_id);
CREATE INDEX idx_complaints_target ON public.complaints(target_id);
