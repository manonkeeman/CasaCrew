import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { CleaningScheduleInfo, CleaningTask, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table, Textarea } from '../../components/ui';
import { TaskPhotosSection } from '../../components/TaskPhotos';

interface CreateTaskPayload {
  weekNumber: number;
  name: string;
  description?: string;
  dueDate?: string;
  assignedTo?: string;
}

export function CleaningSchedulePage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [weekFilter, setWeekFilter] = useState<number | ''>('');

  const infoQuery = useQuery({
    queryKey: ['cleaning-schedule-info'],
    queryFn: () => api.get<CleaningScheduleInfo>('/api/cleaning/schedule/info'),
  });

  const tasksQuery = useQuery({
    queryKey: ['cleaning-tasks', weekFilter],
    queryFn: () =>
      api.get<CleaningTask[]>(weekFilter === '' ? '/api/cleaning/tasks' : `/api/cleaning/tasks?weekNumber=${weekFilter}`),
  });

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const assignableUsers = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT' || u.role === 'CLEANER');

  const createMutation = useMutation({
    mutationFn: (payload: CreateTaskPayload) => api.post('/api/cleaning/tasks', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cleaning-tasks'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const toggleMutation = useMutation({
    mutationFn: (id: number) => api.put(`/api/cleaning/tasks/${id}/toggle`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cleaning-tasks'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/cleaning/tasks/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cleaning-tasks'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      weekNumber: Number(form.get('weekNumber')),
      name: String(form.get('name')),
      description: String(form.get('description') || '') || undefined,
      dueDate: String(form.get('dueDate') || '') || undefined,
      assignedTo: String(form.get('assignedTo') || '') || undefined,
    });
  }

  const tasks = tasksQuery.data ?? [];
  const info = infoQuery.data;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Schoonmaakrooster</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe taak'}</Button>
      </div>

      {error && <Banner kind="error" message={error} />}

      {info && (
        <p className="mb-6 text-sm text-slate-500">
          Huidige ISO-week {info.isoWeek} ({info.year}) — rotatieweek {info.rotationWeek} van {info.rotationLength}
        </p>
      )}

      {showForm && (
        <Card title="Nieuwe schoonmaaktaak">
          <form onSubmit={handleCreate} className="grid grid-cols-2 gap-x-4">
            <Field label="Weeknummer">
              <Input name="weekNumber" type="number" min={1} max={53} required defaultValue={info?.rotationWeek} />
            </Field>
            <Field label="Taaknaam">
              <Input name="name" required />
            </Field>
            <Field label="Deadline (optioneel)">
              <Input name="dueDate" type="date" />
            </Field>
            <Field label="Toewijzen aan (optioneel)">
              <select name="assignedTo" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                <option value="">Niemand</option>
                {assignableUsers.map((u) => (
                  <option key={u.id} value={u.email}>
                    {u.username} ({u.role === 'CLEANER' ? 'schoonmaker' : 'student'})
                  </option>
                ))}
              </select>
            </Field>
            <div className="col-span-2 mb-4">
              <Field label="Omschrijving (optioneel)">
                <Textarea name="description" rows={2} />
              </Field>
            </div>
            <div className="col-span-2">
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Taak toevoegen'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      <div className="mb-4 mt-6 flex items-center gap-2">
        <label className="text-sm text-slate-600">Filter op week:</label>
        <Input
          type="number"
          min={1}
          max={53}
          className="w-24"
          value={weekFilter}
          onChange={(e) => setWeekFilter(e.target.value === '' ? '' : Number(e.target.value))}
          placeholder="Alle"
        />
        {weekFilter !== '' && (
          <button className="text-xs text-emerald-700 underline" onClick={() => setWeekFilter('')}>
            Wis filter
          </button>
        )}
      </div>

      <Card>
        <Table head={['Week', 'Taak', 'Toegewezen aan', 'Deadline', 'Status', '']}>
          {tasks.map((t) => (
            <tr key={t.id}>
              <td className="py-2 pr-4">{t.weekNumber}</td>
              <td className="py-2 pr-4">
                <p className="font-medium text-slate-800">{t.name}</p>
                {t.description && <p className="text-xs text-slate-500">{t.description}</p>}
                {t.incidentReport && <p className="mt-1 text-xs text-red-600">Incident: {t.incidentReport}</p>}
                {t.comment && <p className="mt-1 text-xs text-slate-400">Opmerking: {t.comment}</p>}
                <TaskPhotosSection taskId={t.id} canDelete />
              </td>
              <td className="py-2 pr-4">{t.assignedTo ?? '-'}</td>
              <td className="py-2 pr-4">{t.deadline ? new Date(t.deadline).toLocaleDateString('nl-NL') : '-'}</td>
              <td className="py-2 pr-4">
                <button
                  onClick={() => toggleMutation.mutate(t.id)}
                  className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                    t.completed ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'
                  }`}
                >
                  {t.completed ? 'Voltooid' : 'Open'}
                </button>
              </td>
              <td className="py-2 text-right">
                <button
                  className="text-xs font-medium text-red-600 hover:underline"
                  onClick={() => {
                    if (confirm(`Taak "${t.name}" verwijderen?`)) deleteMutation.mutate(t.id);
                  }}
                >
                  Verwijderen
                </button>
              </td>
            </tr>
          ))}
          {tasks.length === 0 && (
            <tr>
              <td colSpan={6} className="py-6 text-center text-slate-400">Nog geen schoonmaaktaken</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
