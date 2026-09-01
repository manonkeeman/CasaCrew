import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { MaintenanceRequest, MaintenanceStatus } from '../../lib/types';
import { Banner, Button, Card, Field, Textarea } from '../../components/ui';
import { MAINTENANCE_STATUS_LABEL, MAINTENANCE_STATUS_STYLE, URGENCY_LABEL, URGENCY_STYLE } from '../../lib/maintenanceLabels';

export function MaintenanceRequestsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [handlingId, setHandlingId] = useState<number | null>(null);

  const requestsQuery = useQuery({
    queryKey: ['maintenance-requests'],
    queryFn: () => api.get<MaintenanceRequest[]>('/api/maintenance-requests'),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status, adminNote }: { id: number; status: MaintenanceStatus; adminNote: string }) =>
      api.put(`/api/maintenance-requests/${id}/status`, { status, adminNote }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['maintenance-requests'] });
      setHandlingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  function handleUpdate(e: FormEvent<HTMLFormElement>, id: number) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    statusMutation.mutate({
      id,
      status: form.get('status') as MaintenanceStatus,
      adminNote: String(form.get('adminNote') || ''),
    });
  }

  const requests = requestsQuery.data ?? [];
  const openCount = requests.filter((r) => r.status !== 'RESOLVED').length;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Onderhoudsmeldingen</h1>
        <span className="text-sm text-stone-500">{openCount} open</span>
      </div>
      {error && <Banner kind="error" message={error} />}

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
            <p className="mt-2 text-xs text-stone-400">Gemeld door {r.reportedByUsername}</p>
            {r.adminNote && (
              <p className="mt-2 rounded-lg bg-stone-50 p-2 text-xs text-stone-600">
                <span className="font-medium">Notitie:</span> {r.adminNote}
              </p>
            )}

            {handlingId === r.id ? (
              <form onSubmit={(e) => handleUpdate(e, r.id)} className="mt-3">
                <Field label="Status">
                  <select name="status" defaultValue={r.status} className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                    <option value="OPEN">Open</option>
                    <option value="IN_PROGRESS">In behandeling</option>
                    <option value="RESOLVED">Opgelost</option>
                  </select>
                </Field>
                <Field label="Notitie (optioneel)">
                  <Textarea name="adminNote" rows={2} defaultValue={r.adminNote ?? ''} />
                </Field>
                <div className="space-x-2">
                  <Button type="submit" disabled={statusMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setHandlingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <button className="mt-3 text-xs font-medium text-emerald-700 hover:underline" onClick={() => setHandlingId(r.id)}>
                Status bijwerken
              </button>
            )}
          </Card>
        ))}
        {requests.length === 0 && <p className="text-sm text-stone-400">Nog geen onderhoudsmeldingen.</p>}
      </div>
    </div>
  );
}
