import { Navigate, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './components/ProtectedRoute';
import { DashboardLayout } from './layouts/DashboardLayout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage as AdminDashboardPage } from './pages/admin/DashboardPage';
import { StudentsPage } from './pages/admin/StudentsPage';
import { RoomsPage } from './pages/admin/RoomsPage';
import { CleanerPage } from './pages/admin/CleanerPage';
import { OrganizationProfilePage } from './pages/admin/OrganizationProfilePage';
import { PaymentSettingsPage } from './pages/admin/PaymentSettingsPage';
import { RentSettingsPage } from './pages/admin/RentSettingsPage';
import { EmailTemplatesPage } from './pages/admin/EmailTemplatesPage';
import { CleaningSchedulePage } from './pages/admin/CleaningSchedulePage';
import { HuisregelsPage as AdminHuisregelsPage } from './pages/admin/HuisregelsPage';
import { NoodgegevensPage as AdminNoodgegevensPage } from './pages/admin/NoodgegevensPage';
import { WasteSchedulePage as AdminWasteSchedulePage } from './pages/admin/WasteSchedulePage';
import { ComplaintsPage as AdminComplaintsPage } from './pages/admin/ComplaintsPage';
import { ExpensesPage as AdminExpensesPage } from './pages/admin/ExpensesPage';
import { InvoicesPage as AdminInvoicesPage } from './pages/admin/InvoicesPage';
import { MaintenanceRequestsPage as AdminMaintenanceRequestsPage } from './pages/admin/MaintenanceRequestsPage';
import { AnnouncementsAdminPage } from './pages/admin/AnnouncementsAdminPage';
import { MaintenanceReportPage } from './pages/shared/MaintenanceReportPage';
import { CalendarPage } from './pages/shared/CalendarPage';
import { DashboardPage as StudentDashboardPage } from './pages/student/DashboardPage';
import { ProfilePage } from './pages/student/ProfilePage';
import { InvoicesPage } from './pages/student/InvoicesPage';
import { AnnouncementsPage } from './pages/student/AnnouncementsPage';
import { HuisregelsPage as StudentHuisregelsPage } from './pages/student/HuisregelsPage';
import { NoodgegevensPage as StudentNoodgegevensPage } from './pages/student/NoodgegevensPage';
import { WasteSchedulePage as StudentWasteSchedulePage } from './pages/student/WasteSchedulePage';
import { ComplaintsPage as StudentComplaintsPage } from './pages/student/ComplaintsPage';
import { HousematesPage } from './pages/student/HousematesPage';
import { DashboardPage as CleanerDashboardPage } from './pages/cleaner/DashboardPage';
import { CleanerTasksPage } from './pages/cleaner/TasksPage';
import { ShiftsPage } from './pages/cleaner/ShiftsPage';
import {
  BanknotesIcon,
  BedIcon,
  BookOpenIcon,
  BuildingIcon,
  CalendarIcon,
  ClipboardCheckIcon,
  CalendarPlusIcon,
  ClockIcon,
  EuroIcon,
  ExclamationBubbleIcon,
  HomeIcon,
  MailIcon,
  MegaphoneIcon,
  PhoneIcon,
  ReceiptIcon,
  RecycleIcon,
  SparklesIcon,
  UserIcon,
  UsersIcon,
  WrenchIcon,
} from './components/icons';

const ADMIN_NAV = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: HomeIcon },
  { to: '/admin/students', label: 'Studenten', icon: UsersIcon },
  { to: '/admin/rooms', label: 'Kamers', icon: BedIcon },
  { to: '/admin/cleaning-schedule', label: 'Schoonmaakrooster', icon: SparklesIcon },
  { to: '/admin/waste-schedule', label: 'Afvalschema', icon: RecycleIcon },
  { to: '/admin/cleaner', label: 'Schoonmaakaccount', icon: UserIcon },
  { to: '/admin/huisregels', label: 'Huisregels', icon: BookOpenIcon },
  { to: '/admin/noodgegevens', label: 'Noodgegevens', icon: PhoneIcon },
  { to: '/admin/complaints', label: 'Klachten', icon: ExclamationBubbleIcon },
  { to: '/admin/maintenance', label: 'Onderhoud', icon: WrenchIcon },
  { to: '/admin/calendar', label: 'Agenda', icon: CalendarPlusIcon },
  { to: '/admin/announcements', label: 'Aankondigingen', icon: MegaphoneIcon },
  { to: '/admin/organization', label: 'Huisprofiel', icon: BuildingIcon },
  { to: '/admin/invoices', label: 'Facturen', icon: ReceiptIcon },
  { to: '/admin/payment-settings', label: 'Betaalgegevens', icon: BanknotesIcon },
  { to: '/admin/expenses', label: 'Uitgaven', icon: EuroIcon },
  { to: '/admin/rent-settings', label: 'Huurregels', icon: CalendarIcon },
  { to: '/admin/email-templates', label: 'E-mailsjablonen', icon: MailIcon },
];

