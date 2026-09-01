import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { WasteScheduleEntry } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

export function WasteSchedulePage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingPolicy, setEditingPolicy] = useState(false);

  const entriesQuery = useQuery({
    queryKey: ['waste-schedule'],
    queryFn: () => api.get<WasteScheduleEntry[]>('/api/waste-schedule'),
  });
  const policyQuery = useQuery({
    queryKey: ['deposit-policy'],
    queryFn: () => api.get<{ policy: string }>('/api/waste-schedule/deposit-policy'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, scheduleInfo }: { id: number; scheduleInfo: string }) =>
      api.put(`/api/waste-schedule/${id}`, { scheduleInfo }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['waste-schedule'] });
      setEditingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const policyMutation = useMutation({
    mutationFn: (policy: string) => api.put('/api/waste-schedule/deposit-policy', { policy }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['deposit-policy'] });
      setEditingPolicy(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleUpdate(e: FormEvent<HTMLFormElement>, entry: WasteScheduleEntry) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    updateMutation.mutate({ id: entry.id, scheduleInfo: String(form.get('scheduleInfo') || '') });
  }

  function handleSavePolicy(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    policyMutation.mutate(String(form.get('policy') || ''));
  }

  const entries = entriesQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-stone-800">Afvalschema</h1>
      <p className="mb-6 text-sm text-stone-500">
        Vaste afvalcategorieën. De ophaaldag/frequentie is adresafhankelijk (gemeente Utrechtse Heuvelrug / RMN) — vul
        die zelf in via je eigen afvalkalender op{' '}
        <a href="https://www.mijnafvalwijzer.nl" target="_blank" rel="noreferrer" className="text-emerald-700 underline">
          mijnafvalwijzer.nl
        </a>
        .
      </p>
      {error && <Banner kind="error" message={error} />}

      <div className="space-y-4">
        {entries.map((entry) => (
          <Card key={entry.id}>
            {editingId === entry.id ? (
              <form onSubmit={(e) => handleUpdate(e, entry)}>
                <h3 className="mb-2 font-semibold text-stone-800">{entry.wasteType}</h3>
                <Field label="Ophaaldag / frequentie">
                  <Input name="scheduleInfo" defaultValue={entry.scheduleInfo ?? ''} placeholder="bv. elke donderdag, oneven weken" autoFocus />
                </Field>
                <div className="space-x-2">
                  <Button type="submit" disabled={updateMutation.isPending}>Opslaan</Button>
                  <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>Annuleren</Button>
                </div>
              </form>
            ) : (
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-stone-800">{entry.wasteType}</h3>
                  <p className="mt-1 text-sm text-stone-600">
                    {entry.scheduleInfo || <span className="text-stone-400">Nog niet ingevuld</span>}
                  </p>
                </div>
                <button className="text-xs font-medium text-emerald-700 hover:underline" onClick={() => setEditingId(entry.id)}>
                  Bewerken
                </button>
              </div>
            )}
          </Card>
        ))}
        {entries.length === 0 && <p className="text-sm text-stone-400">Nog geen afvalschema beschikbaar.</p>}
      </div>

      <div className="mt-6">
        <Card
          title="Statiegeldbeleid"
          actions={
            <Button variant="secondary" onClick={() => setEditingPolicy((v) => !v)}>
              {editingPolicy ? 'Annuleren' : 'Bewerken'}
            </Button>
          }
        >
          {editingPolicy ? (
            <form onSubmit={handleSavePolicy}>
              <Field label="Policy">
                <Textarea name="policy" rows={4} defaultValue={policyQuery.data?.policy ?? ''} />
              </Field>
              <Button type="submit" disabled={policyMutation.isPending}>
                {policyMutation.isPending ? 'Bezig...' : 'Opslaan'}
              </Button>
            </form>
          ) : (
            <p className="whitespace-pre-wrap text-sm text-stone-600">
              {policyQuery.data?.policy || 'Nog geen policy ingesteld.'}
            </p>
          )}
        </Card>
      </div>
    </div>
  );
}
