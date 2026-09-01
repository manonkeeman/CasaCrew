import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Announcement } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

const TYPE_LABEL: Record<string, string> = {
  mededeling: 'Mededeling',
  onderhoud: 'Onderhoud',
  evenement: 'Evenement',
};

export function AnnouncementsAdminPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const announcementsQuery = useQuery({
    queryKey: ['announcements'],
    queryFn: () => api.get<Announcement[]>('/api/announcements'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { type: string; title: string; body: string; author?: string }) =>
      api.post('/api/announcements', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['announcements'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/announcements/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['announcements'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      type: String(form.get('type')),
      title: String(form.get('title')),
      body: String(form.get('body')),
      author: String(form.get('author') || '') || undefined,
    });
  }

  const announcements = announcementsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Aankondigingen</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe aankondiging'}</Button>
      </div>

      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <Card title="Nieuwe aankondiging">
          <form onSubmit={handleCreate}>
            <Field label="Type">
              <select name="type" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm" defaultValue="mededeling">
                <option value="mededeling">Mededeling</option>
                <option value="onderhoud">Onderhoud</option>
                <option value="evenement">Evenement</option>
              </select>
            </Field>
            <Field label="Titel">
              <Input name="title" required maxLength={120} />
            </Field>
            <Field label="Tekst">
              <Textarea name="body" rows={4} required maxLength={2000} />
            </Field>
            <Field label="Auteur (optioneel)">
              <Input name="author" maxLength={80} placeholder="Beheerder" />
            </Field>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Bezig...' : 'Plaatsen'}
            </Button>
          </form>
        </Card>
      )}

      <div className="mt-6 space-y-4">
        {announcements.map((a) => (
          <Card key={a.id}>
            <div className="mb-1 flex items-center justify-between">
              <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
                {TYPE_LABEL[a.type] ?? a.type}
              </span>
              <div className="flex items-center gap-3">
                <span className="text-xs text-slate-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
                <button
                  className="text-xs font-medium text-red-600 hover:underline"
                  onClick={() => {
                    if (confirm(`Aankondiging "${a.title}" verwijderen?`)) deleteMutation.mutate(a.id);
                  }}
                >
                  Verwijderen
                </button>
              </div>
            </div>
            <h3 className="font-semibold text-slate-800">{a.title}</h3>
            <p className="mt-1 whitespace-pre-wrap text-sm text-slate-600">{a.body}</p>
            {a.author && <p className="mt-2 text-xs text-slate-400">— {a.author}</p>}
          </Card>
        ))}
        {announcements.length === 0 && <p className="text-sm text-slate-400">Nog geen aankondigingen.</p>}
      </div>
    </div>
  );
}