const STUDENT_NAV = [
  { to: '/student/dashboard', label: 'Dashboard', icon: HomeIcon },
  { to: '/student/housemates', label: 'Huisgenoten', icon: UsersIcon },
  { to: '/student/profile', label: 'Mijn profiel', icon: UserIcon },
  { to: '/student/invoices', label: 'Mijn facturen', icon: BanknotesIcon },
  { to: '/student/announcements', label: 'Mededelingen', icon: MegaphoneIcon },
  { to: '/student/huisregels', label: 'Huisregels', icon: BookOpenIcon },
  { to: '/student/noodgegevens', label: 'Noodgegevens', icon: PhoneIcon },
  { to: '/student/waste-schedule', label: 'Afvalschema', icon: RecycleIcon },
  { to: '/student/complaints', label: 'Klachten', icon: ExclamationBubbleIcon },
  { to: '/student/maintenance', label: 'Onderhoud', icon: WrenchIcon },
  { to: '/student/calendar', label: 'Agenda', icon: CalendarPlusIcon },
];

const CLEANER_NAV = [
  { to: '/cleaner/dashboard', label: 'Dashboard', icon: HomeIcon },
  { to: '/cleaner/tasks', label: 'Mijn taken', icon: ClipboardCheckIcon },
  { to: '/cleaner/shifts', label: 'In/uitchecken', icon: ClockIcon },
];

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute allow={['ROLE_ADMIN']} />}>
        <Route element={<DashboardLayout title="Beheer" navItems={ADMIN_NAV} />}>
          <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          <Route path="/admin/students" element={<StudentsPage />} />
          <Route path="/admin/rooms" element={<RoomsPage />} />
          <Route path="/admin/cleaning-schedule" element={<CleaningSchedulePage />} />
          <Route path="/admin/waste-schedule" element={<AdminWasteSchedulePage />} />
          <Route path="/admin/cleaner" element={<CleanerPage />} />
          <Route path="/admin/huisregels" element={<AdminHuisregelsPage />} />
          <Route path="/admin/noodgegevens" element={<AdminNoodgegevensPage />} />
          <Route path="/admin/complaints" element={<AdminComplaintsPage />} />
          <Route path="/admin/maintenance" element={<AdminMaintenanceRequestsPage />} />
          <Route path="/admin/calendar" element={<CalendarPage />} />
          <Route path="/admin/announcements" element={<AnnouncementsAdminPage />} />
          <Route path="/admin/organization" element={<OrganizationProfilePage />} />
          <Route path="/admin/invoices" element={<AdminInvoicesPage />} />
          <Route path="/admin/payment-settings" element={<PaymentSettingsPage />} />
          <Route path="/admin/expenses" element={<AdminExpensesPage />} />
          <Route path="/admin/rent-settings" element={<RentSettingsPage />} />
          <Route path="/admin/email-templates" element={<EmailTemplatesPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allow={['ROLE_STUDENT']} />}>
        <Route element={<DashboardLayout title="Mijn kamer" navItems={STUDENT_NAV} />}>
          <Route path="/student" element={<Navigate to="/student/dashboard" replace />} />
          <Route path="/student/dashboard" element={<StudentDashboardPage />} />
          <Route path="/student/housemates" element={<HousematesPage />} />
          <Route path="/student/profile" element={<ProfilePage />} />
          <Route path="/student/invoices" element={<InvoicesPage />} />
          <Route path="/student/announcements" element={<AnnouncementsPage />} />
          <Route path="/student/huisregels" element={<StudentHuisregelsPage />} />
          <Route path="/student/noodgegevens" element={<StudentNoodgegevensPage />} />
          <Route path="/student/waste-schedule" element={<StudentWasteSchedulePage />} />
          <Route path="/student/complaints" element={<StudentComplaintsPage />} />
          <Route path="/student/maintenance" element={<MaintenanceReportPage />} />
          <Route path="/student/calendar" element={<CalendarPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allow={['ROLE_CLEANER']} />}>
        <Route element={<DashboardLayout title="Schoonmaak" navItems={CLEANER_NAV} />}>
          <Route path="/cleaner" element={<Navigate to="/cleaner/dashboard" replace />} />
          <Route path="/cleaner/dashboard" element={<CleanerDashboardPage />} />
          <Route path="/cleaner/tasks" element={<CleanerTasksPage />} />
          <Route path="/cleaner/shifts" element={<ShiftsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
