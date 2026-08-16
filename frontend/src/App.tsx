import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './components/ProtectedRoute';
import { DashboardLayout } from './layouts/DashboardLayout';
import { LoginPage } from './pages/LoginPage';
import { StudentsPage } from './pages/admin/StudentsPage';
import { RoomsPage } from './pages/admin/RoomsPage';
import { CleanerPage } from './pages/admin/CleanerPage';
import { OrganizationProfilePage } from './pages/admin/OrganizationProfilePage';
import { PaymentSettingsPage } from './pages/admin/PaymentSettingsPage';
import { RentSettingsPage } from './pages/admin/RentSettingsPage';
import { EmailTemplatesPage } from './pages/admin/EmailTemplatesPage';
import { ProfilePage } from './pages/student/ProfilePage';
import { InvoicesPage } from './pages/student/InvoicesPage';
import { AnnouncementsPage } from './pages/student/AnnouncementsPage';

const ADMIN_NAV = [
  { to: '/admin/students', label: 'Studenten' },
  { to: '/admin/rooms', label: 'Kamers' },
  { to: '/admin/cleaner', label: 'Schoonmaakaccount' },
  { to: '/admin/organization', label: 'Huisprofiel' },
  { to: '/admin/payment-settings', label: 'Betaalgegevens' },
  { to: '/admin/rent-settings', label: 'Huurregels' },
  { to: '/admin/email-templates', label: 'E-mailsjablonen' },
];

const STUDENT_NAV = [
  { to: '/student/profile', label: 'Mijn profiel' },
  { to: '/student/invoices', label: 'Mijn facturen' },
  { to: '/student/announcements', label: 'Mededelingen' },
];

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute allow={['ROLE_ADMIN']} />}>
        <Route element={<DashboardLayout title="Beheer" navItems={ADMIN_NAV} />}>
          <Route path="/admin" element={<Navigate to="/admin/students" replace />} />
          <Route path="/admin/students" element={<StudentsPage />} />
          <Route path="/admin/rooms" element={<RoomsPage />} />
          <Route path="/admin/cleaner" element={<CleanerPage />} />
          <Route path="/admin/organization" element={<OrganizationProfilePage />} />
          <Route path="/admin/payment-settings" element={<PaymentSettingsPage />} />
          <Route path="/admin/rent-settings" element={<RentSettingsPage />} />
          <Route path="/admin/email-templates" element={<EmailTemplatesPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allow={['ROLE_STUDENT']} />}>
        <Route element={<DashboardLayout title="Mijn kamer" navItems={STUDENT_NAV} />}>
          <Route path="/student" element={<Navigate to="/student/profile" replace />} />
          <Route path="/student/profile" element={<ProfilePage />} />
          <Route path="/student/invoices" element={<InvoicesPage />} />
          <Route path="/student/announcements" element={<AnnouncementsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
