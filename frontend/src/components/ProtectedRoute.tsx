import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../lib/types';

export function ProtectedRoute({ allow }: { allow: Role[] }) {
  const { role, isLoading } = useAuth();

  if (isLoading) {
    return <div className="flex h-screen items-center justify-center text-slate-500">Laden...</div>;
  }

  if (!role) {
    return <Navigate to="/login" replace />;
  }

  if (!allow.includes(role)) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
