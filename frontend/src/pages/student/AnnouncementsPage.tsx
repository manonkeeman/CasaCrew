import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { Announcement } from '../../lib/types';
import { Card } from '../../components/ui';

const TYPE_LABEL: Record<string, string> = {
  mededeling: 'Mededeling',
  onderhoud: 'Onderhoud',
  evenement: 'Evenement',
};

export function AnnouncementsPage() {
  const query = useQuery({
    queryKey: ['announcements'],
    queryFn: () => api.get<Announcement[]>('/api/announcements'),
  });

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Mededelingen</h1>
      <div className="space-y-4">
        {(query.data ?? []).map((a) => (
          <Card key={a.id}>
            <div className="mb-1 flex items-center justify-between">
              <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
                {TYPE_LABEL[a.type] ?? a.type}
              </span>
              <span className="text-xs text-slate-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
            </div>
            <h3 className="font-semibold text-slate-800">{a.title}</h3>
            <p className="mt-1 whitespace-pre-wrap text-sm text-slate-600">{a.body}</p>
            {a.author && <p className="mt-2 text-xs text-slate-400">— {a.author}</p>}
          </Card>
        ))}
        {(query.data ?? []).length === 0 && <p className="text-sm text-slate-400">Nog geen mededelingen.</p>}
      </div>
    </div>
  );
}
