import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table } from '../../components/ui';

export function CleanerPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [showCreate, setShowCreate] = useState(false);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const cleaners = (usersQuery.data ?? []).filter((u) => u.role === 'CLEANER');

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Record<string, unknown> }) =>
      api.put(`/api/users/${id}/profile`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setEditingId(null);
      setSuccess('Gegevens bijgewerkt.');
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { username: string; email: string; password: string; role: string }) =>
      api.post('/api/users', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setShowCreate(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  function handleSave(e: FormEvent<HTMLFormElement>, cleaner: UserResponse) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    const form = new FormData(e.currentTarget);
    updateMutation.mutate({
      id: cleaner.id,
      body: {
        username: String(form.get('username')),
        email: String(form.get('email')),
        phoneNumber: String(form.get('phoneNumber') || ''),
      },
    });
  }

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      username: String(form.get('username')),
      email: String(form.get('email')),
      password: String(form.get('password')),
      role: 'CLEANER',
    });
  }

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Schoonmaakaccount</h1>
        <Button onClick={() => setShowCreate((v) => !v)}>{showCreate ? 'Annuleren' : 'Nieuw account'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}

      {showCreate && (
        <Card title="Nieuw schoonmaakaccount">
          <form onSubmit={handleCreate} className="grid grid-cols-3 gap-4">
            <Field label="Naam">
              <Input name="username" required minLength={2} />
            </Field>
            <Field label="E-mailadres">
              <Input name="email" type="email" required />
            </Field>
            <Field label="Wachtwoord">
              <Input name="password" type="password" required minLength={8} />
            </Field>
            <div className="col-span-3">
              <Button type="submit" disabled={createMutation.isPending}>Aanmaken</Button>
            </div>
          </form>
        </Card>
      )}

      <div className="mt-6 space-y-4">
        {cleaners.map((cleaner) => (
          <Card key={cleaner.id}>
            {editingId === cleaner.id ? (
              <form onSubmit={(e) => handleSave(e, cleaner)} className="grid grid-cols-3 gap-4">
                <Field label="Naam">
                  <Input name="username" defaultValue={cleaner.username} required />
                </Field>
                <Field label="E-mailadres">
                  <Input name="email" type="email" defaultValue={cleaner.email} required />
                </Field>
                <Field label="Telefoonnummer">
                  <Input name="phoneNumber" defaultValue={cleaner.phoneNumber ?? ''} />
                </Field>
                <div className="col-span-3 space-x-2">
                  <Button type="submit" disabled={updateMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <Table head={['Naam', 'E-mail', 'Telefoon', '']}>
                <tr>
                  <td className="py-2 pr-4">{cleaner.username}</td>
                  <td className="py-2 pr-4">{cleaner.email}</td>
                  <td className="py-2 pr-4">{cleaner.phoneNumber ?? '-'}</td>
                  <td className="py-2 text-right">
                    <button className="text-xs font-medium text-emerald-700 hover:underline" onClick={() => setEditingId(cleaner.id)}>
                      Bewerken
                    </button>
                  </td>
                </tr>
              </Table>
            )}
          </Card>
        ))}
        {cleaners.length === 0 && <p className="text-sm text-slate-400">Nog geen schoonmaakaccount aangemaakt.</p>}
      </div>
    </div>
  );
}
