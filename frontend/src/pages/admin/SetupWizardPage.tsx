import { useState, type FormEvent, type ReactNode } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../../lib/apiClient';
import type {
  OnboardingStatus,
  OrganizationPaymentSettings,
  OrganizationProfile,
  OrganizationRentSettings,
  Room,
  UserResponse,
} from '../../lib/types';
import { Banner, Button, Field, Input, Table, Textarea } from '../../components/ui';

interface StepDef {
  key: keyof OnboardingStatus;
  label: string;
  description: string;
  render: () => ReactNode;
}

function StepShell({ title, description, children }: { title: string; description: string; children: ReactNode }) {
  return (
    <div>
      <h2 className="mb-1 text-lg font-semibold text-stone-800">{title}</h2>
      <p className="mb-5 text-sm text-stone-500">{description}</p>
      {children}
    </div>
  );
}

function StepProfile() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['organization-profile'],
    queryFn: () => api.get<OrganizationProfile>('/api/admin/organization/profile'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationProfile) => api.put<OrganizationProfile>('/api/admin/organization/profile', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organization-profile'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    mutation.mutate({
      name: String(form.get('name') || ''),
      address: String(form.get('address') || '') || null,
    });
  }

  if (query.isLoading) return <p className="text-stone-400">Laden...</p>;

  return (
    <StepShell title="Huisprofiel" description="De naam en het adres van je huis, zichtbaar voor studenten en op documenten.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleSubmit} className="max-w-md">
        <Field label="Naam">
          <Input name="name" defaultValue={query.data?.name} required />
        </Field>
        <Field label="Adres">
          <Input name="address" defaultValue={query.data?.address ?? ''} placeholder="Straat 1, 1234 AB Plaats" required />
        </Field>
        <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
      </form>
    </StepShell>
  );
}

function StepRooms() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const roomsQuery = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/api/rooms'),
  });

  const createMutation = useMutation({
    mutationFn: (name: string) => api.post('/api/rooms', { name }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const name = String(form.get('name') || '').trim();
    if (!name) return;
    createMutation.mutate(name);
    e.currentTarget.reset();
  }

  const rooms = roomsQuery.data ?? [];

  return (
    <StepShell title="Kamers" description="Voeg de kamers toe die je wilt verhuren. Je kan er later altijd meer toevoegen.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleCreate} className="mb-5 flex items-end gap-3">
        <Field label="Kamernaam of -nummer">
          <Input name="name" required placeholder="bv. Kamer 3" />
        </Field>
        <Button type="submit" disabled={createMutation.isPending}>Toevoegen</Button>
      </form>
      {rooms.length > 0 ? (
        <ul className="space-y-1 text-sm text-stone-600">
          {rooms.map((room) => (
            <li key={room.id} className="rounded-lg bg-stone-50 px-3 py-2">{room.name}</li>
          ))}
        </ul>
      ) : (
        <p className="text-sm text-stone-400">Nog geen kamers toegevoegd.</p>
      )}
    </StepShell>
  );
}

function StepRentSettings() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['rent-settings'],
    queryFn: () => api.get<OrganizationRentSettings>('/api/admin/organization/rent-settings'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationRentSettings) =>
      api.put<OrganizationRentSettings>('/api/admin/organization/rent-settings', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rent-settings'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    const amount = form.get('defaultRentAmount');
    const invoiceDay = form.get('rentInvoiceDayOfMonth');
    const dueDay = form.get('rentDueDayOfMonth');
    mutation.mutate({
      defaultRentAmount: amount ? Number(amount) : null,
      rentInvoiceDayOfMonth: invoiceDay ? Number(invoiceDay) : null,
      rentDueDayOfMonth: dueDay ? Number(dueDay) : null,
    });
  }

  if (query.isLoading) return <p className="text-stone-400">Laden...</p>;

  return (
    <StepShell title="Huurinstellingen" description="Standaardbedrag en de dagen waarop facturen worden aangemaakt en vervallen.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleSubmit} className="max-w-md">
        <Field label="Standaard huurbedrag (€)">
          <Input name="defaultRentAmount" type="number" step="0.01" min="0.01" defaultValue={query.data?.defaultRentAmount ?? ''} required />
        </Field>
        <Field label="Dag van de maand waarop facturen worden aangemaakt (1-28)">
          <Input name="rentInvoiceDayOfMonth" type="number" min="1" max="28" defaultValue={query.data?.rentInvoiceDayOfMonth ?? 1} required />
        </Field>
        <Field label="Dag van de maand waarop de huur vervalt (1-28)">
          <Input name="rentDueDayOfMonth" type="number" min="1" max="28" defaultValue={query.data?.rentDueDayOfMonth ?? 8} required />
        </Field>
        <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
      </form>
    </StepShell>
  );
}

