import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { SupplyReport, SupplyUrgency } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table, Textarea } from '../../components/ui';

const URGENCY_LABEL: Record<SupplyUrgency, string> = {
  LOW: 'Laag',
  MEDIUM: 'Gemiddeld',
  HIGH: 'Hoog',
};

const STATUS_LABEL: Record<string, string> = {
  PENDING: 'In behandeling',
  ORDERED: 'Besteld',
  RECEIVED: 'Opgelost',
};

const STATUS_STYLE: Record<string, string> = {
  PENDING: 'bg-amber-50 text-amber-700',
  ORDERED: 'bg-sky-50 text-sky-700',
  RECEIVED: 'bg-emerald-50 text-emerald-700',
};

export function SupplyReportsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const reportsQuery = useQuery({
    queryKey: ['supply-reports'],
    queryFn: () => api.get<SupplyReport[]>('/api/supply-reports'),
  });

  const createMutation = useMutation({
    mutationFn: (body: { itemName: string; notes: string; urgency: SupplyUrgency }) =>
      api.post('/api/supply-reports', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['supply-reports'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Melden mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const data = new FormData(e.currentTarget);
    createMutation.mutate({
      itemName: String(data.get('itemName') || ''),
      notes: String(data.get('notes') || ''),
      urgency: data.get('urgency') as SupplyUrgency,
    });
  }

  const reports = reportsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Voorraadmeldingen</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe melding'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <Card title="Voorraad melden">
          <form onSubmit={handleCreate}>
            <Field label="Artikel">
              <Input name="itemName" required placeholder="Bijv. wc-papier" />
            </Field>
            <Field label="Urgentie">
              <select name="urgency" required defaultValue="MEDIUM" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                {Object.entries(URGENCY_LABEL).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </Field>
            <Field label="Opmerking (optioneel)">
              <Textarea name="notes" rows={2} />
            </Field>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Bezig...' : 'Melden'}
            </Button>
          </form>
        </Card>
      )}

      <div className="mt-6">
        <Card>
          <Table head={['Artikel', 'Urgentie', 'Opmerking', 'Status', 'Gemeld op']}>
            {reports.map((report) => (
              <tr key={report.id}>
                <td className="py-2 pr-4">{report.itemName}</td>
                <td className="py-2 pr-4">{URGENCY_LABEL[report.urgency] ?? report.urgency}</td>
                <td className="py-2 pr-4">{report.notes ?? '-'}</td>
                <td className="py-2 pr-4">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[report.status] ?? ''}`}>
                    {STATUS_LABEL[report.status] ?? report.status}
                  </span>
                </td>
                <td className="py-2 pr-4">{new Date(report.reportedAt).toLocaleDateString('nl-NL')}</td>
              </tr>
            ))}
            {reports.length === 0 && (
              <tr>
                <td colSpan={5} className="py-6 text-center text-stone-400">Nog geen voorraadmeldingen</td>
              </tr>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
