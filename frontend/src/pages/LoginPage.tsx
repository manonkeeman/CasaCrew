import { useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../lib/apiClient';
import { Banner, Button, Field, Input } from '../components/ui';

export function LoginPage() {
  const { login, role } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (role === 'ROLE_ADMIN') return <Navigate to="/admin" replace />;
  if (role === 'ROLE_STUDENT') return <Navigate to="/student" replace />;
  if (role === 'ROLE_CLEANER') return <Navigate to="/cleaner" replace />;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await login(email, password);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Inloggen mislukt.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-sand px-4">
      <div className="w-full max-w-sm rounded-2xl border border-stone-200 bg-white p-8 shadow-sm shadow-stone-200/50">
        <h1 className="mb-1 text-xl font-bold text-emerald-700">CasaCrew</h1>
        <p className="mb-6 text-sm text-stone-500">Log in op je dashboard</p>
        {error && <Banner kind="error" message={error} />}
        <form onSubmit={handleSubmit}>
          <Field label="E-mailadres">
            <Input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
          </Field>
          <Field label="Wachtwoord">
            <Input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
          </Field>
          <Button type="submit" disabled={isSubmitting} className="w-full">
            {isSubmitting ? 'Bezig...' : 'Inloggen'}
          </Button>
        </form>
      </div>
    </div>
  );
}
