import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Complaint, ComplaintStatus, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

const STATUS_LABEL: Record<ComplaintStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In behandeling',
  RESOLVED: 'Afgehandeld',
};

const STATUS_STYLE: Record<ComplaintStatus, string> = {
  OPEN: 'bg-amber-50 text-amber-700',
  IN_PROGRESS: 'bg-sky-50 text-sky-700',
  RESOLVED: 'bg-emerald-50 text-emerald-700',
};

export function ComplaintsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingPolicy, setEditingPolicy] = useState(false);
  const [handlingId, setHandlingId] = useState<number | null>(null);

  const complaintsQuery = useQuery({
    queryKey: ['complaints'],
    queryFn: () => api.get<Complaint[]>('/api/complaints'),
  });
  const policyQuery = useQuery({
    queryKey: ['complaints-policy'],
    queryFn: () => api.get<{ policy: string }>('/api/complaints/policy'),
  });
  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT');

  const createMutation = useMutation({
    mutationFn: (body: { subject: string; description: string; targetUserId: number }) =>
      api.post('/api/complaints', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['complaints'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Indienen mislukt.'),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status, response }: { id: number; status: ComplaintStatus; response: string }) =>
      api.put(`/api/complaints/${id}/status`, { status, response }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['complaints'] });
      setHandlingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const policyMutation = useMutation({
    mutationFn: (policy: string) => api.put('/api/complaints/policy', { policy }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['complaints-policy'] });
      setEditingPolicy(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    const targetUserId = Number(form.get('targetUserId'));
    if (!targetUserId) {
      setError('Kies een student.');
      return;
    }
    createMutation.mutate({
      subject: String(form.get('subject') || ''),
      description: String(form.get('description') || ''),
      targetUserId,
    });
  }

  function handleSavePolicy(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    policyMutation.mutate(String(form.get('policy') || ''));
  }

  function handleUpdateStatus(e: FormEvent<HTMLFormElement>, id: number) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    statusMutation.mutate({
      id,
      status: form.get('status') as ComplaintStatus,
      response: String(form.get('response') || ''),
    });
  }

  const complaints = complaintsQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-stone-800">Klachten</h1>
      {error && <Banner kind="error" message={error} />}

      <Card
        title="Klachtenpolicy"
        actions={
          <Button variant="secondary" onClick={() => setEditingPolicy((v) => !v)}>
            {editingPolicy ? 'Annuleren' : 'Bewerken'}
          </Button>
        }
      >
        {editingPolicy ? (
          <form onSubmit={handleSavePolicy}>
            <Field label="Policy">
              <Textarea name="policy" rows={4} defaultValue={policyQuery.data?.policy ?? ''} />
            </Field>
            <Button type="submit" disabled={policyMutation.isPending}>
              {policyMutation.isPending ? 'Bezig...' : 'Opslaan'}
            </Button>
          </form>
        ) : (
          <p className="whitespace-pre-wrap text-sm text-stone-600">
            {policyQuery.data?.policy || 'Nog geen policy ingesteld.'}
          </p>
        )}
      </Card>

      <div className="mb-6 mt-6 flex items-center justify-between">
        <h2 className="text-lg font-semibold text-stone-800">Alle klachten</h2>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Klacht indienen bij student'}</Button>
      </div>

      {showForm && (
        <Card title="Klacht indienen bij een student" actions={undefined}>
          <form onSubmit={handleCreate}>
            <Field label="Student">
              <select name="targetUserId" required className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                <option value="">Kies een student...</option>
                {students.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.username} ({s.email})
                  </option>
                ))}
              </select>
            </Field>
            <Field label="Onderwerp">
              <Input name="subject" required />
            </Field>
            <Field label="Omschrijving">
              <Textarea name="description" rows={3} required />
            </Field>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Bezig...' : 'Indienen'}
            </Button>
          </form>
        </Card>
      )}

      <div className="space-y-4">
        {complaints.map((c) => (
          <Card key={c.id}>
            <div className="mb-2 flex items-start justify-between gap-4">
              <div>
                <span className="mr-2 rounded-full bg-stone-100 px-2 py-0.5 text-xs font-medium text-stone-600">
                  {c.direction === 'STUDENT_TO_ADMIN' ? `Van ${c.authorUsername} naar beheerder` : `Van beheerder naar ${c.targetUsername}`}
                </span>
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[c.status]}`}>
                  {STATUS_LABEL[c.status]}
                </span>
              </div>
              <span className="shrink-0 text-xs text-stone-400">{new Date(c.createdAt).toLocaleDateString('nl-NL')}</span>
            </div>
            <h3 className="font-semibold text-stone-800">{c.subject}</h3>
            <p className="mt-1 whitespace-pre-wrap text-sm text-stone-600">{c.description}</p>
            {c.response && (
              <p className="mt-2 rounded-lg bg-stone-50 p-2 text-xs text-stone-600">
                <span className="font-medium">Reactie:</span> {c.response}
              </p>
            )}

            {handlingId === c.id ? (
              <form onSubmit={(e) => handleUpdateStatus(e, c.id)} className="mt-3">
                <Field label="Status">
                  <select name="status" defaultValue={c.status} className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                    <option value="OPEN">Open</option>
                    <option value="IN_PROGRESS">In behandeling</option>
                    <option value="RESOLVED">Afgehandeld</option>
                  </select>
                </Field>
                <Field label="Reactie (optioneel)">
                  <Textarea name="response" rows={2} defaultValue={c.response ?? ''} />
                </Field>
                <div className="space-x-2">
                  <Button type="submit" disabled={statusMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setHandlingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <button className="mt-3 text-xs font-medium text-emerald-700 hover:underline" onClick={() => setHandlingId(c.id)}>
                Status bijwerken
              </button>
            )}
          </Card>
        ))}
        {complaints.length === 0 && <p className="text-sm text-stone-400">Nog geen klachten.</p>}
      </div>
    </div>
  );
}
