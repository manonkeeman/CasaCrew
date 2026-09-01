import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { api, ApiError } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import type { CleaningTask, Shift } from '../../lib/types';
import { Banner, Button, Card, StatCard } from '../../components/ui';

export function DashboardPage() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const tasksQuery = useQuery({
    queryKey: ['my-cleaning-tasks'],
    queryFn: () => api.get<CleaningTask[]>('/api/cleaning/tasks/me'),
  });
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

  const tasks = tasksQuery.data ?? [];
  const openTasks = tasks.filter((t) => !t.completed);

  const shifts = shiftsQuery.data ?? [];
  const activeShift = shifts.find((s) => s.checkInAt && !s.checkOutAt);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Welkom, {user?.fullName ?? user?.username}</h1>
      {error && <Banner kind="error" message={error} />}

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <StatCard label="Openstaande taken" value={`${openTasks.length} / ${tasks.length}`} />
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-sm font-medium text-slate-500">Shiftstatus</p>
          {activeShift ? (
            <div className="mt-1 flex items-center justify-between">
              <p className="text-sm text-emerald-700">
                Ingecheckt sinds {new Date(activeShift.checkInAt!).toLocaleTimeString('nl-NL')}
              </p>
              <Button variant="secondary" onClick={() => checkOutMutation.mutate()} disabled={checkOutMutation.isPending}>
                {checkOutMutation.isPending ? 'Bezig...' : 'Uitchecken'}
              </Button>
            </div>
          ) : (
            <div className="mt-1 flex items-center justify-between">
              <p className="text-sm text-slate-600">Niet ingecheckt</p>
              <Button onClick={() => checkInMutation.mutate()} disabled={checkInMutation.isPending}>
                {checkInMutation.isPending ? 'Bezig...' : 'Inchecken'}
              </Button>
            </div>
          )}
        </div>
      </div>

      <Card title="Mijn openstaande taken">
        <div className="space-y-3">
          {openTasks.map((t) => (
            <div key={t.id} className="flex items-start justify-between gap-4 border-b border-slate-100 pb-3 last:border-0 last:pb-0">
              <div>
                <p className="text-xs text-slate-400">Week {t.weekNumber}</p>
                <p className="font-medium text-slate-800">{t.name}</p>
              </div>
              {t.deadline && (
                <span className="shrink-0 text-xs text-slate-400">
                  Deadline: {new Date(t.deadline).toLocaleDateString('nl-NL')}
                </span>
              )}
            </div>
          ))}
          {openTasks.length === 0 && <p className="text-sm text-slate-400">Geen openstaande taken. Goed bezig!</p>}
        </div>
      </Card>
    </div>
  );
}
