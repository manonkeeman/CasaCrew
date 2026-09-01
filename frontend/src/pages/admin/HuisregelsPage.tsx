import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Huisregel } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

export function HuisregelsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const rulesQuery = useQuery({
    queryKey: ['huisregels'],
    queryFn: () => api.get<Huisregel[]>('/api/huisregels'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { title: string; content?: string; orderIndex: number }) =>
      api.post('/api/huisregels', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['huisregels'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Record<string, unknown> }) =>
      api.put(`/api/huisregels/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['huisregels'] });
      setEditingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/huisregels/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['huisregels'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  const rules = rulesQuery.data ?? [];

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      title: String(form.get('title')),
      content: String(form.get('content') || '') || undefined,
      orderIndex: rules.length,
    });
  }

  function handleUpdate(e: FormEvent<HTMLFormElement>, rule: Huisregel) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    updateMutation.mutate({
      id: rule.id,
      body: {
        title: String(form.get('title')),
        content: String(form.get('content') || ''),
      },
    });
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Huisregels</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe regel'}</Button>
      </div>

      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <Card title="Nieuwe huisregel">
          <form onSubmit={handleCreate}>
            <Field label="Titel">
              <Input name="title" required />
            </Field>
            <Field label="Inhoud (optioneel)">
              <Textarea name="content" rows={3} />
            </Field>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Bezig...' : 'Regel toevoegen'}
            </Button>
          </form>
        </Card>
      )}

      <div className="mt-6 space-y-4">
        {rules.map((rule) => (
          <Card key={rule.id}>
            {editingId === rule.id ? (
              <form onSubmit={(e) => handleUpdate(e, rule)}>
                <Field label="Titel">
                  <Input name="title" defaultValue={rule.title} required />
                </Field>
                <Field label="Inhoud">
                  <Textarea name="content" rows={3} defaultValue={rule.content ?? ''} />
                </Field>
                <div className="space-x-2">
                  <Button type="submit" disabled={updateMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <div>
                <div className="flex items-center justify-between">
                  <h3 className="font-semibold text-slate-800">{rule.title}</h3>
                  <div className="space-x-3">
                    <button className="text-xs font-medium text-emerald-700 hover:underline" onClick={() => setEditingId(rule.id)}>
                      Bewerken
                    </button>
                    <button
                      className="text-xs font-medium text-red-600 hover:underline"
                      onClick={() => {
                        if (confirm(`Huisregel "${rule.title}" verwijderen?`)) deleteMutation.mutate(rule.id);
                      }}
                    >
                      Verwijderen
                    </button>
                  </div>
                </div>
                {rule.content && <p className="mt-2 whitespace-pre-wrap text-sm text-slate-600">{rule.content}</p>}
              </div>
            )}
          </Card>
        ))}
        {rules.length === 0 && <p className="text-sm text-slate-400">Nog geen huisregels.</p>}
      </div>
    </div>
  );
}
