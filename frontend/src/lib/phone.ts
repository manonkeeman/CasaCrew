export function telHref(phoneNumber: string): string {
  return `tel:${phoneNumber.replace(/[^+\d]/g, '')}`;
}