function StepPaymentSettings() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['payment-settings'],
    queryFn: () => api.get<OrganizationPaymentSettings>('/api/admin/organization/payment-settings'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationPaymentSettings) =>
      api.put<OrganizationPaymentSettings>('/api/admin/organization/payment-settings', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payment-settings'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    mutation.mutate({
      bunqMeUsername: String(form.get('bunqMeUsername') || '') || null,
      iban: String(form.get('iban') || '') || null,
      accountHolderName: String(form.get('accountHolderName') || '') || null,
    });
  }

  if (query.isLoading) return <p className="text-stone-400">Laden...</p>;

  return (
    <StepShell title="Betaalgegevens" description="Wordt gebruikt in huurherinneringen naar studenten (WhatsApp/e-mail).">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleSubmit} className="max-w-md">
        <Field label="IBAN">
          <Input name="iban" defaultValue={query.data?.iban ?? ''} placeholder="NL00 BANK 0123456789" required />
        </Field>
        <Field label="Naam rekeninghouder">
          <Input name="accountHolderName" defaultValue={query.data?.accountHolderName ?? ''} required />
        </Field>
        <Field label="Bunq.me-gebruikersnaam (optioneel)">
          <Input name="bunqMeUsername" defaultValue={query.data?.bunqMeUsername ?? ''} placeholder="JouwBunqNaam" />
        </Field>
        <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
      </form>
    </StepShell>
  );
}

interface Huisregel {
  id: number;
  title: string;
  content: string | null;
}

function StepHuisregels() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const rulesQuery = useQuery({
    queryKey: ['huisregels'],
    queryFn: () => api.get<Huisregel[]>('/api/huisregels'),
  });

  const rules = rulesQuery.data ?? [];

  const createMutation = useMutation({
    mutationFn: (body: { title: string; content?: string; orderIndex: number }) => api.post('/api/huisregels', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['huisregels'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      title: String(form.get('title')),
      content: String(form.get('content') || '') || undefined,
      orderIndex: rules.length,
    });
    e.currentTarget.reset();
  }

  return (
    <StepShell title="Huisregels" description="Voeg minstens één huisregel toe. Studenten kunnen deze terugvinden in hun dashboard.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleCreate} className="mb-5 max-w-md">
        <Field label="Titel">
          <Input name="title" required />
        </Field>
        <Field label="Inhoud (optioneel)">
          <Textarea name="content" rows={3} />
        </Field>
        <Button type="submit" disabled={createMutation.isPending}>
          {createMutation.isPending ? 'Bezig...' : 'Regel toevoegen'}
        </Button>
      </form>
      {rules.length > 0 ? (
        <ul className="space-y-1 text-sm text-stone-600">
          {rules.map((rule) => (
            <li key={rule.id} className="rounded-lg bg-stone-50 px-3 py-2 font-medium">{rule.title}</li>
          ))}
        </ul>
      ) : (
        <p className="text-sm text-stone-400">Nog geen huisregels.</p>
      )}
    </StepShell>
  );
}

