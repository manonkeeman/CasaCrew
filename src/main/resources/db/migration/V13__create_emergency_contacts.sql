-- Noodgegevens: vaste lijst van noodcontacten per organisatie (bv. huisarts,
-- politie, beheerder). De admin kan alleen het telefoonnummer bewerken --
-- de labels/categorieën liggen vast en worden geseed door
-- EmergencyContactService (net als de e-mailtemplates in V11/V12).

CREATE TABLE public.emergency_contacts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL REFERENCES public.organizations(id) ON DELETE CASCADE,
    label VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    order_index INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_emergency_contacts_organization ON public.emergency_contacts(organization_id);
