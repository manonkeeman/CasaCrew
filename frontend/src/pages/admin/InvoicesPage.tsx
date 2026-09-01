import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError, downloadFile } from '../../lib/apiClient';
import type { Invoice, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, StatCard, Table, Textarea } from '../../components/ui';
import { BanknotesIcon, ClockIcon, ExclamationBubbleIcon } from '../../components/icons';

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
  CANCELLED: 'bg-stone-100 text-stone-500',
};

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export function InvoicesPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const invoicesQuery = useQuery({
    queryKey: ['invoices'],
    queryFn: () => api.get<Invoice[]>('/api/invoices'),
  });
  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT');

  const createMutation = useMutation({
    mutationFn: (body: {
      title: string;
      description?: string;
      amount: number;
      issueDate: string;
      dueDate: string;
      studentEmail: string;
    }) => api.post('/api/invoices', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['invoices'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      api.put(`/api/invoices/${id}/status?status=${status}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['invoices'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/invoices/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['invoices'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      title: String(form.get('title') || ''),
      description: String(form.get('description') || '') || undefined,
      amount: Number(form.get('amount') || 0),
      issueDate: String(form.get('issueDate') || todayIso()),
      dueDate: String(form.get('dueDate') || todayIso()),
      studentEmail: String(form.get('studentEmail') || ''),
    });
  }

  const invoices = invoicesQuery.data ?? [];
  const openInvoices = invoices.filter((i) => i.status === 'OPEN' || i.status === 'OVERDUE');
  const overdueInvoices = invoices.filter((i) => i.status === 'OVERDUE');
  const openTotal = openInvoices.reduce((sum, i) => sum + i.amount, 0);
  const paidThisYear = invoices
    .filter((i) => i.status === 'PAID' && i.paidAt && new Date(i.paidAt).getFullYear() === new Date().getFullYear())
    .reduce((sum, i) => sum + i.amount, 0);

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Facturen</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe factuur'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Openstaand" value={`€${openTotal.toFixed(2)}`} hint={`${openInvoices.length} facturen`} icon={BanknotesIcon} />
        <StatCard label="Ontvangen dit jaar" value={`€${paidThisYear.toFixed(2)}`} icon={ClockIcon} />
        <StatCard label="Te laat" value={overdueInvoices.length} icon={ExclamationBubbleIcon} />
      </div>

      {showForm && (
        <div className="mb-6">
          <Card title="Nieuwe factuur">
            <form onSubmit={handleCreate}>
              <Field label="Student">
                <select name="studentEmail" required className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                  <option value="">Kies een student...</option>
                  {students.map((s) => (
                    <option key={s.id} value={s.email}>
                      {s.username} ({s.email})
                    </option>
                  ))}
                </select>
              </Field>
              <Field label="Titel">
                <Input name="title" required maxLength={120} placeholder="Huur september" />
              </Field>
              <Field label="Omschrijving (optioneel)">
                <Textarea name="description" rows={2} />
              </Field>
              <Field label="Bedrag (€)">
                <Input name="amount" type="number" step="0.01" min="0.01" required />
              </Field>
              <div className="grid grid-cols-2 gap-x-4">
                <Field label="Factuurdatum">
                  <Input name="issueDate" type="date" defaultValue={todayIso()} required />
                </Field>
                <Field label="Vervaldatum">
                  <Input name="dueDate" type="date" defaultValue={todayIso()} required />
                </Field>
              </div>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Factuur aanmaken'}
              </Button>
            </form>
          </Card>
        </div>
      )}

      <Card>
        <Table head={['Student', 'Titel', 'Bedrag', 'Vervaldatum', 'Status', '']}>
          {invoices.map((invoice) => (
            <tr key={invoice.id}>
              <td className="py-2 pr-4">{invoice.studentName}</td>
              <td className="py-2 pr-4">{invoice.title}</td>
              <td className="py-2 pr-4">€{invoice.amount.toFixed(2)}</td>
              <td className="py-2 pr-4">{invoice.dueDate}</td>
              <td className="py-2 pr-4">
                <select
                  value={invoice.status}
                  onChange={(e) => statusMutation.mutate({ id: invoice.id, status: e.target.value })}
                  className={`rounded-full border-0 px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[invoice.status] ?? ''}`}
                >
                  {Object.entries(STATUS_LABEL).map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </td>
              <td className="py-2 text-right whitespace-nowrap">
                <button
                  className="mr-3 text-xs font-medium text-emerald-700 hover:underline"
                  onClick={() => downloadFile(`/api/invoices/${invoice.id}/pdf`, `factuur-${invoice.id}.pdf`)}
                >
                  PDF
                </button>
                <button
                  className="text-xs font-medium text-red-600 hover:underline"
                  onClick={() => {
                    if (confirm(`Factuur "${invoice.title}" voor ${invoice.studentName} verwijderen?`)) deleteMutation.mutate(invoice.id);
                  }}
                >
                  Verwijderen
                </button>
              </td>
            </tr>
          ))}
          {invoices.length === 0 && (
            <tr>
              <td colSpan={6} className="py-6 text-center text-stone-400">Nog geen facturen</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
