import type {
  ButtonHTMLAttributes,
  ComponentType,
  InputHTMLAttributes,
  LabelHTMLAttributes,
  ReactNode,
  SVGProps,
  TextareaHTMLAttributes,
} from 'react';

export function Card({ title, children, actions }: { title?: string; children: ReactNode; actions?: ReactNode }) {
  return (
    <div className="rounded-2xl border border-stone-200 bg-white p-6 shadow-sm shadow-stone-200/50">
      {(title || actions) && (
        <div className="mb-4 flex items-center justify-between">
          {title && <h2 className="text-lg font-semibold text-stone-800">{title}</h2>}
          {actions}
        </div>
      )}
      {children}
    </div>
  );
}

export function Button({ variant = 'primary', className = '', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'danger' }) {
  const styles = {
    primary: 'bg-emerald-600 text-white shadow-sm shadow-emerald-900/10 hover:bg-emerald-700 disabled:bg-emerald-300',
    secondary: 'bg-stone-100 text-stone-700 hover:bg-stone-200 disabled:text-stone-400',
    danger: 'bg-red-50 text-red-700 hover:bg-red-100 disabled:text-red-300',
  }[variant];
  return (
    <button
      className={`rounded-xl px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed ${styles} ${className}`}
      {...props}
    />
  );
}

export function Label(props: LabelHTMLAttributes<HTMLLabelElement>) {
  return <label className="mb-1 block text-sm font-medium text-stone-600" {...props} />;
}

export function Input(props: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 disabled:bg-stone-50"
      {...props}
    />
  );
}

export function Textarea(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
      {...props}
    />
  );
}

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="mb-4">
      <Label>{label}</Label>
      {children}
    </div>
  );
}

export function Banner({ kind, message }: { kind: 'error' | 'success'; message: string }) {
  const styles = kind === 'error' ? 'bg-red-50 text-red-700 border-red-200' : 'bg-emerald-50 text-emerald-700 border-emerald-200';
  return <div className={`mb-4 rounded-lg border px-4 py-2 text-sm ${styles}`}>{message}</div>;
}

export function Table({ head, children }: { head: ReactNode[]; children: ReactNode }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-stone-200 text-stone-500">
            {head.map((h, i) => (
              <th key={i} className="pb-2 pr-4 font-medium">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-stone-100">{children}</tbody>
      </table>
    </div>
  );
}

export function StatCard({
  label,
  value,
  hint,
  icon: Icon,
}: {
  label: string;
  value: ReactNode;
  hint?: ReactNode;
  icon?: ComponentType<SVGProps<SVGSVGElement>>;
}) {
  return (
    <div className="rounded-2xl border border-stone-200 bg-white p-5 shadow-sm shadow-stone-200/50">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-stone-500">{label}</p>
        {Icon && (
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
            <Icon className="h-4 w-4" />
          </span>
        )}
      </div>
      <p className="mt-1 text-2xl font-bold text-stone-800">{value}</p>
      {hint && <p className="mt-1 text-xs text-stone-400">{hint}</p>}
    </div>
  );
}
