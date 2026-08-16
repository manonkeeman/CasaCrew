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
