import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { Payment, PaymentStatus, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Table, Textarea } from '../../components/ui';

const STATUS_LABEL: Record<PaymentStatus, string> = {
  OPEN: 'Open',
  PAID: 'Betaald',
  OVERDUE: 'Te laat',
  CANCELLED: 'Geannuleerd',
};

const STATUS_STYLE: Record<PaymentStatus, string> = {
  OPEN: 'bg-amber-50 text-amber-700',
  PAID: 'bg-emerald-50 text-emerald-700',
  OVERDUE: 'bg-red-50 text-red-700',
  CANCELLED: 'bg-slate-100 text-slate-500',
};

interface CreatePaymentPayload {
  amount: number;
  description: string;
  status: PaymentStatus;
  paidAt: string | null;
  studentEmail: string;
}

export function PaymentsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);

  const paymentsQuery = useQuery({
    queryKey: ['payments'],
    queryFn: () => api.get<Payment[]>('/api/payments'),
  });

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });

  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT' || u.role === 'ROLE_STUDENT');

  const createMutation = useMutation({
    mutationFn: (body: CreatePaymentPayload) => api.post('/api/payments', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/payments/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['payments'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const data = new FormData(e.currentTarget);
    const paidAtDate = String(data.get('paidAt') || '');
    createMutation.mutate({
      amount: Number(data.get('amount') || 0),
      description: String(data.get('description') || ''),
      status: data.get('status') as PaymentStatus,
      paidAt: paidAtDate ? `${paidAtDate}T00:00:00` : null,
      studentEmail: String(data.get('studentEmail') || ''),
    });
  }

  const payments = paymentsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Betalingen</h1>
        <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe betaling'}</Button>
      </div>
      {error && <Banner kind="error" message={error} />}

      {showForm && (
        <Card title="Nieuwe betaling">
          <form onSubmit={handleCreate} className="grid grid-cols-2 gap-x-4">
            <Field label="Student">
              <select name="studentEmail" required className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                <option value="">Kies een student</option>
                {students.map((s) => (
                  <option key={s.id} value={s.email}>{s.username} ({s.email})</option>
                ))}
              </select>
            </Field>
            <Field label="Bedrag (€)">
              <Input name="amount" type="number" step="0.01" min="0.01" required />
            </Field>
            <Field label="Status">
              <select name="status" required defaultValue="OPEN" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                {Object.entries(STATUS_LABEL).map(([value, label]) => (
                  <option key={value} value={value}>{label}</option>
                ))}
              </select>
            </Field>
            <Field label="Betaald op (optioneel)">
              <Input name="paidAt" type="date" />
            </Field>
            <div className="col-span-2">
              <Field label="Omschrijving">
                <Textarea name="description" rows={2} />
              </Field>
            </div>
            <div className="col-span-2">
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Toevoegen'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      <div className="mt-6">
        <Card>
          <Table head={['Student', 'Omschrijving', 'Bedrag', 'Betaald op', 'Status', '']}>
            {payments.map((payment) => (
              <tr key={payment.id}>
                <td className="py-2 pr-4">{payment.studentName}</td>
                <td className="py-2 pr-4">{payment.description ?? '-'}</td>
                <td className="py-2 pr-4">€{payment.amount.toFixed(2)}</td>
                <td className="py-2 pr-4">{payment.paidAt ? new Date(payment.paidAt).toLocaleDateString('nl-NL') : '-'}</td>
                <td className="py-2 pr-4">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLE[payment.status] ?? ''}`}>
                    {STATUS_LABEL[payment.status] ?? payment.status}
                  </span>
                </td>
                <td className="py-2 text-right">
                  <button
                    className="text-xs font-medium text-red-600 hover:underline"
                    onClick={() => {
                      if (confirm('Deze betaling verwijderen?')) deleteMutation.mutate(payment.id);
                    }}
                  >
                    Verwijderen
                  </button>
                </td>
              </tr>
            ))}
            {payments.length === 0 && (
              <tr>
                <td colSpan={6} className="py-6 text-center text-stone-400">Nog geen betalingen geregistreerd</td>
              </tr>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
