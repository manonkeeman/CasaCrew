import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { WasteScheduleEntry } from '../../lib/types';
import { Card } from '../../components/ui';

export function WasteSchedulePage() {
  const entriesQuery = useQuery({
    queryKey: ['waste-schedule'],
    queryFn: () => api.get<WasteScheduleEntry[]>('/api/waste-schedule'),
  });
  const policyQuery = useQuery({
    queryKey: ['deposit-policy'],
    queryFn: () => api.get<{ policy: string }>('/api/waste-schedule/deposit-policy'),
  });

  const entries = entriesQuery.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-stone-800">Afvalschema</h1>

      <div className="space-y-4">
        {entries.map((entry) => (
          <Card key={entry.id}>
            <h3 className="font-semibold text-stone-800">{entry.wasteType}</h3>
            <p className="mt-1 text-sm text-stone-600">
              {entry.scheduleInfo || <span className="text-stone-400">Nog niet ingevuld</span>}
            </p>
          </Card>
        ))}
        {entries.length === 0 && <p className="text-sm text-stone-400">Nog geen afvalschema beschikbaar.</p>}
      </div>

      {policyQuery.data?.policy && (
        <div className="mt-6">
          <Card title="Statiegeldbeleid">
            <p className="whitespace-pre-wrap text-sm text-stone-600">{policyQuery.data.policy}</p>
          </Card>
        </div>
      )}
    </div>
  );
}
