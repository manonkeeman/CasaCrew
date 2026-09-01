import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { Announcement, CleaningScheduleInfo, CleaningTask, Invoice, Room, UserResponse } from '../../lib/types';
import { Card, StatCard } from '../../components/ui';

const ANNOUNCEMENT_TYPE_LABEL: Record<string, string> = {
  mededeling: 'Mededeling',
  onderhoud: 'Onderhoud',
  evenement: 'Evenement',
};

export function DashboardPage() {
  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const roomsQuery = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/api/rooms'),
  });
  const cleaningInfoQuery = useQuery({
    queryKey: ['cleaning-schedule-info'],
    queryFn: () => api.get<CleaningScheduleInfo>('/api/cleaning/schedule/info'),
  });
  const cleaningTasksQuery = useQuery({
    queryKey: ['cleaning-tasks', ''],
    queryFn: () => api.get<CleaningTask[]>('/api/cleaning/tasks'),
  });
  const invoicesQuery = useQuery({
    queryKey: ['invoices'],
    queryFn: () => api.get<Invoice[]>('/api/invoices'),
  });
  const announcementsQuery = useQuery({
    queryKey: ['announcements'],
    queryFn: () => api.get<Announcement[]>('/api/announcements'),
  });

  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT');
  const rooms = roomsQuery.data ?? [];
  const roomsOccupied = rooms.filter((r) => r.occupantId !== null).length;

  const isoWeek = cleaningInfoQuery.data?.isoWeek;
  const tasksThisWeek = (cleaningTasksQuery.data ?? []).filter((t) => t.weekNumber === isoWeek);
  const openTasksThisWeek = tasksThisWeek.filter((t) => !t.completed).length;

  const invoices = invoicesQuery.data ?? [];
  const openInvoices = invoices.filter((i) => i.status === 'OPEN' || i.status === 'OVERDUE');
  const overdueInvoices = invoices.filter((i) => i.status === 'OVERDUE');
  const openInvoicesTotal = openInvoices.reduce((sum, i) => sum + i.amount, 0);

  const recentAnnouncements = [...(announcementsQuery.data ?? [])]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5);

  const expiringLeases = students
    .filter((s) => s.leaseEndDate)
    .map((s) => ({
      ...s,
      daysLeft: Math.ceil((new Date(s.leaseEndDate!).getTime() - Date.now()) / (1000 * 60 * 60 * 24)),
    }))
    .filter((s) => s.daysLeft <= 60)
    .sort((a, b) => a.daysLeft - b.daysLeft);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Dashboard</h1>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Studenten" value={students.length} />
        <StatCard
          label="Kamers bezet"
          value={`${roomsOccupied} / ${rooms.length}`}
          hint={rooms.length - roomsOccupied > 0 ? `${rooms.length - roomsOccupied} vrij` : undefined}
        />
        <StatCard
          label="Schoonmaaktaken deze week"
          value={isoWeek !== undefined ? `${openTasksThisWeek} / ${tasksThisWeek.length}` : '-'}
          hint="nog open"
        />
        <StatCard
          label="Openstaande facturen"
          value={`€${openInvoicesTotal.toFixed(2)}`}
          hint={overdueInvoices.length > 0 ? `${overdueInvoices.length} te laat` : `${openInvoices.length} openstaand`}
        />
      </div>

      {expiringLeases.length > 0 && (
        <div className="mb-6">
        <Card title="Aflopende contracten">
          <div className="space-y-3">
            {expiringLeases.map((s) => (
              <div key={s.id} className="flex items-center justify-between gap-4 border-b border-slate-100 pb-3 last:border-0 last:pb-0">
                <div>
                  <span className="font-medium text-slate-800">{s.username}</span>
                  <span className="ml-2 text-xs text-slate-400">{s.roomName ?? 'Geen kamer'}</span>
                </div>
                <span className="rounded-full bg-amber-50 px-2 py-0.5 text-xs font-medium text-amber-700">
                  {s.daysLeft < 0 ? 'Verlopen' : `Nog ${s.daysLeft} dagen`} · {s.leaseEndDate}
                </span>
              </div>
            ))}
          </div>
        </Card>
        </div>
      )}

      <Card title="Laatste aankondigingen">
        <div className="space-y-3">
          {recentAnnouncements.map((a) => (
            <div key={a.id} className="flex items-start justify-between gap-4 border-b border-slate-100 pb-3 last:border-0 last:pb-0">
              <div>
                <span className="mr-2 rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
                  {ANNOUNCEMENT_TYPE_LABEL[a.type] ?? a.type}
                </span>
                <span className="font-medium text-slate-800">{a.title}</span>
              </div>
              <span className="shrink-0 text-xs text-slate-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
            </div>
          ))}
          {recentAnnouncements.length === 0 && <p className="text-sm text-slate-400">Nog geen aankondigingen.</p>}
        </div>
      </Card>
    </div>
  );
}
