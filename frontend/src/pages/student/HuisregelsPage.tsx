import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { Huisregel } from '../../lib/types';
import { Card } from '../../components/ui';

export function HuisregelsPage() {
  const query = useQuery({
    queryKey: ['huisregels'],
    queryFn: () => api.get<Huisregel[]>('/api/huisregels'),
  });

  const rules = query.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Huisregels</h1>
      <div className="space-y-4">
        {rules.map((rule) => (
          <Card key={rule.id}>
            <h3 className="font-semibold text-slate-800">{rule.title}</h3>
            {rule.content && <p className="mt-1 whitespace-pre-wrap text-sm text-slate-600">{rule.content}</p>}
          </Card>
        ))}
        {rules.length === 0 && <p className="text-sm text-slate-400">Nog geen huisregels.</p>}
      </div>
    </div>
  );
}
