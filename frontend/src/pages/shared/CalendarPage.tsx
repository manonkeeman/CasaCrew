import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import type { CalendarEvent } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export function CalendarPage() {
  const { user, role } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const eventsQuery = useQuery({
    queryKey: ['calendar-events'],
    queryFn: () => api.get<CalendarEvent[]>('/api/calendar-events'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { title: string; description?: string; eventDate: string; eventTime?: string }) =>
      api.post('/api/calendar-events', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendar-events'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Toevoegen mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/calendar-events/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['calendar-events'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      title: String(form.get('title') || ''),
      description: String(form.get('description') || '') || undefined,
      eventDate: String(form.get('eventDate') || todayIso()),
      eventTime: String(form.get('eventTime') || '') || undefined,
    });
  }

  const events = eventsQuery.data ?? [];
  const today = todayIso();
  const upcoming = events.filter((e) => e.eventDate >= today);
  const past = events.filter((e) => e.eventDate < today);

  function canDelete(event: CalendarEvent) {
    return role === 'ROLE_ADMIN' || event.createdByUsername === user?.username;
  }

  function renderEvent(event: CalendarEvent) {
    return (
      <Card key={event.id}>
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-xs text-stone-400">
              {new Date(event.eventDate).toLocaleDateString('nl-NL', { weekday: 'long', day: 'numeric', month: 'long' })}
              {event.eventTime && ` · ${event.eventTime.slice(0, 5)}`}
            </p>
            <h3 className="font-semibold text-stone-800">{event.title}</h3>
            {event.description && <p className="mt-1 text-sm text-stone-600">{event.description}</p>}
            {event.createdByUsername && <p className="mt-2 text-xs text-stone-400">Door {event.createdByUsername}</p>}
          </div>
          {canDelete(event) && (
            <button
              className="shrink-0 text-xs font-medium text-red-600 hover:underline"
              onClick={() => {
                if (confirm(`"${event.title}" verwijderen?`)) deleteMutation.mutate(event.id);
              }}
            >
              Verwijderen
            </button>
          )}
        </div>
      </Card>
    );
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Agenda</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Item toevoegen'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <div className="mb-6">
          <Card title="Nieuw agenda-item">
            <form onSubmit={handleCreate}>
              <Field label="Titel">
                <Input name="title" required maxLength={150} placeholder="bv. Huisfeest" />
              </Field>
              <Field label="Omschrijving (optioneel)">
                <Textarea name="description" rows={2} />
              </Field>
              <div className="grid grid-cols-2 gap-x-4">
                <Field label="Datum">
                  <Input name="eventDate" type="date" defaultValue={todayIso()} required />
                </Field>
                <Field label="Tijd (optioneel)">
                  <Input name="eventTime" type="time" />
                </Field>
              </div>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Toevoegen'}
              </Button>
            </form>
          </Card>
        </div>
      )}

      <div className="space-y-4">
        {upcoming.map(renderEvent)}
        {upcoming.length === 0 && <p className="text-sm text-stone-400">Nog geen aankomende items.</p>}
      </div>

      {past.length > 0 && (
        <div className="mt-8">
          <h2 className="mb-3 text-sm font-medium text-stone-500">Eerder</h2>
          <div className="space-y-4 opacity-60">{past.map(renderEvent)}</div>
        </div>
      )}
    </div>
  );
}
