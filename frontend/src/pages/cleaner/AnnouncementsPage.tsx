import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Announcement } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';
import { ANNOUNCEMENT_TYPE_ICON, ANNOUNCEMENT_TYPE_LABEL } from '../../lib/announcementTypes';
import { MegaphoneIcon } from '../../components/icons';

export function CleanerAnnouncementsPage() {
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
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Plaatsen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      type: String(form.get('type')),
      title: String(form.get('title')),
      body: String(form.get('body')),
      author: 'Schoonmaak',
    });
  }

  const announcements = announcementsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Mededelingen</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Mededeling plaatsen'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <div className="mb-6">
          <Card title="Nieuwe mededeling">
            <form onSubmit={handleCreate}>
              <Field label="Type">
                <select name="type" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm" defaultValue="onderhoud">
                  <option value="mededeling">Mededeling</option>
                  <option value="onderhoud">Onderhoud</option>
                  <option value="evenement">Evenement</option>
                </select>
              </Field>
              <Field label="Titel">
                <Input name="title" required maxLength={120} placeholder="bv. Vloer net gedweild" />
              </Field>
              <Field label="Tekst">
                <Textarea name="body" rows={3} required maxLength={2000} />
              </Field>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Plaatsen'}
              </Button>
            </form>
          </Card>
        </div>
      )}

      <div className="space-y-4">
        {announcements.map((a) => {
          const Icon = ANNOUNCEMENT_TYPE_ICON[a.type] ?? MegaphoneIcon;
          return (
            <Card key={a.id}>
              <div className="flex items-start gap-3">
                <span className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                  <Icon className="h-4.5 w-4.5" />
                </span>
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex items-center justify-between gap-2">
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
                      {ANNOUNCEMENT_TYPE_LABEL[a.type] ?? a.type}
                    </span>
                    <span className="shrink-0 text-xs text-stone-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
                  </div>
                  <h3 className="font-semibold text-stone-800">{a.title}</h3>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-stone-600">{a.body}</p>
                  {a.author && <p className="mt-2 text-xs text-stone-400">— {a.author}</p>}
                </div>
              </div>
            </Card>
          );
        })}
        {announcements.length === 0 && <p className="text-sm text-stone-400">Nog geen mededelingen.</p>}
      </div>
    </div>
  );
}