function StepStudents() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const roomsQuery = useQuery({
    queryKey: ['rooms'],
    queryFn: () => api.get<Room[]>('/api/rooms'),
  });

  const students = (usersQuery.data ?? []).filter((u) => u.role === 'STUDENT');

  const createMutation = useMutation({
    mutationFn: (body: {
      username: string;
      email: string;
      password: string;
      room?: string;
      rentAmount: number;
      sendWelcomeEmail: boolean;
    }) => api.post('/api/admin/students', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['rooms'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      username: String(form.get('username')),
      email: String(form.get('email')),
      password: String(form.get('password')),
      room: String(form.get('room') || '') || undefined,
      rentAmount: Number(form.get('rentAmount')),
      sendWelcomeEmail: form.get('sendWelcomeEmail') === 'on',
    });
    e.currentTarget.reset();
  }

  return (
    <StepShell title="Studenten toewijzen" description="Geef studenten toegang tot hun eigen dashboard, gekoppeld aan een kamer.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleCreate} className="mb-5 grid max-w-2xl grid-cols-2 gap-x-4">
        <Field label="Naam">
          <Input name="username" required minLength={2} />
        </Field>
        <Field label="E-mailadres">
          <Input name="email" type="email" required />
        </Field>
        <Field label="Wachtwoord">
          <Input name="password" type="password" required minLength={8} />
        </Field>
        <Field label="Huurbedrag (€)">
          <Input name="rentAmount" type="number" step="0.01" min="1" required />
        </Field>
        <Field label="Kamer (optioneel)">
          <select name="room" className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm">
            <option value="">Geen kamer</option>
            {(roomsQuery.data ?? []).filter((r) => !r.occupantId).map((r) => (
              <option key={r.id} value={r.name}>{r.name}</option>
            ))}
          </select>
        </Field>
        <label className="mt-6 flex items-center gap-2 text-sm text-stone-600">
          <input type="checkbox" name="sendWelcomeEmail" defaultChecked /> Welkomstmail versturen
        </label>
        <div className="col-span-2 mt-2">
          <Button type="submit" disabled={createMutation.isPending}>
            {createMutation.isPending ? 'Bezig...' : 'Student toevoegen'}
          </Button>
        </div>
      </form>
      <Table head={['Naam', 'E-mail', 'Kamer']}>
        {students.map((s) => (
          <tr key={s.id}>
            <td className="py-2 pr-4">{s.username}</td>
            <td className="py-2 pr-4">{s.email}</td>
            <td className="py-2 pr-4">{s.roomName ?? '-'}</td>
          </tr>
        ))}
        {students.length === 0 && (
          <tr>
            <td colSpan={3} className="py-4 text-center text-stone-400">Nog geen studenten</td>
          </tr>
        )}
      </Table>
    </StepShell>
  );
}

function StepCleaner() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const usersQuery = useQuery({
    queryKey: ['users'],
    queryFn: () => api.get<UserResponse[]>('/api/users'),
  });
  const cleaners = (usersQuery.data ?? []).filter((u) => u.role === 'CLEANER');

  const createMutation = useMutation({
    mutationFn: (body: { username: string; email: string; password: string; role: string }) =>
      api.post('/api/users', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Aanmaken mislukt.'),
  });

  function handleCreate(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = new FormData(e.currentTarget);
    createMutation.mutate({
      username: String(form.get('username')),
      email: String(form.get('email')),
      password: String(form.get('password')),
      role: 'CLEANER',
    });
    e.currentTarget.reset();
  }

  return (
    <StepShell title="Schoonmaakaccount toewijzen" description="Geef de schoonmaker toegang om taken en shifts bij te houden.">
      {error && <Banner kind="error" message={error} />}
      <form onSubmit={handleCreate} className="mb-5 grid max-w-2xl grid-cols-3 gap-4">
        <Field label="Naam">
          <Input name="username" required minLength={2} />
        </Field>
        <Field label="E-mailadres">
          <Input name="email" type="email" required />
        </Field>
        <Field label="Wachtwoord">
          <Input name="password" type="password" required minLength={8} />
        </Field>
        <div className="col-span-3">
          <Button type="submit" disabled={createMutation.isPending}>Aanmaken</Button>
        </div>
      </form>
      <Table head={['Naam', 'E-mail']}>
        {cleaners.map((c) => (
          <tr key={c.id}>
            <td className="py-2 pr-4">{c.username}</td>
            <td className="py-2 pr-4">{c.email}</td>
          </tr>
        ))}
        {cleaners.length === 0 && (
          <tr>
            <td colSpan={2} className="py-4 text-center text-stone-400">Nog geen schoonmaakaccount</td>
          </tr>
        )}
      </Table>
    </StepShell>
  );
}

