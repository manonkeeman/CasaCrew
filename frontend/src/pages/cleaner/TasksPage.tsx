import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { CleaningTask } from '../../lib/types';
import { Banner, Card, Textarea, Button } from '../../components/ui';
import { TaskPhotosSection } from '../../components/TaskPhotos';

export function CleanerTasksPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editingIncidentId, setEditingIncidentId] = useState<number | null>(null);

  const tasksQuery = useQuery({
    queryKey: ['my-cleaning-tasks'],
    queryFn: () => api.get<CleaningTask[]>('/api/cleaning/tasks/me'),
  });

  const toggleMutation = useMutation({
    mutationFn: (id: number) => api.put(`/api/cleaning/tasks/${id}/toggle`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-cleaning-tasks'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const commentMutation = useMutation({
    mutationFn: ({ id, comment }: { id: number; comment: string }) =>
      api.put(`/api/cleaning/tasks/${id}/comment?comment=${encodeURIComponent(comment)}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-cleaning-tasks'] });
      setEditingCommentId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opmerking plaatsen mislukt.'),
  });

  const incidentMutation = useMutation({
    mutationFn: ({ id, incident }: { id: number; incident: string }) =>
      api.put(`/api/cleaning/tasks/${id}/incident?incident=${encodeURIComponent(incident)}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['my-cleaning-tasks'] });
      setEditingIncidentId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Incident melden mislukt.'),
  });

  function submitComment(e: FormEvent<HTMLFormElement>, id: number) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    commentMutation.mutate({ id, comment: String(form.get('comment') || '') });
  }

  function submitIncident(e: FormEvent<HTMLFormElement>, id: number) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    incidentMutation.mutate({ id, incident: String(form.get('incident') || '') });
  }

  const tasks = tasksQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Mijn schoonmaaktaken</h1>
      {error && <Banner kind="error" message={error} />}

      <div className="space-y-4">
        {tasks.map((t) => (
          <Card key={t.id}>
            <div className="flex items-start justify-between">
              <div>
                <p className="text-xs text-slate-400">Week {t.weekNumber}</p>
                <h3 className="font-semibold text-slate-800">{t.name}</h3>
                {t.description && <p className="mt-1 text-sm text-slate-600">{t.description}</p>}
                {t.deadline && (
                  <p className="mt-1 text-xs text-slate-400">
                    Deadline: {new Date(t.deadline).toLocaleDateString('nl-NL')}
                  </p>
                )}
              </div>
              <button
                onClick={() => toggleMutation.mutate(t.id)}
                className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${
                  t.completed ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'
                }`}
              >
                {t.completed ? 'Voltooid' : 'Markeer als klaar'}
              </button>
            </div>

            {t.comment && <p className="mt-3 text-xs text-slate-500">Opmerking: {t.comment}</p>}
            {t.incidentReport && <p className="mt-1 text-xs text-red-600">Incident: {t.incidentReport}</p>}

            <div className="mt-3 flex gap-4">
              {editingCommentId === t.id ? (
                <form onSubmit={(e) => submitComment(e, t.id)} className="flex-1">
                  <Textarea name="comment" rows={2} defaultValue={t.comment ?? ''} autoFocus />
                  <div className="mt-1 space-x-2">
                    <Button type="submit" disabled={commentMutation.isPending}>Opslaan</Button>
                    <Button type="button" variant="secondary" onClick={() => setEditingCommentId(null)}>Annuleren</Button>
                  </div>
                </form>
              ) : (
                <button className="text-xs text-emerald-700 underline" onClick={() => setEditingCommentId(t.id)}>
                  Opmerking toevoegen
                </button>
              )}

              {editingIncidentId === t.id ? (
                <form onSubmit={(e) => submitIncident(e, t.id)} className="flex-1">
                  <Textarea name="incident" rows={2} defaultValue={t.incidentReport ?? ''} autoFocus />
                  <div className="mt-1 space-x-2">
                    <Button type="submit" disabled={incidentMutation.isPending}>Opslaan</Button>
                    <Button type="button" variant="secondary" onClick={() => setEditingIncidentId(null)}>Annuleren</Button>
                  </div>
                </form>
              ) : (
                <button className="text-xs text-red-600 underline" onClick={() => setEditingIncidentId(t.id)}>
                  Incident melden
                </button>
              )}
            </div>

            <TaskPhotosSection taskId={t.id} canDelete={false} />
          </Card>
        ))}
        {tasks.length === 0 && <p className="text-sm text-slate-400">Je hebt nog geen schoonmaaktaken toegewezen gekregen.</p>}
      </div>
    </div>
  );
}
