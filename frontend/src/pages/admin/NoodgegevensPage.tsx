import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import { telHref } from '../../lib/phone';
import type { EmergencyContact } from '../../lib/types';
import { Banner, Button, Card, Field, Input } from '../../components/ui';

export function NoodgegevensPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);

  const contactsQuery = useQuery({
    queryKey: ['emergency-contacts'],
    queryFn: () => api.get<EmergencyContact[]>('/api/emergency-contacts'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, phoneNumber }: { id: number; phoneNumber: string }) =>
      api.put(`/api/emergency-contacts/${id}`, { phoneNumber }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emergency-contacts'] });
      setEditingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  function handleUpdate(e: FormEvent<HTMLFormElement>, contact: EmergencyContact) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    updateMutation.mutate({ id: contact.id, phoneNumber: String(form.get('phoneNumber') || '') });
  }

  const contacts = contactsQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Noodgegevens</h1>
      <p className="mb-6 text-sm text-slate-500">
        Vaste lijst met noodcontacten voor het huis. Je kunt hier alleen de telefoonnummers invullen of aanpassen.
      </p>

      {error && <Banner kind="error" message={error} />}

      <div className="space-y-4">
        {contacts.map((contact) => (
          <Card key={contact.id}>
            {editingId === contact.id ? (
              <form onSubmit={(e) => handleUpdate(e, contact)}>
                <h3 className="mb-2 font-semibold text-slate-800">{contact.label}</h3>
                <Field label="Telefoonnummer">
                  <Input name="phoneNumber" defaultValue={contact.phoneNumber ?? ''} autoFocus />
                </Field>
                <div className="space-x-2">
                  <Button type="submit" disabled={updateMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-slate-800">{contact.label}</h3>
                  <p className="mt-1 text-sm text-slate-600">
                    {contact.phoneNumber || <span className="text-slate-400">Nog geen nummer ingevuld</span>}
                  </p>
                </div>
                <div className="flex shrink-0 items-center gap-3">
                  {contact.phoneNumber && (
                    <a
                      href={telHref(contact.phoneNumber)}
                      className="rounded-lg bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700 hover:bg-emerald-100"
                    >
                      Bel
                    </a>
                  )}
                  <button
                    className="text-xs font-medium text-emerald-700 hover:underline"
                    onClick={() => setEditingId(contact.id)}
                  >
                    Bewerken
                  </button>
                </div>
              </div>
            )}
          </Card>
        ))}
        {contacts.length === 0 && <p className="text-sm text-slate-400">Nog geen noodgegevens beschikbaar.</p>}
      </div>
    </div>
  );
}
