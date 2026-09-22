import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { api, getToken, setToken } from '../lib/apiClient';
import type { LoginResponse, OrganizationRegistrationRequest, Role, UserResponse } from '../lib/types';

interface AuthState {
  user: UserResponse | null;
  role: Role | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: OrganizationRegistrationRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthState | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [role, setRole] = useState<Role | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  async function refreshUser() {
    try {
      const me = await api.get<UserResponse>('/api/users/me');
      setUser(me);
    } catch {
      setToken(null);
      setUser(null);
      setRole(null);
    }
  }

  useEffect(() => {
    const token = getToken();
    const storedRole = localStorage.getItem('casacrew_role') as Role | null;
    if (token && storedRole) {
      setRole(storedRole);
      refreshUser().finally(() => setIsLoading(false));
    } else {
      setIsLoading(false);
    }
  }, []);

  function applySession(response: LoginResponse) {
    setToken(response.token);
    localStorage.setItem('casacrew_role', response.role);
    setRole(response.role);
    setUser(response.user);
  }

  async function login(email: string, password: string) {
    const response = await api.post<LoginResponse>('/api/auth/login', { email, password });
    applySession(response);
  }

  async function register(payload: OrganizationRegistrationRequest) {
    const response = await api.post<LoginResponse>('/api/organizations', payload);
    applySession(response);
  }

  function logout() {
    api.post('/api/auth/logout').catch(() => undefined);
    setToken(null);
    localStorage.removeItem('casacrew_role');
    setUser(null);
    setRole(null);
  }

  return (
    <AuthContext.Provider value={{ user, role, isLoading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
