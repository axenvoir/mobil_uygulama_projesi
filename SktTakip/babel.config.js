module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    [
      'babel-plugin-transform-define',
      {
        replacements: {
          'process.env.SENTRY_DSN': process.env.SENTRY_DSN ?? null,
        },
      },
    ],
  ],
};
