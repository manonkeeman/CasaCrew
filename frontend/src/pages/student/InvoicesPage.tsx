import { useQuery } from '@tanstack/react-query';
import { api, downloadFile } from '../../lib/apiClient';
import type { Invoice } from '../../lib/types';
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

export function InvoicesPage() {
  const query = useQuery({
    queryKey: ['my-invoices'],
    queryFn: () => api.get<Invoice[]>('/api/invoices/me'),
  });

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Mijn facturen</h1>
      <Card>
        <Table head={['Titel', 'Bedrag', 'Vervaldatum', 'Status', '']}>
          {(query.data ?? []).map((invoice) => (
            <tr key={invoice.id}>
              <td className="py-2 pr-4">{invoice.title}</td>
              <td className="py-2 pr-4">€{invoice.amount.toFixed(2)}</td>
              <td className="py-2 pr-4">{invoice.dueDate}</td>
              <td className="py-2 pr-4">
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[invoice.status] ?? ''}`}>
                  {STATUS_LABEL[invoice.status] ?? invoice.status}
                </span>
              </td>
              <td className="py-2 text-right">
                <button
                  className="text-xs font-medium text-emerald-700 hover:underline"
                  onClick={() => downloadFile(`/api/invoices/${invoice.id}/pdf`, `factuur-${invoice.id}.pdf`)}
                >
                  Download PDF
                </button>
              </td>
            </tr>
          ))}
          {(query.data ?? []).length === 0 && (
            <tr>
              <td colSpan={5} className="py-6 text-center text-slate-400">Nog geen facturen</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
