import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError, downloadFile } from '../../lib/apiClient';
import type { Expense, ExpenseCategory } from '../../lib/types';
import { Banner, Button, Card, Field, Input, StatCard, Table, Textarea } from '../../components/ui';
import { BanknotesIcon, CalendarIcon, EuroIcon } from '../../components/icons';

const CATEGORY_LABEL: Record<ExpenseCategory, string> = {
  ONDERHOUD: 'Onderhoud',
  SCHOONMAAK: 'Schoonmaakmiddelen',
  REPARATIE: 'Reparatie',
  INVENTARIS: 'Inventaris / meubels',
  NUTSVOORZIENINGEN: 'Nutsvoorzieningen',
  OVERIG: 'Overig',
};

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export function ExpensesPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const expensesQuery = useQuery({
    queryKey: ['expenses'],
    queryFn: () => api.get<Expense[]>('/api/admin/expenses'),
  });

  const createMutation = useMutation({
    mutationFn: (body: Omit<Expense, 'id' | 'createdByUsername'>) => api.post('/api/admin/expenses', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      setShowForm(false);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Omit<Expense, 'id' | 'createdByUsername'> }) =>
      api.put(`/api/admin/expenses/${id}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['expenses'] });
      setEditingId(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/admin/expenses/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['expenses'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function readForm(form: HTMLFormElement): Omit<Expense, 'id' | 'createdByUsername'> {
    const data = new FormData(form);
    return {
      category: data.get('category') as ExpenseCategory,
      description: String(data.get('description') || ''),
      amount: Number(data.get('amount') || 0),
      expenseDate: String(data.get('expenseDate') || todayIso()),
    };
  }

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    createMutation.mutate(readForm(e.currentTarget));
  }

  function handleUpdate(e: FormEvent<HTMLFormElement>, id: number) {
    e.preventDefault();
    setError(null);
    updateMutation.mutate({ id, body: readForm(e.currentTarget) });
  }

  const expenses = expensesQuery.data ?? [];
  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth();

  const totalThisYear = expenses
    .filter((e) => new Date(e.expenseDate).getFullYear() === currentYear)
    .reduce((sum, e) => sum + e.amount, 0);
  const totalThisMonth = expenses
    .filter((e) => {
      const d = new Date(e.expenseDate);
      return d.getFullYear() === currentYear && d.getMonth() === currentMonth;
    })
    .reduce((sum, e) => sum + e.amount, 0);

  const perCategory = expenses.reduce<Record<string, number>>((acc, e) => {
    acc[e.category] = (acc[e.category] ?? 0) + e.amount;
    return acc;
  }, {});

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Uitgaven</h1>
        <div className="space-x-2">
          <Button variant="secondary" onClick={() => downloadFile('/api/admin/expenses/export.csv', 'uitgaven.csv')}>
            Exporteer CSV
          </Button>
          <Button variant="secondary" onClick={() => downloadFile('/api/admin/expenses/export.pdf', 'uitgaven.pdf')}>
            Exporteer PDF
          </Button>
          <Button onClick={() => setShowForm((v) => !v)}>{showForm ? 'Annuleren' : 'Nieuwe uitgave'}</Button>
        </div>
      </div>
      {error && <Banner kind="error" message={error} />}

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <StatCard label="Totaal dit jaar" value={`€${totalThisYear.toFixed(2)}`} icon={BanknotesIcon} />
        <StatCard label="Totaal deze maand" value={`€${totalThisMonth.toFixed(2)}`} icon={CalendarIcon} />
        <StatCard label="Aantal uitgaven" value={expenses.length} icon={EuroIcon} />
      </div>

      {Object.keys(perCategory).length > 0 && (
        <Card title="Per categorie">
          <div className="space-y-2">
            {Object.entries(perCategory)
              .sort((a, b) => b[1] - a[1])
              .map(([category, total]) => (
                <div key={category} className="flex items-center justify-between text-sm">
                  <span className="text-stone-600">{CATEGORY_LABEL[category as ExpenseCategory] ?? category}</span>
                  <span className="font-medium text-stone-800">€{total.toFixed(2)}</span>
                </div>
              ))}
          </div>
        </Card>
      )}

      {showForm && (
        <div className="mt-6">
          <Card title="Nieuwe uitgave">
            <form onSubmit={handleCreate}>
              <Field label="Categorie">
                <select name="category" required defaultValue="ONDERHOUD" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                  {Object.entries(CATEGORY_LABEL).map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </Field>
              <Field label="Omschrijving">
                <Textarea name="description" rows={2} required />
              </Field>
              <Field label="Bedrag (€)">
                <Input name="amount" type="number" step="0.01" min="0" required />
              </Field>
              <Field label="Datum">
                <Input name="expenseDate" type="date" defaultValue={todayIso()} required />
              </Field>
              <Button type="submit" disabled={createMutation.isPending}>
                {createMutation.isPending ? 'Bezig...' : 'Toevoegen'}
              </Button>
            </form>
          </Card>
        </div>
      )}

      <div className="mt-6">
        <Card>
          <Table head={['Datum', 'Categorie', 'Omschrijving', 'Bedrag', '']}>
            {expenses.map((expense) =>
              editingId === expense.id ? (
                <tr key={expense.id}>
                  <td colSpan={5} className="py-3">
                    <form onSubmit={(e) => handleUpdate(e, expense.id)} className="grid grid-cols-2 gap-x-4">
                      <Field label="Categorie">
                        <select name="category" required defaultValue={expense.category} className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
                          {Object.entries(CATEGORY_LABEL).map(([value, label]) => (
                            <option key={value} value={value}>{label}</option>
                          ))}
                        </select>
                      </Field>
                      <Field label="Datum">
                        <Input name="expenseDate" type="date" defaultValue={expense.expenseDate} required />
                      </Field>
                      <div className="col-span-2">
                        <Field label="Omschrijving">
                          <Textarea name="description" rows={2} defaultValue={expense.description} required />
                        </Field>
                      </div>
                      <Field label="Bedrag (€)">
                        <Input name="amount" type="number" step="0.01" min="0" defaultValue={expense.amount} required />
                      </Field>
                      <div className="col-span-2 space-x-2">
                        <Button type="submit" disabled={updateMutation.isPending}>Opslaan</Button>
                        <Button type="button" variant="secondary" onClick={() => setEditingId(null)}>Annuleren</Button>
                      </div>
                    </form>
                  </td>
                </tr>
              ) : (
                <tr key={expense.id}>
                  <td className="py-2 pr-4">{expense.expenseDate}</td>
                  <td className="py-2 pr-4">{CATEGORY_LABEL[expense.category] ?? expense.category}</td>
                  <td className="py-2 pr-4">{expense.description}</td>
                  <td className="py-2 pr-4">€{expense.amount.toFixed(2)}</td>
                  <td className="py-2 text-right">
                    <button className="mr-3 text-xs font-medium text-emerald-700 hover:underline" onClick={() => setEditingId(expense.id)}>
                      Bewerken
                    </button>
                    <button
                      className="text-xs font-medium text-red-600 hover:underline"
                      onClick={() => {
                        if (confirm(`Uitgave "${expense.description}" verwijderen?`)) deleteMutation.mutate(expense.id);
                      }}
                    >
                      Verwijderen
                    </button>
                  </td>
                </tr>
              ),
            )}
            {expenses.length === 0 && (
              <tr>
                <td colSpan={5} className="py-6 text-center text-stone-400">Nog geen uitgaven geregistreerd</td>
              </tr>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
