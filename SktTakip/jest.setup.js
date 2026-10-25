jest.mock('react-native-localize', () => ({
  getLocales: () => [{languageCode: 'tr', countryCode: 'TR'}],
}));