const STEPS: StepDef[] = [
  { key: 'profileComplete', label: 'Huisprofiel', description: 'Naam en adres', render: () => <StepProfile /> },
  { key: 'roomsComplete', label: 'Kamers', description: 'Minstens 1 kamer', render: () => <StepRooms /> },
  { key: 'rentSettingsComplete', label: 'Huur', description: 'Bedrag en data', render: () => <StepRentSettings /> },
  { key: 'paymentSettingsComplete', label: 'Betalen', description: 'IBAN', render: () => <StepPaymentSettings /> },
  { key: 'huisregelsComplete', label: 'Huisregels', description: 'Minstens 1 regel', render: () => <StepHuisregels /> },
  { key: 'studentsComplete', label: 'Studenten', description: 'Toegang geven', render: () => <StepStudents /> },
  { key: 'cleanerComplete', label: 'Schoonmaak', description: 'Toegang geven', render: () => <StepCleaner /> },
];

export function SetupWizardPage() {
  const navigate = useNavigate();
  const [stepIndex, setStepIndex] = useState(0);

  const statusQuery = useQuery({
    queryKey: ['onboarding-status'],
    queryFn: () => api.get<OnboardingStatus>('/api/admin/organization/onboarding-status'),
  });

  const status = statusQuery.data;
  const activeStep = STEPS[stepIndex];
  const isLastStep = stepIndex === STEPS.length - 1;

  return (
    <div className="min-h-screen bg-sand px-4 py-10">
      <div className="mx-auto max-w-3xl">
        <div className="mb-8 text-center">
          <h1 className="text-xl font-bold text-emerald-700">Welkom bij CasaCrew</h1>
          <p className="mt-1 text-sm text-stone-500">
            Richt je huis in — {status ? `${status.completedSteps} van ${status.totalSteps} stappen voltooid` : 'laden...'}
          </p>
        </div>

        <div className="mb-8 flex items-center justify-between">
          {STEPS.map((step, i) => {
            const done = status?.[step.key] === true;
            const isActive = i === stepIndex;
            return (
              <button
                key={step.label}
                onClick={() => setStepIndex(i)}
                className="flex flex-1 flex-col items-center gap-1.5"
              >
                <span
                  className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold ${
                    done
                      ? 'bg-emerald-600 text-white'
                      : isActive
                        ? 'border-2 border-emerald-600 text-emerald-700'
                        : 'border border-stone-300 text-stone-400'
                  }`}
                >
                  {done ? '✓' : i + 1}
                </span>
                <span className={`text-center text-xs ${isActive ? 'font-semibold text-emerald-700' : 'text-stone-400'}`}>
                  {step.label}
                </span>
              </button>
            );
          })}
        </div>

        <div className="rounded-2xl border border-stone-200 bg-white p-8 shadow-sm shadow-stone-200/50">
          {activeStep.render()}
        </div>

        <div className="mt-6 flex items-center justify-between">
          <Button variant="secondary" disabled={stepIndex === 0} onClick={() => setStepIndex((i) => i - 1)}>
            Vorige
          </Button>
          <div className="flex items-center gap-3">
            {!isLastStep && (
              <button
                className="text-sm font-medium text-stone-400 hover:text-stone-600"
                onClick={() => setStepIndex((i) => i + 1)}
              >
                Sla over
              </button>
            )}
            {isLastStep ? (
              <Button onClick={() => navigate('/admin/dashboard')}>Naar dashboard</Button>
            ) : (
              <Button onClick={() => setStepIndex((i) => i + 1)}>Volgende</Button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
