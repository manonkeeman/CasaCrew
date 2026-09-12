import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { SupplyReport, SupplyStatus, SupplyUrgency } from '../../lib/types';
import { Banner, Card, Table } from '../../components/ui';

const URGENCY_LABEL: Record<SupplyUrgency, string> = {
  LOW: 'Laag',
  MEDIUM: 'Gemiddeld',
  HIGH: 'Hoog',
};

const STATUS_LABEL: Record<SupplyStatus, string> = {
  PENDING: 'In behandeling',
  ORDERED: 'Besteld',
  RECEIVED: 'Opgelost',
};

export function SupplyReportsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const reportsQuery = useQuery({
    queryKey: ['supply-reports'],
    queryFn: () => api.get<SupplyReport[]>('/api/supply-reports'),
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: SupplyStatus }) =>
      api.patch(`/api/supply-reports/${id}/status`, { status }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['supply-reports'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/supply-reports/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['supply-reports'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  const reports = reportsQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-stone-800">Voorraadmeldingen</h1>
      {error && <Banner kind="error" message={error} />}

      <Card>
        <Table head={['Artikel', 'Urgentie', 'Opmerking', 'Gemeld door', 'Gemeld op', 'Status', '']}>
          {reports.map((report) => (
            <tr key={report.id}>
              <td className="py-2 pr-4">{report.itemName}</td>
              <td className="py-2 pr-4">{URGENCY_LABEL[report.urgency] ?? report.urgency}</td>
              <td className="py-2 pr-4">{report.notes ?? '-'}</td>
              <td className="py-2 pr-4">{report.reportedByUsername}</td>
              <td className="py-2 pr-4">{new Date(report.reportedAt).toLocaleDateString('nl-NL')}</td>
              <td className="py-2 pr-4">
                <select
                  className="rounded border border-stone-300 px-2 py-1 text-xs"
                  value={report.status}
                  onChange={(e) => updateStatusMutation.mutate({ id: report.id, status: e.target.value as SupplyStatus })}
                >
                  {Object.entries(STATUS_LABEL).map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </td>
              <td className="py-2 text-right">
                <button
                  className="text-xs font-medium text-red-600 hover:underline"
                  onClick={() => {
                    if (confirm(`Melding "${report.itemName}" verwijderen?`)) deleteMutation.mutate(report.id);
                  }}
                >
                  Verwijderen
                </button>
              </td>
            </tr>
          ))}
          {reports.length === 0 && (
            <tr>
              <td colSpan={7} className="py-6 text-center text-stone-400">Nog geen voorraadmeldingen</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
