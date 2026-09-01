import { useQuery } from '@tanstack/react-query';
import { api, assetUrl } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import type { Announcement, Invoice } from '../../lib/types';
import { Card, StatCard } from '../../components/ui';
import { Avatar } from '../../components/Avatar';
import { BanknotesIcon, BedIcon, EuroIcon, MegaphoneIcon } from '../../components/icons';
import { ANNOUNCEMENT_TYPE_ICON, ANNOUNCEMENT_TYPE_LABEL } from '../../lib/announcementTypes';

const STATUS_LABEL: Record<string, string> = {
  OPEN: 'Open',
  PAID: 'Betaald',
  OVERDUE: 'Te laat',
  CANCELLED: 'Geannuleerd',
};

function greeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Goedemorgen';
  if (hour < 18) return 'Goedemiddag';
  return 'Goedenavond';
}

export function DashboardPage() {
  const { user } = useAuth();

  const invoicesQuery = useQuery({
    queryKey: ['my-invoices'],
    queryFn: () => api.get<Invoice[]>('/api/invoices/me'),
  });
  const announcementsQuery = useQuery({
    queryKey: ['announcements'],
    queryFn: () => api.get<Announcement[]>('/api/announcements'),
  });

  const invoices = invoicesQuery.data ?? [];
  const nextOpenInvoice = invoices
    .filter((i) => i.status === 'OPEN' || i.status === 'OVERDUE')
    .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())[0];

  const recentAnnouncements = [...(announcementsQuery.data ?? [])]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5);

  const firstName = (user?.fullName ?? user?.username ?? '').split(' ')[0];

  return (
    <div>
      <div className="relative mb-6 overflow-hidden rounded-2xl bg-gradient-to-br from-emerald-700 to-emerald-900 p-6 text-white shadow-sm">
        <div className="pointer-events-none absolute -right-10 -top-16 h-48 w-48 rounded-full bg-amber-400/20 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-16 right-24 h-40 w-40 rounded-full bg-emerald-300/10 blur-3xl" />
        <div className="relative flex items-center gap-4">
          <Avatar name={user?.username ?? '?'} photoUrl={assetUrl(user?.profileImagePath)} size="lg" />
          <div>
            <p className="text-sm text-emerald-200">{greeting()}</p>
            <h1 className="text-2xl font-bold">{firstName || 'daar'} 👋</h1>
            <p className="mt-1 text-sm text-emerald-100">Hier is je overzicht voor vandaag.</p>
          </div>
        </div>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <StatCard label="Mijn kamer" value={user?.roomName ?? 'Geen kamer'} icon={BedIcon} />
        <StatCard
          label="Openstaande factuur"
          value={nextOpenInvoice ? `€${nextOpenInvoice.amount.toFixed(2)}` : 'Geen'}
          hint={
            nextOpenInvoice
              ? `${STATUS_LABEL[nextOpenInvoice.status] ?? nextOpenInvoice.status} · vervalt ${nextOpenInvoice.dueDate}`
              : undefined
          }
          icon={BanknotesIcon}
        />
        <StatCard label="Huur per maand" value={user?.rentAmount ? `€${user.rentAmount.toFixed(2)}` : '-'} icon={EuroIcon} />
      </div>

      <Card title="Laatste aankondigingen">
        <div className="space-y-3">
          {recentAnnouncements.map((a) => {
            const Icon = ANNOUNCEMENT_TYPE_ICON[a.type] ?? MegaphoneIcon;
            return (
              <div key={a.id} className="flex items-start gap-3 border-b border-stone-100 pb-3 last:border-0 last:pb-0">
                <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                  <Icon className="h-4 w-4" />
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-medium text-stone-800">{a.title}</span>
                    <span className="shrink-0 text-xs text-stone-400">{new Date(a.createdAt).toLocaleDateString('nl-NL')}</span>
                  </div>
                  <span className="text-xs text-stone-500">{ANNOUNCEMENT_TYPE_LABEL[a.type] ?? a.type}</span>
                </div>
              </div>
            );
          })}
          {recentAnnouncements.length === 0 && <p className="text-sm text-stone-400">Nog geen aankondigingen.</p>}
        </div>
      </Card>
    </div>
  );
}
