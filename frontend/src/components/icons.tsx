import type { ReactNode, SVGProps } from 'react';

type IconProps = SVGProps<SVGSVGElement>;

function base(props: IconProps, children: ReactNode) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.6}
      strokeLinecap="round"
      strokeLinejoin="round"
      {...props}
    >
      {children}
    </svg>
  );
}

export function HomeIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M3.5 11.5 12 4l8.5 7.5" />
      <path d="M5.5 10v9a1 1 0 0 0 1 1H9v-5.5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1V20h2.5a1 1 0 0 0 1-1v-9" />
    </>,
  );
}

export function UsersIcon(props: IconProps) {
  return base(
    props,
    <>
      <circle cx="9" cy="8" r="3" />
      <path d="M3.5 20c0-3 2.5-5.5 5.5-5.5s5.5 2.5 5.5 5.5" />
      <circle cx="17" cy="8.5" r="2.3" />
      <path d="M15.5 14.7c2.4.4 4 2.5 4 5.3" />
    </>,
  );
}

export function BedIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M3 19v-7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v7" />
      <path d="M3 19v1.5M21 19v1.5" />
      <path d="M3 15h18" />
      <rect x="5.5" y="8.5" width="6" height="4" rx="1" />
    </>,
  );
}

export function SparklesIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M11 3.5 12.3 8l4.2 1.3-4.2 1.4L11 15l-1.4-4.3L5.5 9.3l4.1-1.3z" />
      <path d="M18 15.5 18.7 18l2.3.8-2.3.7-.7 2.4-.8-2.4-2.2-.7 2.2-.8z" />
    </>,
  );
}

export function UserIcon(props: IconProps) {
  return base(
    props,
    <>
      <circle cx="12" cy="8" r="3.3" />
      <path d="M5 20c0-3.6 3.1-6.5 7-6.5s7 2.9 7 6.5" />
    </>,
  );
}

export function BookOpenIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M12 6.5c-1.6-1.3-3.7-2-6.5-2v12.5c2.8 0 4.9.7 6.5 2 1.6-1.3 3.7-2 6.5-2V4.5c-2.8 0-4.9.7-6.5 2Z" />
      <path d="M12 6.5V19" />
    </>,
  );
}

export function PhoneIcon(props: IconProps) {
  return base(
    props,
    <path d="M6 4h3l1.3 4.2-2 1.6a11 11 0 0 0 5.9 5.9l1.6-2L20 15v3a2 2 0 0 1-2 2C10.8 20 4 13.2 4 6a2 2 0 0 1 2-2Z" />,
  );
}

export function MegaphoneIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M4 11v3a1.5 1.5 0 0 0 1.5 1.5H7l1 4.5h2l-.8-4.5H10l9 3.5V7.5L10 11H5.5A1.5 1.5 0 0 0 4 12.5Z" />
      <path d="M19 9.5v6" />
    </>,
  );
}

export function BuildingIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="4.5" y="3.5" width="9" height="17" rx="1" />
      <path d="M13.5 10.5H19a1 1 0 0 1 1 1V20a.5.5 0 0 1-.5.5H13.5" />
      <path d="M7.5 7.5h1M10.5 7.5h1M7.5 10.5h1M10.5 10.5h1M7.5 13.5h1M10.5 13.5h1M7.5 16.5h1M10.5 16.5h1" />
    </>,
  );
}

export function BanknotesIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="2.5" y="6.5" width="19" height="11" rx="2" />
      <circle cx="12" cy="12" r="2.6" />
      <path d="M5.5 6.5v.01M18.5 17.5v.01" />
    </>,
  );
}

export function CalendarIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2" />
      <path d="M3.5 9.5h17M8 3v3M16 3v3" />
      <path d="m8.5 14 2 2 4-4" />
    </>,
  );
}

export function MailIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="3" y="5.5" width="18" height="13" rx="2" />
      <path d="m4 7 8 6 8-6" />
    </>,
  );
}

export function ClipboardCheckIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="5" y="4.5" width="14" height="16" rx="2" />
      <path d="M9 4.5V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v.5" />
      <path d="m9 13 2 2 4-4.5" />
    </>,
  );
}

export function ClockIcon(props: IconProps) {
  return base(
    props,
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>,
  );
}

export function EuroIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M16 6.5a6.5 6.5 0 1 0 0 11" />
      <path d="M4.5 10h8M4.5 14h6.5" />
    </>,
  );
}

export function ExclamationBubbleIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M4 5.5h16v11H9l-4 3.5v-3.5H4z" />
      <path d="M12 9v3.5" />
      <path d="M12 15.2v.01" />
    </>,
  );
}

export function WrenchIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M14.5 6.5a4 4 0 0 1-5.4 5.4L4 17l3 3 5.1-5.1a4 4 0 0 1 5.4-5.4l-2.8 2.8-2-2z" />
    </>,
  );
}

export function CalendarPlusIcon(props: IconProps) {
  return base(
    props,
    <>
      <rect x="3.5" y="5" width="17" height="15.5" rx="2" />
      <path d="M3.5 9.5h17M8 3v3M16 3v3" />
      <path d="M12 12.5v5M9.5 15h5" />
    </>,
  );
}

export function ReceiptIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="M6 3h12v18l-2.5-1.5L13 21l-2.5-1.5L8 21l-2-1.5z" />
      <path d="M8.5 8h7M8.5 11.5h7M8.5 15h4.5" />
    </>,
  );
}

export function RecycleIcon(props: IconProps) {
  return base(
    props,
    <>
      <path d="m10.5 4 3 4.5h-2.3L14 12" />
      <path d="m19.5 15-2 4h-3.4" />
      <path d="M17 15.5 20 15l1 3" />
      <path d="m4.5 15 2.3 4H10" />
      <path d="M4 11.5 6.5 15l2-1.2" />
      <path d="M8.5 4.5 6 4l-1 3.3" />
    </>,
  );
}
