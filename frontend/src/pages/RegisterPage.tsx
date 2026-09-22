import { useState, type FormEvent } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../lib/apiClient';
import { Banner, Button, Field, Input } from '../components/ui';

export function RegisterPage() {
  const { register, role } = useAuth();
  const [organizationName, setOrganizationName] = useState('');
  const [adminUsername, setAdminUsername] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Na registreren wordt de rol hier meteen ROLE_ADMIN; deze guard stuurt
  // dan door naar de wizard in plaats van naar het algemene /admin, zodat
  // een net aangemaakt huis altijd met de setup-flow start.
  if (role === 'ROLE_ADMIN') return <Navigate to="/admin/setup" replace />;
  if (role === 'ROLE_STUDENT') return <Navigate to="/student" replace />;
  if (role === 'ROLE_CLEANER') return <Navigate to="/cleaner" replace />;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);

    if (adminPassword !== confirmPassword) {
      setError('Wachtwoorden komen niet overeen.');
      return;
    }

    setIsSubmitting(true);
    try {
      await register({ organizationName, adminUsername, adminEmail, adminPassword });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Registreren mislukt.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-sand px-4 py-10">
      <div className="w-full max-w-sm rounded-2xl border border-stone-200 bg-white p-8 shadow-sm shadow-stone-200/50">
        <h1 className="mb-1 text-xl font-bold text-emerald-700">CasaCrew</h1>
        <p className="mb-6 text-sm text-stone-500">Richt je huis in als beheerder</p>
        {error && <Banner kind="error" message={error} />}
        <form onSubmit={handleSubmit}>
          <Field label="Naam van je huis/organisatie">
            <Input
              required
              value={organizationName}
              onChange={(e) => setOrganizationName(e.target.value)}
              placeholder="Bijv. Villa Vredestein"
            />
          </Field>
          <Field label="Jouw naam">
            <Input
              required
              minLength={2}
              value={adminUsername}
              onChange={(e) => setAdminUsername(e.target.value)}
              autoComplete="name"
            />
          </Field>
          <Field label="E-mailadres">
            <Input
              type="email"
              required
              value={adminEmail}
              onChange={(e) => setAdminEmail(e.target.value)}
              autoComplete="email"
            />
          </Field>
          <Field label="Wachtwoord">
            <Input
              type="password"
              required
              minLength={8}
              value={adminPassword}
              onChange={(e) => setAdminPassword(e.target.value)}
              autoComplete="new-password"
            />
          </Field>
          <Field label="Bevestig wachtwoord">
            <Input
              type="password"
              required
              minLength={8}
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
            />
          </Field>
          <Button type="submit" disabled={isSubmitting} className="w-full">
            {isSubmitting ? 'Bezig...' : 'Huis aanmaken'}
          </Button>
        </form>
        <p className="mt-4 text-center text-sm text-stone-500">
          Al een account?{' '}
          <Link to="/login" className="font-medium text-emerald-700 hover:underline">
            Log in
          </Link>
        </p>
      </div>
    </div>
  );
}
