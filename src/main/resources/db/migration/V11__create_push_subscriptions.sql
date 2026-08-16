-- Pushmeldingen (web + native) bestonden nog niet. Elke ingelogde gebruiker
-- kan straks één of meer subscriptions registreren (bv. meerdere browsers/
-- toestellen); platform bepaalt welke kolommen gevuld zijn.

CREATE TABLE public.push_subscriptions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    platform VARCHAR(10) NOT NULL,
    endpoint TEXT,
    p256dh VARCHAR(255),
    auth VARCHAR(255),
    fcm_token VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_push_subscriptions_user ON public.push_subscriptions(user_id);
CREATE INDEX idx_push_subscriptions_organization ON public.push_subscriptions(organization_id);
