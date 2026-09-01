import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { MaintenanceRequest } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';
import { MAINTENANCE_STATUS_LABEL, MAINTENANCE_STATUS_STYLE, URGENCY_LABEL, URGENCY_STYLE } from '../../lib/maintenanceLabels';

export function MaintenanceReportPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const requestsQuery = useQuery({
    queryKey: ['my-maintenance-requests'],
    queryFn: () => api.get<MaintenanceRequest[]>('/api/maintenance-requests/me'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { title: string; description: string; location?: string; urgency: string }) =>
      api.post('/api/maintenance-requests', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-maintenance-requests'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Melden mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      title: String(form.get('title') || ''),
      description: String(form.get('description') || ''),
      location: String(form.get('location') || '') || undefined,
      urgency: String(form.get('urgency') || 'MEDIUM'),
    });
  }

  const requests = requestsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Onderhoud</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Iets melden'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <div className="mb-6">
          <Card title="Iets kapot of defect melden">
            <form onSubmit={handleCreate}>
              <Field label="Titel">
                <Input name="title" required maxLength={150} placeholder="bv. Kraan badkamer lekt" />
              </Field>
              <Field label="Omschrijving">
                <Textarea name="description" rows={3} required />
              </Field>
              <Field label="Locatie (optioneel)">
                <Input name="location" maxLength={100} placeholder="bv. Badkamer boven" />
              </Field>
              <Field label="Urgentie">
                <select name="urgency" defaultValue="MEDIUM" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                  <option value="LOW">Laag</option>
                  <option value="MEDIUM">Gemiddeld</option>
                  <option value="HIGH">Hoog</option>
                </select>
              </Field>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Melden'}
              </Button>
            </form>
          </Card>
        </div>
      )}

      <div className="space-y-4">
        {requests.map((r) => (
          <Card key={r.id}>
            <div className="mb-2 flex items-start justify-between gap-4">
              <div className="space-x-2">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${URGENCY_STYLE[r.urgency]}`}>
                  {URGENCY_LABEL[r.urgency]}
                </span>
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${MAINTENANCE_STATUS_STYLE[r.status]}`}>
                  {MAINTENANCE_STATUS_LABEL[r.status]}
                </span>
                {r.location && (
                  <span className="rounded-full bg-stone-100 px-2 py-0.5 text-xs font-medium text-stone-600">{r.location}</span>
                )}
              </div>
              <span className="shrink-0 text-xs text-stone-400">{new Date(r.createdAt).toLocaleDateString('nl-NL')}</span>
            </div>
            <h3 className="font-semibold text-stone-800">{r.title}</h3>
            <p className="mt-1 whitespace-pre-wrap text-sm text-stone-600">{r.description}</p>
            {r.adminNote && (
              <p className="mt-2 rounded-lg bg-stone-50 p-2 text-xs text-stone-600">
                <span className="font-medium">Reactie beheerder:</span> {r.adminNote}
              </p>
            )}
          </Card>
        ))}
        {requests.length === 0 && <p className="text-sm text-stone-400">Nog geen meldingen.</p>}
      </div>
    </div>
  );
}
