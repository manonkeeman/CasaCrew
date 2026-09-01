import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import type { Complaint, ComplaintStatus } from '../../lib/types';
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
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const complaintsQuery = useQuery({
    queryKey: ['my-complaints'],
    queryFn: () => api.get<Complaint[]>('/api/complaints/me'),
  });
  const policyQuery = useQuery({
    queryKey: ['complaints-policy'],
    queryFn: () => api.get<{ policy: string }>('/api/complaints/policy'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { subject: string; description: string }) => api.post('/api/complaints', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-complaints'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Indienen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      subject: String(form.get('subject') || ''),
      description: String(form.get('description') || ''),
    });
  }

  const complaints = complaintsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Klachten</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Klacht indienen'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {policyQuery.data?.policy && (
        <Card title="Klachtenpolicy">
          <p className="whitespace-pre-wrap text-sm text-stone-600">{policyQuery.data.policy}</p>
        </Card>
      )}

      {showForm && (
        <div className="mt-6">
          <Card title="Nieuwe klacht naar de beheerder">
            <form onSubmit={handleCreate}>
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
        </div>
      )}

      <div className="mt-6 space-y-4">
        {complaints.map((c) => {
          const isMine = c.authorUsername === user?.username;
          return (
            <Card key={c.id}>
              <div className="mb-2 flex items-start justify-between gap-4">
                <div>
                  <span className="mr-2 rounded-full bg-stone-100 px-2 py-0.5 text-xs font-medium text-stone-600">
                    {isMine ? 'Jouw klacht naar de beheerder' : 'Klacht van de beheerder'}
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
            </Card>
          );
        })}
        {complaints.length === 0 && <p className="text-sm text-stone-400">Nog geen klachten.</p>}
      </div>
    </div>
  );
}
