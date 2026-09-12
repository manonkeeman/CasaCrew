import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import type { Payment } from '../../lib/types';
import { Card, Table } from '../../components/ui';

const STATUS_LABEL: Record<string, string> = {
  OPEN: 'Open',
  PAID: 'Betaald',
  OVERDUE: 'Te laat',
  CANCELLED: 'Geannuleerd',
};

const STATUS_STYLE: Record<string, string> = {
  OPEN: 'bg-amber-50 text-amber-700',
  PAID: 'bg-emerald-50 text-emerald-700',
  OVERDUE: 'bg-red-50 text-red-700',
  CANCELLED: 'bg-slate-100 text-slate-500',
};

export function PaymentsPage() {
  const query = useQuery({
    queryKey: ['my-payments'],
    queryFn: () => api.get<Payment[]>('/api/payments/me'),
  });

  const payments = query.data ?? [];

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-stone-800">Mijn betalingen</h1>
      <Card>
        <Table head={['Omschrijving', 'Bedrag', 'Betaald op', 'Status']}>
          {payments.map((payment) => (
            <tr key={payment.id}>
              <td className="py-2 pr-4">{payment.description ?? '-'}</td>
              <td className="py-2 pr-4">€{payment.amount.toFixed(2)}</td>
              <td className="py-2 pr-4">{payment.paidAt ? new Date(payment.paidAt).toLocaleDateString('nl-NL') : '-'}</td>
              <td className="py-2 pr-4">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[payment.status] ?? ''}`}>
                  {STATUS_LABEL[payment.status] ?? payment.status}
                </span>
              </td>
            </tr>
          ))}
          {payments.length === 0 && (
            <tr>
              <td colSpan={4} className="py-6 text-center text-stone-400">Nog geen betalingen</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
