import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Shift } from '../../lib/types';
import { Banner, Button, Card, Table } from '../../components/ui';

export function ShiftsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const shiftsQuery = useQuery({
    queryKey: ['my-shifts'],
    queryFn: () => api.get<Shift[]>('/api/shifts/me'),
  });

  const checkInMutation = useMutation({
    mutationFn: () => api.post('/api/shifts/checkin'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-shifts'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Inchecken mislukt.'),
  });

  const checkOutMutation = useMutation({
    mutationFn: () => api.post('/api/shifts/checkout'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-shifts'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Uitchecken mislukt.'),
  });

  const shifts = shiftsQuery.data ?? [];
  const activeShift = shifts.find((s) => s.checkInAt && !s.checkOutAt);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">In- en uitchecken</h1>
      {error && <Banner kind="error" message={error} />}

      <Card>
        {activeShift ? (
          <div className="flex items-center justify-between">
            <p className="text-sm text-slate-600">
              Ingecheckt sinds {new Date(activeShift.checkInAt!).toLocaleTimeString('nl-NL')}
            </p>
            <Button variant="secondary" onClick={() => checkOutMutation.mutate()} disabled={checkOutMutation.isPending}>
              {checkOutMutation.isPending ? 'Bezig...' : 'Uitchecken'}
            </Button>
          </div>
        ) : (
          <div className="flex items-center justify-between">
            <p className="text-sm text-slate-600">Je bent nu niet ingecheckt.</p>
            <Button onClick={() => checkInMutation.mutate()} disabled={checkInMutation.isPending}>
              {checkInMutation.isPending ? 'Bezig...' : 'Inchecken'}
            </Button>
          </div>
        )}
      </Card>

      <Card title="Eerdere shifts">
        <div className="mt-2">
          <Table head={['Datum', 'Inchecken', 'Uitchecken', 'Notities']}>
            {shifts.map((s) => (
              <tr key={s.id}>
                <td className="py-2 pr-4">{new Date(s.shiftDate).toLocaleDateString('nl-NL')}</td>
                <td className="py-2 pr-4">{s.checkInAt ? new Date(s.checkInAt).toLocaleTimeString('nl-NL') : '-'}</td>
                <td className="py-2 pr-4">{s.checkOutAt ? new Date(s.checkOutAt).toLocaleTimeString('nl-NL') : '-'}</td>
                <td className="py-2 pr-4">{s.notes ?? '-'}</td>
              </tr>
            ))}
            {shifts.length === 0 && (
              <tr>
                <td colSpan={4} className="py-6 text-center text-slate-400">Nog geen shifts geregistreerd</td>
              </tr>
            )}
          </Table>
        </div>
      </Card>
    </div>
  );
}
