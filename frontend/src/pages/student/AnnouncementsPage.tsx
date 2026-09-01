import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { Announcement } from '../../lib/types';
import { Card } from '../../components/ui';
import { ANNOUNCEMENT_TYPE_ICON, ANNOUNCEMENT_TYPE_LABEL } from '../../lib/announcementTypes';
import { MegaphoneIcon } from '../../components/icons';

export function AnnouncementsPage() {
  const query = useQuery({
    queryKey: ['announcements'],
    queryFn: () => api.get<Announcement[]>('/api/announcements'),
  });

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-stone-800">Mededelingen</h1>
      <div className="space-y-4">
        {(query.data ?? []).map((a) => {
          const Icon = ANNOUNCEMENT_TYPE_ICON[a.type] ?? MegaphoneIcon;
          return (
            <Card key={a.id}>
              <div className="flex items-start gap-3">
                <span className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                  <Icon className="h-4.5 w-4.5" />
                </span>
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex items-center justify-between gap-2">
                    <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
                      {ANNOUNCEMENT_TYPE_LABEL[a.type] ?? a.type}
                    </span>
                    <span className="shrink-0 text-xs text-stone-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
                  </div>
                  <h3 className="font-semibold text-stone-800">{a.title}</h3>
                  <p className="mt-1 whitespace-pre-wrap text-sm text-stone-600">{a.body}</p>
                  {a.author && <p className="mt-2 text-xs text-stone-400">— {a.author}</p>}
                </div>
              </div>
            </Card>
          );
        })}
        {(query.data ?? []).length === 0 && <p className="text-sm text-stone-400">Nog geen mededelingen.</p>}
      </div>
    </div>
  );
}
