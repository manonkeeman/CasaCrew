export type Role = 'ROLE_ADMIN' | 'ROLE_STUDENT' | 'ROLE_CLEANER';

export interface UserResponse {
  id: number;
  username: string;
  fullName: string | null;
  email: string;
  role: string;
  roomName: string | null;
  phoneNumber: string | null;
  emergencyPhoneNumber: string | null;
  studyOrWork: string | null;
  parentsAddress: string | null;
  favoriteMeal: string | null;
  socialPreference: string | null;
  mealPreference: string | null;
  availabilityStatus: string | null;
  statusToggle: boolean;
  profileImagePath: string | null;
  contractFile: string | null;
  rentAmount: number | null;
  leaseEndDate: string | null;
}

export interface LoginResponse {
  username: string;
  email: string;
  role: Role;
  token: string;
  expiresAt: string;
  user: UserResponse;
}

export interface UserProfileUpdate {
  username?: string;
  fullName?: string;
  email?: string;
  phoneNumber?: string;
  emergencyPhoneNumber?: string;
  studyOrWork?: string;
  parentsAddress?: string;
  favoriteMeal?: string;
  socialPreference?: string;
  mealPreference?: string;
  availabilityStatus?: string;
  statusToggle?: boolean;
}

export interface Room {
  id: number;
  name: string;
  occupantId: number | null;
  occupantUsername: string | null;
}

export interface OrganizationProfile {
  name: string;
  address: string | null;
}

export interface OrganizationPaymentSettings {
  bunqMeUsername: string | null;
  iban: string | null;
  accountHolderName: string | null;
}

export interface OrganizationRentSettings {
  defaultRentAmount: number | null;
  rentInvoiceDayOfMonth: number | null;
  rentDueDayOfMonth: number | null;
}

export interface EmailTemplate {
  id: number;
  type: string;
  subject: string;
  body: string;
  updatedAt: string;
}

export interface Invoice {
  id: number;
  title: string;
  description: string | null;
  amount: number;
  issueDate: string;
  dueDate: string;
  invoiceMonth: number;
  invoiceYear: number;
  status: string;
  reminderCount: number;
  lastReminderSentAt: string | null;
  checkoutUrl: string | null;
  paidAt: string | null;
  studentName: string;
  studentEmail: string;
}

export interface Announcement {
  id: number;
  type: string;
  title: string;
  body: string;
  author: string | null;
  createdAt: string;
}

export interface EmergencyContact {
  id: number;
  label: string;
  phoneNumber: string | null;
  orderIndex: number;
}

export type MaintenanceUrgency = 'LOW' | 'MEDIUM' | 'HIGH';
export type MaintenanceStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';

export interface MaintenanceRequest {
  id: number;
  title: string;
  description: string;
  location: string | null;
  urgency: MaintenanceUrgency;
  status: MaintenanceStatus;
  adminNote: string | null;
  reportedByUsername: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CalendarEvent {
  id: number;
  title: string;
  description: string | null;
  eventDate: string;
  eventTime: string | null;
  createdByUsername: string | null;
}

export interface Housemate {
  id: number;
  username: string;
  fullName: string | null;
  roomName: string | null;
  studyOrWork: string | null;
  favoriteMeal: string | null;
  socialPreference: string | null;
  availabilityStatus: string | null;
  profileImagePath: string | null;
}

export type ExpenseCategory = 'ONDERHOUD' | 'SCHOONMAAK' | 'REPARATIE' | 'INVENTARIS' | 'NUTSVOORZIENINGEN' | 'OVERIG';

export interface Expense {
  id: number;
  category: ExpenseCategory;
  description: string;
  amount: number;
  expenseDate: string;
  createdByUsername: string | null;
}

export type ComplaintDirection = 'STUDENT_TO_ADMIN' | 'ADMIN_TO_STUDENT';
export type ComplaintStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED';

export interface Complaint {
  id: number;
  direction: ComplaintDirection;
  authorUsername: string | null;
  targetUsername: string | null;
  subject: string;
  description: string;
  status: ComplaintStatus;
  response: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface WasteScheduleEntry {
  id: number;
  wasteType: string;
  scheduleInfo: string | null;
  orderIndex: number;
}

export interface Huisregel {
  id: number;
  title: string;
  content: string | null;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

export interface CleaningTask {
  id: number;
  weekNumber: number;
  name: string;
  description: string | null;
  completed: boolean;
  assignedTo: string | null;
  assignedToEmail: string | null;
  comment: string | null;
  incidentReport: string | null;
  deadline: string | null;
}

export interface CleaningScheduleInfo {
  isoWeek: number;
  rotationWeek: number;
  rotationLength: number;
  year: number;
}

export interface Shift {
  id: number;
  cleanerUsername: string | null;
  cleanerEmail: string | null;
  shiftDate: string;
  checkInAt: string | null;
  checkOutAt: string | null;
  notes: string | null;
}
