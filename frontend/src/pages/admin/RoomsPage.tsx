import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Room } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table } from '../../components/ui';

export function RoomsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const roomsQuery = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/api/rooms'),
  });

  const createMutation = useMutation({
    mutationFn: (name: string) => api.post('/api/rooms', { name }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rooms'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/rooms/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rooms'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt (mogelijk nog bezet).'),
  });

  const removeOccupantMutation = useMutation({
    mutationFn: (roomId: number) => api.put(`/api/rooms/${roomId}/remove`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rooms'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Loskoppelen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    const name = String(form.get('name') || '').trim();
    if (!name) return;
    createMutation.mutate(name);
    e.currentTarget.reset();
  }

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Kamers</h1>
      {error && <Banner kind="error" message={error} />}

      <Card title="Nieuwe kamer" >
        <form onSubmit={handleCreate} className="flex items-end gap-3">
          <Field label="Kamernaam of -nummer">
            <Input name="name" required placeholder="bv. Kamer 3" />
          </Field>
          <Button type="submit" disabled={createMutation.isPending}>Toevoegen</Button>
        </form>
      </Card>

      <div className="mt-6">
        <Card>
          <Table head={['Kamer', 'Bewoner', '']}>
            {(roomsQuery.data ?? []).map((room) => (
              <tr key={room.id}>
                <td className="py-2 pr-4 font-medium">{room.name}</td>
                <td className="py-2 pr-4">{room.occupantUsername ?? <span className="text-slate-400">Leeg</span>}</td>
                <td className="py-2 text-right space-x-3">
                  {room.occupantId && (
                    <button
                      className="text-xs font-medium text-slate-600 hover:underline"
                      onClick={() => removeOccupantMutation.mutate(room.id)}
                    >
                      Loskoppelen
                    </button>
                  )}
                  <button
                    className="text-xs font-medium text-red-600 hover:underline"
                    onClick={() => {
                      if (confirm(`Kamer ${room.name} verwijderen?`)) deleteMutation.mutate(room.id);
                    }}
                  >
                    Verwijderen
                  </button>
                </td>
              </tr>
            ))}
            {(roomsQuery.data ?? []).length === 0 && (
              <tr>
                <td colSpan={3} className="py-6 text-center text-slate-400">Nog geen kamers</td>
              </tr>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
