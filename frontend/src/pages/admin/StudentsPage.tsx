import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Room, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table } from '../../components/ui';

interface CreateStudentPayload {
  username: string;
  email: string;
  password: string;
  room?: string;
  rentAmount: number;
  sendWelcomeEmail: boolean;
}

export function StudentsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const roomsQuery = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/api/rooms'),
  });

  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT' || u.role === 'ROLE_STUDENT');

  const createMutation = useMutation({
    mutationFn: (payload: CreateStudentPayload) => api.post('/api/admin/students', payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/users/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Record<string, unknown> }) =>
      api.patch(`/api/admin/students/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const assignRoomMutation = useMutation({
    mutationFn: ({ roomId, userId }: { roomId: number; userId: number }) =>
      api.put(`/api/rooms/${roomId}/assign/${userId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Kamer toewijzen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      username: String(form.get('username')),
      email: String(form.get('email')),
      password: String(form.get('password')),
      room: String(form.get('room') || '') || undefined,
      rentAmount: Number(form.get('rentAmount')),
      sendWelcomeEmail: form.get('sendWelcomeEmail') === 'on',
    });
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Studenten</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe student'}</Button>
      </div>

      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <Card title="Nieuwe student toevoegen">
          <form onSubmit={handleCreate} className="grid grid-cols-2 gap-x-4">
            <Field label="Naam">
              <Input name="username" required minLength={2} />
            </Field>
            <Field label="E-mailadres">
              <Input name="email" type="email" required />
            </Field>
            <Field label="Wachtwoord">
              <Input name="password" type="password" required minLength={8} />
            </Field>
            <Field label="Huurbedrag (€)">
              <Input name="rentAmount" type="number" step="0.01" min="1" required />
            </Field>
            <Field label="Kamer (optioneel)">
              <select name="room" className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm">
                <option value="">Geen kamer</option>
                {(roomsQuery.data ?? [])
                  .filter((r) => !r.occupantId)
                  .map((r) => (
                    <option key={r.id} value={r.name}>{r.name}</option>
                  ))}
              </select>
            </Field>
            <label className="mt-6 flex items-center gap-2 text-sm text-slate-600">
              <input type="checkbox" name="sendWelcomeEmail" defaultChecked /> Welkomstmail versturen
            </label>
            <div className="col-span-2 mt-2">
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Student toevoegen'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      <Card>
        <Table head={['Naam', 'E-mail', 'Kamer', 'Huur', 'Telefoon', '']}>
          {students.map((s) => (
            <tr key={s.id}>
              <td className="py-2 pr-4">{s.username}</td>
              <td className="py-2 pr-4">{s.email}</td>
              <td className="py-2 pr-4">
                <select
                  className="rounded border border-slate-200 px-2 py-1 text-xs"
                  value={s.roomName ?? ''}
                  onChange={(e) => {
                    const room = (roomsQuery.data ?? []).find((r) => r.name === e.target.value);
                    if (room) assignRoomMutation.mutate({ roomId: room.id, userId: s.id });
                  }}
                >
                  <option value="">Geen kamer</option>
                  {(roomsQuery.data ?? [])
                    .filter((r) => !r.occupantId || r.name === s.roomName)
                    .map((r) => (
                      <option key={r.id} value={r.name}>{r.name}</option>
                    ))}
                </select>
              </td>
              <td className="py-2 pr-4">
                {editingId === s.id ? (
                  <input
                    type="number"
                    step="0.01"
                    defaultValue={s.rentAmount ?? ''}
                    className="w-24 rounded border border-slate-300 px-2 py-1 text-xs"
                    onBlur={(e) => updateMutation.mutate({ id: s.id, body: { rentAmount: Number(e.target.value) } })}
                    autoFocus
                  />
                ) : (
                  <button className="text-xs text-emerald-700 underline" onClick={() => setEditingId(s.id)}>
                    €{s.rentAmount ?? '-'}
                  </button>
                )}
              </td>
              <td className="py-2 pr-4">{s.phoneNumber ?? '-'}</td>
              <td className="py-2 text-right">
                <button
                  className="text-xs font-medium text-red-600 hover:underline"
                  onClick={() => {
                    if (confirm(`Weet je zeker dat je ${s.username} wilt verwijderen?`)) {
                      deleteMutation.mutate(s.id);
                    }
                  }}
                >
                  Verwijderen
                </button>
              </td>
            </tr>
          ))}
          {students.length === 0 && (
            <tr>
              <td colSpan={6} className="py-6 text-center text-slate-400">Nog geen studenten</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
