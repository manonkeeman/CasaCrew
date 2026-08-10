-- Deze drie constraints waren platformbreed uniek, wat niet meer klopt
-- zodra er meerdere organisaties zijn: elke organisatie wil zelf een
-- "Admin"-username kunnen hebben, een "Kamer 1", en zijn eigen set
-- e-mailtemplates per type. users.email en documents.storage_path
-- blijven bewust ongewijzigd (zie V1-plan: email blijft platformbreed
-- uniek, storage_path heeft een UUID-prefix en verwaarloosbaar
-- botsingsrisico).

ALTER TABLE public.users DROP CONSTRAINT uk_user_username;
ALTER TABLE public.users ADD CONSTRAINT uk_user_organization_username UNIQUE (organization_id, username);

ALTER TABLE public.rooms DROP CONSTRAINT rooms_name_key;
ALTER TABLE public.rooms ADD CONSTRAINT uk_room_organization_name UNIQUE (organization_id, name);

ALTER TABLE public.email_templates DROP CONSTRAINT email_templates_type_key;
ALTER TABLE public.email_templates ADD CONSTRAINT uk_email_template_organization_type UNIQUE (organization_id, type);
