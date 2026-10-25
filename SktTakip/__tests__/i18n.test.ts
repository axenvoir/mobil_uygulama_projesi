import {translate, resolveLocale, resolveDeviceLocale} from '../src/i18n';
import {tr} from '../src/i18n/tr';
import {en} from '../src/i18n/en';
import type {TranslationKey} from '../src/i18n/tr';

describe('i18n', () => {
  it('has full key parity between tr and en', () => {
    const trKeys = Object.keys(tr).sort();
    const enKeys = Object.keys(en).sort();
    expect(enKeys).toEqual(trKeys);
  });

  it('translates known keys', () => {
    expect(translate('tr', 'save')).toBe('Kaydet');
    expect(translate('en', 'save')).toBe('Save');
  });

  it('falls back to key for unknown locale', () => {
    expect(translate('tr' as never, 'unknown' as TranslationKey)).toBe('unknown');
  });

  it('resolves locale preferences', () => {
    expect(resolveLocale('tr')).toBe('tr');
    expect(resolveLocale('en')).toBe('en');
    expect(['tr', 'en']).toContain(resolveLocale('system'));
    expect(['tr', 'en']).toContain(resolveDeviceLocale());
  });
});
